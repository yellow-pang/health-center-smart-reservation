# Backend

보건소 스마트 예약·대기 및 혼잡도 분석 시스템의 백엔드 프로젝트입니다.

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
| 기본 패키지 | `egovframework` |
| 신규 도메인 패키지 | `egovframework.healthcenter` |
| DB 접근 방식 | MyBatis |
| 기본 실행 포트 | 8080 |
| 현재 기본 DB 설정 | HSQL |
| 목표 DB | PostgreSQL 18 + pgvector Docker 이미지 |

## 현재 Docker 설정과의 관계

루트 `docker-compose.yml`은 PostgreSQL 18 + pgvector 이미지만 실행하도록 구성되어 있습니다.

```yaml
postgres:
  image: pgvector/pgvector:0.8.2-pg18
  ports:
    - "5432:5432"
  volumes:
    - health-center-postgres-data:/var/lib/postgresql
```

현재 상태에서는 PostgreSQL 컨테이너와 백엔드 템플릿이 직접 충돌하지 않습니다. 다만 백엔드 설정은 아직 HSQL 기준이므로, 백엔드를 실행해도 Docker PostgreSQL을 사용하지 않습니다.

PostgreSQL로 전환하려면 다음 작업이 추가로 필요합니다.

1. `pom.xml`에 PostgreSQL JDBC 드라이버 추가
2. `application.properties` 또는 별도 profile에 `Globals.DbType=postgresql` 기준 추가
3. `Globals.postgresql.DriverClassName`, `Globals.postgresql.Url`, `Globals.postgresql.UserName`, `Globals.postgresql.Password` 설정
4. 신규 보건소 도메인 Mapper XML을 `src/main/resources/egovframework/mapper/healthcenter` 하위에 작성
5. DB 초기화 방식 결정: `data.sql` 또는 Flyway
6. pgvector 사용 시점에만 `CREATE EXTENSION IF NOT EXISTS vector;` 실행

MVP에서는 pgvector 기능을 사용하지 않고, PostgreSQL 이미지만 확장 가능성을 위해 준비합니다.

## 실행 방법

### 1. 백엔드 단독 실행

현재 템플릿 기본 설정은 HSQL 기준입니다.

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

### 2. PostgreSQL 컨테이너 실행

루트 경로에서 실행합니다.

```bash
docker compose up -d postgres
```

현재 PostgreSQL 접속 정보:

| 항목 | 값 |
|---|---|
| Host | localhost |
| Port | 5432 |
| Database | health_center |
| User | health |
| Password | health1234 |

## Swagger 인증 확인

템플릿의 JWT 로그인 예시는 다음 엔드포인트를 사용합니다.

```text
POST /auth/login-jwt
```

기본 예시 계정:

```text
admin / 1
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

1. HSQL 기반 템플릿 실행 확인
2. 기존 샘플 기능 중 유지/삭제할 항목 목록화
3. PostgreSQL 전환 설정 추가
4. `egovframework.healthcenter` 하위 신규 도메인 패키지 생성
5. 공통 응답 형식 `success + data + error` 구현
