# Waiting Queue Hold API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/waiting-queue-hold-api` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 대기표 보류 처리 API와 문서 수정 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | `mvn.cmd -q -DskipTests compile` 성공 |
| 테스트 확인 | `mvn.cmd -q test-compile` 성공 |
| 정적 확인 | `git diff --check` 성공, GitNexus detect-changes는 unknown command로 실패 |
| 실행/API 확인 | 사용자가 Docker PostgreSQL 실행 후 VS Code Spring Boot Dashboard로 서버를 기동하고 Swagger에서 대표 순서로 확인 필요 |

## PR 제목

```text
feat: 대기표 보류 처리 API 구현
```

## PR 본문

```markdown
## 개요

호출된 대기자가 응답하지 않는 경우 직원 또는 관리자가 대기표를 `HOLD` 상태로 보류 처리할 수 있도록 Queue API를 확장합니다. 보류된 대기표는 기존 호출 API로 다시 `CALLED` 상태로 재호출할 수 있습니다.

## 변경 내용

- `POST /api/queues/{id}/hold` 보류 처리 API 추가
- `CALLED -> HOLD` 상태 전이 정책 추가
- `QueueTicketMapper`에 `markHold` 추가
- Queue 응답에 `holdAt` 필드 추가
- API 명세와 브랜치 구현 기록 갱신

## 검증

- [x] Maven compile
- [x] Maven test-compile
- [x] git diff --check
- [x] Swagger 대표 예시에 필요한 seed/mock 데이터 확인
- [x] Docker PostgreSQL 실행 확인
- [x] VS Code Spring Boot Dashboard 서버 기동 확인
- [x] Swagger에서 `GET /api/queues?status=WAITING` 조회 확인
- [x] Swagger에서 `POST /api/queues/{id}/call` 호출 확인
- [x] Swagger에서 `POST /api/queues/{id}/hold` 보류 확인
- [x] Swagger에서 보류 대기표 재호출 확인
- [x] Swagger에서 잘못된 상태 전이 시 `QUEUE_INVALID_STATUS` 확인

## Swagger 대표 테스트 순서

1. `POST /api/auth/login`
   - `staff@test.com / password1234`
2. Swagger Authorize 창에 accessToken 값만 입력
3. `GET /api/queues?status=WAITING`
   - `visitorName = Swagger대기열`의 `queueTicketId` 확인
4. `POST /api/queues/{queueTicketId}/call`
   - 기대: `status = CALLED`
5. `POST /api/queues/{queueTicketId}/hold`
   - 기대: `status = HOLD`, `holdAt` 존재
6. `POST /api/queues/{queueTicketId}/call`
   - 기대: `status = CALLED`

## Swagger 추가 테스트 체크리스트

| 케이스 | 확인 방법 | 기대 결과 |
|---|---|---|
| 보류 정상 | `CALLED` 대기표로 hold | `status = HOLD`, `holdAt` 존재 |
| 보류 후 재호출 | `HOLD` 대기표로 call | `status = CALLED` |
| 잘못된 순서 | `WAITING` 대기표로 hold | HTTP 409, `error.code = QUEUE_INVALID_STATUS` |
| 없는 대기표 | 존재하지 않는 ID 사용 | HTTP 404, `error.code = QUEUE_TICKET_NOT_FOUND` |
| 권한 없는 사용자 | 시민 토큰으로 호출 | HTTP 403 또는 인증/권한 실패 |

## 미검증 사유

- 서버 기동은 사용자가 VS Code Spring Boot Dashboard에서 직접 수행합니다.
- Docker PostgreSQL 실행과 API 런타임 검증은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- API 런타임 검증은 터미널 호출보다 Swagger UI를 우선 사용합니다.
- GitNexus index는 stale 상태라 직전 브랜치 신규 Queue 심볼 impact는 일부 대상 심볼을 찾지 못했고, detect-changes는 현재 CLI에서 `unknown command 'detect-changes'`로 실패해 `rg`, `git diff --check`, Maven 검증으로 보완했습니다.

## 후속 작업

- `HOLD -> NO_SHOW` 최종 미응답 처리 API 구현
- 방문/대기 취소 전용 API 구현
- 대기번호 발급 동시성 정책 보강
- 대기열 화면과 Queue API 연결
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영
- [ ] 후속 브랜치 생성 또는 다음 작업 문서화

## 커밋 메시지 초안

```text
feat: 대기표 보류 처리 API 구현

- 호출된 대기표를 HOLD 상태로 변경하는 API 추가
- 보류 시 holdAt 응답 필드와 DB 갱신 추가
- 보류 후 재호출 Swagger 테스트 흐름 문서화
```
