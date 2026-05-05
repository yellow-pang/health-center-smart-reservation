# PostgreSQL 공통코드 API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/postgres-common-code-api` |
| 원격 브랜치 | `origin/feat/postgres-common-code-api` |
| 작업 트리 | clean |
| base 브랜치 | `origin/main` |
| 브랜치 최신 커밋 | `0ad9788 docs: PostgreSQL 공통코드 API 변경 기록 추가` |
| 빌드 확인 | `mvn -q -DskipTests compile` 성공 |

현재 브랜치는 원격에 push되어 있고 작업 트리가 깨끗하므로 Pull Request를 열어도 된다.  
PR이 merge된 뒤에는 이 브랜치를 닫아도 된다.

단, Docker Desktop 엔진이 실행되지 않아 PostgreSQL 컨테이너 기반 런타임 검증은 아직 완료하지 못했다. 이 내용은 PR 본문에 명시한다.

## PR 제목

```text
feat: PostgreSQL 공통코드 조회 API 구현
```

## PR 본문

```markdown
## 개요

eGovFrame Simple Backend Template 기반 백엔드를 PostgreSQL 기준으로 전환하고, 신규 보건소 도메인의 첫 API로 공통코드 조회 API를 추가했습니다.

이번 PR은 샘플 기능 전체를 삭제하지 않고, PostgreSQL 연결과 MyBatis 기반 신규 도메인 구조를 검증할 수 있는 최소 기능을 먼저 구현하는 것을 목표로 합니다.

## 변경 내용

- `Globals.DbType`을 `postgresql`로 전환
- PostgreSQL JDBC 드라이버 의존성 추가
- PostgreSQL 접속 설정 추가
- Spring SQL init 기반 공통코드 schema/data SQL 추가
- `egovframework.healthcenter.common` 하위 공통 응답 구조 추가
- 공통코드 조회 Controller/Service/Mapper/DTO/VO 추가
- PostgreSQL용 MyBatis mapper XML 추가
- 공통코드 조회 API 인증 예외 경로 추가
- README와 전환/구현 기록 문서 갱신

## 추가된 API

```text
GET /api/common-codes/{groupCode}
GET /api/common-codes?groupCodes=RESERVATION_STATUS,QUEUE_STATUS
```

예시:

```text
GET /api/common-codes/RESERVATION_STATUS
```

## 주요 파일

- `backend/src/main/resources/application.properties`
- `backend/src/main/resources/db/postgresql/schema.sql`
- `backend/src/main/resources/db/postgresql/data.sql`
- `backend/src/main/java/egovframework/healthcenter/common/response/ApiResponse.java`
- `backend/src/main/java/egovframework/healthcenter/common/code/api/CommonCodeController.java`
- `backend/src/main/java/egovframework/healthcenter/common/code/application/CommonCodeQueryService.java`
- `backend/src/main/java/egovframework/healthcenter/common/code/mapper/CommonCodeMapper.java`
- `backend/src/main/resources/egovframework/mapper/healthcenter/common/CommonCode_SQL_postgresql.xml`
- `docs/10_backend_transition/*`
- `docs/11_implementation_log/*`

## 검증

- [x] `mvn -q -DskipTests compile`
- [ ] Docker PostgreSQL 컨테이너 실행 검증
- [ ] `mvn spring-boot:run` 런타임 검증
- [ ] Swagger에서 공통코드 API 호출 검증

## 미검증 사유

현재 작업 환경에서 Docker Desktop 엔진이 실행되지 않아 아래 명령이 실패했습니다.

```bash
docker compose up -d postgres
```

따라서 PostgreSQL 컨테이너 기반 런타임 검증은 Docker Desktop 실행 후 별도로 진행해야 합니다.

## 주의사항

- `Globals.DbType=postgresql`로 전환했기 때문에 기존 eGovFrame 샘플 API는 일부 또는 전체가 동작하지 않을 수 있습니다.
- 기존 샘플에는 `*_postgresql.xml` mapper가 없으므로 샘플 API 정상 동작은 이번 PR의 목표가 아닙니다.
- 이번 PR의 검증 대상은 신규 보건소 공통코드 조회 API입니다.
- DB 비밀번호와 JWT secret 등은 아직 개발 설정 파일에 있으므로 운영 전 환경변수 또는 별도 secret 관리로 분리해야 합니다.
- SQL init은 초기 개발 편의를 위한 방식이며, 이후 Flyway 전환을 검토합니다.

## 다음 작업

- Docker Desktop 실행 후 PostgreSQL 컨테이너 기동 확인
- 백엔드 런타임 실행 및 공통코드 API 호출 확인
- 샘플 API 실패 범위 확인
- 게시판, 일정, 회원, SNS 샘플 제거 범위 확정
- 보건소 기본 정보 또는 예약 가능 슬롯 API 구현 착수
```

## Merge 후 브랜치 정리 기준

PR이 `main`에 merge되고 아래 조건이 확인되면 브랜치를 삭제해도 된다.

1. PR merge 완료
2. `main`에서 변경 내용 확인
3. 필요한 경우 `main` 기준 빌드 확인
4. 후속 작업 브랜치가 별도로 생성됨

삭제 대상:

```text
feat/postgres-common-code-api
```

## 후속 브랜치 이름 추천

```text
feat/verify-postgres-common-code-api
feat/remove-egov-sample-code
feat/office-service-type-api
```
