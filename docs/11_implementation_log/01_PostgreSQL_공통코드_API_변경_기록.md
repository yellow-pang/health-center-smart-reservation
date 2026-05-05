# PostgreSQL 공통코드 API 변경 기록

## 문서 목적

이 문서는 이번 변경을 처음 보는 사람이 “무엇이 바뀌었고, 왜 그렇게 바뀌었는지”를 빠르게 이해할 수 있도록 작성한 구현 기록이다.

이번 변경의 핵심은 eGovFrame Simple Backend Template을 HSQL 샘플 실행 상태에서 PostgreSQL 기반 신규 보건소 도메인 구현 상태로 한 단계 전환한 것이다. 샘플 전체를 바로 삭제하지 않고, 먼저 실제 PostgreSQL을 사용하는 가장 작은 기능인 공통코드 조회 API를 추가했다.

## 배경

프로젝트 백엔드는 eGovFrame Simple Backend Template을 기반으로 한다. 템플릿은 게시판, 일정, 회원, 파일, JWT, Swagger, MyBatis 샘플을 포함하고 있으며 기본 DB는 HSQL이었다.

하지만 보건소 스마트 예약·대기 시스템의 목표 DB는 PostgreSQL 18 + pgvector Docker 이미지이다. 따라서 실제 도메인 구현을 시작하려면 다음 기반이 먼저 필요했다.

1. PostgreSQL JDBC 드라이버
2. `Globals.DbType=postgresql` 기준 설정
3. PostgreSQL용 MyBatis mapper 로딩
4. 신규 보건소 도메인 패키지
5. PostgreSQL에서 실제 데이터를 읽는 최소 API

이 중 최소 API로 공통코드 조회를 선택했다.

## 왜 공통코드 API부터 구현했는가

공통코드는 예약 상태, 대기 상태, 방문 유형, 사용자 역할, 혼잡도처럼 여러 화면과 API에서 반복적으로 쓰이는 기준 데이터이다.

예약 생성이나 대기 호출 같은 핵심 도메인을 먼저 구현하면 필요한 테이블과 정책이 많아진다. 반면 공통코드 조회는 다음 장점이 있다.

| 이유 | 설명 |
|---|---|
| DB 연결 검증에 적합 | PostgreSQL 테이블을 만들고 실제 SELECT를 수행할 수 있다. |
| 도메인 리스크가 낮음 | 상태 전이, 동시성, 인증 권한 같은 복잡한 정책이 적다. |
| 이후 기능의 기반 | 예약 상태, 대기 상태, 혼잡도 표시명 등에 재사용된다. |
| 화면 연동에 유용 | 프론트엔드 셀렉트 박스, 필터, 상태 배지에 바로 사용할 수 있다. |

따라서 “PostgreSQL 전환 검증”과 “보건소 도메인 기반 마련”을 동시에 만족하는 첫 기능으로 공통코드를 구현했다.

## 변경 전 상태

변경 전 백엔드는 다음 상태였다.

| 항목 | 변경 전 |
|---|---|
| 기본 DB | HSQL |
| DB 타입 | `Globals.DbType=hsql` |
| DB 초기화 | `src/main/resources/db/shtdb.sql` |
| mapper 위치 | `egovframework/mapper/let/**/*_hsql.xml` |
| 신규 도메인 | 자리만 준비 |
| 보건소 API | 없음 |

HSQL 기준으로 템플릿 샘플이 정상 빌드 및 실행되는 것을 먼저 확인한 뒤, PostgreSQL 전환 작업을 진행했다.

## 변경 후 상태

변경 후 백엔드는 다음 상태이다.

| 항목 | 변경 후 |
|---|---|
| 기본 DB | PostgreSQL |
| DB 타입 | `Globals.DbType=postgresql` |
| DB 접속 | `jdbc:postgresql://127.0.0.1:5432/health_center` |
| DB 초기화 | Spring SQL init |
| schema SQL | `backend/src/main/resources/db/postgresql/schema.sql` |
| seed SQL | `backend/src/main/resources/db/postgresql/data.sql` |
| 신규 Java 패키지 | `egovframework.healthcenter.common` |
| 신규 mapper 위치 | `egovframework/mapper/healthcenter/common` |
| 첫 API | `GET /api/common-codes/{groupCode}`, `GET /api/common-codes` |

이제 백엔드는 PostgreSQL 컨테이너가 실행된 상태에서 구동되어야 한다.

## 변경된 주요 파일

### 1. Maven 의존성

파일:

```text
backend/pom.xml
```

변경:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

이유:

PostgreSQL에 접속하려면 JDBC 드라이버가 필요하다. eGovFrame의 `BasicDataSource`가 `org.postgresql.Driver`를 사용해 PostgreSQL 연결을 만들 수 있도록 추가했다.

### 2. application 설정

파일:

```text
backend/src/main/resources/application.properties
```

핵심 변경:

```properties
Globals.DbType=postgresql

Globals.postgresql.DriverClassName=org.postgresql.Driver
Globals.postgresql.Url=jdbc:postgresql://127.0.0.1:5432/health_center
Globals.postgresql.UserName=health
Globals.postgresql.Password=health1234

spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:/db/postgresql/schema.sql
spring.sql.init.data-locations=classpath:/db/postgresql/data.sql
spring.sql.init.encoding=UTF-8
```

이유:

기존 `EgovConfigAppDatasource`는 `Globals.DbType` 값을 읽고, `Globals.{DbType}.*` 설정을 사용해 DataSource를 만든다. 따라서 `postgresql`이라는 DB 타입 이름과 `Globals.postgresql.*` 설정을 맞춰 추가했다.

SQL init은 개발 초기 단계에서 테이블과 seed 데이터를 빠르게 재현하기 위해 사용했다. 장기적으로는 Flyway 전환을 검토한다.

### 3. 보안 예외 경로

파일:

```text
backend/src/main/java/egovframework/com/security/SecurityConfig.java
```

추가 경로:

```java
"/api/common-codes",
"/api/common-codes/**",
```

이유:

공통코드 조회는 프론트엔드 초기 화면 구성에도 쓰일 수 있고, 예약 상태나 혼잡도 표시명 같은 공개 목록을 내려주는 용도이다. 그래서 MVP 초기에는 인증 없이 조회 가능하도록 했다.

관리자용 공통코드 수정 API는 아직 만들지 않았고, 향후 구현 시 별도 권한을 적용해야 한다.

### 4. PostgreSQL schema/data SQL

파일:

```text
backend/src/main/resources/db/postgresql/schema.sql
backend/src/main/resources/db/postgresql/data.sql
```

생성 테이블:

```text
common_code_groups
common_codes
```

이유:

공통코드 그룹과 코드를 분리해 관리하기 위함이다.

- `common_code_groups`: `RESERVATION_STATUS`, `QUEUE_STATUS` 같은 그룹
- `common_codes`: `RESERVED`, `WAITING`, `HIGH` 같은 실제 코드

초기 seed 데이터에는 사용자 역할, 예약 상태, 방문 유형, 방문 상태, 대기 상태, 혼잡도 코드가 포함된다.

### 5. 공통 응답 구조

파일:

```text
backend/src/main/java/egovframework/healthcenter/common/response/ApiResponse.java
backend/src/main/java/egovframework/healthcenter/common/response/ApiError.java
```

응답 형식:

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

이유:

프로젝트 설계 문서에서 신규 보건소 API는 `success + data + error` 형식을 사용하기로 정했다. 기존 eGovFrame 샘플의 `ResultVO`와 섞지 않고, 신규 도메인 API용 응답 구조를 별도로 만들었다.

### 6. 공통코드 API 계층

추가된 Java 파일:

```text
backend/src/main/java/egovframework/healthcenter/common/code/api/CommonCodeController.java
backend/src/main/java/egovframework/healthcenter/common/code/application/CommonCodeQueryService.java
backend/src/main/java/egovframework/healthcenter/common/code/dto/CommonCodeResponse.java
backend/src/main/java/egovframework/healthcenter/common/code/mapper/CommonCodeMapper.java
backend/src/main/java/egovframework/healthcenter/common/code/mapper/CommonCodeVO.java
```

역할:

| 파일 | 역할 |
|---|---|
| `CommonCodeController` | HTTP API 제공 |
| `CommonCodeQueryService` | 조회 트랜잭션과 응답 변환 처리 |
| `CommonCodeResponse` | API 응답 DTO |
| `CommonCodeMapper` | MyBatis DAO |
| `CommonCodeVO` | Mapper 조회 결과 VO |

이유:

신규 보건소 도메인은 `egovframework.healthcenter` 아래에서 Controller, Service, Mapper, DTO 책임을 분리한다. 조회 서비스에는 `@Transactional(readOnly = true)`를 적용해 조회 전용 의도를 명확히 했다.

### 7. MyBatis mapper XML

파일:

```text
backend/src/main/resources/egovframework/mapper/healthcenter/common/CommonCode_SQL_postgresql.xml
```

제공 쿼리:

| SQL id | 설명 |
|---|---|
| `selectActiveCodes` | 사용 중인 전체 공통코드 조회 |
| `selectActiveCodesByGroupCode` | 특정 그룹의 사용 중 공통코드 조회 |
| `selectActiveCodesByGroupCodes` | 여러 그룹의 사용 중 공통코드 조회 |

이유:

현재 MyBatis mapper 로딩 규칙은 DB 타입 접미사를 사용한다.

```text
*_postgresql.xml
```

따라서 PostgreSQL용 신규 mapper 파일명을 `CommonCode_SQL_postgresql.xml`로 맞췄다.

## 추가된 API

### 그룹별 공통코드 조회

```http
GET /api/common-codes/{groupCode}
```

예시:

```http
GET /api/common-codes/RESERVATION_STATUS
```

응답 예시:

```json
{
  "success": true,
  "data": [
    {
      "groupCode": "RESERVATION_STATUS",
      "code": "RESERVED",
      "codeName": "예약 완료",
      "description": "사용자가 예약을 완료한 상태",
      "sortOrder": 1
    }
  ],
  "error": null
}
```

### 여러 그룹 공통코드 일괄 조회

```http
GET /api/common-codes?groupCodes=RESERVATION_STATUS,QUEUE_STATUS
```

응답은 그룹 코드별 Map 구조로 내려간다.

```json
{
  "success": true,
  "data": {
    "RESERVATION_STATUS": [
      {
        "groupCode": "RESERVATION_STATUS",
        "code": "RESERVED",
        "codeName": "예약 완료",
        "description": "사용자가 예약을 완료한 상태",
        "sortOrder": 1
      }
    ]
  },
  "error": null
}
```

## 실행 방법

### 1. PostgreSQL 컨테이너 실행

프로젝트 루트에서 실행한다.

```bash
docker compose up -d postgres
```

현재 접속 정보:

| 항목 | 값 |
|---|---|
| Host | localhost |
| Port | 5432 |
| Database | health_center |
| User | health |
| Password | health1234 |

### 2. 백엔드 실행

```bash
cd backend
mvn spring-boot:run
```

### 3. Swagger 확인

```text
http://localhost:8080/swagger-ui/index.html
```

### 4. API 직접 확인

```text
http://localhost:8080/api/common-codes/RESERVATION_STATUS
http://localhost:8080/api/common-codes?groupCodes=RESERVATION_STATUS,QUEUE_STATUS
```

## 검증 결과

현재 확인된 검증:

```text
mvn -q -DskipTests compile
```

결과:

```text
성공
```

런타임 검증은 아직 완료하지 못했다. 현재 작업 환경에서 Docker Desktop 엔진이 실행되어 있지 않아 `docker compose up -d postgres`가 실패했기 때문이다.

실행 검증을 완료하려면 Docker Desktop을 실행한 뒤 PostgreSQL 컨테이너를 기동하고 백엔드를 실행해야 한다.

## 의도적으로 하지 않은 일

이번 변경에서는 아래 작업을 하지 않았다.

| 제외한 작업 | 이유 |
|---|---|
| 샘플 코드 삭제 | PostgreSQL 첫 API 검증 후 삭제 범위를 확정하기 위함 |
| 예약 도메인 구현 | 첫 단계에서는 DB 연결과 공통코드 기반만 검증하기 위함 |
| 대기 도메인 구현 | 예약/방문 흐름 이후 구현하는 것이 자연스러움 |
| 대시보드 구현 | 데이터 생성 흐름이 먼저 필요함 |
| Flyway 도입 | 초기에는 SQL init으로 빠르게 검증하고, 안정화 후 전환 예정 |
| pgvector 사용 | MVP에서 AI 기능은 제외되어 있음 |
| 운영용 secret 분리 | 현재는 개발 환경 기준이며, 운영 전 환경변수 분리가 필요함 |

## 현재 리스크

| 리스크 | 설명 | 대응 |
|---|---|---|
| 샘플 API 깨짐 | `Globals.DbType=postgresql` 전환 후 기존 샘플의 PostgreSQL mapper가 없어 샘플 API는 실패할 수 있음 | 샘플 API 정상 동작을 목표로 삼지 않고, 샘플 제거 범위를 확정 |
| Docker 미검증 | Docker Desktop 미실행으로 런타임 검증이 보류됨 | Docker 실행 후 공통코드 API 호출 검증 |
| SQL init 반복 실행 | `spring.sql.init.mode=always`는 실행 때마다 SQL을 수행함 | SQL은 `IF NOT EXISTS`, `ON CONFLICT`로 반복 실행 가능하게 작성 |
| 보안 설정 | 공통코드 조회를 공개 API로 열어둠 | 수정 API 구현 시 STAFF/ADMIN 권한 적용 |
| 비밀값 노출 | DB 비밀번호가 properties에 있음 | 운영 전 환경변수 또는 별도 secret 관리로 전환 |

## 다음 작업

다음 순서로 진행하는 것이 좋다.

1. Docker Desktop 실행
2. `docker compose up -d postgres`
3. `cd backend && mvn spring-boot:run`
4. Swagger 또는 브라우저에서 공통코드 API 확인
5. 샘플 API 실패 범위 확인
6. 게시판, 일정, 회원, SNS 샘플 제거 계획 확정
7. 보건소 기본 정보 또는 예약 가능 슬롯 API 구현

## 커밋 메시지

제목:

```text
docs: PostgreSQL 공통코드 API 변경 기록 추가
```

내용:

```text
- PostgreSQL 전환 배경과 변경 이유 정리
- 공통코드 API를 첫 검증 기능으로 선택한 이유 설명
- 변경된 설정, SQL, Java 계층, Mapper 역할 정리
- 실행 방법과 검증 상태 기록
- 남은 리스크와 다음 작업 정리
```
