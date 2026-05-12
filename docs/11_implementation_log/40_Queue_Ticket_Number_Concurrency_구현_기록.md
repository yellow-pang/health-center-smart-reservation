# Queue Ticket Number Concurrency 구현 기록

## 1. 작업 목표

- 동시 체크인/현장 접수 시 같은 보건소, 업무 유형, 발급일 기준 대기번호가 중복 발급되지 않도록 보강한다.
- 기존 `MAX(ticket_number) + 1` 방식의 경쟁 조건을 PostgreSQL 원자적 upsert 기반 채번으로 바꾼다.
- DB 제약으로도 당일 대기번호 중복을 막는다.

## 2. 작업 범위

- [x] 이번 브랜치에 포함: `queue_ticket_counters` 채번 테이블 추가
- [x] 이번 브랜치에 포함: `issueWaitingTicket` SQL을 `INSERT ... ON CONFLICT DO UPDATE ... RETURNING` 방식으로 변경
- [x] 이번 브랜치에 포함: `queue_tickets` 당일 대기번호 유니크 인덱스 추가
- [x] 이번 브랜치에 포함: seed 데이터 삽입 후 채번 테이블을 현재 최대 대기번호와 동기화
- [x] 이번 브랜치에 포함: API 명세와 전체 체크리스트 갱신
- [x] 이번 브랜치에서 제외: 별도 API 엔드포인트 추가
- [x] 이번 브랜치에서 제외: 부하/동시 요청 런타임 테스트 자동화
- [x] 이번 브랜치에서 제외: 대기열 화면 연동

## 3. 작업 전 체크리스트

- [x] 현재 브랜치 확인: `fix/queue-ticket-number-concurrency`
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` Queue 후속 작업 확인
- [x] 이전 브랜치 기록의 대기번호 동시성 후속 작업 확인
- [x] `QueueTicketMapper.issueWaitingTicket` 호출 지점 확인
- [x] `QueueTicket_SQL_postgresql.xml` 발급 SQL 확인
- [x] PostgreSQL schema와 seed 구조 확인

## 4. 영향 분석

| 대상 | 직접 영향 | 위험도 |
|---|---|---|
| `schema.sql` | 채번 테이블과 당일 대기번호 유니크 인덱스 추가 | MEDIUM |
| `QueueTicket_SQL_postgresql.xml` | 대기표 발급 SQL 변경 | MEDIUM |
| `data.sql` | seed 대기표 삽입 후 채번 테이블 동기화 | LOW |
| `VisitCommandService` | 호출 시그니처 변경 없음, 체크인/현장접수 발급 흐름 간접 영향 | LOW |
| `docs/04_api/01_API_명세서.md` | 대기번호 중복 방지 정책 추가 | LOW |

GitNexus 확인:

| 명령 | 결과 | 보완 |
|---|---|---|
| `npm.cmd exec -- gitnexus status` | stale index 확인 | `rg`, XML 파싱, 정적 확인으로 보완 |

## 5. 구현 체크리스트

- [x] PostgreSQL schema 수정
- [x] MyBatis 발급 SQL 수정
- [x] seed 데이터 후처리 수정
- [x] API 명세 갱신
- [x] 전체 체크리스트 갱신
- [x] 구현 기록 작성
- [x] PR 문서 초안 작성
- [x] 커밋 메시지 초안 작성

## 6. 검증 체크리스트

- [x] `QueueTicket_SQL_postgresql.xml` XML 파싱 확인
- [x] `rg`로 `queue_ticket_counters`, `issueWaitingTicket` 참조 확인
- [x] `git diff --check`
- [ ] `mvn.cmd -q -DskipTests compile`
- [ ] `mvn.cmd -q test-compile`
- [ ] Swagger에서 체크인 또는 현장 접수 대표 예시 확인

## 7. 구현 내용

### 7.1 DB 제약

- `queue_ticket_counters` 테이블을 추가했다.
- 유니크 키는 `(health_center_id, service_type_id, issued_date)`이다.
- `queue_tickets`에는 `(health_center_id, service_type_id, issued_at::date, ticket_number)` 유니크 인덱스를 추가했다.

### 7.2 발급 SQL

- 기존 `MAX(ticket_number) + 1` 단일 계산을 제거했다.
- `queue_ticket_counters`에 오늘 카운터가 없으면 현재 `queue_tickets`의 최대 대기번호 + 1로 초기화한다.
- 카운터가 이미 있으면 `ON CONFLICT DO UPDATE`에서 `last_ticket_number + 1`로 원자적으로 증가시킨다.
- 증가된 `last_ticket_number`를 새 `queue_tickets.ticket_number`로 사용한다.

### 7.3 Seed

- Swagger seed 대기표를 직접 삽입한 뒤, 당일 `queue_tickets`의 최대 대기번호를 `queue_ticket_counters`에 반영한다.
- 이후 체크인/현장접수 API가 seed 대기번호와 충돌하지 않도록 했다.

## 8. Swagger 대표 확인 방법

1. `POST /api/auth/login`에서 `staff@test.com / password1234`로 로그인한다.
2. Swagger Authorize 창에는 `Bearer `를 제외한 accessToken 값만 입력한다.
3. `POST /api/visits/walk-in`을 실행한다.

대표 요청:

```json
{
  "serviceTypeId": 1,
  "visitorName": "Swagger동시성확인",
  "visitorPhone": "010-7777-8888"
}
```

기대 결과:

- HTTP 200
- `success = true`
- `data.ticketNumber`가 같은 업무 유형의 기존 당일 대기번호보다 큰 값
- 이후 `GET /api/queues?status=WAITING`에서 같은 `ticketNumber`가 중복되지 않음

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 구현 중 | seed 삽입 후 채번 테이블 동기화 | seed 대기표가 채번 테이블을 거치지 않으면 다음 API 발급 번호가 낮아질 수 있음 | `data.sql`에 counter upsert 추가 |
| 범위 정리 | 동시 요청 자동화 테스트 | 실제 경쟁 조건 검증에는 병렬 API 호출 또는 DB 트랜잭션 테스트가 필요 | 후속 테스트 후보로 분리 |
| 범위 정리 | 대기열 화면과 Queue API 연결 | 백엔드 안정화 이후 직원 화면에서 활용 필요 | 후속 작업 |

## 10. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
