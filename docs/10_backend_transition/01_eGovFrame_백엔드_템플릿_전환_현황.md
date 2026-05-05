# eGovFrame 백엔드 템플릿 전환 현황

## 목적

이 문서는 보건소 스마트 예약·대기 및 혼잡도 분석 시스템의 백엔드 구현을 시작하기 전에, eGovFrame Simple Backend Template을 프로젝트 기준에 맞게 전환하기 위해 지금까지 확인한 내용을 정리한다.

다음 단계에서는 이 문서를 기준으로 샘플 기능 정리, PostgreSQL 전환, 보건소 도메인 구현 순서를 결정한다.

## 현재 상태 요약

| 항목 | 현재 상태 |
|---|---|
| 백엔드 위치 | `backend` |
| 기반 템플릿 | eGovFrame Simple Backend Template |
| 빌드 도구 | Maven |
| Java | 17 |
| Spring Boot | 3.5.6 |
| API 문서 | Springdoc OpenAPI / Swagger UI |
| DB 접근 방식 | MyBatis |
| 현재 실행 DB | HSQL |
| 목표 DB | PostgreSQL 18 + pgvector Docker 이미지 |
| 신규 도메인 예정 패키지 | `egovframework.healthcenter` |
| 신규 mapper 예정 위치 | `src/main/resources/egovframework/mapper/healthcenter` |

현재 템플릿은 HSQL 기준으로 정상 빌드 및 실행되는 것을 확인했다. 샘플 코드는 삭제하지 않았다. PostgreSQL 전환은 바로 스위칭하지 않고, `postgresql` 설정과 신규 보건소 mapper 경로만 먼저 준비하는 선택지 C 방향으로 진행 중이다.

## 지금까지 진행한 일

1. eGovFrame Simple Backend Template을 `backend` 폴더에 배치했다.
2. 프로젝트 루트에 PostgreSQL 18 + pgvector용 `docker-compose.yml`을 준비했다.
3. `backend/README.md`에 현재 백엔드 기준, 실행 방법, Docker PostgreSQL과의 관계, 향후 전환 원칙을 정리했다.
4. HSQL 기준으로 백엔드가 빌드 및 실행되는 것을 확인했다.
5. `pom.xml`, application 설정, Swagger/JWT, MyBatis, 샘플 Controller/Service/Mapper 구조를 분석했다.
6. 선택지 C 방향으로 PostgreSQL 드라이버, `Globals.postgresql.*` 설정, `healthcenter` mapper 로딩 경로, 신규 패키지 자리만 추가했다.
7. 아직 실제 샘플 코드 삭제, `Globals.DbType` PostgreSQL 전환, 보건소 도메인 구현은 하지 않았다.

## pom.xml 의존성 분석

핵심 유지 후보는 다음과 같다.

| 구분 | 의존성/기능 | 판단 |
|---|---|---|
| Web | `spring-boot-starter-web` | REST API 기반으로 유지 |
| eGovFrame | `egovframe-rte-ptl-mvc`, `egovframe-rte-psl-dataaccess`, `egovframe-rte-fdl-*` | eGovFrame 기반 유지에 필요 |
| MyBatis | eGovFrame dataaccess 경유 | MVP 기본 DB 접근 방식으로 유지 |
| Security | eGovFrame security, Spring Security, JWT | 인증/인가 골격으로 유지하되 보건소 권한 모델에 맞게 재정리 필요 |
| Swagger | `springdoc-openapi-starter-webmvc-ui` | API 명세 확인용으로 유지 |
| HSQL | `hsqldb` | 템플릿 실행 확인용. PostgreSQL 전환 후 제거 또는 test scope 검토 |
| Logging | Log4j2, log4jdbc | SQL 확인에 유용. 운영 기준은 별도 검토 |

정리 또는 재검토 후보는 다음과 같다.

| 구분 | 항목 | 사유 |
|---|---|---|
| DB Driver | `mysql-connector-j` | 현재 목표 DB는 PostgreSQL이므로 전환 시 제거 또는 대체 후보 |
| JPA/QueryDSL | `spring-boot-starter-data-jpa`, QueryDSL 관련 의존성 | MVP는 MyBatis 기준이므로 테스트 샘플 외에는 불필요 |
| Selenium | `selenium-java` | 템플릿 테스트 샘플 성격 |
| JSP | `tomcat-embed-jasper` | REST API 서버에서 JSP를 사용하지 않는다면 제거 후보 |
| 중복 선언 | `tomcat-embed-jasper`, `tomcat-annotations-api`, `commons-lang3` | Maven effective model 경고 발생. 정리 필요 |

Maven dependency tree 실행 중 중복 의존성 경고가 확인되었다. 현재 빌드는 가능하지만 향후 Maven 버전에서 문제가 될 수 있으므로 샘플 정리 단계에서 함께 정리하는 것이 좋다.

## application 설정 분석

주요 설정 파일은 다음과 같다.

| 파일 | 역할 |
|---|---|
| `backend/src/main/resources/application.properties` | 기본 설정. 현재 `spring.profiles.active=dev`, `Globals.DbType=hsql` |
| `backend/src/main/resources/application-dev.properties` | 개발 프로필. OS, HSQL, 로그 보관 기간, 파일 경로 일부 override |
| `backend/src/main/resources/application-prod.properties` | 운영 프로필. 현재도 HSQL 기준 값 포함 |

현재 DB 선택 구조는 `Globals.DbType`에 의존한다.

```properties
Globals.DbType=hsql
```

`EgovConfigAppDatasource`는 `Globals.DbType`이 `hsql`이면 embedded HSQL을 생성하고, `classpath:/db/shtdb.sql`을 로딩한다. 그 외 DB 타입이면 `Globals.{dbType}.DriverClassName`, `Globals.{dbType}.Url`, `Globals.{dbType}.UserName`, `Globals.{dbType}.Password`를 읽어 `BasicDataSource`를 생성한다.

PostgreSQL 전환 시 DB 타입 이름은 `postgresql`로 통일한다. 설정 키는 `Globals.postgresql.*`, mapper 파일 접미사는 `*_postgresql.xml` 기준을 사용한다.

## Swagger와 JWT 예시 구조 분석

Swagger 설정은 `OpenApiConfig`에 있다.

| 항목 | 현재 값 |
|---|---|
| Swagger UI | `/swagger-ui/index.html` |
| OpenAPI Docs | `/v3/api-docs` |
| 스캔 패키지 | `egovframework` |
| Security scheme | `Authorization` 헤더 APIKEY 방식 |

JWT 관련 구조는 다음 파일에 있다.

| 파일 | 역할 |
|---|---|
| `SecurityConfig` | SecurityFilterChain, CORS, 인증 예외 URL, JWT 필터 등록 |
| `JwtAuthenticationFilter` | 모든 요청에서 `Authorization` 헤더를 읽고 JWT 검증 |
| `EgovJwtTokenUtil` | JWT 생성/검증, `LoginVO` 변환 |
| `EgovLoginApiController` | `POST /auth/login-jwt`, `GET /auth/logout` |

주의할 점은 Swagger 문서 예시와 실제 필터 구현이 다르다는 점이다.

- `backend/swagger.md` 예시는 `Authorization: Bearer {JWT_TOKEN}` 형태를 안내한다.
- 현재 `JwtAuthenticationFilter`는 `Bearer ` 접두어 제거 없이 헤더 값을 바로 JWT로 파싱한다.

따라서 다음 단계에서 둘 중 하나로 통일해야 한다. 일반적인 API 관례를 따르려면 `Bearer ` 접두어를 지원하도록 필터를 보완하는 쪽이 자연스럽다.

또한 `EgovJwtTokenUtil`은 현재 secret 값을 debug 로그로 출력한다. 실제 서비스 전 반드시 제거해야 한다.

## MyBatis 설정과 mapper 위치 분석

MyBatis 설정의 핵심 파일은 다음과 같다.

| 파일 | 역할 |
|---|---|
| `EgovConfigAppMapper` | SqlSessionFactoryBean, SqlSessionTemplate 설정 |
| `egovframework/mapper/config/mapper-config.xml` | MyBatis settings, typeAlias 설정 |

현재 mapper 로딩 패턴은 다음과 같다.

```text
classpath:/egovframework/mapper/let/**/*_${Globals.DbType}.xml
```

현재 `Globals.DbType=hsql`이므로 `*_hsql.xml`만 로딩된다.

현재 템플릿 mapper는 다음 DB별 파일을 포함한다.

- `*_hsql.xml`
- `*_mysql.xml`
- `*_oracle.xml`
- `*_tibero.xml`
- `*_cubrid.xml`
- `*_altibase.xml`

PostgreSQL용 mapper 파일은 아직 없다. 신규 보건소 도메인은 기존 `let` 하위가 아니라 `egovframework/mapper/healthcenter` 하위에 둘 예정이므로, mapper 로딩 경로를 다음 중 하나로 결정해야 한다.

1. 기존 `let` 패턴에 `healthcenter` 패턴을 추가한다. 현재 이 방향으로 1차 반영했다.
2. 전체 mapper 루트를 `egovframework/mapper/**/*_${dbType}.xml`처럼 넓힌다.
3. 샘플 정리 후 보건소 mapper 전용 패턴으로 단순화한다.

## 기존 샘플 기능 목록

현재 템플릿에는 다음 샘플 API 기능이 포함되어 있다.

| 영역 | Controller | 주요 API |
|---|---|---|
| 메인 | `EgovMainApiController` | `/mainPage` |
| 로그인 | `EgovLoginApiController` | `/auth/login-jwt`, `/auth/logout` |
| SNS 로그인 | `SnsLoginApiController` | Kakao/Naver 로그인 샘플 |
| 회원 관리 | `EgovMberManageApiController` | `/members`, `/mypage`, `/etc/member_*` |
| 관리자 | `EgovSiteManagerApiController` | `/jwtAuthAPI`, `/admin/password` |
| 게시판 | `EgovBBSManageApiController` | `/board`, `/boardReply`, `/boardFileAtch/{bbsId}` |
| 게시판 속성 | `EgovBBSAttributeManageApiController` | `/bbsMaster` |
| 게시판 이용정보 | `EgovBBSUseInfoManageApiController` | `/bbsUseInf`, `/notUsedBbsMaster` |
| 일정 | `EgovIndvdlSchdulManageApiController` | `/schedule/*` |
| 파일 | `EgovFileMngApiController`, `EgovFileDownloadController` | `/file` |
| 이미지 | `EgovImageProcessController` | `/image` |

관련 Service/DAO는 로그인, 회원, 게시판, 일정, 파일, 공통코드, 사용자 정보 조회 영역으로 나뉜다.

## 유지할 설정과 삭제 가능한 샘플 코드

우선 유지할 후보는 다음과 같다.

| 구분 | 유지 후보 |
|---|---|
| 실행 기반 | `EgovBootApplication`, Maven 구조 |
| 설정 | datasource, mapper, transaction, message, properties, whitelist, common config |
| 보안 | Spring Security 설정, JWT 필터/유틸 골격 |
| API 문서 | OpenAPI/Swagger 설정 |
| 공통 응답 | `ResultVO`, `ResponseCode` 등은 신규 공통 응답 설계 전까지 참고 |
| 파일 처리 | 파일 업로드/다운로드가 보건소 도메인에 필요하면 일부 재사용 가능 |
| 공통코드 | 보건소 공통코드 설계와 맞으면 일부 재사용 또는 재구현 검토 |

삭제 또는 격리 후보는 다음과 같다.

| 구분 | 삭제/격리 후보 |
|---|---|
| 게시판 샘플 | 게시판, 게시판 속성, 게시판 이용정보 관련 Controller/Service/DAO/DTO/VO/Mapper |
| 일정 샘플 | 기존 개인 일정 관리 기능 |
| 회원 샘플 | 기존 회원가입/회원관리 API. 보건소 사용자/담당자/관리자 모델과 다름 |
| SNS 샘플 | MVP 범위 밖 |
| JPA 테스트 | `src/test/java/egovframework/study/jpa` |
| Selenium 테스트 | 템플릿 UI 테스트 샘플 |
| DB 샘플 | `DATABASE`의 벤더별 샘플 DDL/DML, HSQL `shtdb.sql`은 전환 후 정리 후보 |
| 문서 샘플 | `backend/swagger.md`, `backend/Docs` 중 템플릿 변환 참고 문서 |

삭제는 아직 진행하지 않는다. 다음 단계에서 제거 순서와 영향 범위를 먼저 확정한 뒤 진행한다.

## PostgreSQL 전환 전에 확인해야 할 항목

PostgreSQL 전환 전 확인할 항목은 다음과 같다.

1. DB 타입 이름을 `postgresql`로 확정했다.
2. `pom.xml`에 PostgreSQL JDBC 드라이버를 추가했다. MySQL 드라이버 제거 여부는 샘플 정리 단계에서 결정한다.
3. `application.properties`에 PostgreSQL 접속 정보를 추가했다.
4. Docker PostgreSQL 접속 정보와 application 설정을 일치시켰다.
5. MyBatis mapper 로딩 경로에 `healthcenter` 하위를 포함했다.
6. 기존 샘플 mapper를 유지할지, PostgreSQL 전환 전에 제거할지 순서를 결정한다.
7. DB 초기화 방식을 결정한다. 후보는 수동 SQL, Spring SQL init, Flyway이다.
8. HSQL용 `shtdb.sql`을 계속 둘지, PostgreSQL 전환 후 제거할지 결정한다.
9. JWT secret, crypto key, DB 비밀번호를 환경변수 또는 프로필별 비공개 설정으로 분리한다.
10. Swagger의 Authorization 헤더 형식과 JWT 필터 구현을 통일한다.

## 다음 진행 선택지

다음 작업은 아래 순서 중 하나로 진행할 수 있다.

### 선택지 A: 샘플 정리 먼저

템플릿 실행 기반과 공통 설정만 남기고 게시판, 일정, 회원, SNS 샘플을 정리한다.

장점:
- 보건소 도메인 구현 전 코드베이스가 가벼워진다.
- 신규 API와 샘플 API가 섞이지 않는다.

주의:
- eGovFrame 공통 설정과 샘플 기능의 의존 관계를 조심스럽게 끊어야 한다.
- GitNexus impact analysis 기준으로 Controller/Service/DAO 단위 영향 확인이 필요하다.

### 선택지 B: PostgreSQL 전환 먼저

샘플 코드는 유지한 채 PostgreSQL 연결과 신규 mapper 경로를 먼저 잡는다.

장점:
- DB 연결과 MyBatis 설정을 먼저 검증할 수 있다.
- 이후 도메인 구현 시 DB 기반이 준비되어 있다.

주의:
- 기존 샘플 mapper는 PostgreSQL용 파일이 없어서 그대로는 작동하지 않을 수 있다.
- 샘플 기능을 유지한 상태에서 `Globals.DbType=postgresql`로 바꾸면 mapper 누락 문제가 발생할 가능성이 높다.

### 선택지 C: 최소 골격 분리 후 PostgreSQL 전환

샘플 API는 바로 삭제하지 않고, 신규 보건소 도메인 패키지와 mapper 경로만 먼저 추가한 뒤 PostgreSQL 전환을 작게 검증한다.

장점:
- 샘플 코드 삭제 리스크를 낮춘다.
- 신규 도메인 구현의 기준 구조를 먼저 확정할 수 있다.

주의:
- 일정 기간 샘플 코드와 신규 코드가 함께 존재한다.

## 권장 방향

현재 상태에서는 선택지 C를 권장한다.

1. eGovFrame 핵심 설정은 유지한다.
2. PostgreSQL 전환을 위해 `healthcenter` mapper 경로와 DB 설정만 작게 추가한다.
3. 아주 작은 헬스체크 또는 공통코드 조회 수준의 보건소 도메인 API로 PostgreSQL 연결을 검증한다.
4. 연결이 확인되면 게시판, 일정, 회원, SNS 샘플을 순서대로 정리한다.

이 방식은 템플릿을 한 번에 크게 비우지 않으면서도, 실제 목표 아키텍처로 이동하기 쉽다.

## 아직 하지 않은 일

- 샘플 코드 삭제
- `Globals.DbType` PostgreSQL 전환
- PostgreSQL용 mapper 작성
- 보건소 API 구현
- 실제 커밋 생성

## 커밋 메시지 초안

```text
docs: record egovframe backend transition status
```
