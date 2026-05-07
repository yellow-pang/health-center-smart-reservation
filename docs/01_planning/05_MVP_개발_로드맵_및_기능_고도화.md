# 개발 방향성 전체 정리

## 문서 목적

이 문서는 보건소 스마트 예약·대기 및 혼잡도 분석 시스템을 앞으로 어떤 방향과 순서로 개발할지 정리한다.

현재 프로젝트는 eGovFrame Simple Backend Template 기반 백엔드에 PostgreSQL 설정과 공통코드 조회 API가 일부 반영된 상태이다. 이 문서는 앞으로의 개발 로드맵과 우선순위를 확정하는 데 목적이 있다.

## 최종 목표

MVP의 최종 목표는 단일 보건소 기준으로 다음 흐름이 동작하는 것이다.

```text
로그인
→ 업무 유형 조회
→ 예약 가능 시간 조회
→ 예약 신청
→ 방문 당일 체크인 또는 현장 접수
→ 대기번호 발급
→ 직원 대기 호출
→ 처리 시작
→ 처리 완료
→ 관리자 대시보드와 사용자 혼잡도에 반영
```

MVP에서는 의료정보, 검사 결과, 처방 정보, 실제 알림 연동, pgvector 기반 AI 기능은 구현하지 않는다.

## 개발 대원칙

| 구분 | 원칙 |
|---|---|
| 백엔드 기반 | eGovFrame Simple Backend Template 유지 |
| 빌드 도구 | Maven 유지, Gradle 혼용 금지 |
| DB 접근 | MyBatis 기본 사용, MVP에서 JPA 사용 금지 |
| DB | PostgreSQL 18 + pgvector Docker 이미지 기준 |
| AI 기능 | MVP 제외, 향후 확장으로만 고려 |
| 패키지 | 신규 기능은 `egovframework.healthcenter` 하위에 작성 |
| Mapper XML | `src/main/resources/egovframework/mapper/healthcenter` 하위에 작성 |
| 응답 형식 | `success + data + error` 구조 유지 |
| 상태값 | 비즈니스 상태 전이는 Java Enum, 표시명은 DB 공통코드 |
| 구현 단위 | 한 번에 전체 구현하지 않고 Context 단위로 작게 진행 |
| 샘플 코드 | 제거 대상 목록과 영향 범위 확인 후 정리 |

## 현재 프로젝트 기준

현재까지 완료 또는 확인된 흐름은 아래와 같다.

| 영역 | 현재 상태 |
|---|---|
| 프로젝트 설계 문서 | 기획, 요구사항, Bounded Context, ERD, API 명세 작성됨 |
| 백엔드 템플릿 | eGovFrame Simple Backend Template 기반 |
| DB 전환 준비 | `Globals.DbType=postgresql` 설정됨 |
| 공통코드 | PostgreSQL schema/data, 조회 API, mapper 존재 |
| 신규 패키지 | `egovframework.healthcenter.common` 일부 존재 |
| 샘플 코드 | 게시판, 일정, 회원, SNS 등 템플릿 샘플 다수 남음 |

따라서 다음 개발은 “기존 공통코드 검증 → 샘플 정리 → Office Context 구현” 순서가 자연스럽다.

## 전체 개발 단계

### 1단계. 개발 환경 기준 확인

목표:

- 백엔드 빌드와 PostgreSQL 런타임 검증이 가능한 기준 환경을 확인한다.

필요 항목:

| 항목 | 필요 이유 |
|---|---|
| JDK 17 | Spring Boot/eGovFrame 백엔드 실행 |
| Maven | 백엔드 빌드와 실행 |
| Docker Desktop | PostgreSQL 18 + pgvector 컨테이너 실행 |
| GitNexus | 코드 영향도 확인 |
| rg | 빠른 코드 탐색 |

완료 기준:

- `java -version`으로 Java 17 확인
- `mvn -v` 확인
- `docker --version` 확인
- `docker compose up -d postgres` 실행 가능
- `npm.cmd exec -- gitnexus status` 실행 가능

### 2단계. 현재 백엔드 상태 검증

목표:

- 이미 추가된 PostgreSQL 공통코드 API가 실제로 동작하는지 확인한다.

검증 대상:

```text
GET /api/common-codes/{groupCode}
GET /api/common-codes?groupCodes=RESERVATION_STATUS,QUEUE_STATUS
```

확인할 내용:

- PostgreSQL 컨테이너 기동 여부
- `schema.sql`, `data.sql` 반복 실행 가능 여부
- 공통코드 seed 데이터 생성 여부
- Swagger에서 공통코드 API 노출 여부
- API 응답이 `success + data + error` 형식인지 여부

완료 기준:

- `mvn -q -DskipTests compile` 성공
- 백엔드 실행 성공
- 공통코드 API가 PostgreSQL 데이터를 정상 조회
- 기존 샘플 API 실패 여부는 별도로 기록

### 3단계. 샘플 코드 정리

목표:

- eGovFrame 샘플 기능과 신규 보건소 도메인 코드가 섞이지 않도록 정리한다.

우선 제거 후보:

| 우선순위 | 대상 | 이유 |
|---:|---|---|
| 1 | SNS 로그인 샘플 | MVP 범위 밖 |
| 2 | 게시판 샘플 | 예약·대기 시스템 핵심 도메인과 무관 |
| 3 | 개인 일정 샘플 | 예약 슬롯 모델과 충돌 가능 |
| 4 | 회원가입/회원관리 샘플 | 보건소 Member 모델과 다름 |
| 5 | JPA/QueryDSL 테스트 샘플 | MVP 기준은 MyBatis |
| 6 | Selenium 테스트 샘플 | REST API 검증 우선 |
| 7 | HSQL 샘플 DB | PostgreSQL 전환 후 정리 후보 |

유지 후보:

- eGovFrame 핵심 설정
- datasource 설정
- MyBatis mapper 설정
- transaction 설정
- Swagger/OpenAPI 설정
- Security/JWT 골격
- 신규 `egovframework.healthcenter` 코드

완료 기준:

- 제거 대상과 유지 대상을 문서로 확정
- Controller, Service, DAO, Mapper, Test 영향 범위 확인
- 샘플 제거 후 백엔드 빌드 성공
- Swagger에 불필요한 샘플 API 노출 감소

## MVP 구현 로드맵

### 4단계. Common Context 정리

목표:

- 신규 도메인 전체에서 사용할 공통 응답, 예외, 공통코드 기반을 정리한다.

구현 또는 정리 대상:

- `ApiResponse`
- `ApiError`
- `ErrorCode`
- `CustomException`
- `GlobalExceptionHandler`
- 공통코드 조회 API
- 공통코드 캐시 적용 검토

주의:

- 공통코드는 화면 표시명과 코드 목록 제공 용도로 사용한다.
- 예약 상태와 대기 상태의 비즈니스 전이는 Java Enum과 Policy에서 처리한다.

완료 기준:

- 신규 API 오류 응답이 일관된 형식으로 내려간다.
- 공통코드 조회 API가 안정적으로 동작한다.
- 공통코드 수정 API는 MVP 후순위로 둔다.

### 5단계. Office Context 구현

목표:

- 예약과 대기 기능의 기준정보인 보건소, 업무 유형, 창구를 구현한다.

우선 구현 API:

```text
GET /api/service-types
```

후속 API:

```text
POST /api/admin/service-types
PUT /api/admin/service-types/{id}
GET /api/admin/staff
```

필요 테이블:

- `health_centers`
- `service_types`
- `service_windows`
- `staff_service_assignments`

초기 seed 후보:

```text
HEALTH_CENTER: 기본 보건소 1개
VACCINATION: 예방접종
HEALTH_CHECK: 건강검진/검사
HEALTH_CONSULT: 건강상담
```

완료 기준:

- 업무 유형 목록 조회 가능
- 업무 유형은 공통코드가 아니라 `service_types` 기준정보로 관리
- 예약 슬롯 생성 시 업무 유형을 참조할 수 있음

### 6단계. Member/Auth Context 구현

목표:

- 사용자, 직원, 관리자 권한을 구분하고 JWT 기반 인증 흐름을 정리한다.

우선 구현 API:

```text
POST /api/auth/login
POST /api/auth/reissue
POST /api/auth/logout
```

필요 테이블:

- `members`
- `refresh_tokens`

역할:

```text
CITIZEN
GUARDIAN
STAFF
ADMIN
```

주의:

- 기존 eGovFrame 로그인 샘플을 그대로 쓰지 말고 보건소 Member 모델에 맞춘다.
- Swagger 명세의 `Authorization: Bearer {accessToken}` 형식과 실제 JWT 필터 구현을 통일한다.
- JWT secret, DB password 같은 비밀값은 운영 전 환경변수로 분리한다.

완료 기준:

- 로그인 시 Access Token과 Refresh Token 발급
- Role 기반 API 접근 제한 가능
- STAFF/ADMIN만 현장 접수와 대기 호출 API 접근 가능

### 7단계. Reservation Context 구현

목표:

- 사용자가 업무 유형과 시간대를 선택해 예약하고, 예약 내역 조회와 취소를 할 수 있게 한다.

구현 API:

```text
GET /api/reservation-slots
POST /api/reservations
GET /api/reservations/me
GET /api/reservations/{id}
DELETE /api/reservations/{id}
POST /api/admin/reservation-slots
```

필요 테이블:

- `reservation_slots`
- `reservations`

핵심 정책:

| 정책 | 내용 |
|---|---|
| 예약 단위 | 30분 |
| 예약 가능 기간 | 오늘부터 14일 |
| 당일 예약 | 허용 |
| 기본 정원 | 업무별 5명 |
| 취소 가능 시간 | 방문 1시간 전까지 |
| 중복 예약 | 동일 사용자 동일 시간대 제한 |
| 정원 초과 | 예약 불가 |

구현 주의:

- 예약 생성은 하나의 트랜잭션으로 처리한다.
- 정원 증가는 동시성 문제가 있으므로 조건부 update 또는 DB lock을 검토한다.
- 상태 변경은 단순 문자열 update가 아니라 Policy 검증 후 수행한다.

완료 기준:

- 예약 가능 슬롯 조회 가능
- 예약 생성 시 정원과 중복 예약 검증
- 내 예약 조회 가능
- 예약 취소 시 슬롯 잔여 인원 복구

### 8단계. Visit Context 구현

목표:

- 예약 방문자 체크인과 현장 방문자 접수를 처리한다.

구현 API:

```text
POST /api/visits/check-in
POST /api/visits/walk-in
```

필요 테이블:

- `visits`
- `reservations`
- `queue_tickets`

방문 유형:

```text
RESERVED
WALK_IN
STAFF_PROXY
```

핵심 정책:

- 예약자가 방문하면 직원이 체크인한다.
- 예약 시간 10분 초과 지각 시 일반 대기열로 이동 가능하다.
- 현장 접수는 직원 또는 관리자가 등록한다.
- 체크인 또는 현장 접수 후 대기번호를 발급한다.

완료 기준:

- 예약번호로 체크인 가능
- 현장 방문자 접수 가능
- 체크인/접수 후 방문 정보와 대기번호 생성

### 9단계. Queue Context 구현

목표:

- 직원이 대기열을 조회하고 대기자를 호출, 처리 시작, 처리 완료할 수 있게 한다.

구현 API:

```text
GET /api/queues
POST /api/queues/{id}/call
POST /api/queues/{id}/start
POST /api/queues/{id}/complete
POST /api/queues/{id}/hold
```

필요 테이블:

- `queue_tickets`
- `service_processes`
- `service_windows`

대기 상태:

```text
WAITING
CALLED
IN_PROGRESS
HOLD
COMPLETED
CANCELED
NO_SHOW
```

핵심 정책:

- 업무 유형별로 대기번호를 발급한다.
- 예약 시간대 안에서는 예약자를 우선 호출한다.
- 호출 후 3분 미응답 시 HOLD 처리한다.
- HOLD 상태는 재호출 가능하다.
- 처리 완료 시 QueueTicket과 ServiceProcess 상태를 함께 반영한다.

완료 기준:

- 직원이 업무 유형별 대기열 조회 가능
- 상태 전이가 정책에 맞게 제한됨
- 처리 완료 데이터가 대시보드 집계에 활용 가능

### 10단계. Dashboard/Congestion Context 구현

목표:

- 관리자에게 운영 지표를 제공하고, 사용자에게 현재 혼잡도를 제공한다.

구현 API:

```text
GET /api/dashboard/summary
GET /api/dashboard/hourly-visits
GET /api/dashboard/service-wait-times
GET /api/dashboard/visit-type-ratio
GET /api/dashboard/no-show-rate
GET /api/congestion/current
```

초기 구현 방식:

- MVP에서는 실시간 SQL 집계
- 배치 집계 테이블은 2차 기능으로 보류

핵심 지표:

- 오늘 방문자 수
- 현재 대기 인원
- 평균 대기시간
- 노쇼율
- 시간대별 방문자 수
- 업무별 평균 대기시간
- 예약/현장 방문 비율
- 현재 혼잡도

완료 기준:

- 관리자 대시보드 요약 조회 가능
- 업무별 현재 혼잡도 조회 가능
- 대기/방문/처리 데이터가 지표에 반영됨

## 프론트엔드 개발 방향

프론트엔드는 React 기반으로 백엔드 API와 분리해 구현한다.

우선 화면:

| 우선순위 | 화면 | 대상 |
|---:|---|---|
| 1 | 로그인 화면 | 전체 |
| 2 | 업무 유형 선택 화면 | 시민/보호자 |
| 3 | 예약 가능 시간 조회 화면 | 시민/보호자 |
| 4 | 예약 신청/완료 화면 | 시민/보호자 |
| 5 | 내 예약 조회/취소 화면 | 시민/보호자 |
| 6 | 직원 체크인/현장 접수 화면 | STAFF |
| 7 | 직원 대기열/호출 화면 | STAFF |
| 8 | 관리자 대시보드 | ADMIN |
| 9 | 사용자 혼잡도 화면 | PUBLIC |

화면 구현 원칙:

- 고령층도 사용할 수 있도록 큰 버튼과 명확한 문구를 사용한다.
- 상태명과 업무 유형명은 하드코딩하지 않고 API 또는 mock data 구조로 분리한다.
- API 연동 전에는 mock data로 화면 흐름을 먼저 확인한다.
- 공통코드 API를 사용해 예약 상태, 대기 상태, 혼잡도 표시명을 매핑한다.

## 테스트 방향

초기에는 빌드 가능 상태와 Swagger 수동 검증을 우선한다.

기능 안정화 후 테스트를 추가한다.

우선 테스트 후보:

| 영역 | 테스트 |
|---|---|
| 공통코드 | 그룹별 코드 조회, 여러 그룹 일괄 조회 |
| 인증 | 로그인 성공, 비밀번호 오류, 토큰 재발급 |
| 예약 | 예약 성공, 정원 초과, 중복 예약, 취소 시간 초과 |
| 체크인 | 정상 체크인, 예약 없음, 이미 체크인 |
| 현장 접수 | 현장 접수 후 대기번호 발급 |
| 대기 | 호출, 처리 시작, 완료, HOLD, 잘못된 상태 전이 |
| 대시보드 | 방문자 수, 대기 인원, 평균 대기시간 계산 |

## 문서 작성 순서

앞으로 12번 폴더에는 아래 순서로 문서를 추가하는 것이 좋다.

| 순서 | 파일 | 목적 |
|---:|---|---|
| 01 | `01_다음_작업_목록.md` | 다음 방향 정리 |
| 02 | `02_개발_방향성_전체_정리.md` | 전체 개발 로드맵 |
| 03 | `03_샘플_코드_정리_계획.md` | 샘플 제거 대상과 유지 대상 |
| 04 | `04_개발환경_기준_체크리스트.md` | JDK, Maven, Docker 기준 확인 절차 |
| 05 | `05_Office_Context_첫_API_구현_계획.md` | `GET /api/service-types` 구현 계획 |
| 06 | `06_Auth_Member_Context_구현_계획.md` | 로그인과 권한 구현 계획 |
| 07 | `07_Reservation_Context_구현_계획.md` | 예약 기능 구현 계획 |
| 08 | `08_Visit_Queue_Context_구현_계획.md` | 체크인, 현장 접수, 대기 호출 구현 계획 |
| 09 | `09_Dashboard_Context_구현_계획.md` | 대시보드와 혼잡도 구현 계획 |

## 우선순위 결론

가장 먼저 할 일은 코드를 더 쓰는 것이 아니라, 샘플 코드 정리 계획을 확정하는 것이다.

그 다음 순서는 아래와 같다.

1. 개발 환경 기준 확인
2. PostgreSQL 공통코드 API 실행 검증
3. 샘플 코드 정리
4. Office Context의 `GET /api/service-types` 구현
5. Auth/Member Context 구현
6. Reservation Context 구현
7. Visit/Queue Context 구현
8. Dashboard/Congestion Context 구현
9. 프론트엔드 화면 연동
10. 테스트와 README 실행 문서 정리

이 순서가 좋은 이유는 기준정보와 인증을 먼저 안정화한 뒤 예약, 방문, 대기, 대시보드로 이어지는 실제 업무 흐름을 단계적으로 쌓을 수 있기 때문이다.
