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

## 3. 실행 방법

프로젝트 루트 디렉토리에서 아래의 Gradle 명령어를 사용하여 배치 잡을 실행합니다.

```bash
./gradlew bootRun --args='[스프링_옵션] [잡_이름] [잡_파라미터]'
```

### 전체 실행 명령어 예시

"Denims Jacket" 카테고리(코드: 263)를 크롤링하는 예시입니다.

```bash
./gradlew bootRun --args='--spring.main.web-application-type=NONE --batch.run.modeman=true modeManCrawlingJob categoryCode=263 categoryName="Denims Jacket"'
```

### 명령어 인자 설명

- `--spring.main.web-application-type=NONE`
  - 내장 웹 서버(Tomcat)를 실행하지 않고 배치 애플리케이션으로만 실행합니다.
  - 포트 충돌을 방지하고 리소스를 절약하기 위해 사용합니다.

- `--batch.run.modeman=true`
  - 애플리케이션 실행 시 배치를 실제로 동작시키는 활성화 플래그입니다. (`ModeManBatchRunner` 참조)
  - 기본값은 `false`이므로, 실행을 위해서는 반드시 `true`로 설정해야 합니다.

- `modeManCrawlingJob`
  - 실행할 Spring Batch Job의 이름입니다. (`ModeManBatchJobConfig` 참조)

- `categoryCode=<카테고리_코드>`
  - 크롤링할 대상 카테고리의 번호를 지정하는 잡 파라미터입니다. (예: `263`)

- `categoryName="<카테고리_이름>"`
  - 크롤링할 대상 카테고리의 이름을 지정하는 잡 파라미터입니다.
  - 이름에 공백이 있을 경우, 반드시 따옴표(`"`)로 감싸야 합니다. (예: `"Denims Jacket"`)

## 4. 아키텍처

이 배치는 `modeManCrawlingJob`이라는 하나의 Job과 2개의 순차적인 Step으로 구성됩니다.

- **Job: `modeManCrawlingJob`**
  1.  **Step 1: `modeManListCrawlingStep` (Tasklet 기반)**
      - `ModeManListCrawlingTasklet`이 실행됩니다.
      - 잡 파라미터로 받은 카테고리의 상품 '목록' 페이지를 크롤링하여 각 상품의 기본 정보(`ProductBrief`) 리스트를 수집합니다.
      - 수집된 `ProductBrief` 리스트는 다음 스텝으로 전달하기 위해 `ExecutionContext`에 저장됩니다.

  2.  **Step 2: `modeManDetailProcessingStep` (Chunk 기반)**
      - **Reader (`ProductBriefReader`):** 이전 스텝의 `ExecutionContext`에서 `ProductBrief` 리스트를 가져와, 청크 단위로 하나씩 읽습니다.
      - **Processor (`ModeManDetailCrawlingProcessor`):** `ProductBrief` 하나를 받아 해당 상품의 '상세' 페이지 URL로 접근하여 HTML을 크롤링합니다. `ModeManJsonLdParser`를 통해 페이지 내의 JSON-LD 스키마를 파싱하여 최종 데이터 형태인 `ProductSnapshot` DTO를 생성합니다.
      - **Writer (`NdjsonSnapshotWriter`):** Processor가 생성한 `ProductSnapshot` DTO를 청크(기본 10개) 단위로 모아, 지정된 경로의 파일에 NDJSON 형태로 한 줄씩 기록합니다.

## 5. 결과물 (Output)

- **경로 형식**: `{output-root-dir}/{site-dir}/{YYYY-MM-DD}/{snapshot-file-name}`
- **경로 설정**: `src/main/resources/application.yml` 파일의 `local` 프로필에 정의된 `redline.crawl.*` 속성을 통해 구성됩니다.
- **실제 예시 경로**: `/Users/soheejjang/programming/backend/output/modeman/2026-03-07/snapshot.ndjson`
- **파일 포맷**: **NDJSON (Newline Delimited JSON)**. 파일의 각 줄이 하나의 독립적인 JSON 객체인 텍스트 파일입니다.

## 6. 주요 파일

- `src/main/java/com/jj/redline/batch/config/ModeManBatchJobConfig.java`
  - Spring Batch의 Job과 Step의 흐름을 정의하는 핵심 설정 파일.

- `src/main/java/com/jj/redline/batch/runner/ModeManBatchRunner.java`
  - 애플리케이션 실행 시 Job을 트리거하고, 커맨드 라인 인자를 해석하여 Job 파라미터로 변환하는 역할을 합니다.

- `src/main/java/com/jj/redline/batch/tasklet/ModeManListCrawlingTasklet.java`
  - Step 1에서 상품 '목록' 페이지를 크롤링하는 로직을 담고 있습니다.

- `src/main/java/com/jj/redline/batch/processor/ModeManDetailCrawlingProcessor.java`
  - Step 2에서 상품 '상세' 페이지 크롤링을 지휘하고 Parser를 호출합니다.

- `src/main/java/com/jj/redline/crawling/modeman/detail/ModeManJsonLdParser.java`
  - 상세 페이지의 HTML에 포함된 JSON-LD 데이터를 파싱하여 `ProductSnapshot` DTO를 채우는 가장 핵심적인 파싱 로직을 담고 있습니다.

- `src/main/java/com/jj/redline/batch/output/NdjsonSnapshotWriter.java`
  - 최종 결과물인 `ProductSnapshot` DTO를 NDJSON 파일로 저장합니다.

- `src/main/java/com/jj/redline/domain/dto/ProductSnapshot.java`
  - 최종 결과물 하나의 데이터 구조를 정의하는 메인 DTO 클래스입니다.

- `src/main/resources/application.yml`
  - 데이터베이스, 배치 실행, 결과물 경로 등 애플리케이션의 주요 설정을 담고 있는 파일입니다.
