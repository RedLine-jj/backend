# Redline

편집샵들의 재고를 추적하고, 원하는 제품이 어느 사이트에서 판매 중인지 한눈에 확인할 수 있는 서비스입니다. 품절 상태인 제품은 재입고 시 알림을 받을 수 있습니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| 언어 / 프레임워크 | Java 17, Spring Boot 3.5 |
| 데이터베이스 | MySQL, Redis |
| ORM | JPA/Hibernate, QueryDSL |
| 인증 | JWT |
| 크롤링 | Spring Batch, Jsoup |
| AI 모델 매칭 | Groq LLM (llama-3.3-70b) |
| 실시간 알림 | Redis Pub/Sub, SSE (Server-Sent Events) |
| Rate Limiting | Bucket4j (IP당 분당 60회) |
| API 문서 | SpringDoc OpenAPI (Swagger) |
| 배포 | systemd (EC2) |

## API 목록

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | /api/auth/signup | 회원가입 | |
| POST | /api/auth/login | 로그인 | |
| POST | /api/auth/refresh | 토큰 갱신 | |
| GET | /api/sites | 편집샵 목록 | |
| GET | /api/brands | 브랜드 목록 | |
| GET | /api/models | 모델 목록 (커서 페이징, brandIds/types 필터) | |
| GET | /api/models/types | 모델 타입 목록 (앱 시작 시 1회 캐시) | |
| GET | /api/site-options | 사이트별 재고/가격 목록 | |
| GET | /api/site-options/{id} | 재고/가격 상세 | |
| GET | /api/site-options/{id}/logs | 재고/가격 변동 이력 | |
| GET | /api/subscriptions | 내 구독 목록 | JWT |
| GET | /api/subscriptions/count | 내 구독 수 | JWT |
| POST | /api/subscriptions | 구독 등록 | JWT |
| DELETE | /api/subscriptions/{id} | 구독 삭제 | JWT |
| GET | /api/dashboard/price-comparison | 사이트별 가격 비교 | |
| GET | /api/notifications | 알림 목록 | JWT |
| GET | /api/notifications/unread-count | 읽지 않은 알림 수 | JWT |
| GET | /api/notifications/stream | SSE 실시간 알림 스트림 | JWT |
| PATCH | /api/notifications/{id}/read | 알림 읽음 처리 | JWT |
| PATCH | /api/notifications/read-all | 전체 읽음 처리 | JWT |

### 공통 응답 형식

```json
{
  "success": true,
  "message": "...",
  "data": {}
}
```

## 크롤링 아키텍처

Spring Batch 기반으로 편집샵 사이트를 크롤링하여 상품/재고 데이터를 수집합니다.

### 지원 사이트

| 사이트 | 플랫폼 | 목록 파싱 | 상세 파싱 |
|--------|--------|----------|----------|
| ModeMan | Cafe24 | HTML (CSS 셀렉터) | JSON-LD |
| SemiBasement | imweb | HTML (`data-product-properties` JSON) | OMS API (`/ajax/oms/OMS_get_products.cm`) |

### 데이터 흐름

```
스케줄러 (20분 / 1시간)
  ↓
Reader: 카테고리별 목록 페이지 순회 → ProductBrief 수집
  ↓
Processor: 상세 페이지/API 호출 → ProductSnapshot 변환
  ↓
Writer (DbSnapshotWriter): DB upsert
  ├─ 브랜드 매칭 (exact → alias → 정규화 → 생성)
  ├─ 모델 매칭 (exact → alias → AI 매칭 → 생성/스킵)
  ├─ 사이즈별 재고 upsert
  └─ 재입고 감지 → Redis PUBLISH
```

### 크로스사이트 상품 동일성 매칭

#### 문제

여러 편집샵 사이트에서 같은 데님 제품을 다른 이름으로 판매하여 동일 상품 식별이 불가능했습니다.

| 사이트 | 상품명 |
|--------|--------|
| ModeMan | `SC11936 13oz Denim Blouse 1936 Model A.Navy` |
| SemiBasement | `13oz. DENIM BLOUSE 1936 MODEL (One Wash)` |

모델코드 유무, 대소문자, 색상/워싱 표기가 사이트마다 달라 단순 문자열 비교로는 매칭이 불가능합니다.

#### 해결: LLM 기반 매칭 + alias 캐싱

새 모델이 들어오면, 같은 브랜드의 **타 사이트 기존 모델 목록을 DB에서 조회**하여 LLM에 "이 중에 같은 제품이 있는가?"를 질문합니다. 매칭 결과는 alias 테이블에 저장되어 이후 동일 상품은 LLM 호출 없이 즉시 매칭됩니다.

```
"0105W Wide Denim" (FULLCOUNT) 들어옴
  → DB 조회: FULLCOUNT 기존 모델 = ["0105W WIDE STRAIGHT DENIM", "1101 USED WASH...", ...]
  → LLM: "0105W WIDE STRAIGHT DENIM" 매칭 (confidence: 100%)
  → alias 저장 → 다음부터 LLM 호출 없이 바로 매칭
```

**3단계 매칭 파이프라인:**

| 순서 | 방법 | LLM 호출 | 설명 |
|------|------|---------|------|
| 1 | exact | X | 이름 완전 일치 |
| 2 | alias | X | `tb_model_alias` 조회 (이전 LLM 매칭 결과 캐시) |
| 3 | LLM | O (최초 1회) | 타 사이트 모델 목록과 비교, confidence ≥ 85% |
| 4 | 생성/스킵 | X | 새 제품이면 생성, LLM 실패 시 스킵 후 재시도 |

#### 최적화

- **같은 사이트 내 비교 스킵** — 타 사이트 모델이 있을 때만 LLM 호출하여 불필요한 API 요청 제거
- **alias 캐시 누적** — 크롤링 주기가 반복될수록 LLM 호출량이 점진적으로 0에 수렴
- **rate limit 대응** — API 실패 시 해당 상품을 스킵하고 다음 크롤링에서 재시도 (중복 모델 방지)
- **모델코드 우선 비교** — LLM 프롬프트에서 모델코드가 다르면 무조건 다른 제품으로 판단 (SC15708 ≠ SC15655)

#### 표준 이름

먼저 DB에 들어간 사이트의 이름이 표준이 됩니다. ModeMan을 먼저 크롤링하면 ModeMan 이름이 표준.

### 브랜드 정규화

사이트별 브랜드 표기 차이를 자동 + 수동으로 처리합니다.

| 방법 | 예시 |
|------|------|
| exact | `FULLCOUNT` = `FULLCOUNT` |
| alias (`tb_brand_alias`) | `SUGAR CANE & CO.` → SUGARCANE 브랜드 |
| 정규화 (대문자 + 특수문자 제거) | `FULL COUNT` → `FULLCOUNT` 자동 매칭 |

### 새 사이트 추가 방법

1. `Site` enum에 새 값 추가
2. `crawling/{site}/` 패키지에 HttpClient, ListParser, DetailParser 구현
3. `batch/` 레이어에 Reader, Processor, BatchJobConfig 추가
4. `BatchScheduler`에 Job 등록
5. `DbSnapshotWriter.resolveModelType()`에 카테고리 매핑 추가

### 수동 배치 실행

```bash
GROQ_API_KEY=your-key ./gradlew bootRun --args='--spring.profiles.active=manual-batch'
```

### 환경변수 (크롤링)

```env
GROQ_API_KEY=        # Groq API Key (AI 모델 매칭용)
```

## 재입고 알림 아키텍처

```
배치 크롤링 → DbSnapshotWriter (재입고 감지)
                    ↓ Redis PUBLISH "restock"
              RestockSubscriber
                    ↓
         구독자 매칭 → 알림 DB 저장 → Redis unread INCR → SSE 푸시
                                                              ↓
                                              프론트엔드 토스트 + 뱃지 갱신
```

## 로컬 실행

**사전 요구사항:** Java 17, MySQL, Redis

```bash
./gradlew bootRun
```

기본 프로파일은 `local`입니다. 실행 전 아래 환경변수를 설정하거나 `.env` 파일을 준비하세요.

### 환경변수

```env
DB_HOST=
DB_PORT=3306
DB_NAME=redline
DB_USERNAME=
DB_PASSWORD=
REDIS_HOST=redis
REDIS_PORT=6379
GROQ_API_KEY=          # Groq API Key (AI 모델 매칭)
JWT_SECRET=            # JWT 서명 키
```

### API 문서

- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/api-docs

## CI/CD

GitHub Actions로 자동화되어 있습니다.

| 워크플로우 | 트리거 | 동작 |
|-----------|--------|------|
| CI | PR → main | `./gradlew build -x test` (빌드 검증) |
| CD | push → main | JAR 빌드 → EC2 SCP 전송 → systemd 재시작 |

```
PR 생성 → CI 빌드 검증 → 머지 → CD 자동 배포
                                    ├─ ./gradlew bootJar
                                    ├─ scp → EC2
                                    └─ systemctl restart redline
```

**인프라:** EC2 + Nginx + systemd
