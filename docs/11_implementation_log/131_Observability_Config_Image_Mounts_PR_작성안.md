# Observability Config Image Mounts Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `fix/observability-config-image-mounts` |
| base 브랜치 | `dev` |
| 작업 트리 | 관측성 설정 파일 이미지 포함, bind mount 제거, 구현 기록, PR 문서 추가 |
| 주요 커밋 | 커밋 전 |
| 빌드 확인 | Docker/Jenkins runtime 확인은 사용자 직접 수행 |
| 테스트 확인 | `git diff --check` 통과 |
| 실행 확인 | Jenkins Pipeline, Docker, VM 컨테이너 확인은 사용자 직접 수행 |

## PR 제목

```text
fix: 관측성 설정 파일을 이미지에 포함
```

## PR 본문

```markdown
## 개요

Jenkins 배포 단계에서 Prometheus 설정 파일 bind mount가 실패하는 문제를 구조적으로 해결합니다.

Jenkins 컨테이너 내부 workspace 파일을 Docker host bind mount source로 직접 사용하지 않고, Prometheus/Grafana/Loki/Promtail 설정 파일을 각 custom image에 포함하도록 변경했습니다.

## 원인

Jenkins는 컨테이너 안에서 checkout을 수행하지만 Docker daemon은 VM host의 Docker socket을 통해 실행됩니다.

이때 Compose의 상대 경로 bind mount가 Docker daemon 관점의 host path로 해석되면서, Jenkins workspace 내부 파일을 찾지 못하거나 디렉터리로 잘못 생성해 파일-디렉터리 타입 불일치 오류가 발생했습니다.

## 변경 내용

- Prometheus custom image 추가
  - `infra/observability/prometheus/Dockerfile`
  - `prometheus.yml` image 내부 COPY
- Loki custom image 추가
  - `infra/observability/loki/Dockerfile`
  - `loki-config.yml` image 내부 COPY
- Promtail custom image 추가
  - `infra/observability/promtail/Dockerfile`
  - `promtail-config.yml` image 내부 COPY
- Grafana custom image 추가
  - `infra/observability/grafana/Dockerfile`
  - datasource/dashboard provisioning image 내부 COPY
- Compose 관측성 서비스 수정
  - 설정 파일 bind mount 제거
  - custom local image build 적용
- Grafana dashboard provider path를 `/etc/grafana/dashboards`로 변경
- 문제 해결 기록 문서 추가
- 설정 변경 시 image rebuild가 필요하다는 운영 기준과 환경별 설정 외부화 후속 후보 기록

## 검증

- [x] `git diff --check`
- [ ] Jenkins Pipeline 실행
- [ ] Docker Compose build 단계에서 custom observability image build 확인
- [ ] Deploy 단계에서 bind mount 오류가 사라졌는지 확인
- [ ] VM에서 Prometheus/Grafana/Loki/Promtail 컨테이너 확인
- [ ] Prometheus target 확인
- [ ] Grafana dashboard 확인
- [ ] Loki log query 확인

## 런타임 확인 기준

Jenkins Pipeline 실행 후 VM에서 확인:

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

Jenkins 로그에서 아래 오류가 다시 나오지 않아야 합니다.

```text
error mounting ".../infra/observability/prometheus/prometheus.yml"
```

## 미검증 사유

- 프로젝트 운영 기준상 Jenkins Pipeline 실행, Docker 실행, VM 컨테이너 확인, 브라우저 확인은 사용자가 직접 수행합니다.

## 후속 작업

- Promtail Docker log mount 권한 확인
- VM마다 Prometheus target, scrape interval, retention이 달라질 경우 환경별 관측성 config 외부화 검토
- Grafana dashboard 패널 추가
- k6 부하 테스트 시나리오 작성
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] Jenkins 배포 성공 확인
- [ ] VM 컨테이너 확인 결과 기록

## 커밋 메시지 초안

제목:

```text
fix: 관측성 설정 파일을 이미지에 포함
```

본문:

```text
- Prometheus/Grafana/Loki/Promtail 설정용 Dockerfile 추가
- Jenkins workspace bind mount 의존 제거
- Grafana dashboard provisioning 경로를 이미지 내부 경로로 변경
- 환경별 관측성 설정 외부화 후속 후보 기록
- 관측성 Compose 구조 해결 기록 추가
```
