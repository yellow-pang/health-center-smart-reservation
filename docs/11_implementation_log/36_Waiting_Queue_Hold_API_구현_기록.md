# Waiting Queue Hold API 구현 기록

## 1. 작업 목표

- 호출된 대기자가 응답하지 않는 경우 직원 또는 관리자가 대기표를 보류 처리할 수 있게 한다.
- 보류된 대기표는 기존 호출 API로 재호출할 수 있게 한다.
- Swagger에서 호출 후 보류, 보류 후 재호출 흐름을 확인할 수 있게 문서를 정리한다.

## 2. 작업 범위

- [x] 이번 브랜치에 포함: `POST /api/queues/{queueTicketId}/hold`
- [x] 이번 브랜치에 포함: `CALLED -> HOLD` 상태 전이
- [x] 이번 브랜치에 포함: `holdAt` 응답 필드 추가
- [x] 이번 브랜치에 포함: `HOLD -> CALLED` 재호출 흐름 문서화
- [x] 이번 브랜치에서 제외: `HOLD -> NO_SHOW`
- [x] 이번 브랜치에서 제외: 방문/대기 취소 전용 API
- [x] 이번 브랜치에서 제외: 대기번호 발급 동시성 정책 보강

## 3. 작업 전 체크리스트

- [x] 이전 PR 문서 `35_Waiting_Queue_Lifecycle_API_PR_작성안.md` 후속 작업 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] `docs/04_api/01_API_명세서.md` 대기 상태 흐름 확인
- [x] `docs/02_domain/02_업무_흐름도.md` HOLD 흐름 확인
- [x] GitNexus impact 시도
- [x] 영향받는 파일 확인

## 4. 영향 분석

GitNexus 확인:

| 대상 | 결과 | 보완 확인 |
|---|---|---|
| `gitnexus status` | stale index 확인 | `rg`와 Maven 검증 병행 |
| `gitnexus impact QueueCommandService` | 대상 심볼을 찾지 못함 | 직전 브랜치 신규 심볼이라 stale index에 없음 |
| `gitnexus impact QueueController` | 대상 심볼을 찾지 못함 | `rg` 직접 참조 확인 |
| `gitnexus impact QueueTicketMapper` | LOW, 직접 영향 `VisitCommandService` | Queue API 상태 전이 추가 영향 확인 |
| `gitnexus impact QueueTicketPolicy` | 대상 심볼을 찾지 못함 | `rg` 직접 참조 확인 |
| `gitnexus impact QueueTicketVO` | LOW, 직접 영향 `VisitCommandService` | `holdAt` 필드 추가 영향 확인 |

`rg` 기준 blast radius:

| 대상 | 직접 영향 | 위험도 |
|---|---|---|
| `QueueController` | 신규 `POST /api/queues/{id}/hold` | LOW |
| `QueueCommandService` | `hold` 상태 전이 추가 | LOW |
| `QueueTicketPolicy` | `CALLED -> HOLD` 정책 추가 | LOW |
| `QueueTicketMapper`, XML | `markHold`, `hold_at` 매핑 추가 | MEDIUM |
| `QueueTicketResponse`, `QueueTicketVO` | 응답 필드 `holdAt` 추가 | LOW |

## 5. 구현 체크리스트

- [x] Controller 수정
- [x] CommandService 수정
- [x] Policy 수정
- [x] Mapper 수정
- [x] Mapper XML 수정
- [x] DTO/VO 수정
- [x] API 명세 갱신
- [x] Swagger 대표 테스트 순서 작성

## 6. 검증 체크리스트

- [x] Maven compile 확인
- [x] Maven test-compile 확인
- [x] `git diff --check` 확인
- [x] Swagger URL과 인증 방법 작성
- [x] Swagger 대표 예시와 테스트 순서 작성
- [x] GitNexus detect-changes 시도 및 실패 사유 기록
- [ ] 사용자가 Docker PostgreSQL 실행 확인
- [ ] 사용자가 VS Code Spring Boot Dashboard로 서버 기동 확인
- [ ] 사용자가 Swagger에서 대표 예시로 API 런타임 호출 확인

## 7. 구현 내용

### 7.1 API

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/queues/{queueTicketId}/hold` | 호출된 대기표 보류 처리 | 같은 보건소 `STAFF`, `ADMIN` |

### 7.2 상태 전이

- `CALLED` 상태만 `HOLD`로 보류 처리할 수 있다.
- 보류 성공 시 `queue_tickets.status = HOLD`, `hold_at = CURRENT_TIMESTAMP`로 갱신한다.
- `HOLD` 상태는 기존 호출 API에서 재호출 가능하다.
- 보류 처리만으로 Visit 상태는 바꾸지 않는다.

## 8. 사용자 직접 런타임 확인 방법

1. `POST /api/auth/login`에서 `staff@test.com / password1234`로 로그인한다.
2. Swagger Authorize 창에는 `Bearer `를 제외한 accessToken 값만 입력한다.
3. `GET /api/queues?status=WAITING`에서 `Swagger대기열`의 `queueTicketId`를 확인한다.
4. `POST /api/queues/{queueTicketId}/call`을 실행해 `CALLED` 상태로 만든다.
5. `POST /api/queues/{queueTicketId}/hold`를 실행한다.

기대 결과:

- HTTP 200
- `success = true`
- `data.status = HOLD`
- `data.holdAt` 값이 존재

재호출 확인:

1. 같은 `queueTicketId`로 `POST /api/queues/{queueTicketId}/call`을 다시 실행한다.
2. `data.status = CALLED`로 돌아오면 정상이다.

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 범위 정리 | `HOLD -> NO_SHOW` 구현 | 업무 흐름도에는 최종 미응답이 있으나 이번 브랜치는 보류/재호출까지만 처리 | 후속 작업 |
| 범위 정리 | 방문/대기 취소 전용 API | 접수 이후 사용자가 이탈하거나 접수를 취소하는 정책 필요 | 후속 작업 |
| 범위 정리 | 대기번호 발급 동시성 정책 보강 | 현재는 업무 유형별 당일 최대 번호 + 1 방식이라 동시 접수 경쟁 조건 검증 필요 | 후속 작업 |
| 검증 중 | GitNexus detect-changes 확인 | CLI에서 `unknown command 'detect-changes'`로 실패 | `git status`, `git diff --check`, Maven 검증으로 보완 |

## 10. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
