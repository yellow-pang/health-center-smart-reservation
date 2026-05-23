# Jenkins Observability Profile Deploy Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `fix/jenkins-observability-profile-deploy` |
| base 브랜치 | `dev` |
| 작업 트리 | Jenkinsfile 관측성 profile 반영, 구현 기록, PR 문서 추가 |
| 주요 커밋 | 커밋 전 |
| 빌드 확인 | 코드 빌드 영향 없음 |
| 테스트 확인 | `git diff --check` 통과 |
| 실행 확인 | Jenkins Pipeline, Docker, VM 컨테이너 확인은 사용자 직접 수행 |

## PR 제목

```text
fix: Jenkins 배포에 관측성 Compose profile 포함
```

## PR 본문

```markdown
## 개요

VM 배포 후 Grafana, Loki, Prometheus 컨테이너가 생성되지 않는 문제를 수정합니다.

원인은 관측성 서비스가 Docker Compose `observability` profile 아래에 있는데, Jenkins Pipeline의 `docker compose config/build/up/ps` 명령이 profile 없이 실행되고 있었기 때문입니다.

## 변경 내용

- Jenkinsfile에 `COMPOSE_PROFILE_ARGS='--profile observability'` 추가
- `Docker Compose Config` 단계에 observability profile 적용
- `Docker Build` 단계에 observability profile 적용
- `Deploy` 단계의 `up`과 `ps`에 observability profile 적용
- 구현 기록과 PR 문서 추가

## 검증

- [x] `git diff --check`
- [ ] Jenkins Pipeline 실행
- [ ] Jenkins `Docker Compose Config` 로그에 prometheus/grafana/loki/promtail 서비스가 포함되는지 확인
- [ ] Jenkins 배포 후 VM에서 컨테이너 확인
- [ ] Prometheus target `health-center-backend` 확인
- [ ] Grafana dashboard 확인

## 런타임 확인 기준

Jenkins Pipeline 완료 후 VM에서 확인:

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

Prometheus scrape까지 확인하려면 Jenkins Credentials의 `.env`에 아래 값이 필요합니다.

```env
OBSERVABILITY_PROMETHEUS_SCRAPE_ENABLED=true
```

## 미검증 사유

- 프로젝트 운영 기준상 Jenkins Pipeline 실행, Docker 실행, VM 컨테이너 확인, 브라우저 확인은 사용자가 직접 수행한다.

## 후속 작업

- Jenkins에서 관측성 profile을 켜고 끄는 파라미터화 검토
- 운영용 Grafana/Prometheus/Loki 접근 경로와 방화벽 기준 문서화
- k6 부하 테스트 시나리오 추가
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] Jenkins 배포 확인 결과 기록
- [ ] VM 컨테이너 확인 결과 기록

## 커밋 메시지 초안

제목:

```text
fix: Jenkins 배포에 관측성 Compose profile 포함
```

본문:

```text
- Jenkins docker compose config/build/up/ps에 observability profile 적용
- VM 배포 후 Prometheus/Grafana/Loki 컨테이너가 생성되도록 보정
- Jenkins 관측성 배포 확인 기준 문서화
```
