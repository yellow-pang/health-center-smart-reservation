# K6 Vaccination Queue Load Scenarios Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `test/k6-queue-load-scenarios` |
| base 브랜치 | `dev` |
| 작업 트리 | k6 스크립트, 실행 문서, 구현 기록, PR 문서, 전체 체크리스트 갱신 |
| 주요 커밋 | 커밋 전 |
| 빌드 확인 | 코드 빌드 대상 아님 |
| 테스트 확인 | `git diff --check` 통과 |
| 실행 확인 | Docker k6, 로컬 k6, VM 대상 부하 테스트는 사용자 직접 수행 |

## PR 제목

```text
test: 예방접종 대기 흐름 k6 부하 테스트 추가
```

## PR 본문

```markdown
## 개요

배포 VM을 대상으로 예방접종 현장 접수와 대기 처리 흐름을 k6로 부하 테스트할 수 있도록 스크립트와 실행 문서를 추가합니다.

이번 시나리오는 고령층 방문자가 예방접종 업무에 몰리는 상황을 가정합니다. 직원 계정으로 로그인한 뒤 예방접종 현장 접수, 대기열 조회, 일부 대기표 호출/시작/완료 흐름을 반복합니다.

## 변경 내용

- k6 예방접종 대기 흐름 시나리오 추가
  - `performance/k6/vaccination-queue-flow.js`
- Docker 기반 실행 방법 문서화
- 로컬 k6 설치 실행 방법 문서화
- 환경변수 예시, git 미추적 로컬 env, summary 결과 저장 폴더 추가
- VUS, duration, queue action 비율, threshold 환경변수화
- 부하 테스트 개념, 지표, 데이터 주의사항 정리
- 전체 체크리스트와 구현 기록 갱신

## 기본 실행 예시

```powershell
docker run --rm `
  --env-file performance/k6/k6.local.env `
  -v "${PWD}/performance/k6:/scripts" `
  grafana/k6:0.54.0 run /scripts/vaccination-queue-flow.js
```

## 검증

- [x] k6 스크립트 정적 확인
- [x] `git diff --check`
- [ ] Docker 방식으로 VM 대상 실행
- [ ] 로컬 k6 설치 방식으로 VM 대상 실행
- [ ] `http_req_failed` 1% 미만 확인
- [ ] 조회 API p95 1초 이내 확인
- [ ] 쓰기 API p95 1.5초 이내 확인
- [ ] Grafana에서 request rate, latency, error rate 확인
- [ ] Loki에서 backend error log 확인

## 미검증 사유

- 프로젝트 운영 기준상 Docker 실행, VM 대상 부하 테스트, Grafana/Loki 브라우저 확인은 사용자가 직접 수행합니다.
- 이 시나리오는 배포 VM DB에 실제 테스트 방문/대기 데이터를 생성하므로 실행 시점을 사용자가 통제해야 합니다.
- VM 테스트는 기본적으로 로컬 PC에서 k6를 실행하고 `BASE_URL`만 VM 공개 주소로 바꾸는 방식입니다. Jenkins 배포용 루트 `.env`에는 k6 설정을 넣지 않습니다.

## 후속 작업

- Jenkins 수동 파라미터 기반 k6 smoke stage 추가
- k6 `--summary-export` JSON 결과 저장
- 예약 생성 동시성 시나리오 추가
- K6 테스트 데이터 정리 스크립트 또는 운영 절차 추가
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] VM 대상 Docker k6 실행 결과 기록
- [ ] Grafana/Loki 확인 결과 기록
- [ ] 후속 자동화 범위 결정

## 커밋 메시지 초안

제목:

```text
test: 예방접종 대기 흐름 k6 부하 테스트 추가
```

본문:

```text
- 직원 로그인 기반 예방접종 현장 접수 시나리오 작성
- 대기열 조회와 호출/시작/완료 상태 변경 부하를 환경변수로 조절
- Docker와 로컬 k6 실행 방법 문서화
- 부하 테스트 지표와 데이터 주의사항 정리
```
