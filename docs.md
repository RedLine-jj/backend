# ModeMan 상품 크롤링 배치

## 1. 개요

이 프로젝트는 "ModeMan" 웹사이트([https://mode-man.com/](https://mode-man.com/))의 특정 카테고리 상품 정보를 수집하여, NDJSON(Newline Delimited JSON) 형식의 파일로 저장하는 Spring Batch 애플리케이션입니다.

## 2. 주요 기술

- Java 17
- Spring Boot
- Spring Batch
- Gradle
- Jsoup (HTML Parsing)
- Jackson (JSON Processing)

## 3. 수동 실행 방법

필요할 때 일회성으로 배치 잡을 직접 실행할 수 있습니다. 프로젝트 루트 디렉토리에서 아래의 Gradle 명령어를 사용합니다.

```bash
./gradlew bootRun --args='[스프링_옵션] [잡_이름] [잡_파라미터]'
```

### 전체 실행 명령어 예시

"Denims Jacket" 카테고리(코드: 263)를 크롤링하는 예시입니다.

```bash
./gradlew bootRun --args='--spring.main.web-application-type=NONE --batch.run.modeman=true modeManCrawlingJob categoryCode=263 categoryName="Denims Jacket"'
```

### 명령어 인자 설명

- `--spring.main.web-application-type=NONE`: 내장 웹 서버 없이 순수 배치 애플리케이션으로 실행합니다.
- `--batch.run.modeman=true`: 수동 실행을 활성화하는 플래그입니다.
- `modeManCrawlingJob`: 실행할 Job의 이름입니다.
- `categoryCode`, `categoryName`: 크롤링할 대상 카테고리를 지정하는 잡 파라미터입니다.

## 4. 자동 스케줄링

이 애플리케이션을 서버로 배포하면, 내장된 스케줄러(`BatchScheduler.java`)를 통해 주기적으로 크롤링을 자동 실행합니다.

### 실행 주기

스케줄러는 '활성 구독자'의 유무에 따라 두 가지 주기로 동작합니다.

- **기본 주기 (구독자 없음)**: **1시간마다** 실행됩니다. (`runSlowScheduledJob`)
- **빠른 주기 (구독자 있음)**: **30분마다** 실행됩니다. (`runFastScheduledJob`)

> **[중요]** 현재 구독 확인 로직(`hasActiveSubscriptions`)은 테스트를 위해 항상 `true`를 반환하도록 되어있습니다. 따라서 지금 애플리케이션을 서버로 실행하면 **무조건 30분마다 크롤링이 실행됩니다.** 추후 실제 데이터베이스를 연동하여 구독 여부를 확인하는 로직을 구현해야 합니다.

### 크롤링 대상

스케줄러는 실행될 때마다 아래에 정의된 모든 카테고리를 순차적으로 크롤링합니다.

- `Denim Jackets` (code: 263)
- `Denim Pants` (code: 858)

### 설정 변경 방법

- **주기 변경**: `src/main/java/com/jj/redline/batch/scheduler/BatchScheduler.java` 파일 상단의 `@Scheduled(cron = "...")` 표현식을 수정하여 실행 주기를 변경할 수 있습니다.
- **대상 변경**: 같은 파일의 `categoriesToCrawl` 리스트를 수정하여 크롤링할 카테고리를 추가하거나 변경할 수 있습니다.

## 5. 프로젝트 구조 및 주요 파일

배치 작업의 핵심 로직과 관련된 주요 파일 구조는 다음과 같습니다.

```
src
└── main
    └── java
        └── com/jj/redline
            ├── RedlineApplication.java  (애플리케이션 시작점, @EnableScheduling)
            ├── batch
            │   ├── config
            │   │   └── ModeManBatchJobConfig.java  (배치 Job/Step 설정)
            │   ├── scheduler
            │   │   └── BatchScheduler.java  (자동 실행 스케줄러)
            │   ├── runner
            │   │   └── ModeManBatchRunner.java  (수동 실행 러너)
            │   ├── tasklet
            │   │   └── ModeManListCrawlingTasklet.java  (1. 목록 크롤링)
            │   ├── reader
            │   │   └── ProductBriefReader.java  (2. 처리 대상 데이터 읽기)
            │   ├── processor
            │   │   └── ModeManDetailCrawlingProcessor.java  (3. 상세 크롤링)
            │   └── output
            │       └── NdjsonSnapshotWriter.java  (4. 파일 저장)
            ├── common
            │   ├── ApiResponse.java  (현재 미사용)
            │   ├── config
            │   │   ├── HttpClientConfig.java  (HTTP 클라이언트 설정)
            │   │   └── ModeManCrawlProperties.java  (크롤링 경로/속성 설정)
            │   └── util
            │       ├── MoneyParser.java  (금액 파싱 유틸)
            │       ├── QueryParamExtractor.java  (URL 쿼리 파라미터 추출 유틸)
            │       ├── TimeUtil.java  (시간 관련 유틸)
            │       └── UrlNormalizer.java  (URL 정규화 유틸)
            ├── config
            │   ├── QueryDslConfig.java  (현재 미사용)
            │   └── SwaggerConfig.java  (현재 미사용)
            ├── crawling
            │   └── modeman
            │       ├── ModeManHttpClient.java  (ModeMan 전용 HTTP 클라이언트)
            │       ├── detail
            │       │   └── ModeManJsonLdParser.java  (상세 페이지 파싱 로직)
            │       └── list
            │           ├── ModeManListParser.java  (목록 페이지 파싱 로직)
            │           └── ModeManListSelectors.java  (목록 페이지 CSS 선택자)
            └── domain
                └── dto
                    ├── CategoryDto.java
                    ├── ParseStatus.java
                    ├── ProductBrief.java
                    ├── ProductOption.java
                    ├── ProductSnapshot.java
                    ├── Site.java
                    └── StockStatus.java
```

## 6. 크롤링 프로세스 흐름

`modeManCrawlingJob`은 두 개의 순차적인 단계(Step)로 구성됩니다.

1.  **실행**: `BatchScheduler`(자동) 또는 `ModeManBatchRunner`(수동)가 잡 파라미터를 설정하여 `modeManCrawlingJob`을 실행합니다.
2.  **Step 1: 목록 수집 (`modeManListCrawlingStep`)**: `ModeManListCrawlingTasklet`이 카테고리 목록 페이지의 모든 상품 정보를 `ProductBrief` DTO 리스트로 수집하여 다음 스텝에 전달합니다.
3.  **Step 2: 상세 처리 (`modeManDetailProcessingStep`)**: 청크 단위로 동작하며, `Reader`가 `ProductBrief`를 하나씩 읽고, `Processor`가 상세 페이지를 크롤링/파싱하여 최종 `ProductSnapshot` DTO를 만들면, `Writer`가 파일에 기록합니다.

## 7. 결과물 (Output)

- **경로 형식**: `{output-root-dir}/{site-dir}/{YYYY-MM-DD}/{snapshot-file-name}`
- **경로 설정**: `src/main/resources/application.yml` 파일의 `local` 프로필에 정의된 `redline.crawl.*` 속성을 통해 구성됩니다.
- **실제 예시 경로**: `/Users/soheejjang/programming/backend/output/modeman/2026-03-07/snapshot.ndjson`
- **파일 포맷**: **NDJSON (Newline Delimited JSON)**. 파일의 각 줄이 하나의 독립적인 JSON 객체인 텍스트 파일입니다.
