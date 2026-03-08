# ModeMan 상품 크롤링 배치

## 1. 개요

"ModeMan" 웹사이트([https://mode-man.com/](https://mode-man.com/))의 상품 정보를 수집하여, **NDJSON(Newline Delimited JSON)** 형식의 파일로 저장하는 Spring Batch 애플리케이션입니다.

## 2. 주요 기술

| Technology | Purpose |
| :--- | :--- |
| `Java 17` | Runtime |
| `Spring Boot` | Application framework |
| `Spring Batch` | Batch processing |
| `Gradle` | Build tool |
| `Jsoup` | HTML parsing |
| `Jackson` | JSON processing |

## 3. 실행 방법

이 애플리케이션은 두 가지 모드로 실행할 수 있습니다.

### 3.1. 자동 스케줄러로 실행 (Scheduler Mode)

애플리케이션이 서버로 항상 실행 중인 상태로, 내장된 스케줄러가 주기적으로 크롤링을 실행합니다.

- **활성화 방법**: `scheduler` 스프링 프로필을 사용합니다.

- **IDE에서 실행**:
  1. `Run/Debug Configurations` 메뉴로 이동합니다.
  2. 'Active profiles' 입력란에 `scheduler`를 입력합니다.
  3. `RedlineApplication`을 실행합니다. (Program arguments는 비워둡니다.)

- **터미널에서 실행**:
  ```bash
  ./gradlew bootRun --args='--spring.profiles.active=scheduler'
  ```

### 3.2. 수동으로 1회 실행 (Manual Mode)

필요할 때 일회성으로 배치 잡을 직접 실행합니다. 웹 서버가 구동되지 않으며, 잡 실행 후 애플리케이션이 종료됩니다.

- **활성화 방법**: `--batch.run.modeman=true` 인자를 사용합니다. (서버 안전을 위해 기본값은 `false`입니다.)

- **IDE에서 실행**:
  1. `Run/Debug Configurations` 메뉴로 이동합니다.
  2. 'Program arguments' 입력란에 `--batch.run.modeman=true`를 입력합니다.
  3. `RedlineApplication`을 실행합니다.

- **터미널에서 실행**:
  ```bash
  ./gradlew bootRun --args='--batch.run.modeman=true'
  ```
  > **팁**: `./gradlew bootRun --args='--batch.run.modeman=true customParam=1234'` 와 같이 Job Parameter를 추가할 수 있습니다.

---

## 4. 배치 프로세스 상세

`modeManCrawlingJob`은 `crawlingStep`이라는 단일 스텝으로 구성되며, Chunk 기반으로 동작합니다.

### 4.1. 프로세스 흐름

```
Scheduler / Runner
        │
        ▼
modeManCrawlingJob
        │
        ▼
   crawlingStep (Chunk: 10)
        │
 ┌──────┼────────┐
 ▼      ▼        ▼
Read  Process   Write
```

1.  **Job Start**: `JobCompletionNotificationListener`가 잡 시작 시 고유한 파일명을 생성하여 실행 컨텍스트에 저장합니다.
2.  **Read**: `MultiCategoryProductReader`가 대상 카테고리의 모든 페이지를 순회하며 상품 목록(`ProductBrief`)을 읽어옵니다.
3.  **Process**: `ModeManDetailCrawlingProcessor`가 각 상품의 상세 페이지를 크롤링하고 파싱하여 최종 데이터(`ProductSnapshot`)를 생성합니다.
4.  **Write**: `NdjsonSnapshotWriter`가 처리된 데이터를 NDJSON 형식으로 파일에 한 줄씩 기록합니다.
5.  **Job End**: `JobCompletionNotificationListener`가 잡 종료 후 실행 결과를 요약한 `meta.ndjson` 파일을 생성합니다.

### 4.2. 자동 스케줄링 (`scheduler` 모드)

- **위치**: `src/main/java/com/jj/redline/batch/scheduler/BatchScheduler.java`
- **실행 주기**:
  - **빠른 주기**: 구독자가 있으면 **20초마다** 실행 (`runFastScheduledJob`)
  - **느린 주기**: 구독자가 없으면 **1시간마다** 실행 (`runSlowScheduledJob`)

> **[중요]** 현재 구독 확인 로직(`hasActiveSubscriptions`)은 테스트를 위해 항상 `true`를 반환하도록 되어있습니다. 따라서 지금 `scheduler` 프로필로 애플리케이션을 실행하면 **무조건 20초마다 크롤링이 실행됩니다.** 추후 실제 데이터베이스를 연동하여 구독 여부를 확인하는 로직을 구현해야 합니다.

### 4.3. 크롤링 대상

- **위치**: `src/main/java/com/jj/redline/batch/reader/MultiCategoryProductReader.java`
- **현재 대상 카테고리**:
  - `Denim Jackets` (code: 263)
  - `Denim Pants` (code: 858)

### 4.4. 실패 처리 전략

**현재 실패 처리(Skip, Retry 등) 로직은 구현되어 있지 않습니다.**

크롤링 중 예외(네트워크, 파싱 오류 등)가 발생하면 해당 Chunk는 실패 처리되며, 재시도나 건너뛰기 없이 **Job이 중단됩니다.**

---

## 5. 결과물 (Output)

- **경로**: `{output-root-dir}/{site-dir}/{YYYY-MM-DD}/`
- **예시**:
  ```
  output/
  └ modeman/
      └ 2026-03-08/
          ├ snapshot-20260308-102030.ndjson
          └ meta.ndjson
  ```

- **데이터 파일 (`snapshot-yyyyMMdd-HHmmss.ndjson`)**:
  - 개별 상품의 상세 정보(`ProductSnapshot`)가 NDJSON 형식으로 저장됩니다.
  - 잡 실행마다 고유한 타임스탬프를 가진 파일이 생성됩니다.

- **메타 파일 (`meta.ndjson`)**:
  - 잡 실행에 대한 요약 정보(시작/종료 시간, 처리 건수, 에러 샘플 등)가 누적 기록됩니다.

---

## 6. 주요 설정 및 클래스

### 6.1. 설정

- **위치**: `src/main/resources/application.yml`
- **주요 프로퍼티**: `redline.crawl.*` (결과물 경로 등)

### 6.2. 프로젝트 구조

```
src
└── main
    └── java
        └── com/jj/redline
            ├── RedlineApplication.java                 # 애플리케이션 시작점, @EnableScheduling
            ├── batch
            │   ├── config/ModeManBatchJobConfig.java   # 배치 Job/Step 정의
            │   ├── listener/JobCompletionNotificationListener.java # Job 실행 전후 처리
            │   ├── scheduler/BatchScheduler.java       # 자동 실행 스케줄러
            │   ├── runner/ModeManBatchRunner.java        # 수동 실행 로직
            │   ├── reader/MultiCategoryProductReader.java # 상품 목록(Brief) 크롤링 및 제공 (Read)
            │   ├── processor/ModeManDetailCrawlingProcessor.java # 상품 상세 크롤링 및 가공 (Process)
            │   └── output
            │       ├── NdjsonSnapshotWriter.java       # 크롤링 결과(Snapshot) 파일 저장 (Write)
            │       └── MetaWriter.java                 # 배치 실행 메타데이터 보고서 저장
            ├── crawling
            │   └── modeman
            │       ├── ModeManHttpClient.java          # ModeMan 사이트 전용 HTTP 클라이언트
            │       ├── detail/ModeManJsonLdParser.java # 상세 페이지의 JSON-LD 데이터 파싱
            │       └── list/ModeManListParser.java     # 목록 페이지의 상품 리스트 파싱
            └── domain
                └── dto/ProductSnapshot.java            # 최종 결과물 데이터 DTO
```