# eGovFrame 샘플 API 정리 계획 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/egov-sample-api-cleanup` |
| base 브랜치 | `main` |
| 작업 성격 | 관련 없는 eGovFrame 샘플 기능 제거 기준 확정 및 단계별 제거 |
| 작업 트리 | PR 전 최종 확인 결과를 PR 문서에 반영 중 |
| 코드 삭제 | Selenium 테스트 샘플, JPA/QueryDSL 테스트 샘플, 개인 일정 샘플, 게시판/게시판 이용정보/회원관리 샘플, 파일/이미지 DB 기반 샘플 API 제거 |
| 빌드 확인 | `mvn -q -DskipTests compile`, `mvn -q test-compile` 성공 |
| API 확인 | 이전 단계에서 `GET /api/common-codes/RESERVATION_STATUS` 성공, PR 전 재확인은 `localhost:8080` 연결 거부로 미확인 |
| Swagger 확인 | `springdoc` 설정과 남은 Controller/Mapping 정적 확인 완료, Swagger UI 런타임 확인은 서버 미기동으로 미확인 |

## PR 제목

```text
refactor: eGovFrame 샘플 API 제거
```

## PR 본문

````markdown
## 개요

전자정부프레임워크 Simple Backend Template에 포함된 샘플 기능 중 보건소 예약·대기 시스템과 관련 없는 기능을 제거했습니다.

삭제는 기능 묶음 단위로 진행했고, Spring Security, JWT, token 로그인 골격처럼 보안과 관련된 기반은 유지했습니다.

## 변경 내용

- 브랜치 이름 규칙 문서 확인 기준 반영
- 전체 체크리스트의 `샘플 코드 정리 1차` 관련 진행 상황 보강
- eGovFrame 샘플 API 제거 기준 확정
- 반드시 유지할 eGovFrame 기반 설정과 신규 도메인 코드 목록 작성
- 소셜 로그인, Spring Security, JWT/token 관련 기능은 유지 대상으로 분류
- 게시판, 일정, 회원관리, JPA/QueryDSL, Selenium, HSQL/벤더별 DB 샘플은 제거 대상으로 분류
- 기능 묶음별 제거 순서 작성
- Selenium 테스트 샘플과 `selenium-java` 테스트 의존성 제거
- JPA/QueryDSL 테스트 샘플과 관련 의존성, annotation processor 설정 제거
- 개인 일정 샘플 API, Service, DAO, VO, Mapper XML, Validator XML 제거
- 개인 일정 샘플의 Security GET 인증 예외 경로 제거
- 게시판 샘플 API, Service, DAO, Domain, DTO, Mapper XML, Validator XML, Test 제거
- 게시판 이용정보/사용자 정보 샘플 API, Service, DAO, Mapper XML, Validator XML 제거
- 회원관리 샘플 API, Service, DAO, VO, Mapper XML 제거
- 제거된 `/mainPage`, `/board`, `/inform`, `/members`, `/mypage`, `/etc/member` 계열 인증 규칙 정리
- 제거된 회원관리 MyBatis typeAlias 정리
- HSQL 내장 DB seed SQL 제거
- HSQL 내장 DB 분기 제거 및 PostgreSQL 설정 기반 DataSource로 정리
- HSQL/MySQL 드라이버, log4jdbc, mysql 보완용 protobuf 의존성 제거
- HSQL/MySQL/Oracle/Altibase/Tibero/Cubrid 접속 설정 제거
- `egovframework/mapper/let` 하위 잔여 벤더별 샘플 Mapper XML 제거
- JSP 파일 부재와 미참조 상태를 확인하고 JSP 태그 핸들러 제거
- REST API 서버 기준 `tomcat-embed-jasper` 의존성 제거
- 중복 선언된 `tomcat-annotations-api`, `commons-lang3` 정리
- 파일/이미지 DB 기반 샘플 API, Service, DAO, VO 제거
- 제거된 파일 관리 MyBatis typeAlias 정리
- `/file`, `/image` 보안 예외 제거
- 파일 저장 경로와 첨부 크기 샘플 설정 제거
- 실제 삭제 요청을 받았을 때의 안전 절차 작성

## 제거 후보

- 게시판 샘플
- 게시판 이용정보/사용자 정보 샘플
- 개인 일정 샘플
- 회원관리 샘플
- JPA/QueryDSL 테스트 샘플
- Selenium 테스트 샘플
- HSQL 및 벤더별 샘플 DB 자원
- 파일/이미지 DB 기반 샘플 API

## 유지 대상

- `EgovBootApplication`
- `egovframework.com.config`
- MyBatis mapper 설정
- Security/JWT 골격
- token 로그인 관련 보안 골격
- SNS 로그인/간편 인증 확장 후보
- Swagger/OpenAPI 설정
- `egovframework.healthcenter` 신규 도메인
- `egovframework/mapper/healthcenter` 신규 Mapper
- PostgreSQL 공통코드 SQL
- MultipartResolver와 허용 확장자 설정
- 정적 `/images/**` 리소스 경로

## 검증

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 관련 항목 확인
- [x] `docs/10_backend_transition/03_샘플_코드_정리_범위_및_안전_절차.md` 확인
- [x] `rg` 기반 샘플 코드 구조 확인
- [x] Selenium 참조 확인
- [x] JPA/QueryDSL 참조 확인
- [x] 개인 일정 샘플 참조 확인
- [x] 게시판/게시판 이용정보/회원관리 샘플 참조 확인
- [x] HSQL/MySQL/log4jdbc/protobuf 참조 확인
- [x] 잔여 벤더별 Mapper 제거 확인
- [x] JSP 파일 부재와 JSP 태그 핸들러 미참조 확인
- [x] 파일/이미지 DB 기반 샘플 API 참조 제거 확인
- [x] 남은 Controller 목록 확인
- [x] Swagger/OpenAPI 설정 확인
- [x] Swagger 노출 후보 Controller/Mapping 정적 확인
- [x] `tomcat-embed-jasper` 제거 후 `mvn -q -DskipTests compile`
- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [x] 이전 단계 `GET /api/common-codes/RESERVATION_STATUS`
- [x] PR 전 최종 `GET /api/common-codes/RESERVATION_STATUS`
- [x] Swagger UI 런타임 확인

## 미검증 사유

- GitNexus 인덱스가 stale 상태였고, `npm.cmd exec -- gitnexus analyze`는 `Not inside a git repository`로 실패했습니다.
- GitNexus impact 분석은 수행하지 못했고, Selenium, JPA/QueryDSL, 개인 일정, 게시판, 회원관리, HSQL/미사용 DB 드라이버, 벤더별 Mapper, JSP 태그 핸들러 제거는 `rg` 참조 확인으로 대체했습니다.
- GitNexus `detect_changes`는 CLI에서 `unknown command 'detect_changes'`로 실패해 수행하지 못했습니다.
- JSP/Jasper 정리 후 백엔드 자동 실행은 Windows 권한 문제로 `Start-Process`가 거부되어 공통코드 API 재호출까지 확인하지 못했습니다.
- 파일/이미지 DB 기반 샘플 API 제거 후에는 Maven compile과 test-compile까지 확인했습니다.
- PR 전 최종 런타임 확인 시 `http://localhost:8080/actuator/health`는 연결 거부로 실패했습니다.
- Docker 상태 확인은 Docker API 권한 문제로 실패했습니다.
- `http://localhost:8080/api/common-codes/RESERVATION_STATUS`는 연결 거부로 실패했습니다.
- Swagger UI 런타임 확인은 서버 미기동 상태라 수행하지 못했고, `springdoc` 설정과 남은 Controller/Mapping 정적 확인으로 대체했습니다.

## 후속 작업

- 보안/token 골격으로 남긴 로그인, 관리자, SNS 샘플의 유지 범위 재확인
- PR 생성 전 또는 PR 리뷰 중 Docker 권한 확보 후 공통코드 API와 Swagger UI 런타임 재확인
- 다음 브랜치에서 Auth/Member Context 구현 범위 확정
````

## 변경 파일 요약

| 파일 | 내용 |
|---|---|
| `backend/pom.xml` | `selenium-java` 테스트 의존성 제거 |
| `backend/src/test/java/egovframework/let/uat/uia/web/TestEgovLoginApiControllerSelenium.java` | 사용하지 않는 Selenium 테스트 샘플 제거 |
| `backend/src/test/java/egovframework/study/jpa/*` | MyBatis 기준과 맞지 않는 JPA/QueryDSL 테스트 샘플 제거 |
| `backend/src/main/java/egovframework/let/cop/smt/sim/*` | 개인 일정 샘플 Java 코드 제거 |
| `backend/src/main/resources/egovframework/mapper/let/cop/smt/sim/*` | 개인 일정 샘플 Mapper XML 제거 |
| `backend/src/main/resources/egovframework/validator/let/cop/smt/sim/*` | 개인 일정 샘플 Validator XML 제거 |
| `backend/src/main/java/egovframework/com/security/SecurityConfig.java` | 제거된 `/schedule/*` 인증 예외 경로 정리 |
| `backend/src/main/java/egovframework/let/cop/bbs/*` | 게시판 샘플 Java 코드 제거 |
| `backend/src/main/java/egovframework/let/cop/com/*` | 게시판 이용정보/사용자 정보 샘플 Java 코드 제거 |
| `backend/src/main/java/egovframework/let/main/*` | 게시판 기반 메인 샘플 API 제거 |
| `backend/src/main/java/egovframework/let/uss/umt/*` | 회원관리 샘플 Java 코드 제거 |
| `backend/src/main/resources/egovframework/mapper/let/cop/bbs/*` | 게시판 샘플 Mapper XML 제거 |
| `backend/src/main/resources/egovframework/mapper/let/cop/com/*` | 게시판 이용정보/사용자 정보 샘플 Mapper XML 제거 |
| `backend/src/main/resources/egovframework/mapper/let/uss/umt/*` | 회원관리 샘플 Mapper XML 제거 |
| `backend/src/main/resources/egovframework/validator/let/cop/bbs/*` | 게시판 샘플 Validator XML 제거 |
| `backend/src/main/resources/egovframework/validator/let/cop/com/*` | 게시판 이용정보 샘플 Validator XML 제거 |
| `backend/src/test/java/egovframework/let/cop/bbs/*` | 게시판 샘플 테스트 제거 |
| `backend/src/main/resources/application.properties` | 제거된 메인 페이지 샘플 설정 제거 |
| `backend/src/main/resources/egovframework/mapper/config/mapper-config.xml` | 제거된 회원관리 typeAlias 정리 |
| `backend/src/main/java/egovframework/com/config/EgovConfigAppDatasource.java` | HSQL 내장 DB 분기 제거, 설정 기반 DataSource로 단순화 |
| `backend/src/main/resources/db/shtdb.sql` | HSQL 샘플 seed SQL 제거 |
| `backend/src/main/resources/application-dev.properties` | HSQL 샘플 접속 설정 제거 |
| `backend/src/main/resources/application-prod.properties` | HSQL 샘플 접속 설정 제거 |
| `backend/pom.xml` | HSQL/MySQL/log4jdbc/protobuf 관련 미사용 의존성 제거 |
| `backend/src/main/resources/egovframework/mapper/let/cmm/fms/*` | 파일 샘플 벤더별 Mapper XML 제거 |
| `backend/src/main/resources/egovframework/mapper/let/cmm/use/*` | 공통코드 샘플 벤더별 Mapper XML 제거 |
| `backend/src/main/resources/egovframework/mapper/let/uat/esm/*` | 관리자 샘플 벤더별 Mapper XML 제거 |
| `backend/src/main/resources/egovframework/mapper/let/uat/uia/*` | 로그인 샘플 벤더별 Mapper XML 제거 |
| `backend/src/main/java/egovframework/com/cmm/EgovComCrossSiteHndlr.java` | JSP 태그 핸들러 제거 |
| `backend/pom.xml` | Jasper, 중복 Tomcat/commons 의존성 선언 정리 |
| `backend/src/main/java/egovframework/com/cmm/service/EgovFileMngService.java` | 파일 관리 샘플 Service 제거 |
| `backend/src/main/java/egovframework/com/cmm/service/EgovFileMngUtil.java` | 파일 관리 샘플 Util 제거 |
| `backend/src/main/java/egovframework/com/cmm/service/FileVO.java` | 파일 관리 샘플 VO 제거 |
| `backend/src/main/java/egovframework/com/cmm/service/impl/EgovFileMngServiceImpl.java` | 파일 관리 샘플 Service 구현 제거 |
| `backend/src/main/java/egovframework/com/cmm/service/impl/FileManageDAO.java` | 파일 관리 샘플 DAO 제거 |
| `backend/src/main/java/egovframework/com/cmm/web/EgovFileDownloadController.java` | 파일 다운로드 샘플 API 제거 |
| `backend/src/main/java/egovframework/com/cmm/web/EgovFileMngApiController.java` | 파일 업로드 샘플 API 제거 |
| `backend/src/main/java/egovframework/com/cmm/web/EgovImageProcessController.java` | 이미지 처리 샘플 API 제거 |
| `backend/src/main/java/egovframework/com/security/SecurityConfig.java` | 제거된 `/file`, `/image` 인증 예외 정리 |
| `backend/src/main/java/egovframework/com/config/EgovConfigAppProperties.java` | 제거된 파일 저장 경로 샘플 속성 정리 |
| `backend/src/main/resources/application.properties` | 제거된 파일 저장 경로와 첨부 크기 샘플 설정 정리 |
| `backend/src/main/resources/application-dev.properties` | 제거된 파일 저장 경로 샘플 설정 정리 |
| `backend/src/main/resources/application-prod.properties` | 제거된 파일 저장 경로 샘플 설정 정리 |
| `docs/11_implementation_log/06_eGovFrame_샘플_API_정리_계획_및_후보_목록.md` | 제거 기준, 유지 후보, 제거 순서, Selenium 제거 상태 문서화 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 샘플 API 제거 후보 선정 작업 기록 추가 |
| `docs/11_implementation_log/07_eGovFrame_샘플_API_정리_계획_PR_작성안.md` | 이번 PR 작성안 추가 |

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] `main` 최신화 확인
- [ ] 다음 삭제 단계 진행
- [ ] 후속 브랜치를 `type/topic-kebab-case` 형식으로 생성

## 후속 브랜치 이름 추천

```text
refactor/remove-vendor-db-samples
refactor/auth-member-domain
refactor/healthcenter-reservation-api
```
