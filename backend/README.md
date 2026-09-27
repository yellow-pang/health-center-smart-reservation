# Backend

보건소 스마트 예약·대기 및 혼잡도 분석 시스템의 백엔드 프로젝트입니다.

## CI 검증과 운영 DB 변경

Java 17 / Maven 3.9에서 일반 단위 테스트와 패키징을 실행합니다. 일반 테스트는 DB에 연결하지 않습니다.

```bash
mvn --batch-mode --no-transfer-progress verify
```

DB migration 검증은 **별도로 만든 PostgreSQL 18 일회용 DB**에서 실행합니다. 아래 값은 CI 전용 예시이며 운영 `.env`를 불러오면 안 됩니다. DB 이름은 안전장치로 `_ci`로 끝나야 합니다. 각 테스트는 임의 이름의 schema를 만들고 해당 schema만 삭제합니다.

```bash
TEST_DATABASE_URL=jdbc:postgresql://localhost:5432/health_center_ci \
TEST_DATABASE_USERNAME=health_ci \
TEST_DATABASE_PASSWORD=ci-only \
mvn --batch-mode --no-transfer-progress verify -Pmigration-test
```

이 검증은 빈 DB의 최초 적용/재실행, 기존 schema의 무승인 baseline 거부, 명시적인 baseline 0 채택과 기존 비밀번호·예약 상태·공통코드 보존을 확인합니다.

운영은 `SPRING_PROFILES_ACTIVE=prod`를 명시해야 합니다. `prod`는 `spring.sql.init.mode=never`와 Flyway를 사용하고 `dev`는 기존 `schema.sql` / `data.sql` 개발 데이터를 유지합니다. V1은 기존 schema의 idempotent snapshot이고 V2는 필요한 공통코드만 추가합니다. 기존 공통코드를 덮어쓰거나 테스트 계정·보건소·예약 데이터를 생성하지 않습니다. 빈 운영 DB의 실제 보건소/업무/관리자 계정은 별도 초기 설정이 필요합니다.

기존 DB를 Flyway로 처음 전환할 때:

1. 기존 DB를 백업하고 복원 가능한지 확인한 뒤, 복제한 DB에서 새 애플리케이션의 `prod` 기동을 검증합니다. 기존 테이블 구조는 `db/postgresql/schema.sql`과 호환되어야 합니다. `IF NOT EXISTS`는 임의 schema 차이를 교정하지 않습니다.
2. 대상 DB와 백업을 확인하고 **최초 전환에서만** `FLYWAY_BASELINE_ON_MIGRATE=true`를 명시합니다. 기본값은 `false`이므로 이 승인 없이 Flyway 이력이 없는 기존 DB를 자동 채택하지 않습니다.
3. baseline은 버전 `0`입니다. V1/V2가 실행된 뒤 `/actuator/health`와 기존 데이터를 확인하고 `FLYWAY_BASELINE_ON_MIGRATE=false`로 되돌립니다. 이미 이력이 있으면 재시작 시 migration은 재실행되지 않습니다.
4. 배포된 V1/V2를 수정하지 말고 후속 변경은 V3 이후 파일로 추가합니다. 애플리케이션 이미지를 롤백해도 DB migration은 되돌아가지 않으므로 schema 변경은 이전 버전과 호환되어야 합니다. `flyway clean`은 비활성화되어 있습니다.

운영 프로필은 비밀번호 재설정 토큰을 응답에 노출하지 않습니다. 실제 이메일/SMS 전달 기능을 연결하기 전에는 운영 비밀번호 재설정 기능을 완성된 것으로 취급하지 않습니다.

Docker 이미지는 `/actuator/health`를 확인하는 healthcheck를 포함합니다. 배포 과정은 컨테이너 시작만 확인하지 말고 healthy 상태와 프런트/외부 URL까지 확인해야 합니다.

부모 BOM의 Flyway 11.7.2는 PostgreSQL 17까지만 검증하므로, PostgreSQL 18을 지원하는 11.19.1로 `flyway.version`을 고정하고 core/PostgreSQL 모듈에 같은 버전을 적용합니다.

참고: [Spring Boot DB 초기화](https://docs.spring.io/spring-boot/how-to/data-initialization.html), [Flyway baseline](https://documentation.red-gate.com/flyway/reference/commands/baseline), [Flyway 11.19.1 PostgreSQL 지원 범위](https://github.com/flyway/flyway/blob/flyway-11.19.1/flyway-database/flyway-database-postgresql/src/main/java/org/flywaydb/database/postgresql/PostgreSQLDatabase.java).

현재 `backend` 폴더는 전자정부프레임워크 공식 Simple Backend Template을 기반으로 배치되어 있습니다. 이 템플릿은 Spring Boot 기반 REST API 구조, Maven 빌드, JWT 인증 예시, Swagger/OpenAPI 설정, MyBatis 기반 샘플 기능을 포함합니다.

## 현재 기준

| 항목 | 내용 |
|---|---|
| Template | eGovFrame Simple Backend Template |
| Build Tool | Maven |
| Java | 17 |
| Spring Boot | 3.5.6 |
| Spring Framework | 6.2.11 |
| API Docs | Springdoc OpenAPI / Swagger UI |
| Observability | Spring Boot Actuator / Micrometer |
| 기본 패키지 | `egovframework` |
| 신규 도메인 패키지 | `egovframework.healthcenter` |
| DB 접근 방식 | MyBatis |
| 기본 실행 포트 | 8080 |
| 현재 기본 DB 설정 | PostgreSQL |
| DB 이미지 | PostgreSQL 18 + pgvector Docker 이미지 |

## 현재 Docker 설정과의 관계

루트 `docker-compose.yml`은 PostgreSQL, backend, frontend와 선택적인 관측 서비스를 실행합니다. PostgreSQL 서비스의 주요 설정은 다음과 같습니다.

```yaml
postgresql:
  image: pgvector/pgvector:0.8.2-pg18
  ports:
    - "5432:5432"
  volumes:
    - health-center-postgres-data:/var/lib/postgresql
```

현재 백엔드는 `Globals.DbType=postgresql` 기준으로 PostgreSQL을 사용합니다. PostgreSQL 컨테이너가 실행된 상태에서 백엔드를 실행해야 합니다.

현재 완료된 PostgreSQL 전환 범위:

1. `pom.xml`에 PostgreSQL JDBC 드라이버 추가
2. `application.properties`에 `Globals.DbType=postgresql` 설정
3. `Globals.postgresql.DriverClassName`, `Globals.postgresql.Url`, `Globals.postgresql.UserName`, `Globals.postgresql.Password` 설정
4. 신규 보건소 도메인 Mapper XML 경로 추가
5. 공통코드 조회용 PostgreSQL schema/data SQL 추가
6. 공통코드 조회 API 추가

MVP에서는 pgvector 기능을 사용하지 않고, PostgreSQL 이미지만 확장 가능성을 위해 준비합니다.

## 실행 방법

### 1. PostgreSQL 컨테이너 실행

루트 경로에서 먼저 실행합니다.

```bash
docker compose up -d postgresql
```

현재 PostgreSQL 접속 정보:

| 항목 | 값 |
|---|---|
| Host | localhost |
| Port | 5432 |
| Database | health_center |
| User | health |
| Password | health1234 |

### 2. 백엔드 실행

PostgreSQL 컨테이너가 실행된 뒤 백엔드를 실행합니다.

```bash
cd backend
mvn spring-boot:run
```

실행 후 기본 화면:

```text
http://localhost:8080/
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Actuator:

```text
GET http://localhost:8080/actuator/health
GET http://localhost:8080/actuator/info
GET http://localhost:8080/actuator/metrics
GET http://localhost:8080/actuator/prometheus
```

`/actuator/health`와 `/actuator/info`는 공개 상태 확인용입니다. `/actuator/metrics`, `/actuator/prometheus`, 기타 `/actuator/**` endpoint는 ADMIN 권한 토큰이 필요합니다.

Prometheus가 주기적으로 scrape해야 하는 로컬/내부 관측성 스택에서는 아래 환경변수를 `true`로 설정하면 `/actuator/prometheus`만 토큰 없이 수집할 수 있습니다. 기본값은 `false`입니다.

```text
OBSERVABILITY_PROMETHEUS_SCRAPE_ENABLED=true
```

루트 Compose의 관측성 profile은 Prometheus, Grafana, Loki, Promtail을 함께 실행합니다.

```bash
docker compose --env-file .env --profile observability up -d --build
```

접속 URL:

```text
Prometheus: http://localhost:9090
Grafana:    http://localhost:3001
Loki:       http://localhost:3100
```

공통코드 조회 API:

```text
GET http://localhost:8080/api/common-codes/RESERVATION_STATUS
GET http://localhost:8080/api/common-codes?groupCodes=RESERVATION_STATUS,QUEUE_STATUS
```

## Swagger 인증 확인

보건소 로그인 API는 다음 엔드포인트를 사용합니다.

```text
POST /api/auth/login
```

기본 예시 계정:

```text
admin@test.com / password1234
```

토큰을 받은 뒤 Swagger UI 상단의 `Authorize` 버튼에서 토큰을 설정하면 인증이 필요한 API를 테스트할 수 있습니다.

자세한 내용은 [swagger.md](./swagger.md)를 참고합니다.

## 주요 폴더

```text
backend
 ├─ src/main/java/egovframework
 │   ├─ com
 │   └─ let
 ├─ src/main/resources
 │   ├─ application.properties
 │   ├─ application-dev.properties
 │   ├─ application-prod.properties
 │   ├─ db
 │   └─ egovframework
 ├─ DATABASE
 ├─ Docs
 ├─ pom.xml
 └─ swagger.md
```

## 프로젝트 적용 원칙

- eGovFrame 핵심 설정은 임의로 제거하지 않습니다.
- Maven 구조를 유지하고 Gradle로 변환하지 않습니다.
- 샘플 기능 제거 전에는 제거 대상 목록을 먼저 정리합니다.
- 템플릿은 먼저 HSQL 기준으로 실행 확인한 뒤 PostgreSQL로 전환합니다.
- 신규 보건소 도메인은 `egovframework.healthcenter` 하위 패키지에 작성합니다.
- Mapper XML은 `src/main/resources/egovframework/mapper/healthcenter` 하위에 둡니다.
- MVP에서는 JPA를 사용하지 않고 MyBatis를 기본 DB 접근 방식으로 사용합니다.
- 신규 보건소 API는 `success + data + error` 공통 응답 형식을 사용합니다.
- Request/Response/Command DTO는 `record`를 우선 사용합니다.
- Mapper 조회 결과용 VO는 MyBatis 매핑 편의를 위해 Setter를 제한적으로 허용합니다.
- VO의 Setter를 비즈니스 상태 변경에 사용하지 않습니다.
- 예약 취소, 대기 호출, 처리 완료 같은 상태 변경은 Service와 Policy 클래스로 명확하게 분리합니다.
- 조회 서비스는 `@Transactional(readOnly = true)`, 변경 서비스는 `@Transactional`을 사용합니다.
- 대시보드 통계와 공통코드는 MyBatis SQL Mapper 중심으로 구현합니다.
- 신규 기능은 보건소 예약·대기 도메인 기준으로 작게 나누어 추가합니다.

## 다음 확인 사항

1. Docker Desktop 실행 후 PostgreSQL 컨테이너 기동 확인
2. 백엔드 실행 및 Swagger 대표 흐름 확인
3. `/actuator/health` 공개 상태 확인
4. ADMIN 토큰으로 `/actuator/metrics`, `/actuator/prometheus` 보호 endpoint 확인
5. Prometheus/Grafana/Loki/k6 후속 운영성 고도화 범위 결정
