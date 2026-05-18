# Admin Dashboard Localtime Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `fix/admin-dashboard-localtime` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | DataSource, application properties, Docker Compose, env example, README/배포/브랜치 문서 변경 있음 |
| 주요 커밋 | 아직 없음 |
| Maven compile | `mvn.cmd -q -DskipTests compile` 통과 |
| Maven test-compile | `mvn.cmd -q test-compile` 통과 |
| 정적 공백 확인 | `git diff --check` 통과. LF/CRLF 경고만 표시 |
| GitNexus 변경 감지 | `gitnexus detect_changes`는 현재 CLI에 없는 명령이라 실패, `git status`/`git diff --stat`로 보완 |
| 실행/API 확인 | 미수행. 사용자가 Docker/Spring Boot Dashboard/Swagger/브라우저로 직접 확인 필요 |

## PR 제목

```text
fix: 접수 시간 한국시간 저장 기준 보정
```

## PR 본문

```markdown
## 개요

직원이 예약자 체크인 또는 현장 접수를 처리했을 때 관리자 대시보드 시간대별 방문자 수가 실제 한국시간 접수 시간과 다르게 집계되는 문제를 보정합니다.

방문 저장은 `visits.checked_in_at = CURRENT_TIMESTAMP`, 대시보드 조회는 `EXTRACT(HOUR FROM visits.checked_in_at)` 기준입니다. 따라서 DB 세션/JVM/container 시간대를 `Asia/Seoul` 기본값으로 맞춰, 한국시간 09:25 접수는 `9~10시` 구간에 집계되도록 합니다.

## 변경 내용

- `Globals.TimeZone=${APP_TIME_ZONE:Asia/Seoul}` 설정 추가
- DataSource 초기화 시 JVM 기본 시간대를 `APP_TIME_ZONE` 기준으로 설정
- BasicDataSource 연결 초기화 SQL로 `SET TIME ZONE` 적용
- Docker Compose에서 PostgreSQL server/session, backend JVM, frontend container 시간대 기본값을 `Asia/Seoul`로 정렬
- `.env.example`에 `APP_TIME_ZONE`, `DB_TIME_ZONE`, `JAVA_TOOL_OPTIONS` 추가
- README와 배포 전 체크리스트에 시간대 환경변수 확인 기준 추가
- 구현 기록과 PR 문서에 저장/조회 기준 및 Swagger 확인 방법 기록

## 검증

- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `git diff --check`
- [x] `git status`, `git diff --stat` 변경 범위 확인
- [ ] Swagger `GET /api/dashboard/hourly-visits?date=2026-05-18` 대표 예시 확인
- [ ] 직원 현장 접수 후 해당 한국시간 hour의 `visitCount` 증가 확인
- [ ] 브라우저 `/admin/dashboard` 시간대별 방문자 수 차트 확인

## Swagger 대표 예시

`GET /api/dashboard/hourly-visits?date=2026-05-18`

기대 결과:

- `success: true`
- `data`가 0시부터 23시까지 24개 항목 포함
- 한국시간 09:25 접수 후 `hour: 9` 항목의 `visitCount` 증가

## 추가 테스트 체크리스트

- [ ] Happy: 현장 접수 직후 한국시간 현재 hour에 방문자 수 증가
- [ ] Happy: 예약자 체크인 직후 한국시간 현재 hour에 방문자 수 증가
- [ ] Edge: `date` 파라미터 생략 시 한국시간 오늘 날짜 기준 조회
- [ ] Edge: 자정 전후 접수 시 한국시간 날짜 기준으로 집계
- [ ] Bad: ADMIN이 아닌 계정으로 Dashboard API 접근 시 403 처리

## 미검증 사유

- 서버 기동, Docker 실행, Swagger Try it out, 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- 기존 운영 DB에 이미 UTC처럼 저장된 과거 데이터 보정은 이번 브랜치 범위에서 제외했습니다.

## 남은 위험

- 기존 볼륨/운영 DB의 과거 데이터가 이미 잘못된 시각으로 저장되어 있으면 별도 보정 SQL 또는 데이터 초기화가 필요합니다.
- 외부 DB를 사용할 경우 DB 서버 정책에 따라 `SET TIME ZONE` 권한/적용 여부를 런타임에서 확인해야 합니다.

## 후속 작업

- 필요 시 기존 `visits.checked_in_at`, `queue_tickets.issued_at` 데이터 보정 기준 결정
- 운영 배포 후 `SHOW TIMEZONE`, `CURRENT_TIMESTAMP`, Dashboard Swagger 결과 확인
```

## 커밋 메시지 초안

```text
fix: 접수 시간 한국시간 저장 기준 보정

- DataSource 연결 초기화 시 PostgreSQL 세션 timezone 설정
- backend JVM과 Docker Compose 시간대 기본값을 Asia/Seoul로 정렬
- 대시보드 시간대별 방문자 수 검증 기준 문서화
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] Swagger/브라우저 확인 결과 필요 시 구현 기록에 추가 반영
