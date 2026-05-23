# Observability Config Image Mounts 구현 기록

## 1. 작업 목표

- Jenkins 배포 단계에서 Prometheus 설정 파일 bind mount가 실패한 원인을 구조적으로 해결한다.
- Jenkins 컨테이너 내부 workspace 경로를 Docker host bind mount source로 직접 사용하지 않도록 관측성 설정 파일을 각 이미지에 포함한다.
- 나중에 문제 해결 과정으로 정리할 수 있도록 원인, 선택지, 최종 구조를 문서화한다.

## 2. 발생 증상

Jenkins 배포 단계에서 아래 오류가 발생했다.

```text
error mounting "/var/jenkins_home/workspace/health-center-deploy/infra/observability/prometheus/prometheus.yml"
to rootfs at "/etc/prometheus/prometheus.yml": not a directory
Are you trying to mount a directory onto a file (or vice-versa)?
```

## 3. 원인 분석

Jenkins는 컨테이너 안에서 실행되고 Docker daemon은 VM host의 `/var/run/docker.sock`을 통해 실행된다.

```text
Jenkins 컨테이너가 보는 workspace:
/var/jenkins_home/workspace/health-center-deploy

Docker daemon이 bind mount source로 해석하는 경로:
VM host의 /var/jenkins_home/workspace/health-center-deploy
```

Jenkins home이 Docker named volume이면 VM host의 동일 절대 경로에 실제 checkout 파일이 없을 수 있다. 이 상태에서 Docker Compose가 아래처럼 파일 bind mount를 시도하면 Docker가 없는 source 경로를 디렉터리로 만들거나 잘못 해석해 파일-디렉터리 타입 불일치 오류가 난다.

```yaml
volumes:
  - ./infra/observability/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
```

## 4. 해결 방향 비교

| 방식 | 장점 | 단점 | 판단 |
|---|---|---|---|
| Jenkins home을 VM host bind mount로 변경 | Jenkins workspace와 Docker host 경로를 맞출 수 있음 | Jenkins 운영 구조를 바꾸고 기존 volume 이관이 필요 | 보류 |
| Compose에서 절대 host path를 별도 변수로 관리 | host path를 명시할 수 있음 | VM마다 경로 관리가 필요하고 Jenkins workspace 의존이 남음 | 보류 |
| 관측성 설정 파일을 custom image에 포함 | Jenkins workspace bind mount 의존 제거, 배포 재현성 향상 | 설정 변경 시 image rebuild 필요 | 선택 |

현재 프로젝트는 포트폴리오용 개인 VM/소규모 배포 환경이므로 설정 파일을 이미지에 포함하는 방식의 장점이 더 크다고 판단했다.

```text
1. Docker bind mount 경로 문제를 줄인다.
2. Jenkins workspace와 Docker host 경로 차이를 덜 신경 써도 된다.
3. docker compose 배포가 더 단순해진다.
4. 배포 결과가 image 기준으로 재현 가능해진다.
```

다만 이 방식은 설정 파일 수정 시 image rebuild가 필요하다. 현재 Jenkins Pipeline은 `docker compose --profile observability build`를 수행하므로 Prometheus/Grafana/Loki/Promtail 설정 변경도 다음 Jenkins 배포에서 image rebuild 대상에 포함된다.

## 5. 작업 범위

- [x] `fix/observability-config-image-mounts` 브랜치에서 진행
- [x] Prometheus 설정 파일을 image에 포함
- [x] Loki 설정 파일을 image에 포함
- [x] Promtail 설정 파일을 image에 포함
- [x] Grafana provisioning/dashboard 파일을 image에 포함
- [x] Compose의 관측성 설정 파일 bind mount 제거
- [x] Promtail의 Docker log 수집용 host mount는 유지
- [x] 구현 기록과 PR 문서 작성

제외한다.

- [ ] Docker Compose/Jenkins Pipeline 직접 실행
- [ ] Prometheus/Grafana/Loki 브라우저 확인
- [ ] Jenkins home volume 구조 변경
- [ ] 환경별 관측성 설정 외부화

## 6. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `infra/observability/prometheus/Dockerfile` | `prom/prometheus:v2.55.1` 기반으로 `prometheus.yml` COPY |
| `infra/observability/loki/Dockerfile` | `grafana/loki:3.2.1` 기반으로 `loki-config.yml` COPY |
| `infra/observability/promtail/Dockerfile` | `grafana/promtail:3.2.1` 기반으로 `promtail-config.yml` COPY |
| `infra/observability/grafana/Dockerfile` | `grafana/grafana:11.3.0` 기반으로 provisioning/dashboard COPY |
| `docker-compose.yml` | 관측성 서비스 `image`를 local custom image로 변경하고 `build` 추가 |
| `docker-compose.yml` | Prometheus/Loki/Promtail/Grafana 설정 파일 bind mount 제거 |
| `infra/observability/grafana/provisioning/dashboards/dashboards.yml` | dashboard provider path를 `/etc/grafana/dashboards`로 변경 |
| `docs/11_implementation_log/126_Observability_Compose_Stack_구현_기록.md` | 설정 파일 이미지 포함 방식 반영 |

## 7. 유지한 bind mount

Promtail의 아래 mount는 유지했다.

```yaml
- /var/lib/docker/containers:/var/lib/docker/containers:ro
- /var/run/docker.sock:/var/run/docker.sock:ro
```

이 mount는 설정 파일 주입이 아니라 Docker container log discovery와 log file read를 위한 VM host runtime 경로다. Jenkins workspace와 무관하므로 이번 오류의 원인과 다르다.

## 8. 검증 체크리스트

- [x] `git diff --check`
- [ ] Jenkins Pipeline 실행
- [ ] Docker Compose build 단계에서 custom observability image build 확인
- [ ] Deploy 단계에서 bind mount 오류가 사라졌는지 확인
- [ ] VM에서 관측성 컨테이너 확인
- [ ] Prometheus target 확인
- [ ] Grafana dashboard 확인
- [ ] Loki log query 확인

## 9. 사용자 확인 방법

Jenkins Pipeline 실행 후 VM에서 확인한다.

```bash
docker compose --env-file .env --profile observability ps
```

기대 컨테이너:

```text
health-center-prometheus
health-center-grafana
health-center-loki
health-center-promtail
```

이번 오류가 해결됐는지 보려면 Jenkins 로그에서 아래 형태의 bind mount 오류가 다시 나오지 않아야 한다.

```text
error mounting ".../infra/observability/prometheus/prometheus.yml"
```

## 10. 남은 위험

- Promtail은 VM Docker host의 `/var/lib/docker/containers` 접근 권한이 필요하다.
- Grafana provisioning 파일은 이미지에 포함되므로 변경 후에는 `docker compose build grafana` 또는 Jenkins build가 다시 수행되어야 한다.
- Prometheus scrape를 위해 Jenkins Credentials의 `.env`에 `OBSERVABILITY_PROMETHEUS_SCRAPE_ENABLED=true`가 필요하다.
- VM마다 Prometheus target, Grafana datasource, retention 같은 설정이 달라지기 시작하면 custom image 방식만으로는 불편할 수 있다. 이 경우 공통 설정은 image에 포함하고 환경별 설정은 별도 config/env로 분리하는 후속 작업을 검토한다.

## 11. 후속 보류 항목

| 항목 | 이유 | 처리 |
|---|---|---|
| 환경별 관측성 설정 외부화 | VM/운영 환경마다 Prometheus target, scrape interval, Grafana root URL, retention 정책이 달라지면 image rebuild만으로 관리하기 번거로워질 수 있다. | `docs/14_deferred_cleanup/01_보류_정리_목록.md`의 `DC-029`로 기록 |

## 12. 커밋 메시지 초안

```text
fix: 관측성 설정 파일을 이미지에 포함

- Prometheus/Grafana/Loki/Promtail 설정용 Dockerfile 추가
- Jenkins workspace bind mount 의존 제거
- Grafana dashboard provisioning 경로를 이미지 내부 경로로 변경
- 관측성 Compose 구조 해결 기록 추가
```
