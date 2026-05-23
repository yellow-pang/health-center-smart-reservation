# Jenkins Observability Profile Deploy 구현 기록

## 1. 작업 목표

- Jenkins 배포 후 VM Docker 컨테이너 목록에 Grafana, Loki, Prometheus가 생성되지 않는 원인을 확인한다.
- 원인이 `docker compose` 실행 시 `observability` profile이 포함되지 않았기 때문인지 확인하고 Jenkinsfile을 보정한다.
- Docker 실행과 Jenkins Pipeline 런타임 확인은 사용자가 직접 수행할 수 있도록 기준을 남긴다.

## 2. 원인 확인

`docker-compose.yml`의 관측성 서비스는 아래처럼 profile 아래에 있다.

```text
profiles:
  - observability
```

기존 `Jenkinsfile`은 아래 명령만 실행했다.

```text
docker compose --env-file .env config
docker compose --env-file .env build
docker compose --env-file .env up -d --remove-orphans
docker compose --env-file .env ps
```

Docker Compose profile 서비스는 `--profile observability` 또는 `COMPOSE_PROFILES=observability`가 없으면 기본 배포 대상에 포함되지 않는다. 따라서 Jenkins 배포 후 `health-center-grafana`, `health-center-loki`, `health-center-prometheus`, `health-center-promtail` 컨테이너가 없는 것이 정상적인 증상이다.

## 3. 작업 범위

- [x] 현재 브랜치 `fix/jenkins-observability-profile-deploy` 생성
- [x] Jenkinsfile 확인
- [x] Docker Compose profile 원인 확인
- [x] Jenkinsfile의 `config/build/up/ps` 단계에 observability profile 반영
- [x] 구현 기록 작성
- [x] PR 문서 초안 작성

제외한다.

- [ ] Jenkins Pipeline 직접 실행
- [ ] Docker Compose 직접 실행
- [ ] VM 컨테이너 런타임 확인
- [ ] Grafana/Prometheus/Loki 브라우저 확인

## 4. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `Jenkinsfile` | `COMPOSE_PROFILE_ARGS='--profile observability'` 환경변수 추가 |
| `Jenkinsfile` | `docker compose config/build/up/ps`에 `${COMPOSE_PROFILE_ARGS}` 적용 |
| `docs/11_implementation_log/128_Jenkins_Observability_Profile_Deploy_구현_기록.md` | 원인과 변경 내용 기록 |
| `docs/11_implementation_log/129_Jenkins_Observability_Profile_Deploy_PR_작성안.md` | PR 초안 작성 |

## 5. 검증 체크리스트

- [x] `git diff --check`
- [ ] Jenkins Pipeline 실행
- [ ] Jenkins `Docker Compose Config` 단계에서 observability 서비스가 포함되는지 확인
- [ ] Jenkins `Deploy` 이후 VM에서 컨테이너 확인
- [ ] Prometheus target 확인
- [ ] Grafana dashboard 확인

## 6. 사용자 확인 방법

Jenkins Pipeline 실행 후 VM에서 확인한다.

```bash
docker compose --env-file .env --profile observability ps
```

기대 컨테이너:

```text
health-center-postgres
health-center-backend
health-center-frontend
health-center-prometheus
health-center-grafana
health-center-loki
health-center-promtail
```

Prometheus scrape까지 확인하려면 Jenkins Credentials의 `.env`에 아래 값이 있어야 한다.

```env
OBSERVABILITY_PROMETHEUS_SCRAPE_ENABLED=true
```

이 값이 `false`이면 컨테이너는 올라오지만 Prometheus의 backend target은 인증 차단으로 DOWN 또는 오류 상태가 될 수 있다.

## 7. 남은 위험

- 관측성 profile이 Jenkins 배포에 항상 포함된다. 운영 VM에서 Grafana/Prometheus/Loki를 계속 유지하는 의도에는 맞지만, 원하지 않을 경우 Jenkinsfile의 `COMPOSE_PROFILE_ARGS` 값을 비워야 한다.
- Promtail은 Docker socket과 container log 경로를 읽으므로 VM Docker 권한과 마운트 경로를 런타임에서 확인해야 한다.
- Grafana/Prometheus/Loki 포트는 Cloudflare Tunnel public hostname에 연결하지 않는 것을 권장한다.

## 8. 커밋 메시지 초안

```text
fix: Jenkins 배포에 관측성 Compose profile 포함

- Jenkins docker compose config/build/up/ps에 observability profile 적용
- VM 배포 후 Prometheus/Grafana/Loki 컨테이너가 생성되도록 보정
- Jenkins 관측성 배포 확인 기준 문서화
```
