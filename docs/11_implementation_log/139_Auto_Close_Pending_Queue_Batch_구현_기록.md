# 미처리 대기표 자동 NO_SHOW 배치 구현 기록

## 1. 작업 목표

- 관리자가 수동 마감을 놓친 과거 미처리 대기표를 자동으로 `NO_SHOW` 처리한다.
- 기존 수동 마감 API와 같은 QueueTicket, Visit, Reservation 동기화 규칙을 사용한다.
- 서버 기동, 실제 스케줄 실행 로그 확인은 사용자가 직접 수행할 수 있도록 기준을 남긴다.

## 2. 작업 범위

- [x] 전체 체크리스트 8순위와 보류 목록 DC-028 확인
- [x] GitNexus impact 확인
- [x] Spring Scheduler 기반 자동 마감 배치 추가
- [x] 자동 마감용 Service/Mapper/XML 추가
- [x] 환경변수 기반 실행 여부, cron, 보관 일수 설정 추가
- [x] 자동 마감 서비스 단위 테스트 추가
- [x] API 명세, 전체 체크리스트, 보류 목록 갱신
- [x] PR 문서 초안 작성

제외한다.

- [ ] 서버 직접 기동
- [ ] Docker 실행
- [ ] 실제 스케줄 런타임 로그 확인
- [ ] 휴무일/예외일 제외 정책
- [ ] dry-run/count 전용 운영 API
- [ ] 배치 실행 이력 테이블

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 확인
- [x] `docs/14_deferred_cleanup/01_보류_정리_목록.md` 확인
- [x] `docs/04_api/01_API_명세서.md` 확인
- [x] 현재 브랜치 확인: `feat/auto-close-pending-queue-batch`

## 4. 영향 범위 확인

GitNexus 상태:

- `gitnexus status`: stale
- `gitnexus analyze`: 루트에서 실행했지만 `Not inside a git repository`로 실패
- `gitnexus impact -r health-center-smart-reservation EgovBootApplication`: LOW, 직접 영향 없음
- `gitnexus impact -r health-center-smart-reservation QueueCommandService`: LOW, 직접 영향 `QueueController`
- `gitnexus impact -r health-center-smart-reservation QueueTicketMapper`: LOW, 직접 영향 `VisitCommandService`, `QueueQueryService`, `QueueCommandService`
- `gitnexus detect-changes -r health-center-smart-reservation --scope all`: 현재 CLI에서 `unknown command 'detect-changes'`로 실패해 `git status`, `git diff --stat`, `git diff --check`, Maven 검증으로 보완

`rg` 기준 직접 확인한 blast radius:

| 대상 | 직접 영향 |
|---|---|
| `QueueAutoCloseScheduler` | 신규 스케줄러, API 노출 없음 |
| `QueueCommandService.autoCloseOverduePendingTickets` | 신규 배치 진입점 |
| `QueueTicketMapper` 자동 마감 메서드 | 신규 MyBatis statement 호출 |
| `QueueTicket_SQL_postgresql.xml` 자동 마감 SQL | 과거 미처리 대기표, Visit, Reservation 상태 갱신 |
| `application.properties` | 배치 활성 여부, cron, 보관 일수 기본값 |

위험도 판단:

- 위험도: LOW
- 이유: 기존 수동 마감 API 계약은 유지하고, 신규 스케줄러와 신규 mapper statement만 추가했다.
- 완화: 대상 상태를 `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS`로 제한해 재실행 시 이미 종료된 건을 제외한다.

## 5. 정책 결정

| 항목 | 결정 |
|---|---|
| 배치 방식 | Spring Scheduler |
| 기본 활성 여부 | `QUEUE_AUTO_CLOSE_ENABLED=true` |
| 기본 실행 시각 | `QUEUE_AUTO_CLOSE_CRON=0 10 18 * * *` |
| 기준 시간대 | `Globals.TimeZone`, 기본 `Asia/Seoul` |
| 기본 보관 일수 | `QUEUE_AUTO_CLOSE_RETENTION_DAYS=2` |
| 대상 기준 | 실행일 기준 2일 전까지 발급된 미처리 대기표 |
| 대상 상태 | `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS` |
| 결과 상태 | QueueTicket/Visit/Reservation `NO_SHOW` |
| 중복 실행 | 이미 `NO_SHOW`인 건은 제외되어 추가 변경 없음 |

## 6. 구현 내용

| 파일 | 내용 |
|---|---|
| `backend/src/main/java/egovframework/healthcenter/queue/batch/QueueSchedulingConfig.java` | 배치 활성 설정이 켜진 경우 `@EnableScheduling` 적용 |
| `backend/src/main/java/egovframework/healthcenter/queue/batch/QueueAutoCloseScheduler.java` | cron 기반 자동 마감 스케줄러 추가 |
| `backend/src/main/java/egovframework/healthcenter/queue/application/QueueCommandService.java` | 자동 마감 유스케이스 추가 |
| `backend/src/main/java/egovframework/healthcenter/queue/dto/QueueAutoCloseResult.java` | 배치 실행 결과 record 추가 |
| `backend/src/main/java/egovframework/healthcenter/queue/mapper/QueueTicketMapper.java` | 자동 마감 count/update mapper 메서드 추가 |
| `backend/src/main/resources/egovframework/mapper/healthcenter/queue/QueueTicket_SQL_postgresql.xml` | 자동 마감 대상 count, Visit/Reservation/QueueTicket update SQL 추가 |
| `backend/src/main/resources/application.properties` | 배치 설정 기본값 추가 |
| `backend/src/test/java/egovframework/healthcenter/queue/application/QueueCommandServiceTest.java` | 자동 마감 대상 있음/없음 단위 테스트 추가 |
| `docs/04_api/01_API_명세서.md` | 자동 배치 정책과 설정 문서화 |

## 7. 검증 체크리스트

- [x] `git diff --check`
- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `mvn.cmd -q "-Dtest=QueueCommandServiceTest" test`
- [x] `gitnexus detect-changes` 시도 후 CLI 미지원 확인
- [ ] 백엔드 서버 기동
- [ ] 실제 스케줄 실행 로그 확인
- [ ] Swagger 수동 마감 API와 충돌 없음 확인

단위 테스트 참고:

- Mockito inline mock maker 경고와 SLF4J provider 중복 경고가 표시됐지만 테스트는 통과했다.

## 8. 런타임 확인 기준

사용자가 백엔드를 실행한 뒤 설정을 임시로 짧게 바꿔 스케줄 동작을 확인한다.

예시 환경변수:

```text
QUEUE_AUTO_CLOSE_ENABLED=true
QUEUE_AUTO_CLOSE_CRON=0 */1 * * * *
QUEUE_AUTO_CLOSE_RETENTION_DAYS=2
```

기대 로그:

```text
event=queue.pending_auto_closed ... cutoffDate=YYYY-MM-DD retentionDays=2 closedCount=N
event=queue.pending_auto_close_scheduler_completed ... closedCount=N
```

Swagger 대표 확인:

```text
GET /api/queues?date=YYYY-MM-DD&status=NO_SHOW&limit=100
```

기대 결과:

- 자동 마감 기준일 이전 미처리 대기표가 `NO_SHOW`로 조회된다.
- 이미 `COMPLETED`, `NO_SHOW`, `CANCELED`인 대기표는 변경되지 않는다.

## 9. 사용자 코드 점검 결과

| 점검 시점 | 사용자 의견 | 반영 여부 |
|---|---|---|
|  |  |  |

## 10. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 구현 중 | 휴무일/예외일 제외 정책 | 운영일 기준이 아직 문서화되어 있지 않음 | 후속 고도화 후보 |
| 구현 중 | dry-run/count 전용 운영 API | 운영자가 배치 대상 건수를 사전에 확인할 수 있음 | 후속 고도화 후보 |
| 구현 중 | 배치 실행 이력 테이블 | 자동 변경 감사 추적성을 높일 수 있음 | 후속 고도화 후보 |

## 11. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] 보류 목록 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리

## 12. 커밋 메시지 초안

```text
feat: 미처리 대기표 자동 노쇼 배치 추가

- Spring Scheduler 기반 과거 미처리 대기표 자동 마감 추가
- 실행일 기준 보관 일수 이전 대기표를 NO_SHOW로 일괄 처리
- Visit과 CHECKED_IN 예약 상태를 기존 수동 마감 규칙으로 동기화
- 배치 설정, 단위 테스트, 문서와 체크리스트 갱신
```
