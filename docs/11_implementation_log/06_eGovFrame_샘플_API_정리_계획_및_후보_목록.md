# eGovFrame 샘플 API 정리 계획 및 후보 목록

## 1. 작업 목표

이번 브랜치의 목표는 전자정부프레임워크 Simple Backend Template에 포함된 샘플 기능을 바로 삭제하지 않고, 먼저 제거 후보와 유지 후보를 분류하는 것이다.

사용자가 이 문서에서 제거 대상을 직접 선택하면, 다음 요청에서 선택한 기능 묶음 하나만 삭제한다.

## 2. 작업 범위

### 포함

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md`의 `샘플 코드 정리 1차` 항목 확인
- [x] 샘플 정리 관련 문서 확인
- [x] 현재 코드 구조와 샘플 기능 후보 탐색
- [x] 제거 후보와 유지 후보 목록 작성
- [x] 다음 삭제 작업의 작은 단위 추천

### 제외

- [x] 실제 Java 코드 삭제 제외
- [x] 실제 Mapper XML 삭제 제외
- [x] 실제 의존성 제거 제외
- [x] eGovFrame 핵심 설정 변경 제외
- [x] `egovframework.healthcenter` 신규 도메인 변경 제외
- [x] 실제 커밋, push, 배포 제외

## 3. 현재 브랜치 정보

| 항목 | 내용 |
|---|---|
| 브랜치 | `refactor/egov-sample-api-cleanup` |
| 작업 방향 | 사용하지 않는 eGovFrame 샘플 기능 정리 준비 |
| 이번 단위 | 삭제 전 후보 목록과 안전 절차 문서화 |
| 다음 단위 | 사용자가 고른 기능 묶음 1개 삭제 |

## 4. 확인한 기준 문서

| 문서 | 확인 내용 |
|---|---|
| `docs/README.md` | 백엔드 기준, 문서 읽는 순서, 샘플 제거 전 제안 원칙 |
| `docs/09_agent/05_문서기반_자동진행_운영가이드.md` | 한 번에 하나의 작은 단위, 샘플 대량 삭제 전 확인 필요 |
| `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` | `type/topic-kebab-case` 브랜치 규칙, 구현 기록과 PR 문서 작성 기준 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | `샘플 코드 정리 1차`가 다음 관련 작업임을 확인 |
| `docs/10_backend_transition/03_샘플_코드_정리_범위_및_안전_절차.md` | SNS, 게시판, 일정, 회원, JPA/QueryDSL, Selenium 순서 확인 |
| `docs/10_backend_transition/01_eGovFrame_백엔드_템플릿_전환_현황.md` | 샘플 기능 목록과 유지해야 할 기반 확인 |
| `docs/10_backend_transition/04_DB_접근_방식_JPA_MyBatis_판단.md` | MVP 신규 도메인은 MyBatis 유지, JPA/QueryDSL은 샘플 정리 후보 |

## 5. 현재 코드 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/egov-sample-api-cleanup` |
| GitNexus 상태 | stale |
| GitNexus analyze | `Not inside a git repository`로 실패 |
| 대체 탐색 | `rg`, 파일 구조 확인 사용 |
| 삭제 실행 | 하지 않음 |

GitNexus 인덱스 갱신이 실패했으므로 이번 문서는 `rg` 결과와 파일 구조 기준으로 작성했다. 실제 삭제 요청을 받을 때는 선택한 대상별로 GitNexus 또는 `rg` 기반 영향 확인을 다시 수행한다.

## 6. 반드시 유지할 것

아래 항목은 eGovFrame Simple Backend Template 기반 실행과 신규 보건소 도메인 구현에 필요한 기반이므로 이번 정리 대상에서 제외한다.

| 구분 | 유지 대상 | 이유 |
|---|---|---|
| 실행 진입점 | `backend/src/main/java/egovframework/EgovBootApplication.java` | Spring Boot 애플리케이션 시작점 |
| Maven 구조 | `backend/pom.xml` | 전자정부프레임워크 템플릿 기준 빌드 도구 |
| 핵심 설정 | `egovframework.com.config` | DataSource, Mapper, Transaction, Properties, Swagger 등 기반 설정 |
| MyBatis 설정 | `EgovConfigAppMapper`, `egovframework/mapper/config/mapper-config.xml` | MyBatis mapper 로딩 기반 |
| Security 골격 | `egovframework.com.security`, `egovframework.com.jwt` | Auth/Member 구현 전 재사용 가능 |
| Swagger | `OpenApiConfig`, springdoc 의존성 | API 확인과 포트폴리오 시연에 필요 |
| 신규 도메인 | `egovframework.healthcenter` | 보건소 MVP 신규 코드 |
| 신규 Mapper | `egovframework/mapper/healthcenter` | PostgreSQL 공통코드 API mapper |
| PostgreSQL SQL | `db/postgresql/schema.sql`, `db/postgresql/data.sql` | 현재 검증된 공통코드 DB 기반 |
| 공통 파일 기능 | `egovframework.com.cmm.service`, `egovframework.com.cmm.web` 일부 | 게시판/일정과 연결되어 있지만 향후 첨부 기능 재사용 가능성 있음 |
| 비밀번호 암호화 | `egovframework.let.utl.sim.service.EgovFileScrty` | 기존 로그인/관리자 샘플과 연결, Auth 전환 전 영향 확인 필요 |

## 7. 제거 후보 목록

### 7.1 1순위: SNS 로그인 샘플

| 항목 | 내용 |
|---|---|
| 후보 경로 | `backend/src/main/java/egovframework/com/sns` |
| 대표 파일 | `SnsLoginApiController.java`, `SnsUtils.java`, `SnsVO.java` |
| 제거 이유 | MVP에서 실제 Kakao/Naver 로그인 연동은 제외 |
| 예상 위험 | 기존 로그인 샘플 `EgovLoginService`를 호출하므로 Auth 구현 전 참조 확인 필요 |
| 추천 판단 | 가장 먼저 삭제 후보로 적합 |
| 삭제 전 확인 | `rg -n "egovframework.com.sns|SnsLoginApiController|SnsUtils|SnsVO" backend/src` |

선택 가능 작업명:

```text
SNS 로그인 샘플 제거
```

### 7.2 2순위: 게시판 샘플

| 항목 | 내용 |
|---|---|
| 후보 경로 | `backend/src/main/java/egovframework/let/cop/bbs` |
| 관련 Mapper | `backend/src/main/resources/egovframework/mapper/let/cop/bbs` |
| 관련 Validator | `backend/src/main/resources/egovframework/validator/let/cop/bbs` |
| 관련 테스트 | `backend/src/test/java/egovframework/let/cop/bbs` |
| 제거 이유 | 보건소 예약, 대기, 혼잡도 MVP와 무관 |
| 예상 위험 | 메인 API, 게시판 이용정보, 파일 공통 기능과 연결됨 |
| 추천 판단 | SNS 제거 후 별도 브랜치에서 진행 |
| 삭제 전 확인 | `EgovMainApiController`, `EgovBBSUseInfoManageApiController`, 파일 첨부 참조 |

선택 가능 작업명:

```text
게시판 샘플 제거
```

### 7.3 3순위: 게시판 이용정보/사용자 정보 샘플

| 항목 | 내용 |
|---|---|
| 후보 경로 | `backend/src/main/java/egovframework/let/cop/com` |
| 관련 Mapper | `backend/src/main/resources/egovframework/mapper/let/cop/com` |
| 관련 Validator | `backend/src/main/resources/egovframework/validator/let/cop/com` |
| 제거 이유 | 게시판 사용 권한, 커뮤니티/동호회 사용자 샘플 성격 |
| 예상 위험 | 게시판 샘플과 강하게 연결되어 단독 삭제 시 빌드 오류 가능 |
| 추천 판단 | 게시판 샘플과 같은 묶음 또는 직후 단계에서 정리 |

선택 가능 작업명:

```text
게시판 이용정보 샘플 제거
```

### 7.4 4순위: 개인 일정 샘플

| 항목 | 내용 |
|---|---|
| 후보 경로 | `backend/src/main/java/egovframework/let/cop/smt/sim` |
| 관련 Mapper | `backend/src/main/resources/egovframework/mapper/let/cop/smt/sim` |
| 관련 Validator | `backend/src/main/resources/egovframework/validator/let/cop/smt/sim` |
| 제거 이유 | 보건소 예약 슬롯 모델과 다른 일정관리 샘플 |
| 예상 위험 | 파일 첨부 서비스와 연결되어 있음 |
| 추천 판단 | 게시판보다 작지만 파일 기능 참조 확인 필요 |

선택 가능 작업명:

```text
개인 일정 샘플 제거
```

### 7.5 5순위: 회원관리 샘플

| 항목 | 내용 |
|---|---|
| 후보 경로 | `backend/src/main/java/egovframework/let/uss/umt` |
| 관련 Mapper | `backend/src/main/resources/egovframework/mapper/let/uss/umt` |
| 제거 이유 | 보건소 Member/Auth 모델과 다름 |
| 예상 위험 | 기존 로그인/관리자 샘플과 인증 흐름이 연결될 수 있음 |
| 추천 판단 | Auth/Member Context 구현 계획이 확정된 뒤 삭제 |

선택 가능 작업명:

```text
회원관리 샘플 제거
```

### 7.6 6순위: JPA/QueryDSL 테스트 샘플

| 항목 | 내용 |
|---|---|
| 후보 경로 | `backend/src/test/java/egovframework/study/jpa` |
| 관련 의존성 | `spring-boot-starter-data-jpa`, `querydsl-jpa`, `querydsl-apt` |
| 관련 Maven 설정 | QueryDSL annotation processor |
| 제거 이유 | MVP 신규 도메인은 MyBatis 기준 |
| 예상 위험 | 의존성과 annotation processor를 함께 정리해야 Maven 설정 영향 발생 |
| 추천 판단 | 테스트 샘플 삭제와 의존성 정리를 같은 작은 단위로 검토 |

선택 가능 작업명:

```text
JPA QueryDSL 테스트 샘플 제거
```

### 7.7 7순위: Selenium 테스트 샘플

| 항목 | 내용 |
|---|---|
| 후보 파일 | `backend/src/test/java/egovframework/let/uat/uia/web/TestEgovLoginApiControllerSelenium.java` |
| 관련 의존성 | `selenium-java` |
| 제거 이유 | 백엔드 REST API 검증 우선순위가 더 높음 |
| 예상 위험 | 테스트 의존성 제거 시 다른 테스트 import 여부 확인 필요 |
| 추천 판단 | JPA/QueryDSL 테스트 정리와 별도 작은 단위로 가능 |

선택 가능 작업명:

```text
Selenium 테스트 샘플 제거
```

### 7.8 8순위: HSQL/벤더별 샘플 DB 자원

| 항목 | 내용 |
|---|---|
| 후보 파일 | `backend/src/main/resources/db/shtdb.sql`, `egovframework/mapper/let/**/*_hsql.xml` 등 |
| 제거 이유 | 현재 실행 DB는 PostgreSQL |
| 예상 위험 | eGovFrame 템플릿 회귀 확인이나 HSQL 프로필 실행에 필요할 수 있음 |
| 추천 판단 | PostgreSQL 기반 신규 도메인이 더 안정화된 뒤 진행 |

선택 가능 작업명:

```text
HSQL 및 벤더별 샘플 DB 자원 정리
```

## 8. 보류 또는 신중 검토 대상

| 대상 | 판단 | 이유 |
|---|---|---|
| `egovframework.com.cmm` 전체 | 보류 | 파일, 메시지, 공통 유틸이 여러 샘플과 설정에 걸쳐 사용됨 |
| 파일 업로드/다운로드 API | 보류 | 게시판/일정 샘플과 연결되어 있지만 향후 민원 첨부 기능에 재사용 가능 |
| 이미지 처리 Controller | 보류 | 파일 기능과 함께 판단 필요 |
| 로그인 샘플 `egovframework.let.uat.uia` | 보류 | Auth/Member 신규 구현 전까지 JWT 골격 검증에 필요할 수 있음 |
| 관리자 샘플 `egovframework.let.uat.esm` | 보류 | Security/JWT 권한 흐름 확인용으로 남길 수 있음 |
| `mysql-connector-j` | 보류 | 현재 PostgreSQL 기준에서는 제거 후보지만 Maven/CVE 주석과 샘플 DB 정리와 함께 판단 |
| `hsqldb` | 보류 | HSQL 실행 회귀 확인을 버릴지 결정 필요 |
| `tomcat-embed-jasper` | 보류 | REST API 서버 기준 제거 후보지만 중복 선언 정리와 함께 판단 |

## 9. 사용자 선택지

다음 요청에서 아래 중 하나를 골라 주면 그 기능 묶음만 정리한다.

| 추천 순서 | 선택 문구 | 작업 크기 | 위험 |
|---:|---|---|---|
| 1 | SNS 로그인 샘플 제거 | 작음 | 낮음 |
| 2 | Selenium 테스트 샘플 제거 | 작음 | 낮음 |
| 3 | JPA QueryDSL 테스트 샘플 제거 | 중간 | 중간 |
| 4 | 개인 일정 샘플 제거 | 중간 | 중간 |
| 5 | 게시판 샘플 제거 | 큼 | 높음 |
| 6 | 회원관리 샘플 제거 | 큼 | 높음 |

현재 기준으로는 `SNS 로그인 샘플 제거`를 첫 실제 삭제 작업으로 추천한다.

## 10. 실제 삭제 요청을 받으면 진행할 절차

1. 선택된 기능 묶음의 Controller/Service/DAO/Mapper/Test 참조를 `rg`로 확인한다.
2. 가능하면 GitNexus impact/context를 다시 시도한다.
3. 삭제 대상 파일과 유지 대상 파일을 작업 전 보고한다.
4. 사용자가 고른 기능 묶음 하나만 삭제한다.
5. `mvn -q -DskipTests compile`을 실행한다.
6. 가능하면 백엔드 실행과 공통코드 API를 확인한다.
7. 브랜치 기록과 전체 체크리스트를 갱신한다.
8. PR 작성안을 갱신한다.

## 11. 이번 브랜치 체크리스트

### 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 확인
- [x] 샘플 정리 관련 문서 확인
- [x] 현재 브랜치와 작업 트리 확인

### 구현 체크리스트

- [x] 삭제 후보 목록 작성
- [x] 유지 후보 목록 작성
- [x] 보류 후보 목록 작성
- [x] 다음 삭제 작업 선택지 작성
- [x] 실제 코드 삭제는 하지 않음

### 검증 체크리스트

- [x] 문서 작성 완료
- [x] `rg` 기반 코드 구조 확인
- [ ] 빌드 확인
- [ ] API 수동 호출 확인

이번 브랜치에서는 코드 삭제가 없으므로 빌드와 API 수동 호출은 필수 검증으로 보지 않는다. 실제 삭제 작업에서 다시 확인한다.

## 12. 다음 작업 추천

1. 사용자가 이 문서에서 삭제 대상을 선택한다.
2. 첫 삭제 작업은 `SNS 로그인 샘플 제거`로 진행한다.
3. 삭제 전 `SnsLoginApiController`, `SnsUtils`, `SnsVO` 영향 범위를 다시 확인한다.
4. 삭제 후 Maven compile과 공통코드 API를 확인한다.

## 13. 커밋 메시지 초안

```text
docs: eGovFrame 샘플 API 정리 후보 문서화

- 샘플 API 제거 전 유지 대상과 삭제 후보 분류
- SNS, 게시판, 일정, 회원, JPA, Selenium 정리 우선순위 작성
- 실제 삭제 전 안전 절차와 사용자 선택지 정리
```
