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
- [x] Docker 방식으로 로컬 backend 대상 실행
- [x] Docker 방식으로 VM 대상 실행
- [ ] 로컬 k6 설치 방식으로 VM 대상 실행
- [x] `http_req_failed` 1% 미만 확인
- [x] 조회 API p95 1초 이내 확인
- [x] 쓰기 API p95 1.5초 이내 확인
- [ ] Grafana에서 request rate, latency 확인
- [ ] Loki datasource로 backend error log 확인

## 미검증 사유

- 프로젝트 운영 기준상 Docker 실행, VM 대상 부하 테스트, Grafana/Loki 브라우저 확인은 사용자가 직접 수행합니다.
- 이 시나리오는 배포 VM DB에 실제 테스트 방문/대기 데이터를 생성하므로 실행 시점을 사용자가 통제해야 합니다.
- VM 테스트는 기본적으로 로컬 PC에서 k6를 실행하고 `BASE_URL`만 VM 공개 주소로 바꾸는 방식입니다. Jenkins 배포용 루트 `.env`에는 k6 설정을 넣지 않습니다.

## 로컬 실행 결과

1차 실행:

- checks: 100.00% (532/532)
- http_req_failed: 0.00% (0/266)
- 전체 http_req_duration p95: 70.93ms
- walk_in p95: 73.97ms
- queue_list p95: 59.63ms
- queue_action p95: 75.96ms

2차 실행:

- checks: 100.00% (550/550)
- http_req_failed: 0.00% (0/275)
- 전체 http_req_duration p95: 65.36ms
- walk_in p95: 66.10ms
- queue_list p95: 61.76ms
- queue_action p95: 66.16ms
- vus_max: 3

로컬 기본 부하 기준에서는 두 번 모두 실패 없이 안정적으로 통과했습니다. 다만 2차 실행도 `vus_max=3`으로 표시되어, `VUS=5` 강도 상향을 의도했다면 `k6.local.env` 저장 여부와 `--env-file` 적용 여부를 다시 확인해야 합니다.

3차 실행:

- checks: 100.00% (1818/1818)
- http_req_failed: 0.00% (0/909)
- 전체 http_req_duration p95: 79.98ms
- walk_in p95: 69.16ms
- queue_list p95: 83.36ms
- queue_action p95: 82.02ms
- http_reqs: 909 requests, 11.24/s
- vus_max: 5

로컬 강도 상향 기준에서도 실패 없이 통과했습니다. `queue_list`와 `queue_action` p95가 80ms 초반으로 늘었지만, 문서 기준인 조회 1초와 쓰기 1.5초보다 충분히 낮습니다.

VM 도메인 대상 실행:

- checks: 100.00% (1400/1400)
- http_req_failed: 0.00% (0/700)
- 전체 http_req_duration p95: 244.67ms
- auth_login p95: 368.71ms
- walk_in p95: 181.96ms
- queue_list p95: 278.78ms
- queue_action p95: 174.97ms
- http_reqs: 700 requests, 8.64/s
- vus_max: 5

VM 도메인 대상 VUS 5 기준에서도 실패 없이 통과했습니다. 로컬보다 응답 시간은 증가했지만, 도메인/TLS/네트워크 경유가 포함된 결과이며 조회 1초, 쓰기 1.5초 기준보다 충분히 낮습니다.

VM 도메인 대상 3단계 실행:

- checks: 100.00% (6900/6900)
- http_req_failed: 0.00% (0/3450)
- 전체 http_req_duration p95: 380.60ms
- auth_login p95: 312.10ms
- walk_in p95: 247.18ms
- queue_list p95: 473.06ms
- queue_action p95: 271.45ms
- http_reqs: 3450 requests, 17.11/s
- vus_max: 10

VM 도메인 대상 VUS 10, 약 3분 20초 기준에서도 실패 없이 통과했습니다. 가장 높은 p95는 `queue_list` 473.06ms로, 기준 안에서는 안정적이지만 부하를 더 올릴 경우 대기열 조회가 1차 병목 후보입니다.

VM 도메인 대상 4단계 실행:

- checks: 100.00% (14122/14122)
- http_req_failed: 0.00% (0/7061)
- 전체 http_req_duration p95: 1.90s
- auth_login p95: 346.64ms
- walk_in p95: 499.48ms
- queue_list p95: 2.31s
- queue_action p95: 501.05ms
- http_reqs: 7061 requests, 21.97/s
- vus_max: 20
- threshold 결과: `http_req_duration{api:queue_list}` 초과

VM 도메인 대상 VUS 20, 약 5분 20초 기준에서는 기능 흐름과 HTTP 성공률은 유지됐지만, 대기열 조회 p95가 2.31초로 조회 기준 1초를 초과했습니다. 이 결과는 4단계 부하에서 `GET /api/queues`가 1차 병목 후보임을 보여줍니다.

## Grafana 확인 방법

`Health Center Backend Overview` 대시보드에서 k6 실행 시간대를 `Last 15 minutes` 또는 `Last 30 minutes`로 맞추고 아래를 확인합니다.

- `HTTP Request Rate`: k6 실행 중 요청량이 올라갔다가 종료 후 내려가는지 확인
- `Average HTTP Latency`: 부하 중 계속 상승하지 않고 안정적인지 확인
- `JVM Heap Memory`: 급격히 계속 증가하지 않는지 확인
- `Backend Error Logs`: 반복 ERROR 로그가 없는지 확인

Loki는 Grafana datasource로 연결되어 있으므로 VM NAT에서 3100 포트를 따로 열 필요는 없습니다. Grafana `Explore`에서 datasource를 `Loki`로 선택한 뒤 아래 쿼리를 사용할 수 있습니다.

```logql
{service="backend"} |= "ERROR"
```

전체 backend 로그 확인:

```logql
{service="backend"}
```

## 후속 작업

- Jenkins 수동 파라미터 기반 k6 smoke stage 추가
- k6 `--summary-export` JSON 결과 저장
- 예약 생성 동시성 시나리오 추가
- `GET /api/queues` 성능 개선 검토
  - 날짜/상태/업무 유형 인덱스 확인
  - 대기열 조회 응답 payload 축소 검토
  - 페이지네이션 또는 limit 적용 검토
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
