# Waiting Queue Lifecycle API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/waiting-queue-lifecycle-api` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 대기열 조회, 호출, 처리 시작, 처리 완료 API와 Swagger seed 추가 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | `mvn.cmd -q -DskipTests compile` 성공 |
| 테스트 확인 | `mvn.cmd -q test-compile` 성공 |
| 정적 확인 | `git diff --check` 성공, GitNexus detect-changes는 unknown command로 실패 |
| 실행/API 확인 | 사용자가 Docker PostgreSQL 실행 후 VS Code Spring Boot Dashboard로 서버를 기동하고 Swagger에서 대표 순서로 확인 필요 |

## PR 제목

```text
feat: 대기열 조회와 상태 전이 API 구현
```

## PR 본문

```markdown
## 개요

현장 접수와 예약자 체크인 이후 발급된 대기표를 직원이 조회하고, 호출/처리 시작/처리 완료 순서로 상태 전이할 수 있도록 Queue Context API를 구현합니다.

## 변경 내용

- `GET /api/queues` 대기열 조회 API 추가
- `POST /api/queues/{id}/call` 대기자 호출 API 추가
- `POST /api/queues/{id}/start` 처리 시작 API 추가
- `POST /api/queues/{id}/complete` 처리 완료 API 추가
- Queue Controller, QueryService, CommandService, Policy, Response DTO 추가
- QueueTicket Mapper/VO 확장
- 처리 시작/완료 시 Visit 상태 갱신
- 예약 기반 방문 완료 시 예약 상태 `COMPLETED` 갱신
- Swagger 테스트용 `Swagger대기열` seed 방문/대기표 추가
- API 명세, 브랜치 구현 기록, 전체 체크리스트 갱신

## 검증

- [x] Maven compile
- [x] Maven test-compile
- [x] git diff --check
- [x] Swagger 대표 예시에 필요한 seed/mock 데이터 추가
- [ ] Docker PostgreSQL 실행 확인
- [ ] VS Code Spring Boot Dashboard 서버 기동 확인
- [ ] Swagger에서 `POST /api/auth/login` 직원 계정 로그인 확인
- [ ] Swagger에서 `GET /api/queues?status=WAITING` 조회 확인
- [ ] Swagger에서 `POST /api/queues/{id}/call` 호출 확인
- [ ] Swagger에서 `POST /api/queues/{id}/start` 처리 시작 확인
- [ ] Swagger에서 `POST /api/queues/{id}/complete` 처리 완료 확인
- [ ] Swagger에서 잘못된 상태 전이 시 `QUEUE_INVALID_STATUS` 확인

## Swagger 대표 테스트 순서

1. `POST /api/auth/login`
   - `staff@test.com / password1234`
2. Swagger Authorize 창에 accessToken 값만 입력
3. `GET /api/queues?status=WAITING`
   - `visitorName = Swagger대기열`의 `queueTicketId` 확인
4. `POST /api/queues/{queueTicketId}/call`
   - 기대: `status = CALLED`
5. `POST /api/queues/{queueTicketId}/start`
   - 기대: `status = IN_PROGRESS`
6. `POST /api/queues/{queueTicketId}/complete`
   - 기대: `status = COMPLETED`

## Swagger 추가 테스트 체크리스트

| 케이스 | 확인 방법 | 기대 결과 |
|---|---|---|
| 대기열 조회 | `GET /api/queues?status=WAITING` | `Swagger대기열` 대기표 조회 |
| 대기자 호출 | `WAITING` 대기표로 call | `status = CALLED` |
| 처리 시작 | `CALLED` 대기표로 start | `status = IN_PROGRESS` |
| 처리 완료 | `IN_PROGRESS` 대기표로 complete | `status = COMPLETED` |
| 잘못된 순서 | `WAITING` 대기표로 start | HTTP 409, `error.code = QUEUE_INVALID_STATUS` |
| 없는 대기표 | 존재하지 않는 ID 사용 | HTTP 404, `error.code = QUEUE_TICKET_NOT_FOUND` |
| 권한 없는 사용자 | 시민 토큰으로 호출 | HTTP 403 또는 인증/권한 실패 |

## 미검증 사유

- 서버 기동은 사용자가 VS Code Spring Boot Dashboard에서 직접 수행합니다.
- Docker PostgreSQL 실행과 API 런타임 검증은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- API 런타임 검증은 터미널 호출보다 Swagger UI를 우선 사용합니다.
- GitNexus index는 stale 상태이며 detect-changes는 현재 CLI에서 `unknown command 'detect-changes'`로 실패해 `rg`, `git diff --check`, Maven 검증으로 보완했습니다.

## 후속 작업

- 보류 `POST /api/queues/{id}/hold` 구현
- 대기번호 발급 동시성 정책 보강
- 방문/대기 취소 전용 API 구현
- 대기열 화면과 Queue API 연결
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영
- [ ] 후속 브랜치 생성 또는 다음 작업 문서화

## 커밋 메시지 초안

```text
feat: 대기열 조회와 상태 전이 API 구현

- 대기열 조회 API 추가
- 대기자 호출, 처리 시작, 처리 완료 API 추가
- QueueTicket 상태 전이와 Visit/Reservation 완료 상태 갱신
- Swagger 테스트용 대기표 seed와 문서 보강
```
