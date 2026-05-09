# PostgreSQL 공통코드 API 런타임 검증 및 SQL 수정 기록

## 1. 작업 목적

이번 작업은 `main`에서 진행 중이던 PostgreSQL 공통코드 API 변경을 별도 브랜치로 분리하고, Docker PostgreSQL 기반 런타임 검증 결과를 기록하기 위한 작업이다.

기존 문서에는 Docker Desktop 미실행으로 런타임 검증이 보류되어 있었다. Docker Desktop 실행 후 실제로 백엔드를 기동하면서 공통코드 seed SQL 오류를 발견했고, API가 정상 응답하도록 수정했다.

## 2. 브랜치 정보

| 항목 | 내용 |
|---|---|
| 작업 브랜치 | `fix-postgres-common-code-runtime-verification` |
| 시작 브랜치 | `main` |
| 작업 성격 | 런타임 검증, SQL 초기 데이터 오류 수정, 문서 갱신 |

처음 추천한 브랜치명은 `fix/postgres-common-code-runtime-verification`이었지만, 현재 환경에서 Git refs 디렉터리 생성이 실패해 슬래시 없는 이름으로 생성했다.

## 3. 작업 배경

PostgreSQL 공통코드 API는 이미 구현되어 있었지만, 이전 검증 시점에는 Docker Desktop 엔진이 실행되지 않아 아래 항목이 미확인 상태였다.

- PostgreSQL 컨테이너 실행
- 백엔드 런타임 실행
- Swagger UI 접근
- 공통코드 API 실제 호출

Docker Desktop 실행 후 검증을 재시도하면서 백엔드가 PostgreSQL에 연결되고 SQL init을 수행하는 단계까지 진행되는 것을 확인했다.

## 4. 발견한 문제

백엔드 실행 중 `backend/src/main/resources/db/postgresql/data.sql`의 두 번째 INSERT 문에서 오류가 발생했다.

```text
ERROR: column reference "description" is ambiguous
```

원인은 `common_code_groups` 테이블과 seed VALUES 양쪽에 모두 `description` 컬럼이 있는데, INSERT SELECT 구문에서 `description`을 별칭 없이 사용한 것이다.

문제가 된 형태:

```sql
SELECT id, code, code_name, description, sort_order, true, true
FROM common_code_groups
CROSS JOIN (...) AS seed(group_code, code, code_name, description, sort_order)
```

PostgreSQL은 `description`이 `common_code_groups.description`인지 `seed.description`인지 판단할 수 없어 초기 데이터 삽입을 중단했다.

## 5. 수정 내용

`common_codes.description`에는 공통코드 그룹 설명이 아니라 seed에 정의한 개별 코드 설명이 들어가야 한다.

따라서 SELECT 대상 컬럼을 모두 명시했다.

```sql
SELECT common_code_groups.id,
       seed.code,
       seed.code_name,
       seed.description,
       seed.sort_order,
       true,
       true
```

이 수정으로 PostgreSQL SQL init 단계에서 컬럼 출처가 명확해졌고, 반복 실행 가능한 `ON CONFLICT` 구조는 그대로 유지했다.

## 6. Actuator 의존성 추가 반영

`backend/pom.xml`에 아래 의존성이 추가되어 있는 상태도 함께 확인했다.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

`/actuator/health` 호출은 현재 HTTP 401로 응답한다. 이는 서버가 응답 중이라는 신호로는 확인됐지만, health endpoint를 공개 상태 확인 API로 사용할지는 별도 보안 정책 결정이 필요하다.

## 7. 검증 결과

| 항목 | 결과 |
|---|---|
| `docker compose up -d postgres` | 성공 |
| PostgreSQL 컨테이너 상태 | `health-center-postgres` 실행 중 |
| `mvn -q -DskipTests compile` | 성공 |
| `mvn spring-boot:run` | 실행 후 API 응답 확인 |
| `GET /api/common-codes/RESERVATION_STATUS` | 성공 |
| `GET /api/common-codes?groupCodes=RESERVATION_STATUS,QUEUE_STATUS` | 성공 |
| Swagger UI | HTTP 200 확인 |
| `/actuator/health` | HTTP 401 확인 |

공통코드 API 응답은 `success + data + error` 형식을 유지했다.

## 8. 변경 파일

| 파일 | 변경 이유 |
|---|---|
| `backend/pom.xml` | Actuator 의존성 추가 확인 |
| `backend/src/main/resources/db/postgresql/data.sql` | seed INSERT의 모호한 컬럼 참조 수정 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 실행 환경과 공통코드 API 검증 완료 반영 |
| `docs/11_implementation_log/01_PostgreSQL_공통코드_API_변경_기록.md` | 런타임 검증 결과와 남은 리스크 갱신 |
| `docs/11_implementation_log/02_PostgreSQL_공통코드_API_PR_작성안.md` | PR 검증 항목과 미검증 사유 갱신 |
| `docs/11_implementation_log/04_PostgreSQL_공통코드_API_런타임_검증_및_SQL_수정_기록.md` | 이번 브랜치 변경 배경과 결과 기록 |

## 9. 확인하지 못한 것

- `gitnexus detect-changes`는 현재 CLI에서 `unknown command`로 실패했다.
- Actuator endpoint 공개 여부는 결정하지 않았다.
- eGovFrame 샘플 API의 실패 범위는 아직 확인하지 않았다.

## 10. 남은 위험과 후속 작업

| 항목 | 설명 | 후속 작업 |
|---|---|---|
| 샘플 API 혼재 | PostgreSQL 전환 후 기존 샘플 mapper가 없어 일부 샘플 API가 실패할 수 있다. | 샘플 API 실패 범위 확인 |
| Actuator 보안 정책 | `/actuator/health`가 현재 인증 필요 상태이다. | 공개 여부 결정 후 Security 설정 반영 |
| SQL init 반복 실행 | 개발 편의상 매 실행 SQL init을 수행한다. | Flyway 전환 시점 검토 |

## 11. 다음 작업 추천

1. 샘플 API 실패 범위 확인
2. SNS 로그인 샘플 제거 가능 여부 영향 분석
3. 안전하면 SNS 로그인 샘플부터 기능 묶음 단위로 정리
4. 이후 Office Context `GET /api/service-types` 구현

## 12. 커밋 메시지 초안

```text
fix: 공통코드 seed SQL 런타임 오류 수정

- PostgreSQL seed INSERT의 모호한 컬럼 참조 수정
- Docker PostgreSQL 기반 공통코드 API 호출 검증
- Actuator 의존성 추가 상태와 health endpoint 응답 기록
- 전체 체크리스트와 PR 작성안의 런타임 검증 결과 갱신
```
