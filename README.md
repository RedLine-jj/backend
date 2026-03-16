# Redline

편집샵들의 재고를 추적하고, 원하는 제품이 어느 사이트에서 판매 중인지 한눈에 확인할 수 있는 서비스입니다. 품절 상태인 제품은 재입고 시 알림을 받을 수 있습니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| 언어 / 프레임워크 | Java 17, Spring Boot 3.5 |
| 데이터베이스 | MySQL, Redis |
| ORM | JPA/Hibernate, QueryDSL |
| 인증 | JWT |
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
