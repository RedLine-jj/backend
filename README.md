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

### AI 모델 매칭

사이트마다 같은 제품의 이름이 다른 문제를 AI(Groq LLM)로 해결합니다.

**동작 원리:** 새 모델이 들어오면, 같은 브랜드의 **기존 모델 목록을 DB에서 조회**하여 AI에게 "이 중에 같은 제품 있어?"라고 질문합니다.

```
SemiBasement: "0105W Wide Denim" (FULLCOUNT) 들어옴
  → DB 조회: FULLCOUNT 기존 모델 = ["0105W WIDE STRAIGHT DENIM", "1101 USED WASH...", ...]
  → AI: "0105W WIDE STRAIGHT DENIM" 매칭 (confidence: 100%)
  → tb_model_alias에 저장 → 다음부터 AI 호출 없이 alias로 바로 매칭
```

**매칭 우선순위:**

| 순서 | 방법 | AI 호출 |
|------|------|---------|
| 1 | exact: 이름 완전 일치 | X |
| 2 | alias: `tb_model_alias` 테이블 조회 | X |
| 3 | AI: Groq LLM에 기존 모델 목록과 비교 요청 | O (최초 1회) |
| 4 | 생성 또는 스킵 | X |

**AI 실패 처리 (rate limit 등):**
- AI 성공 + 매칭됨 → alias 저장, 기존 모델 사용
- AI 성공 + 매칭 없음 → 새 모델 생성 (진짜 새 제품)
- AI 실패 → 스킵, 다음 크롤링에서 재시도 (중복 모델 방지)

**표준 이름:** 먼저 DB에 들어간 사이트의 이름이 표준이 됩니다. ModeMan을 먼저 크롤링하면 ModeMan 이름이 표준.

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

## 배포

배포 파일 구조:

```
/was/
└── redline-0.0.1-SNAPSHOT.jar
├── logs
└── .env
```

배포 절차:

```bash
# 1. JAR 빌드
./gradlew bootJar

# 2. EC2로 전송
scp build/libs/*.jar ec2-user@<EC2_IP>:/deploy/redline-backend/

# 3. 서비스 재시작
sudo systemctl restart redline
```
