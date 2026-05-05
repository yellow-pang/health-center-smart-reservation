# PostgreSQL 전환 준비 완료 및 다음 작업

## 목적

이 문서는 eGovFrame Simple Backend Template에서 선택지 C 방향으로 진행한 1차 전환 결과를 정리한다.

여기서 말하는 전환 완료는 전체 DB 실행 환경을 PostgreSQL로 완전히 바꿨다는 의미가 아니다. 현재 완료된 범위는 샘플 코드를 유지한 상태에서 PostgreSQL 전환에 필요한 기본 설정과 신규 보건소 도메인 작업 위치를 준비한 것이다.

## 완료된 범위

| 구분 | 완료 내용 |
|---|---|
| DB 드라이버 | `pom.xml`에 PostgreSQL JDBC 드라이버 추가 |
| DB 설정 | `application.properties`에 `Globals.postgresql.*` 접속 설정 추가 |
| 기본 실행 DB | `Globals.DbType=hsql` 유지 |
| Mapper 경로 | 기존 `let` mapper 경로 유지 + `healthcenter` mapper 경로 추가 |
| 신규 Java 패키지 | `egovframework.healthcenter` 자리 생성 |
| 신규 Mapper 디렉터리 | `egovframework/mapper/healthcenter` 자리 생성 |
| 문서 | `backend/README.md`, 10번 전환 문서에 `postgresql` 기준 반영 |

## 현재 백엔드 상태

현재 백엔드는 여전히 HSQL 기준으로 실행된다.

```properties
Globals.DbType=hsql
```

따라서 기존 eGovFrame 샘플 기능은 HSQL의 `shtdb.sql`과 `*_hsql.xml` mapper를 기준으로 계속 확인할 수 있다.

PostgreSQL 접속 설정은 이미 추가되어 있다.

```properties
Globals.postgresql.DriverClassName=org.postgresql.Driver
Globals.postgresql.Url=jdbc:postgresql://127.0.0.1:5432/health_center
Globals.postgresql.UserName=health
Globals.postgresql.Password=health1234
```

다만 아직 아래 설정으로 바꾸지는 않았다.

```properties
Globals.DbType=postgresql
```

이 값을 바로 바꾸면 기존 샘플 기능의 `*_postgresql.xml` mapper가 없기 때문에 샘플 API 일부 또는 전체가 깨질 수 있다.

## 현재 MyBatis Mapper 로딩 구조

현재 MyBatis는 다음 두 경로를 함께 확인한다.

```text
classpath:/egovframework/mapper/let/**/*_${Globals.DbType}.xml
classpath:/egovframework/mapper/healthcenter/**/*_${Globals.DbType}.xml
```

이 구조의 의미는 다음과 같다.

- HSQL 실행 시 기존 샘플 mapper는 `let/**/*_hsql.xml`에서 로딩된다.
- 신규 보건소 mapper는 앞으로 `healthcenter/**/*_hsql.xml` 또는 `healthcenter/**/*_postgresql.xml` 형식으로 둘 수 있다.
- PostgreSQL 전환 시에는 보건소 mapper 파일명을 `*_postgresql.xml`로 맞춰야 한다.

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

### 2. DB 초기화 방식 결정

아직 PostgreSQL용 테이블 생성 방식은 확정하지 않았다.

후보는 다음과 같다.

| 방식 | 설명 | 판단 |
|---|---|---|
| 수동 SQL | SQL 파일을 직접 실행 | 초기 검증에는 단순함 |
| Spring SQL init | `schema.sql`, `data.sql` 사용 | Spring Boot 기본 흐름과 맞음 |
| Flyway | 마이그레이션 버전 관리 | 장기적으로 가장 안정적 |

권장 방향은 Flyway이다. 다만 첫 연결 검증은 작은 수동 SQL 또는 단일 schema 파일로 진행해도 된다.

### 3. PostgreSQL용 최소 테이블 작성

전체 ERD를 한 번에 만들기보다, 첫 API 검증에 필요한 최소 테이블만 만든다.

권장 첫 후보는 공통코드 또는 보건소 기본 정보이다.

예시 우선순위:

1. 공통코드
2. 보건소 기본 정보
3. 예약 가능 시간대
4. 예약 생성
5. 대기 상태 조회

공통코드를 먼저 선택하면 도메인 상태값과 대시보드 지표의 기반을 안정적으로 잡을 수 있다.

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

처음부터 예약 전체 흐름을 구현하지 않는다.

권장 첫 API:

```text
GET /api/health
GET /api/common-codes
```

다만 단순 health API는 DB 연결 검증이 약하므로, PostgreSQL 전환 검증 목적이라면 `GET /api/common-codes`처럼 실제 DB 조회를 포함하는 API가 더 좋다.

### 6. `Globals.DbType=postgresql` 전환

PostgreSQL용 최소 테이블과 mapper가 준비된 뒤에 전환한다.

전환 시점에 해야 할 일:

1. `Globals.DbType=postgresql`로 변경한다.
2. HSQL 샘플 API가 깨지는 것을 허용할지 결정한다.
3. 샘플 기능을 계속 유지할 경우 PostgreSQL용 샘플 mapper를 만들어야 한다.
4. 샘플 기능을 제거할 경우 보건소 API 기준으로만 검증한다.

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

1. PostgreSQL 컨테이너 실행 확인
2. DB 초기화 방식 결정
3. 공통코드 또는 보건소 기본 정보용 최소 PostgreSQL 테이블 작성
4. `healthcenter` 하위 Java/Mapper 구조로 최소 조회 API 작성
5. `Globals.DbType=postgresql` 전환 후 최소 API 빌드/실행 확인
6. 샘플 API 동작 실패 범위를 확인하고 정리 계획 확정
7. 샘플 코드 제거
8. 예약·대기 핵심 도메인 구현 시작

## 다음 작업 추천

바로 다음 단계는 PostgreSQL 기반 최소 DB 조회 API를 만드는 것이다.

추천 작업명:

```text
PostgreSQL 연결 검증용 공통코드 조회 API 구현
```

이 작업에서 포함할 범위:

- PostgreSQL 컨테이너 실행 확인
- 공통코드 최소 테이블 생성 SQL 작성
- `egovframework.healthcenter.common` 패키지 생성
- 공통코드 조회 Controller/Service/Mapper 작성
- `CommonCode_SQL_postgresql.xml` 작성
- `Globals.DbType=postgresql` 전환 후 실행 확인

이 작업에서 제외할 범위:

- 게시판/일정/회원 샘플 삭제
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
| GitNexus | 현재 인덱스가 stale이고 재분석 중 오류가 있었으므로 코드 삭제 전 별도 확인 필요 |
| DB 초기화 | Flyway 도입 여부를 빨리 결정해야 이후 SQL 관리가 안정적임 |

## 완료 기준

다음 단계의 완료 기준은 아래와 같다.

- PostgreSQL 컨테이너가 실행된다.
- `Globals.DbType=postgresql` 기준으로 애플리케이션이 실행된다.
- 보건소 도메인 하위의 최소 조회 API가 PostgreSQL에서 데이터를 읽는다.
- Swagger에서 최소 조회 API를 확인할 수 있다.
- 샘플 기능 유지 여부와 삭제 순서가 확정된다.

## 커밋 메시지

제목:

```text
docs: PostgreSQL 전환 준비 완료 및 다음 작업 정리
```

내용:

```text
- PostgreSQL 전환 준비 완료 범위 정리
- 현재 HSQL 유지 상태와 postgresql 설정 상태 설명
- healthcenter Java/mapper 작업 위치 정리
- PostgreSQL 전환 전 남은 작업 목록화
- 다음 단계 권장 순서와 최소 API 구현 범위 정리
```
