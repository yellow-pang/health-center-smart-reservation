# 미처리 대기표 자동 NO_SHOW 배치 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/auto-close-pending-queue-batch` |
| base 브랜치 | `dev` |
| 작업 트리 | 자동 마감 배치, Queue 서비스/Mapper/XML, 설정, 단위 테스트, API 문서, 구현 기록, 체크리스트 갱신 |
| 주요 커밋 | 커밋 전 |
| 빌드 확인 | `mvn.cmd -q -DskipTests compile` 통과 |
| 테스트 확인 | `mvn.cmd -q test-compile`, `mvn.cmd -q "-Dtest=QueueCommandServiceTest" test`, `git diff --check` 통과 |
| 실행/API 확인 | 서버 기동, 실제 스케줄 실행 로그, Swagger 확인은 사용자가 직접 확인 |

## PR 제목

```text
feat: 미처리 대기표 자동 노쇼 배치 추가
```

## PR 본문

```markdown
## 개요

관리자가 수동 마감을 놓친 과거 미처리 대기표가 계속 활성 대기 데이터로 남지 않도록 Spring Scheduler 기반 자동 NO_SHOW 배치를 추가합니다.

기본 정책은 실행일 기준 2일 전까지 발급된 `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS` 대기표를 `NO_SHOW` 처리하는 것입니다. 기존 수동 마감 API와 같은 Visit/Reservation 동기화 규칙을 사용합니다.

## 변경 내용

- 자동 마감 스케줄러 추가
  - `QUEUE_AUTO_CLOSE_ENABLED=true`
  - `QUEUE_AUTO_CLOSE_CRON=0 10 18 * * *`
  - `QUEUE_AUTO_CLOSE_RETENTION_DAYS=2`
- 자동 마감 서비스 로직 추가
  - 실행일 기준 보관 일수 이전 대기표 대상
  - QueueTicket/Visit `NO_SHOW` 처리
  - 예약 기반 방문의 `CHECKED_IN` 예약 `NO_SHOW` 처리
- 자동 마감 MyBatis SQL 추가
- 자동 마감 결과 로그 추가
  - `event=queue.pending_auto_closed`
  - `event=queue.pending_auto_close_scheduler_completed`
- `.env.example`과 `docker-compose.yml`에 `QUEUE_AUTO_CLOSE_*` 설정 반영
- 자동 마감 서비스 단위 테스트 추가
- API 명세, 보류 목록, 전체 체크리스트, 구현 기록 갱신

## 검증

- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `mvn.cmd -q "-Dtest=QueueCommandServiceTest" test`
- [x] `git diff --check`
- [ ] 서버 기동 후 스케줄러 bean 생성 확인
- [ ] 임시 cron으로 `event=queue.pending_auto_closed` 로그 확인
- [ ] Swagger `GET /api/queues?date=YYYY-MM-DD&status=NO_SHOW&limit=100`로 자동 마감 결과 확인
- [ ] Swagger `POST /api/queues/admin/close-pending?date=YYYY-MM-DD` 수동 마감 API 기존 동작 확인

## 런타임 확인 예시

임시 환경변수:

```text
QUEUE_AUTO_CLOSE_ENABLED=true
QUEUE_AUTO_CLOSE_CRON=0 */1 * * * *
QUEUE_AUTO_CLOSE_RETENTION_DAYS=2
```

Swagger 확인:

```text
GET /api/queues?date=YYYY-MM-DD&status=NO_SHOW&limit=100
```

기대 결과:

- 기준일 이전 미처리 대기표가 `NO_SHOW`로 조회됩니다.
- 이미 `COMPLETED`, `NO_SHOW`, `CANCELED`인 대기표는 변경되지 않습니다.

## 미검증 사유

- 프로젝트 운영 기준상 서버 기동, Docker 실행, API 런타임 호출, Swagger/브라우저 확인은 사용자가 직접 수행합니다.
- 실제 스케줄 실행은 서버 런타임과 시간이 필요하므로 정적 검증과 단위 테스트까지만 에이전트가 확인했습니다.

## 후속 작업

- 휴무일/예외일 제외 정책 검토
- dry-run/count 전용 운영 API 검토
- 배치 실행 이력 테이블 또는 감사 로그 테이블 검토
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 서버 실행 환경에서 기본 cron 설정 확인
- [ ] 운영 환경에서 `QUEUE_AUTO_CLOSE_ENABLED` 설정값 확인
- [ ] 전체 체크리스트 후속 후보 갱신

## 커밋 메시지 초안

제목:

```text
feat: 미처리 대기표 자동 노쇼 배치 추가
```

본문:

```text
- Spring Scheduler 기반 과거 미처리 대기표 자동 마감 추가
- 실행일 기준 보관 일수 이전 대기표를 NO_SHOW로 일괄 처리
- Visit과 CHECKED_IN 예약 상태를 기존 수동 마감 규칙으로 동기화
- 배치 설정, 단위 테스트, 문서와 체크리스트 갱신
```
