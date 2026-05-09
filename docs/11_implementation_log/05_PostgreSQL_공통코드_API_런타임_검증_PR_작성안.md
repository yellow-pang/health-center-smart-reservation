# PostgreSQL 공통코드 API 런타임 검증 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `fix-postgres-common-code-runtime-verification` |
| base 브랜치 | `main` |
| 작업 성격 | 런타임 검증, PostgreSQL seed SQL 오류 수정, 문서 갱신 |
| 작업 트리 | PR 문서 작성 전 clean |
| 빌드 확인 | `mvn -q -DskipTests compile` 성공 |
| 런타임 확인 | Docker PostgreSQL, 백엔드 실행, 공통코드 API, Swagger 확인 |

## PR 제목

```text
fix: 공통코드 seed SQL 런타임 오류 수정
```

## PR 본문

```markdown
## 개요

PostgreSQL 공통코드 API를 Docker PostgreSQL 환경에서 실제 실행 검증하고, 백엔드 기동 중 발견된 seed SQL 오류를 수정했습니다.

기존에는 Docker Desktop 미실행으로 런타임 검증이 보류되어 있었고, 이번 작업에서 PostgreSQL 컨테이너 실행, 백엔드 기동, Swagger 접근, 공통코드 API 호출까지 확인했습니다.

## 변경 내용

- PostgreSQL `data.sql`의 공통코드 seed INSERT 오류 수정
- `description` 컬럼 참조를 `seed.description`으로 명확화
- `common_code_groups.id`, `seed.code`, `seed.code_name`, `seed.sort_order` 등 SELECT 대상 컬럼 출처 명시
- `spring-boot-starter-actuator` 의존성 추가 상태 반영
- `/actuator/health` 응답 상태 기록
- 전체 체크리스트와 구현 기록, 기존 PR 작성안의 검증 결과 갱신
- 이번 브랜치 작업 기록 문서 추가

## 원인

PostgreSQL SQL init 중 아래 오류로 애플리케이션 기동이 실패했습니다.

```text
ERROR: column reference "description" is ambiguous
```

`common_code_groups` 테이블과 seed VALUES 양쪽에 모두 `description` 컬럼이 있는데, INSERT SELECT에서 별칭 없이 `description`을 사용해 PostgreSQL이 어느 컬럼인지 판단하지 못했습니다.

## 검증

- [x] `docker compose up -d postgres`
- [x] PostgreSQL 컨테이너 실행 확인
- [x] `mvn -q -DskipTests compile`
- [x] `mvn spring-boot:run`
- [x] `GET /api/common-codes/RESERVATION_STATUS`
- [x] `GET /api/common-codes?groupCodes=RESERVATION_STATUS,QUEUE_STATUS`
- [x] Swagger UI HTTP 200 확인
- [x] `/actuator/health` HTTP 401 확인

## 미검증 사유

- `gitnexus detect-changes`는 현재 CLI에서 `unknown command`로 실패했습니다.
- Actuator `/actuator/health`를 공개 endpoint로 둘지 여부는 아직 결정하지 않았습니다.
- eGovFrame 샘플 API의 PostgreSQL 전환 후 실패 범위는 아직 확인하지 않았습니다.

## 주의사항

- `/actuator/health`는 현재 보안 필터에 걸려 HTTP 401을 반환합니다. 운영/배포 health check 용도로 사용할 경우 별도 보안 정책 결정이 필요합니다.
- 기존 eGovFrame 샘플 API는 PostgreSQL mapper 부재로 일부 실패할 수 있습니다. 이번 PR의 검증 대상은 신규 보건소 공통코드 API입니다.
- SQL init은 현재 개발 편의를 위한 방식이며, 이후 Flyway 전환을 검토할 수 있습니다.

## 후속 작업

- 샘플 API 실패 범위 확인
- SNS 로그인 샘플 제거 가능 여부 영향 분석
- 안전하면 SNS 로그인 샘플부터 기능 묶음 단위로 정리
- Office Context `GET /api/service-types` 구현 착수
```

## 변경 파일 요약

| 파일 | 내용 |
|---|---|
| `backend/pom.xml` | Actuator 의존성 추가 |
| `backend/src/main/resources/db/postgresql/data.sql` | 공통코드 seed INSERT 컬럼 별칭 명시 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 실행 환경과 공통코드 API 검증 완료 반영 |
| `docs/11_implementation_log/01_PostgreSQL_공통코드_API_변경_기록.md` | 런타임 검증 결과와 남은 리스크 갱신 |
| `docs/11_implementation_log/02_PostgreSQL_공통코드_API_PR_작성안.md` | 기존 PR 작성안의 검증 항목 갱신 |
| `docs/11_implementation_log/04_PostgreSQL_공통코드_API_런타임_검증_및_SQL_수정_기록.md` | 이번 브랜치 작업 기록 추가 |
| `docs/11_implementation_log/05_PostgreSQL_공통코드_API_런타임_검증_PR_작성안.md` | 이번 PR 작성안 추가 |

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] `main` 최신화 확인
- [ ] `main`에서 필요 시 `mvn -q -DskipTests compile` 재확인
- [ ] 후속 브랜치 생성 여부 결정
- [ ] `fix-postgres-common-code-runtime-verification` 브랜치 삭제

## 후속 브랜치 이름 추천

```text
refactor-remove-sns-login-sample
refactor-egov-sample-api-cleanup
feat-office-service-type-api
```
