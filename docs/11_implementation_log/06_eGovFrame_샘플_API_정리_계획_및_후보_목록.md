# eGovFrame 샘플 API 정리 계획 및 후보 목록

## 1. 작업 목표

이번 브랜치의 목표는 전자정부프레임워크 Simple Backend Template에 포함된 샘플 기능 중 보건소 스마트 예약·대기 시스템과 관련 없는 기능을 제거하는 것이다.

단, Spring Security, JWT, 전자정부프레임워크 핵심 설정처럼 보안과 실행 기반에 해당하는 기능은 유지한다. 삭제는 한 번에 전부 진행하지 않고, 이 문서의 순서에 따라 작은 기능 묶음 단위로 진행한다.

## 2. 작업 범위

### 포함

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md`의 `샘플 코드 정리 1차` 항목 확인
- [x] 샘플 정리 관련 문서 확인
- [x] 현재 코드 구조와 샘플 기능 후보 탐색
- [x] 제거 후보와 유지 후보 목록 작성
- [x] 사용자가 확정한 제거/유지 기준 반영
- [x] 실제 삭제 작업 순서 작성

### 제외

- [x] 한 번에 전체 샘플 코드 일괄 삭제 제외
- [x] eGovFrame 핵심 설정 변경 제외
- [x] `egovframework.healthcenter` 신규 도메인 변경 제외
- [x] 실제 커밋, push, 배포 제외

## 3. 현재 브랜치 정보

| 항목 | 내용 |
|---|---|
| 브랜치 | `refactor/egov-sample-api-cleanup` |
| 작업 방향 | 보건소 기능과 무관한 eGovFrame 샘플 기능 제거 |
| 이번 단위 | 제거/유지 기준 확정과 단계별 삭제 계획 수립 |
| 다음 단위 | 확정된 순서에 따라 기능 묶음 1개씩 삭제 |

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
| 삭제 실행 | Selenium 테스트 샘플, JPA/QueryDSL 테스트 샘플, 개인 일정 샘플 제거 완료 |

GitNexus 인덱스 갱신이 실패했으므로 이번 문서는 `rg` 결과와 파일 구조 기준으로 작성했다. 실제 삭제를 수행할 때는 선택한 대상별로 GitNexus 또는 `rg` 기반 영향 확인을 다시 수행한다.

## 5.1 사용자가 확정한 정리 기준

| 항목 | 결정 |
|---|---|
| 브랜치 목적 | 관련 없는 예시 코드를 제거하는 것까지 포함 |
| 소셜 로그인 | 향후 UX에서 소셜 로그인과 간편 인증을 엮을 수 있으므로 유지 |
| 게시판 | 제거 |
| 게시판 이용정보/사용자 정보 | 제거 |
| 개인 일정 | 새로 만들 예정이므로 제거 |
| 회원관리 샘플 | 제거 |
| JPA/QueryDSL | MyBatis 기준으로 갈 것이므로 제거 |
| Selenium | 사용하지 않으므로 제거 |
| DB 샘플 | PostgreSQL을 사용할 것이므로 HSQL/벤더별 샘플 DB 정리 |
| 보안 관련 기능 | Spring Security, JWT, token 방식은 유지 |

## 6. 반드시 유지할 것

아래 항목은 eGovFrame Simple Backend Template 기반 실행과 신규 보건소 도메인 구현에 필요한 기반이므로 이번 정리 대상에서 제외한다.

| 구분 | 유지 대상 | 이유 |
|---|---|---|
| 실행 진입점 | `backend/src/main/java/egovframework/EgovBootApplication.java` | Spring Boot 애플리케이션 시작점 |
| Maven 구조 | `backend/pom.xml` | 전자정부프레임워크 템플릿 기준 빌드 도구 |
| 핵심 설정 | `egovframework.com.config` | DataSource, Mapper, Transaction, Properties, Swagger 등 기반 설정 |
| MyBatis 설정 | `EgovConfigAppMapper`, `egovframework/mapper/config/mapper-config.xml` | MyBatis mapper 로딩 기반 |
| Security 골격 | `egovframework.com.security`, `egovframework.com.jwt` | Auth/Member 구현 전 재사용 가능 |
| 기존 token 로그인 골격 | `egovframework.let.uat.uia` 일부 | Spring Security와 token 방식 유지 예정이므로 Auth 전환 전 보류 |
| 관리자/보안 확인 샘플 | `egovframework.let.uat.esm` 일부 | 관리자 비밀번호 변경, JWT 권한 흐름과 연결되어 있어 보안 구조 정리 전 보류 |
| Swagger | `OpenApiConfig`, springdoc 의존성 | API 확인과 포트폴리오 시연에 필요 |
| 신규 도메인 | `egovframework.healthcenter` | 보건소 MVP 신규 코드 |
| 신규 Mapper | `egovframework/mapper/healthcenter` | PostgreSQL 공통코드 API mapper |
| PostgreSQL SQL | `db/postgresql/schema.sql`, `db/postgresql/data.sql` | 현재 검증된 공통코드 DB 기반 |
| 공통 파일 기능 | `egovframework.com.cmm.service`, `egovframework.com.cmm.web` 일부 | 게시판/일정과 연결되어 있지만 향후 첨부 기능 재사용 가능성 있음 |
| 비밀번호 암호화 | `egovframework.let.utl.sim.service.EgovFileScrty` | 기존 로그인/관리자 샘플과 연결, Auth 전환 전 영향 확인 필요 |
| SNS 로그인 샘플 | `egovframework.com.sns` | 향후 소셜 로그인/간편 인증 UX 확장 가능성이 있어 유지 |

## 7. 제거 후보 목록

### 7.1 유지: SNS 로그인 샘플

| 항목 | 내용 |
|---|---|
| 후보 경로 | `backend/src/main/java/egovframework/com/sns` |
| 대표 파일 | `SnsLoginApiController.java`, `SnsUtils.java`, `SnsVO.java` |
| 유지 이유 | 향후 UX에서 소셜 로그인과 간편 인증을 연계할 수 있음 |
| 예상 위험 | 기존 로그인 샘플 `EgovLoginService`를 호출하므로 Auth 구현 전 참조 확인 필요 |
| 추천 판단 | 지금 삭제하지 않고 Auth/Member 설계 때 재검토 |
| 확인 명령 | `rg -n "egovframework.com.sns|SnsLoginApiController|SnsUtils|SnsVO" backend/src` |

보류 작업명:

```text
SNS 로그인 샘플 유지 후 Auth 단계에서 재검토
```

### 7.2 1순위 제거: 게시판 샘플

| 항목 | 내용 |
|---|---|
| 후보 경로 | `backend/src/main/java/egovframework/let/cop/bbs` |
| 관련 Mapper | `backend/src/main/resources/egovframework/mapper/let/cop/bbs` |
| 관련 Validator | `backend/src/main/resources/egovframework/validator/let/cop/bbs` |
| 관련 테스트 | `backend/src/test/java/egovframework/let/cop/bbs` |
| 제거 이유 | 보건소 예약, 대기, 혼잡도 MVP와 무관 |
| 예상 위험 | 메인 API, 게시판 이용정보, 파일 공통 기능과 연결됨 |
| 추천 판단 | 제거 대상 |
| 삭제 전 확인 | `EgovMainApiController`, `EgovBBSUseInfoManageApiController`, 파일 첨부 참조 |

선택 가능 작업명:

```text
게시판 샘플 제거
```

### 7.3 2순위 제거: 게시판 이용정보/사용자 정보 샘플

| 항목 | 내용 |
|---|---|
| 후보 경로 | `backend/src/main/java/egovframework/let/cop/com` |
| 관련 Mapper | `backend/src/main/resources/egovframework/mapper/let/cop/com` |
| 관련 Validator | `backend/src/main/resources/egovframework/validator/let/cop/com` |
| 제거 이유 | 게시판 사용 권한, 커뮤니티/동호회 사용자 샘플 성격 |
| 예상 위험 | 게시판 샘플과 강하게 연결되어 단독 삭제 시 빌드 오류 가능 |
| 추천 판단 | 게시판 샘플과 함께 제거 또는 직후 제거 |

선택 가능 작업명:

```text
게시판 이용정보 샘플 제거
```

### 7.4 3순위 제거: 개인 일정 샘플

| 항목 | 내용 |
|---|---|
| 후보 경로 | `backend/src/main/java/egovframework/let/cop/smt/sim` |
| 관련 Mapper | `backend/src/main/resources/egovframework/mapper/let/cop/smt/sim` |
| 관련 Validator | `backend/src/main/resources/egovframework/validator/let/cop/smt/sim` |
| 제거 이유 | 보건소 예약 슬롯 모델과 다른 일정관리 샘플 |
| 예상 위험 | 파일 첨부 서비스와 연결되어 있음 |
| 추천 판단 | 새 예약/일정 모델을 만들 예정이므로 제거 |
| 처리 상태 | 제거 완료 |

선택 가능 작업명:

```text
개인 일정 샘플 제거
```

### 7.5 4순위 제거: 회원관리 샘플

| 항목 | 내용 |
|---|---|
| 후보 경로 | `backend/src/main/java/egovframework/let/uss/umt` |
| 관련 Mapper | `backend/src/main/resources/egovframework/mapper/let/uss/umt` |
| 제거 이유 | 보건소 Member/Auth 모델과 다름 |
| 예상 위험 | 기존 로그인/관리자 샘플과 인증 흐름이 연결될 수 있음 |
| 추천 판단 | 보건소 Member/Auth 모델과 다르므로 제거하되, 보안/token 골격과 분리 확인 필요 |

선택 가능 작업명:

```text
회원관리 샘플 제거
```

### 7.6 5순위 제거: JPA/QueryDSL 테스트 샘플

| 항목 | 내용 |
|---|---|
| 후보 경로 | `backend/src/test/java/egovframework/study/jpa` |
| 관련 의존성 | `spring-boot-starter-data-jpa`, `querydsl-jpa`, `querydsl-apt` |
| 관련 Maven 설정 | QueryDSL annotation processor |
| 제거 이유 | MVP 신규 도메인은 MyBatis 기준 |
| 예상 위험 | 의존성과 annotation processor를 함께 정리해야 Maven 설정 영향 발생 |
| 추천 판단 | MyBatis 기준과 맞지 않으므로 제거 |
| 처리 상태 | 제거 완료 |

선택 가능 작업명:

```text
JPA QueryDSL 테스트 샘플 제거
```

### 7.7 6순위 제거: Selenium 테스트 샘플

| 항목 | 내용 |
|---|---|
| 후보 파일 | `backend/src/test/java/egovframework/let/uat/uia/web/TestEgovLoginApiControllerSelenium.java` |
| 관련 의존성 | `selenium-java` |
| 제거 이유 | 백엔드 REST API 검증 우선순위가 더 높음 |
| 예상 위험 | 테스트 의존성 제거 시 다른 테스트 import 여부 확인 필요 |
| 추천 판단 | 사용하지 않으므로 제거 |
| 처리 상태 | 제거 완료 |

선택 가능 작업명:

```text
Selenium 테스트 샘플 제거
```

### 7.8 7순위 제거: HSQL/벤더별 샘플 DB 자원

| 항목 | 내용 |
|---|---|
| 후보 파일 | `backend/src/main/resources/db/shtdb.sql`, `egovframework/mapper/let/**/*_hsql.xml` 등 |
| 제거 이유 | 현재 실행 DB는 PostgreSQL |
| 예상 위험 | eGovFrame 템플릿 회귀 확인이나 HSQL 프로필 실행에 필요할 수 있음 |
| 추천 판단 | PostgreSQL 기준으로 정리하되, eGovFrame DB 타입 설정과 Maven 의존성 영향 확인 필요 |

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
| SNS 로그인 샘플 `egovframework.com.sns` | 유지 | 향후 소셜 로그인/간편 인증 UX 확장 후보 |
| 로그인 샘플 `egovframework.let.uat.uia` | 보안 관련 보류 | token 방식과 Spring Security 유지 예정이므로 Auth/Member 전환 전 삭제하지 않음 |
| 관리자 샘플 `egovframework.let.uat.esm` | 보안 관련 보류 | 관리자 권한, 비밀번호 변경, JWT 권한 흐름 확인용으로 남김 |
| `mysql-connector-j` | 보류 | 현재 PostgreSQL 기준에서는 제거 후보지만 Maven/CVE 주석과 샘플 DB 정리와 함께 판단 |
| `hsqldb` | 보류 | HSQL 실행 회귀 확인을 버릴지 결정 필요 |
| `tomcat-embed-jasper` | 보류 | REST API 서버 기준 제거 후보지만 중복 선언 정리와 함께 판단 |

## 9. 제거 진행 순서

사용자가 제거 기준을 확정했으므로 아래 순서로 하나씩 제거한다.

| 순서 | 작업 | 작업 크기 | 위험 | 비고 |
|---:|---|---|---|---|
| 1 | Selenium 테스트 샘플 제거 | 작음 | 낮음 | 완료 |
| 2 | JPA QueryDSL 테스트 샘플 제거 | 중간 | 중간 | 완료 |
| 3 | 개인 일정 샘플 제거 | 중간 | 중간 | 완료 |
| 4 | 게시판 샘플 제거 | 큼 | 높음 | 게시판 이용정보, 메인 API, 파일 기능 참조 확인 |
| 5 | 게시판 이용정보/사용자 정보 샘플 제거 | 큼 | 높음 | 게시판 제거와 묶어서 처리 가능 |
| 6 | 회원관리 샘플 제거 | 큼 | 높음 | 보안/token 골격과 분리 확인 |
| 7 | HSQL 및 벤더별 샘플 DB 자원 정리 | 중간 | 중간 | PostgreSQL 설정 안정화 후 진행 |

첫 실제 삭제 작업은 영향이 가장 작은 `Selenium 테스트 샘플 제거`를 추천한다.

## 10. 실제 삭제 요청을 받으면 진행할 절차

1. 진행 순서의 다음 기능 묶음에 대해 Controller/Service/DAO/Mapper/Test 참조를 `rg`로 확인한다.
2. 가능하면 GitNexus impact/context를 다시 시도한다.
3. 삭제 대상 파일과 유지 대상 파일을 작업 전 보고한다.
4. 기능 묶음 하나만 삭제한다.
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
- [x] 사용자 제거/유지 기준 반영
- [x] Selenium 테스트 샘플 제거
- [x] JPA QueryDSL 테스트 샘플 제거
- [x] 개인 일정 샘플 제거
- [ ] 게시판/게시판 이용정보 샘플 제거
- [ ] 회원관리 샘플 제거
- [ ] HSQL 및 벤더별 샘플 DB 자원 정리

### 검증 체크리스트

- [x] 문서 작성 완료
- [x] `rg` 기반 코드 구조 확인
- [x] Selenium 참조 제거 후 `rg` 확인
- [x] JPA/QueryDSL 참조 제거 후 `rg` 확인
- [x] 개인 일정 샘플 참조 제거 후 `rg` 확인
- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [x] `GET /api/common-codes/RESERVATION_STATUS`

Selenium 테스트 샘플, JPA/QueryDSL 테스트 샘플, 개인 일정 샘플 제거 후 Maven compile과 test-compile을 확인했다. 공통코드 API도 정상 응답을 확인했다.

## 12. 다음 작업 추천

1. 다음 삭제 작업은 `게시판/게시판 이용정보 샘플 제거`로 진행한다.
2. 이후 회원관리, HSQL/벤더별 DB 자원을 순서대로 정리한다.
3. 각 삭제 단계마다 Maven compile과 공통코드 API를 확인한다.

## 13. 커밋 메시지 초안

```text
docs: eGovFrame 샘플 API 제거 기준 확정

- 샘플 API 제거 브랜치 목적을 실제 제거까지로 확정
- 소셜 로그인과 보안/token 관련 기능은 유지 대상으로 분류
- Selenium 테스트 샘플과 의존성 제거
- JPA/QueryDSL 테스트 샘플과 의존성 제거
- 개인 일정 샘플과 Security 인증 예외 경로 제거
- 게시판, 회원, DB 샘플 제거 순서 정리
```
