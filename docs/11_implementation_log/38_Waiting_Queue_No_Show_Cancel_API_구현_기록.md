# Waiting Queue No Show Cancel API 구현 기록

## 1. 작업 목표

- 보류된 대기표를 최종 미응답으로 처리하는 `HOLD -> NO_SHOW` API를 구현한다.
- 접수 이후 이탈 또는 접수 취소를 처리하는 방문/대기 취소 전용 API를 구현한다.
- Swagger 대표 예시가 바로 동작하도록 대기 취소 전용 seed 데이터를 분리한다.

## 2. 작업 범위

- [x] 이번 브랜치에 포함: `POST /api/queues/{queueTicketId}/no-show`
- [x] 이번 브랜치에 포함: `HOLD -> NO_SHOW` 상태 전이
- [x] 이번 브랜치에 포함: `POST /api/queues/{queueTicketId}/cancel`
- [x] 이번 브랜치에 포함: `WAITING`, `CALLED`, `HOLD -> CANCELED` 상태 전이
- [x] 이번 브랜치에 포함: Queue 상태 변경 시 Visit 상태 동기화
- [x] 이번 브랜치에 포함: 예약 기반 방문의 `CHECKED_IN` 예약 상태 동기화
- [x] 이번 브랜치에서 제외: 대기번호 발급 동시성 정책 보강
- [x] 이번 브랜치에서 제외: 대기열 화면 연동

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 관련 Queue 후속 항목 확인
- [x] `docs/11_implementation_log/37_Waiting_Queue_Hold_API_PR_작성안.md` 후속 작업 확인
- [x] `docs/04_api/01_API_명세서.md` 대기 상태 흐름 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] 영향받는 Queue Controller, Service, Policy, Mapper, XML, seed 확인

## 4. 영향 분석

`rg` 기준 blast radius:

| 대상 | 직접 영향 | 위험도 |
|---|---|---|
| `QueueController` | 신규 `no-show`, `cancel` endpoint 추가 | LOW |
| `QueueCommandService` | Queue, Visit, Reservation 상태 동기화 추가 | MEDIUM |
| `QueueTicketPolicy` | `NO_SHOW`, `CANCELED` 상태 전이 검증 추가 | LOW |
| `QueueTicketMapper`, XML | 상태 갱신 SQL 추가 | MEDIUM |
| `data.sql` | Swagger 대기 취소 seed 추가 | LOW |
| `docs/04_api/01_API_명세서.md` | 신규 API 정책 문서화 | LOW |

## 5. 구현 체크리스트

- [x] Controller 수정
- [x] CommandService 수정
- [x] Policy 수정
- [x] Mapper 수정
- [x] Mapper XML 수정
- [x] Swagger 대표 예시용 seed 데이터 확인 및 추가
- [x] API 명세 갱신
- [x] 전체 체크리스트 갱신
- [x] PR 문서 초안 작성

## 6. 검증 체크리스트

- [ ] `mvn.cmd -q -DskipTests compile`
- [ ] `mvn.cmd -q test-compile`
- [x] `git diff --check`
- [x] Swagger URL과 인증 방법 작성
- [x] Swagger 대표 예시 1개 작성
- [x] PR 문서에 Happy/Edge/Bad 추가 테스트 체크리스트 작성

## 7. 구현 내용

### 7.1 API

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/queues/{queueTicketId}/no-show` | 보류 대기표 최종 미응답 처리 | 같은 보건소 `STAFF`, `ADMIN` |
| POST | `/api/queues/{queueTicketId}/cancel` | 방문/대기 취소 처리 | 같은 보건소 `STAFF`, `ADMIN` |

### 7.2 상태 전이

- `HOLD` 상태만 `NO_SHOW`로 최종 미응답 처리할 수 있다.
- `WAITING`, `CALLED`, `HOLD` 상태만 `CANCELED`로 취소할 수 있다.
- `NO_SHOW` 처리 시 QueueTicket과 Visit 상태를 함께 `NO_SHOW`로 변경한다.
- 방문이 예약 기반이고 예약 상태가 `CHECKED_IN`이면 예약 상태도 `NO_SHOW`로 변경한다.
- 취소 처리 시 QueueTicket과 Visit 상태를 함께 `CANCELED`로 변경한다.
- 방문이 예약 기반이고 예약 상태가 `CHECKED_IN`이면 예약 상태도 `CANCELED`로 변경한다.
- 처리 시작 이후(`IN_PROGRESS`, `COMPLETED`)와 이미 종료된 상태(`NO_SHOW`, `CANCELED`)는 취소할 수 없다.

## 8. Swagger 대표 확인 방법

1. `POST /api/auth/login`에서 `staff@test.com / password1234`로 로그인한다.
2. Swagger Authorize 창에는 `Bearer `를 제외한 accessToken 값만 입력한다.
3. `GET /api/queues?status=WAITING`에서 `Swagger대기열`의 `queueTicketId`를 확인한다.
4. `POST /api/queues/{queueTicketId}/call`을 실행해 `CALLED` 상태로 만든다.
5. `POST /api/queues/{queueTicketId}/hold`를 실행해 `HOLD` 상태로 만든다.
6. `POST /api/queues/{queueTicketId}/no-show`를 실행한다.

기대 결과:

- HTTP 200
- `success = true`
- `data.status = NO_SHOW`

취소 확인은 같은 조회 결과에서 `Swagger대기취소`의 `queueTicketId`를 사용해 `POST /api/queues/{queueTicketId}/cancel`을 실행한다.

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 구현 중 | Swagger 대기 취소 seed 분리 | NO_SHOW 대표 흐름이 기존 `Swagger대기열` seed를 소모하므로 취소 API 확인용 seed가 별도로 필요 | `Swagger대기취소` seed 추가 |
| 범위 정리 | 대기번호 발급 동시성 정책 보강 | 현재 업무 유형별 당일 최대 번호 + 1 방식이라 동시 접수 경쟁 조건 검증 필요 | 후속 작업 |
| 범위 정리 | 대기열 화면과 Queue API 연결 | 백엔드 API 구현 이후 직원 화면에서 상태 전이 버튼 연동 필요 | 후속 작업 |

## 10. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
