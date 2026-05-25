# 배포 후 상태 확인과 문제 해결 가이드 작성 기록

## 1. 작업 목표

- Jenkins `observability` profile, 설정 파일 bind mount, Prometheus/Grafana/Loki 확인 절차와 실패 대응을 한 문서로 묶는다.
- 다음 배포 검증에서 바로 사용할 수 있는 쉬운 확인 가이드를 작성한다.
- 포트폴리오 설명에 쓸 운영 상태 확인 흐름을 정리한다.

## 2. 작업 범위

- [x] 전체 체크리스트에서 7순위 배포 후 상태 확인과 문제 해결 가이드 항목 확인
- [x] 이번 브랜치 범위를 문서 작성으로 제한
- [x] Jenkinsfile의 observability profile 적용 상태 확인
- [x] Docker Compose 상태 확인 도구 서비스와 custom image 구조 확인
- [x] Prometheus/Grafana/Loki/Promtail 설정 파일 확인
- [x] 기존 운영 상태 확인 도구 구현 기록 확인
- [x] 배포 후 상태 확인과 문제 해결 가이드 추가
- [x] docs README 읽기 순서와 코드 작성 전 확인 문서 갱신
- [x] 전체 체크리스트 갱신
- [x] PR 문서 초안 작성

제외한다.

- [ ] Jenkins Pipeline 직접 실행
- [ ] Docker Compose 직접 실행
- [ ] Prometheus/Grafana/Loki 브라우저 확인
- [ ] backend/frontend 코드 수정
- [ ] Prometheus/Grafana/Loki/Promtail 설정 구조 변경

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] 관련 설정 파일과 기존 구현 기록 확인

## 4. 구현 체크리스트

- [x] Jenkins `observability` profile 누락 증상과 확인 방법 정리
- [x] 설정 파일 bind mount 오류 원인과 재발 확인 방법 정리
- [x] Prometheus target 확인 절차 정리
- [x] 초보자도 이해할 수 있도록 profile, scrape, target, datasource, bind mount 용어 풀이 추가
- [x] Grafana datasource/dashboard 확인 절차 정리
- [x] Loki/Promtail 로그 수집 확인 절차 정리
- [x] 증상별 빠른 판단표 작성
- [x] 포트폴리오 설명 포인트 작성

## 5. 확인한 현재 코드/설정 상태

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `docs/observability-troubleshooting-runbook` |
| 작업 트리 | 시작 시 변경 없음 |
| Jenkinsfile | `COMPOSE_PROFILE_ARGS = '--profile observability'` 적용됨 |
| Docker Compose | Prometheus/Grafana/Loki/Promtail이 `observability` profile에 있음 |
| 설정 파일 주입 | Prometheus/Grafana/Loki/Promtail 설정 파일은 custom image에 포함됨 |
| 유지 mount | Promtail Docker log 수집용 host mount만 유지 |
| Prometheus scrape | `backend:8080/actuator/prometheus` |
| scrape 허용 변수 | `OBSERVABILITY_PROMETHEUS_SCRAPE_ENABLED` |

## 6. 변경 내용

| 파일 | 변경 내용 |
|---|---|
| `docs/08_deploy/07_배포후_상태확인_문제해결_가이드.md` | Jenkins 배포 후 상태 확인과 장애 대응 가이드 추가 |
| `docs/README.md` | 배포 문서 읽기 순서와 코드 작성 전 확인 문서에 가이드 추가 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 7순위 가이드 문서화 완료 반영 |
| `docs/11_implementation_log/137_Deploy_Status_Check_Guide_작성_기록.md` | 이번 브랜치 구현 기록 작성 |
| `docs/11_implementation_log/138_Deploy_Status_Check_Guide_PR_작성안.md` | PR 문서 초안 작성 |

## 7. 검증 체크리스트

- [x] `git status --short --branch`
- [x] 관련 문서와 설정 파일 정적 확인
- [x] `git diff --check`
- [x] 신규 문서 trailing whitespace 확인
- [ ] GitNexus index 최신화
  - 확인 결과: `gitnexus status`는 stale을 보고했고, repository 루트에서 `gitnexus analyze` 실행 시 `Not inside a git repository`로 실패했다. 같은 위치에서 `git rev-parse --show-toplevel`은 정상이라 이번 문서 변경 검증은 `git status`, `git diff --check`, `rg`로 보완한다.
- [ ] Jenkins Pipeline 실행
- [ ] Docker Compose `observability` profile 실행
- [ ] Prometheus target 확인
- [ ] Grafana dashboard 확인
- [ ] Loki log query 확인

## 8. 사용자 확인 방법

Jenkins Pipeline 성공 후 Ubuntu VM에서 확인한다.

```bash
docker compose --env-file .env --profile observability ps
```

Prometheus 대표 확인:

```text
Prometheus > Status > Targets > health-center-backend
```

기대 결과:

```text
health-center-backend target 상태가 UP이다.
```

## 9. 사용자 코드 점검 결과

| 점검 시점 | 사용자 의견 | 반영 여부 |
|---|---|---|
| 브랜치 작성 중 | 아직 사용자 점검 전 | 대기 |

## 10. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 가이드 작성 중 | 상태 확인 도구 설정 외부화 | custom image 방식은 재현성이 좋지만 환경별 target/retention 변경이 잦아지면 불편할 수 있음 | 기존 `DC-029` 후속 후보 유지 |
| 가이드 작성 중 | Prometheus/Grafana/Loki 포트 외부 공개 정책 재확인 | Grafana/Prometheus/Loki는 외부 공개 대상이 아님 | 운영 주의사항에 반영 |

## 11. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리

## 12. 남은 위험

- 서버 기동, Docker 실행, Jenkins Pipeline, Prometheus/Grafana/Loki 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행해야 한다.
- 이 가이드는 현재 Compose/Jenkins 구조 기준이다. 향후 상태 확인 도구 설정 외부화나 별도 management network를 도입하면 문서를 함께 갱신해야 한다.

## 13. 커밋 메시지 초안

```text
docs: 배포 후 상태 확인 문제 해결 가이드 추가

- Jenkins observability profile 배포 확인 절차 문서화
- Prometheus/Grafana/Loki/Promtail 확인 순서와 실패 대응 정리
- 설정 파일 bind mount 오류 재발 확인 기준과 포트폴리오 설명 포인트 추가
```
