# Queue Ticket Number Concurrency Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `fix/queue-ticket-number-concurrency` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 대기번호 채번 테이블, 원자적 발급 SQL, DB 유니크 인덱스, seed 동기화, 문서 수정 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | `mvn.cmd -q -DskipTests compile` |
| 테스트 확인 | `mvn.cmd -q test-compile` |
| 정적 확인 | `QueueTicket_SQL_postgresql.xml` XML 파싱 성공, `git diff --check` 성공 |
| 실행/API 확인 | Swagger 대표 순서 작성 |

## PR 제목

```text
fix: 대기번호 발급 동시성 보강
```

## PR 본문

```markdown
## 개요

체크인과 현장 접수가 동시에 발생할 때 `MAX(ticket_number) + 1` 계산으로 같은 대기번호가 발급될 수 있는 위험을 줄이기 위해, PostgreSQL 원자적 upsert 기반 채번 테이블과 DB 유니크 인덱스를 추가합니다.

## 변경 내용

- `queue_ticket_counters` 테이블 추가
- `queue_tickets` 당일 대기번호 유니크 인덱스 추가
- `QueueTicketMapper.issueWaitingTicket` SQL을 `INSERT ... ON CONFLICT DO UPDATE ... RETURNING` 기반으로 변경
- Swagger seed 대기표 삽입 후 채번 테이블을 현재 최대 대기번호와 동기화
- API 명세, 브랜치 구현 기록, 전체 체크리스트 갱신

## 검증

- [x] `QueueTicket_SQL_postgresql.xml` XML 파싱 확인
- [x] `rg`로 채번 테이블과 발급 SQL 참조 확인
- [x] git diff --check
- [ ] Maven compile
- [ ] Maven test-compile
- [ ] Swagger에서 `POST /api/visits/walk-in` 대표 예시 확인
- [ ] Swagger에서 `GET /api/queues?status=WAITING` 대기번호 중복 없음 확인

## Swagger 대표 테스트 순서

1. `POST /api/auth/login`
   - `staff@test.com / password1234`
2. Swagger Authorize 창에 accessToken 값만 입력
3. `POST /api/visits/walk-in`

대표 요청:

```json
{
  "serviceTypeId": 1,
  "visitorName": "Swagger동시성확인",
  "visitorPhone": "010-7777-8888"
}
```

기대:

- `success = true`
- `data.ticketNumber`가 같은 업무 유형의 기존 당일 대기번호보다 큰 값
- `GET /api/queues?status=WAITING`에서 같은 `ticketNumber`가 중복되지 않음

## Swagger 추가 테스트 체크리스트

| 케이스 | 확인 방법 | 기대 결과 |
|---|---|---|
| 현장 접수 발급 | `POST /api/visits/walk-in` | 기존 당일 최대 번호보다 큰 대기번호 발급 |
| 예약 체크인 발급 | `POST /api/visits/check-in` | 같은 채번 정책으로 대기번호 발급 |
| 업무 유형별 분리 | 서로 다른 `serviceTypeId`로 현장 접수 | 업무 유형별 번호가 독립 증가 |
| 대기열 조회 | `GET /api/queues?status=WAITING` | 같은 업무 유형 내 `ticketNumber` 중복 없음 |
| 중복 제약 확인 | 같은 일자/업무/번호 중복 삽입 상황 | DB 유니크 인덱스로 실패 |

## 후속 작업

- 동시 요청 자동화 테스트 후보 정리
- 대기열 화면과 Queue API 연결
- Dashboard/Congestion Context 구현 시 대기번호/대기 상태 집계 활용
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영
- [ ] 후속 브랜치 생성 또는 다음 작업 문서화

## 커밋 메시지 초안

```text
fix: 대기번호 발급 동시성 보강

- 당일 업무유형별 대기번호 채번 테이블 추가
- QueueTicket 발급 SQL을 원자적 upsert 방식으로 변경
- 대기번호 중복 방지 유니크 인덱스와 seed counter 동기화 추가
```
