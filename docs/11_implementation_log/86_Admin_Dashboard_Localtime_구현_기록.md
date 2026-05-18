# Admin Dashboard Localtime 구현 기록

## 1. 작업 목표

- 직원이 예약자 체크인 또는 현장 접수를 처리한 시각이 한국시간 기준으로 DB에 저장되도록 보정한다.
- 관리자 대시보드의 시간대별 방문자 수가 `visits.checked_in_at`의 한국시간 hour 기준으로 집계되도록 한다.
- 이번 브랜치에서는 시간 컬럼 타입 변경이나 기존 데이터 마이그레이션 없이 애플리케이션/DB 세션 시간대 설정만 작게 정리한다.

## 2. 이번 브랜치 작업 범위 제안

- 포함:
  - [x] `docs/13_schedule/02_전체_작업_체크리스트.md`에서 Dashboard/Visit/DB/운영 항목 확인
  - [x] `POST /api/visits/check-in`, `POST /api/visits/walk-in` 저장 시각 확인
  - [x] `GET /api/dashboard/hourly-visits` 조회 기준 확인
  - [x] backend JVM 기본 시간대와 PostgreSQL 세션 시간대를 `Asia/Seoul` 기본값으로 정렬
  - [x] Docker Compose와 `.env.example`에 시간대 환경변수 추가
  - [x] Maven compile/test-compile 및 `git diff --check` 확인
  - [x] 브랜치 기록과 PR 문서 초안 작성

- 제외:
  - [ ] 기존 운영 DB에 이미 UTC처럼 저장된 과거 데이터 보정
  - [ ] `TIMESTAMP` 컬럼을 `TIMESTAMPTZ`로 바꾸는 DB 마이그레이션
  - [ ] 서버 기동, Docker 실행, Swagger `Try it out`, 브라우저 확인

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/06_dashboard/01_대시보드_지표_정의서.md` 확인
- [x] `docs/04_api/01_API_명세서.md` 대시보드 API 기준 확인
- [x] `docs/03_database/01_ERD_및_테이블_명세서.md` visits/queue_tickets 시간 컬럼 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] GitNexus impact 확인

## 4. 영향 범위

GitNexus 인덱스 상태:

- `gitnexus status`: stale 경고
- `gitnexus analyze`: `Not inside a git repository` 오류로 실패
- 보완: `--repo health-center-smart-reservation` 옵션으로 impact 분석을 수행하고, `rg`/파일 직접 확인/Maven 검증으로 보완

Impact 결과:

| 대상 | risk | direct callers | affected processes |
|---|---|---:|---|
| `insertWalkInVisit` | LOW | 1 | `VisitController.walkIn` |
| `insertReservedVisit` | LOW | 1 | `VisitController.checkIn` |
| `issueWaitingTicket` | LOW | 2 | `VisitController.checkIn`, `VisitController.walkIn` |
| `selectHourlyVisits` | LOW | 1 | `DashboardController.findHourlyVisits` |
| `basicDataSource` | LOW | 1 | 공통 DataSource 생성 |

HIGH/CRITICAL 위험은 없었다.

## 5. 저장/조회 기준 확인

저장 기준:

- 예약자 체크인: `Visit_SQL_postgresql.xml`의 `insertReservedVisit`에서 `visits.checked_in_at = CURRENT_TIMESTAMP`
- 현장 접수: `Visit_SQL_postgresql.xml`의 `insertWalkInVisit`에서 `visits.checked_in_at = CURRENT_TIMESTAMP`
- 대기표 발급: `QueueTicket_SQL_postgresql.xml`의 `issueWaitingTicket`에서 `queue_tickets.issued_at = CURRENT_TIMESTAMP`

조회 기준:

- 관리자 시간대별 방문자 수: `Dashboard_SQL_postgresql.xml`의 `selectHourlyVisits`
- `v.checked_in_at::date = #{targetDate}`
- `EXTRACT(HOUR FROM v.checked_in_at)` 기준으로 0시부터 23시까지 집계

원인 판단:

- `CURRENT_TIMESTAMP`, `CURRENT_DATE`, `LocalDate.now()`가 실행 환경의 기본 시간대를 따르는데, 컨테이너/JVM/DB 세션 기준이 UTC이면 한국시간 09:25 접수가 대시보드에서 다른 시간대로 집계될 수 있다.
- `visits.checked_in_at` 자체가 집계 기준이므로 저장 세션 시간대를 한국시간으로 맞추는 것이 가장 작은 수정이다.

## 6. 구현 내용

수정 파일:

- `backend/src/main/resources/application.properties`
- `backend/src/main/java/egovframework/com/config/EgovConfigAppDatasource.java`
- `docker-compose.yml`
- `.env.example`
- `README.md`
- `docs/08_deploy/02_Ubuntu_VM_Jenkins_Docker_Compose_배포_계획서.md`
- `docs/08_deploy/03_dev_to_main_배포전_확인_체크리스트.md`

변경 내용:

- `Globals.TimeZone=${APP_TIME_ZONE:Asia/Seoul}` 추가
- DataSource 초기화 시 JVM 기본 시간대를 `Globals.TimeZone` 기준으로 설정
- BasicDataSource connection init SQL에 `SET TIME ZONE 'Asia/Seoul'` 기본 적용
- Docker Compose에서 PostgreSQL server timezone, backend JVM timezone, frontend/container timezone 기본값을 `Asia/Seoul`로 지정
- `.env.example`에 `APP_TIME_ZONE`, `DB_TIME_ZONE`, `JAVA_TOOL_OPTIONS` 예시 추가
- README와 배포 문서에 시간대 환경변수 확인 기준 추가

기대 효과:

- 새 DB 연결에서 `CURRENT_TIMESTAMP`와 `CURRENT_DATE`가 한국시간 세션 기준으로 평가된다.
- Dashboard API에서 `date`를 생략할 때 사용하는 `LocalDate.now()`도 한국시간 기준이 된다.
- 예: 한국시간 09:25에 현장 접수하면 `checked_in_at`이 09시대 값으로 저장되고, `GET /api/dashboard/hourly-visits`에서는 `hour: 9` 항목의 `visitCount`가 증가해야 한다.

## 7. 검증 결과

| 검증 | 결과 |
|---|---|
| `mvn.cmd -q -DskipTests compile` | 통과 |
| `mvn.cmd -q test-compile` | 통과 |
| `git diff --check` | 통과. LF/CRLF 경고만 표시 |
| `gitnexus detect_changes` | 실패. 현재 CLI에 없는 명령으로 확인되어 `git status`, `git diff --stat`, `git diff --check`로 보완 |
| 서버 기동 | 미수행. 사용자 직접 수행 범위 |
| Docker 실행 | 미수행. 사용자 직접 수행 범위 |
| Swagger/API 런타임 확인 | 미수행. 사용자 직접 수행 범위 |
| 브라우저 확인 | 미수행. 사용자 직접 수행 범위 |

## 8. 사용자 직접 확인 방법

Swagger 대표 예시:

`GET /api/dashboard/hourly-visits?date=2026-05-18`

확인 순서:

1. 사용자가 PostgreSQL, 백엔드, 프론트엔드를 실행한다.
2. `staff@test.com / password1234`로 로그인해 현장 접수 또는 예약자 체크인을 처리한다.
3. `admin@test.com / password1234`로 로그인한다.
4. Swagger에서 `GET /api/dashboard/hourly-visits?date=2026-05-18`를 `Try it out`으로 실행한다.

기대 결과:

- `success: true`
- `data`가 0시부터 23시까지 24개 항목 포함
- 한국시간 09:25에 접수했다면 `hour: 9` 항목의 `visitCount`가 증가
- 관리자 대시보드 차트에서도 `09시` 구간 방문자 수 증가

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 원인 확인 중 | 기존 UTC 저장 데이터 보정 여부 결정 | 이미 잘못된 시간으로 저장된 운영 데이터는 설정 수정만으로 자동 보정되지 않음 | 이번 브랜치 제외, 필요 시 별도 마이그레이션 |
| GitNexus 확인 중 | `gitnexus analyze` 실행 실패 원인 확인 | `git status`는 동작하지만 GitNexus analyze가 repo 판정을 실패함 | impact는 `--repo`로 수행, 변경 범위 확인은 일반 명령으로 보완 |

## 10. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] PR 문서 작성
- [x] 전체 체크리스트 갱신
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
- [ ] Swagger Dashboard 대표 예시 확인
- [ ] 브라우저 관리자 대시보드 화면 확인

## 11. 커밋 메시지 초안

```text
fix: 접수 시간 한국시간 저장 기준 보정

- DataSource 연결 초기화 시 PostgreSQL 세션 timezone 설정
- backend JVM과 Docker Compose 시간대 기본값을 Asia/Seoul로 정렬
- 대시보드 시간대별 방문자 수 검증 기준 문서화
```
