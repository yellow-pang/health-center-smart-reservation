# 미처리 대기표 마감 API 구현 기록

## 1. 작업 목표

- 영업일 마감 시 당일 미처리 대기표를 `NO_SHOW`로 일괄 처리한다.
- 시민 혼잡도에 포함되는 활성 대기 상태가 다음 영업일 운영 데이터로 남지 않도록 정리할 수 있게 한다.
- 관리자 화면 UX는 다음 커밋 단위로 분리하고, 이번 작업은 백엔드 API와 문서 갱신까지만 진행한다.

## 2. 작업 범위

- [x] 현재 브랜치와 작업 트리 확인
- [x] 기존 대기표 상태 전이 구조 확인
- [x] 관리자용 미처리 대기표 마감 API 추가
- [x] QueueTicket, Visit, Reservation 상태 동기화 SQL 추가
- [x] API 명세와 전체 체크리스트 갱신
- [x] 보류 목록 DC-026 상태 갱신
- [x] PR 문서 초안 작성

제외한다.

- [ ] 관리자 화면에서 마감 전 미처리 대기표 확인 UX
- [ ] 마감 API 실행 버튼 연동
- [ ] 스케줄러 기반 자동 마감 배치

## 3. 정책 결정

| 항목 | 결정 |
|---|---|
| 마감 대상 날짜 | 요청 `date`, 생략 시 오늘 |
| 마감 대상 상태 | `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS` |
| 마감 결과 상태 | `NO_SHOW` |
| Visit 동기화 | 연결된 Visit도 `NO_SHOW` 처리 |
| Reservation 동기화 | 예약 방문이고 예약 상태가 `CHECKED_IN`이면 예약도 `NO_SHOW` 처리 |
| 권한 | 같은 보건소 소속 `ADMIN`만 실행 |

## 4. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `backend/src/main/java/egovframework/healthcenter/queue/api/QueueController.java` | `POST /api/queues/admin/close-pending` 추가 |
| `backend/src/main/java/egovframework/healthcenter/queue/application/QueueCommandService.java` | 관리자 검증과 미처리 대기표 마감 서비스 추가 |
| `backend/src/main/java/egovframework/healthcenter/queue/mapper/QueueTicketMapper.java` | 마감 대상 카운트, Visit/Reservation/QueueTicket 일괄 갱신 메서드 추가 |
| `backend/src/main/resources/egovframework/mapper/healthcenter/queue/QueueTicket_SQL_postgresql.xml` | 미처리 대기표 조회/NO_SHOW 일괄 처리 SQL 추가 |
| `backend/src/main/java/egovframework/healthcenter/queue/dto/ClosePendingQueueTicketsResponse.java` | 마감 처리일과 처리 건수 응답 DTO 추가 |
| `docs/04_api/01_API_명세서.md` | 미처리 대기표 마감 API 명세 추가 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | Queue Context 진행 상태 반영 |
| `docs/14_deferred_cleanup/01_보류_정리_목록.md` | DC-026 API 구현 완료 및 관리자 UX 후속 분리 |

## 5. Swagger 대표 예시

관리자 계정 로그인 후 Swagger `Try it out`에서 아래 요청을 확인한다.

```text
POST /api/queues/admin/close-pending?date=2026-05-22
```

기대 결과:

- `success: true`
- `data.date`가 요청 날짜와 같다.
- `data.closedCount`가 `NO_SHOW` 처리된 대기표 수로 반환된다.
- 이후 `GET /api/queues?status=NO_SHOW` 또는 DB 확인 시 대상 대기표/방문/예약 상태가 `NO_SHOW`로 정리된다.

## 6. 검증 체크리스트

- [x] `git diff --check`
- [ ] `mvn.cmd -q -DskipTests compile` - 현재 세션 PATH에서 `mvn.cmd` 실행 파일을 찾지 못해 미수행
- [ ] `mvn.cmd -q test-compile` - 현재 세션 PATH에서 `mvn.cmd` 실행 파일을 찾지 못해 미수행
- [ ] Swagger `POST /api/queues/admin/close-pending?date=2026-05-22` 확인
- [ ] 관리자 권한이 아닌 사용자 호출 시 403 확인

## 7. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 마감 API 구현 | 관리자 화면에서 마감 전 미처리 대기표 확인 UX | 마감 전 운영자가 대상 건을 보고 실행해야 실수 처리를 줄일 수 있음 | 다음 커밋 단위로 진행 |
| 마감 API 구현 | 스케줄러 자동 마감 | 운영 자동화에는 유용하지만 운영 시간/예외 기준이 필요함 | 후속 고도화 |

## 8. 브랜치 종료 전 체크리스트

- [x] API 구현 완료
- [x] 문서 갱신
- [x] PR 문서 작성
- [x] 정적 검증 결과와 Maven 미수행 사유 기록
- [x] Swagger 런타임 확인 안내
- [x] 커밋 메시지 정리
