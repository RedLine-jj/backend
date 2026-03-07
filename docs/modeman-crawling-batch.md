# ModeMan 상품 크롤링 배치

## 1. 개요

이 프로젝트는 "ModeMan" 웹사이트([https://mode-man.com/](https://mode-man.com/))의 상품 정보를 수집하여, NDJSON(Newline Delimited JSON) 형식의 파일로 저장하는 Spring Batch 애플리케이션입니다.

## 2. 주요 기술

- Java 17
- Spring Boot
- Spring Batch
- Gradle
- Jsoup (HTML Parsing)
- Jackson (JSON Processing)

## 3. 실행 방법 (Spring Profiles)

이 프로젝트는 **스프링 프로필(Spring Profiles)**을 사용하여 실행 모드를 간편하게 전환합니다.

- **`scheduler`**: 자동 스케줄러를 포함한 전체 웹 애플리케이션 서버를 실행합니다.
- **`manual-batch`**: 웹 서버 없이, 수동으로 배치 잡을 한 번만 실행합니다.

---

### 방법 A: 자동 스케줄러 서버로 실행하기

애플리케이션이 항상 실행 중인 상태로, 내장된 스케줄러가 주기적으로 크롤링을 실행합니다.

#### IDE에서 실행
1. `Run/Debug Configurations` 메뉴로 이동합니다.
2. 'Active profiles' 입력란에 `scheduler`를 입력합니다.
3. `RedlineApplication`을 실행합니다. (Program arguments는 비워둡니다.)

#### 터미널에서 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=scheduler'
```

---

### 방법 B: 수동으로 배치 잡 한 번 실행하기

필요할 때 일회성으로 배치 잡을 직접 실행합니다.

#### IDE에서 실행
1. `Run/Debug Configurations` 메뉴로 이동합니다.
2. 'Active profiles' 입력란에 `manual-batch`를 입력합니다.
3. 'Program arguments' 입력란에 실행할 잡 이름만 입력합니다. (카테고리 파라미터는 더 이상 필요 없습니다.)
   ```
   modeManCrawlingJob
   ```
4. `RedlineApplication`을 실행합니다.

#### 터미널에서 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=manual-batch modeManCrawlingJob'
```

## 4. 자동 스케줄링 상세

`scheduler` 프로필로 실행 시, `BatchScheduler.java`에 의해 크롤링이 자동 실행됩니다.

### 실행 주기
스케줄러는 '활성 구독자'의 유무에 따라 두 가지 주기로 동작합니다.

- **기본 주기 (구독자 없음)**: **1시간마다** 실행됩니다. (`runSlowScheduledJob`)
- **빠른 주기 (구독자 있음)**: **20초마다** 실행됩니다. (`runFastScheduledJob`)

> **[중요]** 현재 구독 확인 로직(`hasActiveSubscriptions`)은 테스트를 위해 항상 `true`를 반환하도록 되어있습니다. 따라서 지금 애플리케이션을 서버로 실행하면 **무조건 30분마다 크롤링이 실행됩니다.** 추후 실제 데이터베이스를 연동하여 구독 여부를 확인하는 로직을 구현해야 합니다.

### 크롤링 대상
`MultiCategoryProductReader`는 실행될 때마다 아래에 정의된 모든 카테고리를 순차적으로 크롤링하며, **각 카테고리의 모든 페이지를 순회하여 상품을 수집합니다.**
- `Denim Jackets` (code: 263)
- `Denim Pants` (code: 858)

### 설정 변경 방법
- **주기 변경**: `src/main/java/com/jj/redline/batch/scheduler/BatchScheduler.java`의 `@Scheduled(cron = "...")` 표현식을 수정합니다.
- **대상 변경**: `src/main/java/com/jj/redline/batch/reader/MultiCategoryProductReader.java`의 `categoriesToCrawl` 리스트를 수정합니다.

## 5. 프로젝트 구조 및 주요 파일

리팩토링된 최신 구조입니다.

```
src
└── main
    └── java
        └── com/jj/redline
            ├── RedlineApplication.java  (애플리케이션 시작점, @EnableScheduling)
            ├── batch
            │   ├── config
            │   │   └── ModeManBatchJobConfig.java  (배치 Job/Step 설정)
            │   ├── listener
            │   │   └── JobCompletionNotificationListener.java (잡 실행 전후 처리)
            │   ├── scheduler
            │   │   └── BatchScheduler.java  (자동 실행 스케줄러)
            │   ├── runner
            │   │   └── ModeManBatchRunner.java  (수동 실행 러너)
            │   ├── reader
            │   │   └── MultiCategoryProductReader.java  (모든 카테고리 목록 및 페이지 크롤링)
            │   ├── processor
            │   │   └── ModeManDetailCrawlingProcessor.java  (상세 크롤링)
            │   └── output
            │       ├── NdjsonSnapshotWriter.java  (데이터 파일 저장)
            │       └── MetaWriter.java (메타 보고서 저장)
            ├── common
            │   ├── ... (각종 설정 및 유틸리티 클래스)
            ├── crawling
            │   └── modeman
            │       ├── ModeManHttpClient.java  (ModeMan 전용 HTTP 클라이언트, URL 인코딩 강화)
            │       ├── detail
            │       │   └── ModeManJsonLdParser.java  (상세 페이지 파싱 로직, HTML 엔티티 디코딩)
            │       └── list
            │           ├── ListParseResult.java (목록 파싱 결과 DTO)
            │           ├── ModeManListParser.java  (목록 페이지 파싱 로직)
            │           └── ModeManListSelectors.java  (목록 페이지 CSS 선택자)
            └── domain
                └── ... (데이터 DTO 클래스)
```

## 6. 크롤링 프로세스 흐름

리팩토링 후, `modeManCrawlingJob`은 **하나의 스텝(`crawlingStep`)**으로 단순화되었습니다.

1.  **실행**: `BatchScheduler`(자동) 또는 `ModeManBatchRunner`(수동)가 `modeManCrawlingJob`을 실행합니다.
2.  **시작**: `JobCompletionNotificationListener`가 잡 시작을 감지하고, 타임스탬프를 포함한 고유한 파일 이름을 생성하여 '실행 컨텍스트'에 저장합니다.
3.  **처리 (Chunk 기반)**: 이 단계에서는 URL 인코딩 및 HTML 엔티티 디코딩을 통해 데이터의 정확성과 안정성을 확보합니다.
    -   **Read**: `MultiCategoryProductReader`가 잡 시작 시 모든 대상 카테고리의 상품 목록을 **페이지네이션을 통해 모든 페이지를 순회하며** 크롤링하여 리스트로 만듭니다. 그 후, 이 리스트에서 상품(`ProductBrief`)을 하나씩 꺼내 Processor에 전달합니다.
    -   **Process**: `ModeManDetailCrawlingProcessor`가 상품의 상세 페이지를 크롤링/파싱하고, HTML 엔티티를 변환하여 최종 데이터(`ProductSnapshot`)를 생성합니다.
    -   **Write**: `NdjsonSnapshotWriter`가 '실행 컨텍스트'에서 고유한 파일 이름을 가져와, 처리된 `ProductSnapshot` 데이터를 해당 파일에 한 줄씩 기록합니다.
4.  **종료**: 모든 처리가 끝나면 `JobCompletionNotificationListener`가 잡 종료를 감지하고, 전체 실행 결과를 집계하여 `meta.ndjson` 파일에 요약 보고서를 기록합니다.

## 7. 결과물 (Output)

- **경로**: `{output-root-dir}/{site-dir}/{YYYY-MM-DD}/` 디렉토리 아래에 생성됩니다.
- **데이터 파일 (`snapshot-yyyyMMdd-HHmmss.ndjson`)**:
    - 크롤링된 개별 상품의 상세 정보(`ProductSnapshot`)가 NDJSON (Newline Delimited JSON) 형식으로 저장됩니다.
    - 각 라인이 하나의 JSON 객체이며, 상품의 모든 상세 데이터(URL, 이미지, 가격, 옵션 등)를 포함합니다.
    - 잡이 실행될 때마다 고유한 타임스탬프(`yyyyMMdd-HHmmss`)가 포함된 파일 이름으로 생성되어, 각 실행의 결과가 독립적인 파일로 저장됩니다.
- **메타 파일 (`meta.ndjson`)**:
    - 각 배치 잡 실행에 대한 요약 정보(`MetaReport`)가 NDJSON 형식으로 누적 기록됩니다.
    - 잡의 시작/종료 시간, 총 처리 시간, 처리된 상품 수(성공/실패), 에러 샘플 등 잡 실행에 대한 메타데이터를 포함합니다.
    - 이 파일은 잡 실행의 이력을 추적하고 전반적인 크롤링 성능을 모니터링하는 데 사용됩니다.
- **경로 설정**: `src/main/resources/application.yml` 파일의 `redline.crawl.*` 속성을 통해 구성됩니다.