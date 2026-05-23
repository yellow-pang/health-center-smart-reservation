# Observability Compose Stack 구현 기록

## 1. 작업 목표

- `chore/observability-compose-stack` 브랜치에서 Prometheus, Grafana, Loki, Promtail 기반 관측성 스택을 Docker Compose profile로 추가한다.
- Actuator/Micrometer로 노출한 backend 메트릭을 Prometheus가 수집하고, Grafana에서 메트릭과 로그를 함께 볼 수 있게 한다.
- 서버 기동, Docker 실행, Grafana/Prometheus 브라우저 확인은 사용자가 직접 수행할 수 있도록 기준만 문서화한다.

## 2. 작업 범위

- [x] 현재 브랜치 `chore/observability-compose-stack` 확인
- [x] 이전 Actuator/Micrometer 구현 기록 확인
- [x] Prometheus Compose 서비스 추가
- [x] Prometheus scrape 설정 추가
- [x] Grafana Compose 서비스 추가
- [x] Prometheus/Loki datasource provisioning 추가
- [x] 기본 backend overview dashboard 추가
- [x] Loki Compose 서비스 추가
- [x] Promtail Compose 서비스와 Docker log 수집 설정 추가
- [x] Prometheus scrape 허용 환경변수 추가
- [x] README, backend README, 전체 체크리스트, 보류 목록 갱신
- [x] PR 문서 초안 작성

제외한다.

- [ ] Docker Compose 실행
- [ ] Prometheus target runtime 확인
- [ ] Grafana dashboard 브라우저 확인
- [ ] Loki log query runtime 확인
- [ ] Cloudflare Tunnel을 통한 Grafana 외부 공개
- [ ] k6 부하 테스트 시나리오 작성
- [ ] 운영용 인증/네트워크 분리 고도화

## 3. 작업 전 체크리스트

- [x] `docs/11_implementation_log/124_Backend_Observability_Actuator_구현_기록.md` 확인
- [x] `docs/11_implementation_log/125_Backend_Observability_Actuator_PR_작성안.md` 확인
- [x] `docker-compose.yml` 현재 상태 확인
- [x] 관련 환경변수 확인
- [x] 영향받는 `SecurityConfig`, `filterChain` GitNexus impact 확인

## 4. 영향도 확인

| 대상 | GitNexus 결과 | 판단 |
|---|---|---|
| `SecurityConfig` | impactedCount 0, direct callers 0, affected processes 0, risk LOW | Prometheus scrape 허용 옵션 추가 |
| `filterChain` | impactedCount 0, direct callers 0, affected processes 0, risk LOW | `/actuator/prometheus`만 환경변수 기반 scrape 허용 또는 ADMIN 보호 |

## 5. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `docker-compose.yml` | `observability` profile에 Prometheus, Grafana, Loki, Promtail 서비스 추가 |
| `.env.example` | 관측성 포트, Grafana 계정, Prometheus scrape 허용 환경변수 추가 |
| `backend/src/main/java/egovframework/com/security/SecurityConfig.java` | `OBSERVABILITY_PROMETHEUS_SCRAPE_ENABLED=true`일 때 `/actuator/prometheus` scrape 허용 |
| `backend/src/main/resources/application.properties` | `Observability.Prometheus.ScrapeEnabled` 설정 추가 |
| `infra/observability/prometheus/prometheus.yml` | backend `/actuator/prometheus` scrape job 추가 |
| `infra/observability/grafana/provisioning/datasources/datasources.yml` | Prometheus, Loki datasource 자동 등록 |
| `infra/observability/grafana/provisioning/dashboards/dashboards.yml` | dashboard 파일 provisioning 설정 |
| `infra/observability/grafana/dashboards/backend-overview.json` | HTTP request rate, 평균 latency, JVM heap, error log 패널 추가 |
| `infra/observability/loki/loki-config.yml` | 단일 노드 filesystem 기반 Loki 설정 |
| `infra/observability/promtail/promtail-config.yml` | Docker container log 수집 설정 |
| `README.md`, `backend/README.md` | 관측성 profile 실행 기준과 확인 URL 추가 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | Prometheus/Grafana/Loki Compose 스택 완료 반영 |
| `docs/14_deferred_cleanup/01_보류_정리_목록.md` | `DC-019`, `DC-020` 정리 완료 반영 |

## 6. 운영 결정

| 항목 | 결정 |
|---|---|
| Compose 실행 방식 | 기본 앱 실행과 분리하기 위해 `observability` profile 사용 |
| Prometheus scrape endpoint | `backend:8080/actuator/prometheus` |
| Prometheus scrape 인증 | 기본값은 보호, `OBSERVABILITY_PROMETHEUS_SCRAPE_ENABLED=true`일 때 scrape 허용 |
| Grafana port | 기본 `3001` |
| Grafana datasource | Prometheus와 Loki 자동 등록 |
| Dashboard | backend overview 1개 기본 제공 |
| 로그 수집 | Promtail이 Docker socket과 container log 경로를 read-only로 읽음 |

운영 공개 환경에서는 `OBSERVABILITY_PROMETHEUS_SCRAPE_ENABLED=false`를 유지한다.
로컬 또는 내부 Docker 네트워크에서 관측성 스택을 확인할 때만 true로 연다.

## 7. 검증 체크리스트

- [x] `git diff --check`
- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [ ] `docker compose --env-file .env --profile observability config` 확인
- [ ] Docker Compose 관측성 profile 실행
- [ ] Prometheus target `health-center-backend` UP 확인
- [ ] Grafana datasource Prometheus/Loki 연결 확인
- [ ] Grafana `Health Center Backend Overview` dashboard 확인
- [ ] Loki backend error log query 확인

## 8. 사용자 확인 방법

서버 기동과 Docker 실행은 사용자가 직접 수행한다.

PowerShell 예시:

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

대표 확인 1개:

```text
Prometheus UI > Status > Targets > health-center-backend
```

기대 결과:

```text
health-center-backend target 상태가 UP이다.
```

## 9. 추가 테스트 체크리스트

- [ ] Happy: Prometheus에서 `health-center-backend` target이 UP이다.
- [ ] Happy: Prometheus에서 `http_server_requests_seconds_count` 쿼리가 조회된다.
- [ ] Happy: Grafana 기본 datasource가 Prometheus로 설정된다.
- [ ] Happy: Grafana dashboard에서 HTTP request rate와 JVM heap 패널이 표시된다.
- [ ] Happy: Loki datasource가 연결된다.
- [ ] Edge: `OBSERVABILITY_PROMETHEUS_SCRAPE_ENABLED=false`일 때 Prometheus scrape가 실패하거나 인증 차단된다.
- [ ] Bad: 외부 공개 환경에서는 Grafana/Prometheus/Loki 포트를 공개하지 않는다.

## 10. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| Compose 스택 구성 중 | 운영용 Prometheus 인증/네트워크 분리 | scrape를 위해 `/actuator/prometheus`를 열면 외부 공개 환경에서 메트릭 노출 위험이 있다. | 기본값 false 유지, 내부 확인 시에만 true. 운영용 management port/내부망 분리는 후속 |
| Compose 스택 구성 중 | k6 시나리오 작성 | 관측성 스택이 붙으면 예약/방문/대기 부하를 걸며 대시보드를 확인할 수 있다. | `test/k6-queue-load-scenarios` 후속 후보 |
| Compose 스택 구성 중 | Promtail 권한 확인 | Docker Desktop/Ubuntu VM 환경에 따라 `/var/lib/docker/containers`와 docker.sock 접근 권한이 다르다. | 사용자 런타임 체크리스트로 남김 |

## 11. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리

## 12. 커밋 메시지 초안

```text
chore: 운영 관측성 Compose 스택 추가

- Prometheus, Grafana, Loki, Promtail Compose profile 추가
- Prometheus backend Actuator scrape 설정 추가
- Grafana datasource와 기본 backend dashboard provisioning 추가
- Prometheus scrape 허용 환경변수와 문서 갱신
```
