# Observability Compose Stack Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `chore/observability-compose-stack` |
| base 브랜치 | `dev` |
| 작업 트리 | Prometheus/Grafana/Loki/Promtail Compose profile, 설정 파일, 문서 갱신 |
| 주요 커밋 | 커밋 전 |
| 빌드 확인 | `mvn.cmd -q -DskipTests compile` 통과 |
| 테스트 확인 | `mvn.cmd -q test-compile` 통과 |
| 실행/API 확인 | Docker 실행, Prometheus/Grafana/Loki 브라우저 확인은 사용자 직접 수행 |

## PR 제목

```text
chore: 운영 관측성 Compose 스택 추가
```

## PR 본문

```markdown
## 개요

Prometheus, Grafana, Loki, Promtail을 Docker Compose `observability` profile로 추가했습니다.

이전 Actuator/Micrometer 작업에서 노출한 `/actuator/prometheus`를 Prometheus가 수집하고, Grafana에서 backend 메트릭과 로그를 함께 확인할 수 있는 1차 운영 관측성 스택입니다.

## 변경 내용

- Docker Compose 관측성 profile 추가
  - Prometheus
  - Grafana
  - Loki
  - Promtail
- Prometheus 설정 추가
  - `backend:8080/actuator/prometheus` scrape job
  - Prometheus self scrape
- Grafana provisioning 추가
  - Prometheus datasource
  - Loki datasource
  - backend overview dashboard
- Loki/Promtail 로그 수집 설정 추가
  - Docker container log read-only 수집
- backend 보안 설정 보강
  - 기본값은 `/actuator/prometheus` ADMIN 보호
  - `OBSERVABILITY_PROMETHEUS_SCRAPE_ENABLED=true`일 때 Prometheus scrape 허용
- 문서 갱신
  - README
  - backend README
  - 전체 체크리스트
  - 보류/고도화 목록
  - 구현 기록

## 검증

- [x] `git diff --check`
- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [ ] `docker compose --env-file .env --profile observability config` 확인
- [ ] Docker Compose 관측성 profile 실행
- [ ] Prometheus target `health-center-backend` UP 확인
- [ ] Grafana datasource Prometheus/Loki 연결 확인
- [ ] Grafana `Health Center Backend Overview` dashboard 확인
- [ ] Loki backend log query 확인

## 런타임 확인 기준

PowerShell:

```powershell
$env:OBSERVABILITY_PROMETHEUS_SCRAPE_ENABLED="true"
docker compose --env-file .env --profile observability up -d --build
```

확인 URL:

```text
Prometheus: http://localhost:9090
Grafana:    http://localhost:3001
Loki:       http://localhost:3100
```

대표 확인:

```text
Prometheus UI > Status > Targets > health-center-backend
```

기대 결과:

```text
health-center-backend target 상태가 UP이다.
```

## 추가 테스트 체크리스트

- [ ] Happy: Prometheus target이 UP이다.
- [ ] Happy: Prometheus에서 `http_server_requests_seconds_count` 쿼리가 조회된다.
- [ ] Happy: Grafana datasource가 자동 등록된다.
- [ ] Happy: Grafana dashboard 패널이 표시된다.
- [ ] Happy: Loki datasource가 연결된다.
- [ ] Edge: scrape 허용 환경변수가 false이면 Prometheus scrape가 차단된다.
- [ ] Bad: 외부 공개 환경에서 Grafana/Prometheus/Loki 포트를 열지 않는다.

## 미검증 사유

- 프로젝트 운영 기준상 Docker 실행과 브라우저 런타임 확인은 사용자가 직접 수행한다.
- Promtail은 Docker socket과 container log 경로를 읽으므로 Windows Docker Desktop, Ubuntu VM, 권한 설정에 따라 추가 확인이 필요하다.
- 운영용 인증, 내부망 분리, Cloudflare Tunnel 공개 여부는 이번 브랜치 범위에서 제외한다.

## 후속 작업

- k6 예약/방문/대기/대시보드 부하 테스트 시나리오 작성
- 운영용 management port 또는 내부망 scrape 정책 고도화
- Grafana dashboard 패널 확장
- Logback JSON encoder와 Loki label 정규화 검토
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] k6 후속 브랜치 생성 여부 결정

## 커밋 메시지 초안

제목:

```text
chore: 운영 관측성 Compose 스택 추가
```

본문:

```text
- Prometheus, Grafana, Loki, Promtail Compose profile 추가
- Prometheus backend Actuator scrape 설정 추가
- Grafana datasource와 기본 backend dashboard provisioning 추가
- Prometheus scrape 허용 환경변수와 문서 갱신
```
