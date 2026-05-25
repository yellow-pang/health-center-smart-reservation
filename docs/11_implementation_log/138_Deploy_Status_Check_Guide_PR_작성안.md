# 배포 후 상태 확인과 문제 해결 가이드 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `docs/observability-troubleshooting-runbook` |
| base 브랜치 | `dev` |
| 작업 트리 | 배포 후 상태 확인 가이드, docs README, 전체 체크리스트, 구현 기록, PR 문서 갱신 |
| 주요 커밋 | 커밋 전 |
| 빌드 확인 | 코드 변경 없음 |
| 테스트 확인 | `git diff --check` 통과, 신규 문서 trailing whitespace 없음 |
| GitNexus 확인 | `gitnexus status` stale 확인, `gitnexus analyze`는 repository 루트에서 `Not inside a git repository`로 실패해 일반 정적 확인으로 보완 |
| 실행/API 확인 | Jenkins Pipeline, Docker Compose, Prometheus/Grafana/Loki 브라우저 확인은 사용자가 직접 확인 |

## PR 제목

```text
docs: 배포 후 상태 확인 문제 해결 가이드 추가
```

## PR 본문

```markdown
## 개요

Jenkins 배포 이후 Prometheus/Grafana/Loki/Promtail 컨테이너가 빠지거나 화면 확인이 막힐 때 사용할 배포 후 상태 확인 가이드를 추가합니다.

이번 PR은 코드 변경 없이, 기존에 해결한 `observability` profile 누락과 Jenkins workspace bind mount 문제를 다음 배포 검증에서 바로 따라갈 수 있는 절차로 정리합니다.

## 변경 내용

- Jenkins 배포 후 상태 확인 가이드 추가
  - `observability` profile 적용 확인
  - Jenkins Pipeline 단계별 확인 포인트
  - VM 컨테이너 확인 절차
  - profile, scrape, target, datasource, bind mount 용어 풀이
- Prometheus/Grafana/Loki/Promtail 확인 절차 정리
  - Prometheus target과 metric 확인
  - Grafana datasource/dashboard 확인
  - Loki/Promtail log query 확인
- 실패 대응표 추가
  - profile 누락
  - scrape 401/403
  - 설정 파일 bind mount 오류
  - Promtail Docker log mount 권한
- 포트폴리오 설명 포인트 추가
- docs README와 전체 체크리스트 갱신

## 검증

- [x] `git status --short --branch`
- [x] 관련 Jenkins/Compose/Prometheus/Grafana/Loki/Promtail 설정 파일 확인
- [x] `git diff --check`
- [x] 신규 문서 trailing whitespace 확인
- [ ] GitNexus index 최신화
- [ ] Jenkins Pipeline 실행
- [ ] `docker compose --env-file .env --profile observability ps` 확인
- [ ] Prometheus target `health-center-backend` UP 확인
- [ ] Grafana `Health Center Backend Overview` dashboard 확인
- [ ] Loki query `{compose_project="health-center"}` 확인

## 대표 확인 절차

Jenkins Pipeline 성공 후 Ubuntu VM에서 확인합니다.

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

Prometheus 대표 확인:

```text
Prometheus > Status > Targets > health-center-backend
```

기대 결과:

```text
health-center-backend target 상태가 UP이다.
```

## 미검증 사유

- 프로젝트 운영 기준상 Jenkins Pipeline 실행, Docker 실행, 브라우저 확인은 사용자가 직접 수행합니다.
- 이번 PR은 문서 변경이므로 Maven/Next 빌드는 생략했습니다.

## 후속 작업

- 다음 VM 배포 검증 결과를 가이드에 실제 캡처/결과 요약으로 보강
- 필요 시 상태 확인 도구 설정 외부화(`DC-029`) 검토
- k6 재측정 결과와 Grafana/Loki 확인 결과를 별도 운영 기록으로 남기기
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] Jenkins Pipeline 실행 결과 기록
- [ ] Prometheus/Grafana/Loki 확인 결과 기록
- [ ] 전체 체크리스트 후속 항목 갱신

## 커밋 메시지 초안

제목:

```text
docs: 배포 후 상태 확인 문제 해결 가이드 추가
```

본문:

```text
- Jenkins observability profile 배포 확인 절차 문서화
- Prometheus/Grafana/Loki/Promtail 확인 순서와 실패 대응 정리
- 설정 파일 bind mount 오류 재발 확인 기준 추가
- docs README와 전체 체크리스트 갱신
```
