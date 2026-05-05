# PostgreSQL 전환 준비 완료 및 다음 작업

## 목적

이 문서는 eGovFrame Simple Backend Template에서 선택지 C 방향으로 진행한 1차 전환 결과를 정리한다.

여기서 말하는 전환 완료는 전체 DB 실행 환경을 PostgreSQL로 완전히 바꿨다는 의미가 아니다. 현재 완료된 범위는 샘플 코드를 유지한 상태에서 PostgreSQL 전환에 필요한 기본 설정과 신규 보건소 도메인 작업 위치를 준비한 것이다.

## 완료된 범위

| 구분 | 완료 내용 |
|---|---|
| DB 드라이버 | `pom.xml`에 PostgreSQL JDBC 드라이버 추가 |
| DB 설정 | `application.properties`에 `Globals.postgresql.*` 접속 설정 추가 |
| 기본 실행 DB | `Globals.DbType=postgresql` 전환 |
| Mapper 경로 | 기존 `let` mapper 경로 유지 + `healthcenter` mapper 경로 추가 |
| 신규 Java 패키지 | `egovframework.healthcenter` 자리 생성 |
| 신규 Mapper 디렉터리 | `egovframework/mapper/healthcenter` 자리 생성 |
| DB 초기화 | 공통코드용 PostgreSQL schema/data SQL 추가 |
| 최소 API | 공통코드 조회 API 추가 |
| 문서 | `backend/README.md`, 10번 전환 문서에 `postgresql` 기준 반영 |

## 현재 백엔드 상태

현재 백엔드는 PostgreSQL 기준으로 실행된다.

```properties
Globals.DbType=postgresql
```

따라서 백엔드 실행 전 PostgreSQL 컨테이너가 먼저 실행되어 있어야 한다.

PostgreSQL 접속 설정은 이미 추가되어 있다.

```properties
Globals.postgresql.DriverClassName=org.postgresql.Driver
Globals.postgresql.Url=jdbc:postgresql://127.0.0.1:5432/health_center
Globals.postgresql.UserName=health
Globals.postgresql.Password=health1234
```

기존 샘플 기능의 `*_postgresql.xml` mapper는 없으므로 PostgreSQL 전환 후 샘플 API 일부 또는 전체는 깨질 수 있다. 현재 검증 대상은 신규 보건소 공통코드 API이다.

## 현재 MyBatis Mapper 로딩 구조

현재 MyBatis는 다음 두 경로를 함께 확인한다.

```text
classpath:/egovframework/mapper/let/**/*_${Globals.DbType}.xml
classpath:/egovframework/mapper/healthcenter/**/*_${Globals.DbType}.xml
```

이 구조의 의미는 다음과 같다.

- PostgreSQL 실행 시 신규 보건소 mapper는 `healthcenter/**/*_postgresql.xml`에서 로딩된다.
- 기존 샘플에는 PostgreSQL mapper가 없으므로 샘플 API 정상 동작은 현재 목표가 아니다.
- 신규 보건소 mapper 파일명은 `*_postgresql.xml`로 맞춘다.

## 현재 생성된 신규 작업 위치

신규 보건소 도메인 Java 코드는 아래 하위에 작성한다.

```text
backend/src/main/java/egovframework/healthcenter
```

신규 보건소 MyBatis mapper XML은 아래 하위에 작성한다.

```text
backend/src/main/resources/egovframework/mapper/healthcenter
```

패키지 구조는 기존 설계 문서의 Bounded Context 기준을 따른다.

```text
egovframework.healthcenter
 ├─ reservation
 ├─ waiting
 ├─ patient
 ├─ staff
 ├─ common
 └─ dashboard
```

실제 구현 시에는 한 번에 모든 패키지를 만들지 않고, 첫 기능에 필요한 최소 패키지만 생성한다.

## 아직 남은 작업

### 1. PostgreSQL 컨테이너 실행 확인

루트 경로에서 PostgreSQL 컨테이너를 실행한다.

```bash
docker compose up -d postgres
```

확인할 항목은 다음과 같다.

- 컨테이너 이름: `health-center-postgres`
- DB: `health_center`
- User: `health`
- Password: `health1234`
- Port: `5432`

### 2. DB 초기화 방식

현재는 Spring SQL init 방식으로 공통코드 최소 테이블과 seed 데이터를 생성한다.

추가된 파일:

```text
backend/src/main/resources/db/postgresql/schema.sql
backend/src/main/resources/db/postgresql/data.sql
```

후속 후보는 다음과 같다.

| 방식 | 설명 | 판단 |
|---|---|---|
| 수동 SQL | SQL 파일을 직접 실행 | 초기 검증에는 단순함 |
| Spring SQL init | `schema.sql`, `data.sql` 사용 | Spring Boot 기본 흐름과 맞음 |
| Flyway | 마이그레이션 버전 관리 | 장기적으로 가장 안정적 |

권장 방향은 현재 SQL init으로 연결과 API를 안정화한 뒤, 배포 재현성이 필요해지는 시점에 Flyway로 전환하는 것이다.

### 3. PostgreSQL용 최소 테이블 작성

공통코드용 최소 테이블 작성은 완료했다.

생성 대상:

```text
common_code_groups
common_codes
```

다음 테이블 후보는 보건소 기본 정보와 예약 가능 시간대이다.

후속 우선순위:

1. 보건소 기본 정보
2. 예약 가능 시간대
3. 예약 생성
4. 대기 상태 조회

공통코드는 이미 첫 PostgreSQL 검증 API 대상으로 구현했다.

### 4. PostgreSQL용 Mapper 작성

신규 mapper 파일은 다음 규칙을 따른다.

```text
backend/src/main/resources/egovframework/mapper/healthcenter/{context}/{Name}_SQL_postgresql.xml
```

예시:

```text
backend/src/main/resources/egovframework/mapper/healthcenter/common/CommonCode_SQL_postgresql.xml
```

Mapper namespace는 Java mapper 또는 DAO 이름과 일치시킨다.

### 5. PostgreSQL 전환 검증용 최소 API 작성

공통코드 조회 API 작성은 완료했다.

추가된 API:

```text
GET /api/common-codes/{groupCode}
GET /api/common-codes
```

예시:

```text
GET /api/common-codes/RESERVATION_STATUS
GET /api/common-codes?groupCodes=RESERVATION_STATUS,QUEUE_STATUS
```

### 6. `Globals.DbType=postgresql` 전환

전환은 완료했다.

전환 시점에 해야 할 일:

전환 후 기준:

1. 샘플 API 정상 동작은 현재 목표로 삼지 않는다.
2. 신규 보건소 API 기준으로 빌드와 실행을 확인한다.
3. 샘플 기능은 제거 범위를 확정한 뒤 정리한다.

권장 방향은 PostgreSQL 전환 시점에 샘플 API 정상 동작을 목표로 삼지 않는 것이다. 샘플 기능은 HSQL 실행 확인용으로 이미 역할을 다했기 때문이다.

### 7. 샘플 코드 정리

PostgreSQL 기반 첫 보건소 API가 동작하면 샘플 기능을 정리한다.

정리 후보:

- 게시판 샘플
- 일정 샘플
- 회원가입/회원관리 샘플
- SNS 로그인 샘플
- JPA/QueryDSL 테스트 샘플
- Selenium 테스트 샘플
- HSQL 샘플 DB 스크립트

삭제 전에는 Controller, Service, DAO, Mapper 단위로 영향 범위를 확인하고 진행한다.

### 8. 보건소 도메인 구현

샘플 정리와 병행하거나 직후에 보건소 도메인을 작게 구현한다.

권장 구현 순서:

1. 공통 응답 형식
2. 공통코드 조회
3. 보건소/진료부서 기본 정보
4. 예약 가능 슬롯 조회
5. 예약 생성
6. 예약 취소
7. 대기 접수
8. 대기 호출
9. 처리 완료
10. 대시보드 지표 조회

## 권장 다음 진행 순서

다음 작업은 아래 순서로 진행하는 것이 좋다.

1. Docker Desktop 실행 후 PostgreSQL 컨테이너 기동 확인
2. 백엔드 실행 및 공통코드 조회 API 확인
3. 샘플 API 동작 실패 범위를 확인하고 정리 계획 확정
4. 샘플 코드 제거
5. 보건소 기본 정보 또는 예약 가능 슬롯 API 구현
6. 예약·대기 핵심 도메인 구현 시작

## 다음 작업 추천

바로 다음 단계는 Docker Desktop을 실행한 뒤 PostgreSQL 컨테이너와 공통코드 API를 실제 실행 검증하는 것이다.

추천 작업명:

```text
PostgreSQL 공통코드 API 실행 검증 및 샘플 정리 착수
```

이 작업에서 포함할 범위:

- PostgreSQL 컨테이너 실행 확인
- 백엔드 실행 확인
- SQL init으로 공통코드 seed 데이터 생성 확인
- Swagger에서 공통코드 조회 API 확인
- 샘플 API 실패 범위 확인
- 샘플 코드 제거 계획 확정

이 작업에서 제외할 범위:

- 예약 도메인 전체 구현
- 대시보드 지표 구현
- pgvector 사용
- 운영용 보안 설정 완성

## 리스크와 주의사항

| 항목 | 주의사항 |
|---|---|
| 샘플 mapper | 기존 샘플에는 PostgreSQL mapper가 없으므로 `Globals.DbType=postgresql` 전환 시 샘플 API는 깨질 수 있음 |
| JWT | Swagger 문서의 Bearer 형식과 실제 필터 구현이 아직 다름 |
| Secret | JWT secret, crypto key, DB 비밀번호가 properties에 직접 있음 |
| GitNexus | 현재 인덱스가 stale 상태일 수 있으므로 코드 삭제 전 별도 확인 필요 |
| DB 초기화 | 현재는 SQL init을 사용하며, 안정화 후 Flyway 전환 검토 |
| Docker | 현재 작업 환경에서는 Docker Desktop 엔진이 실행되지 않아 런타임 검증은 보류됨 |

## 완료 기준

다음 단계의 완료 기준은 아래와 같다.

- PostgreSQL 컨테이너가 실행된다.
- `Globals.DbType=postgresql` 기준으로 애플리케이션이 실행된다.
- 공통코드 조회 API가 PostgreSQL에서 데이터를 읽는다.
- Swagger에서 공통코드 조회 API를 확인할 수 있다.
- 샘플 기능 유지 여부와 삭제 순서가 확정된다.

## 커밋 메시지

제목:

```text
feat: PostgreSQL 공통코드 조회 API 구현
```

내용:

```text
- Globals.DbType을 postgresql로 전환
- 공통코드 테이블 schema/data SQL 추가
- healthcenter 공통 응답 및 공통코드 조회 API 추가
- PostgreSQL용 MyBatis mapper 추가
- 공통코드 조회 API 인증 예외 경로 추가
- README와 전환 문서 갱신
```
