# K6 Vaccination Queue Load Scenarios 구현 기록

## 1. 작업 목표

- 먼저 로컬 Docker Compose 환경에서 예방접종 현장 접수와 대기 처리 흐름을 k6로 연습할 수 있게 한다.
- 같은 스크립트를 배포 VM 주소에도 적용할 수 있게 `BASE_URL` 환경변수로 대상 서버를 바꿀 수 있게 한다.
- 비전공자도 부하 테스트 목적, 실행 방법, 지표 해석을 이해할 수 있도록 문서화한다.
- 기본 실행은 Docker 기반으로 두고, 로컬 k6 설치 방식도 함께 안내한다.

## 2. 작업 범위

- [x] k6 예방접종 현장 접수/대기 처리 시나리오 작성
- [x] VM/배포 서버 주소를 환경변수로 받도록 구성
- [x] Docker 실행 방식을 기본으로 문서화
- [x] 로컬 k6 실행 방식 문서화
- [x] VUS, duration, queue action 비율, threshold를 환경변수로 조절 가능하게 구성
- [x] 전체 체크리스트 갱신
- [x] PR 작성안 작성

제외한다.

- [ ] 실제 VM 부하 테스트 실행
- [ ] Jenkins 자동 실행 stage 추가
- [ ] 예약 생성 동시성 시나리오
- [ ] 테스트 데이터 자동 삭제 API 추가

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 확인
- [x] `docs/14_deferred_cleanup/01_보류_정리_목록.md` 확인
- [x] `docs/04_api/01_API_명세서.md` 확인
- [x] 현재 브랜치 확인: `test/k6-queue-load-scenarios`
- [x] API 요청/응답 DTO 확인

## 4. 부하 테스트 개념 정리

부하 테스트는 많은 사용자가 동시에 들어왔을 때 API가 얼마나 안정적으로 응답하는지 확인하는 실험이다.

이번 테스트는 운영 서비스에 바로 강한 부하를 거는 방식이 아니다. 아래 순서로 진행한다.

```text
1단계: 내 PC에서 Docker Compose로 backend/postgres를 띄운다.
  ↓
2단계: k6 Docker 컨테이너를 실행해서 로컬 backend에 요청을 보낸다.
  ↓
3단계: k6 콘솔 결과를 보고 실패율과 응답 시간을 이해한다.
  ↓
4단계: 로컬에서 흐름이 이해되면 BASE_URL만 배포 VM 주소로 바꿔 실행한다.
```

즉, k6도 Docker로 실행하고, 테스트 대상 서버도 처음에는 로컬 Docker Compose로 띄운 backend를 사용한다. 이 방식은 배포 VM 데이터를 바로 건드리기 전에 안전하게 명령과 결과를 익히기 좋다.

이번 프로젝트에서는 아래 질문에 답하기 위해 사용한다.

| 질문 | 확인 방법 |
|---|---|
| 예방접종 현장 접수가 몰려도 대기번호가 생성되는가 | `POST /api/visits/walk-in` 반복 |
| 대기열 조회가 느려지지 않는가 | `GET /api/queues` 반복 |
| 직원이 호출/시작/완료를 반복해도 오류율이 낮은가 | `POST /api/queues/{id}/call`, `start`, `complete` |
| 응답 시간이 포트폴리오 시연 수준으로 안정적인가 | k6 p95 latency threshold |
| 관측성 스택에서 병목을 볼 수 있는가 | Grafana, Prometheus, Loki 확인 |

핵심 지표:

| 지표 | 의미 |
|---|---|
| VUS | 동시에 움직이는 가상 사용자 수 |
| duration | 부하를 유지하는 시간 |
| http_req_failed | HTTP 요청 실패율 |
| http_req_duration | 응답 시간 |
| p95 | 100개 요청 중 느린 쪽 5개를 제외한 나머지가 들어오는 응답 시간 기준 |
| checks | 시나리오에서 기대한 조건이 맞았는지 |

## 4.1 초보자용 실행 방식 요약

이번 테스트에는 컴퓨터 안에 역할이 3개 있다.

| 역할 | 무엇인가 | 이번 프로젝트에서의 예 |
|---|---|---|
| 테스트 대상 서버 | 요청을 받는 백엔드 API | 로컬 `http://localhost:8080` 또는 배포 VM 주소 |
| k6 실행기 | 가짜 사용자를 만들어 요청을 보내는 도구 | `grafana/k6` Docker 컨테이너 |
| DB | 현장 접수와 대기표가 저장되는 곳 | 로컬 PostgreSQL 또는 VM PostgreSQL |

처음에는 아래처럼 이해하면 된다.

```text
k6 컨테이너
  -> 직원으로 로그인 요청
  -> 예방접종 현장 접수 요청
  -> 대기열 조회 요청
  -> 일부 대기표 호출/시작/완료 요청
  -> 결과를 콘솔에 출력
```

로컬 테스트와 VM 테스트의 차이는 `BASE_URL` 하나다.

| 단계 | BASE_URL | 데이터가 쌓이는 곳 | 권장 목적 |
|---|---|---|---|
| 로컬 연습 | `http://host.docker.internal:8080` | 내 PC의 로컬 DB | 명령과 결과 익히기 |
| VM 확인 | `https://your-vm-domain.example.com` | 배포 VM DB | 실제 배포 환경 반응 확인 |

Docker k6 컨테이너 안에서 `localhost`는 컨테이너 자기 자신을 뜻한다. 그래서 Windows/Mac에서 로컬 backend에 붙을 때는 보통 `http://host.docker.internal:8080`을 사용한다. 로컬에 k6를 직접 설치해서 실행할 때는 `http://localhost:8080`을 사용해도 된다.

Jenkins 배포 `.env`와 k6 `.env`는 역할이 다르다.

| 파일 | 역할 |
|---|---|
| 루트 `.env` | Jenkins와 Docker Compose가 앱 컨테이너를 띄울 때 읽는 설정 |
| `performance/k6/k6.local.env` | k6가 어느 API 주소로 얼마나 요청을 보낼지 읽는 테스트 설정 |

따라서 k6 값을 Jenkins 배포 `.env`에 넣지 않는다. VM 테스트도 보통은 내 PC에서 k6를 실행하고 `BASE_URL`만 VM 공개 주소로 바꾸는 방식으로 진행한다. 이 경우 VM 안에 k6 env 파일이 없어도 된다.

## 5. 구현 내용

| 파일 | 내용 |
|---|---|
| `performance/k6/vaccination-queue-flow.js` | 직원 로그인, 예방접종 현장 접수, 대기열 조회, 일부 대기표 호출/시작/완료 k6 시나리오 |
| `performance/k6/README.md` | Docker 실행, 로컬 실행, 강도 조절, 지표 확인, 데이터 주의사항 문서화 |
| `performance/k6/k6.env.example` | VM 주소와 테스트 강도를 지정하는 환경변수 예시 |
| `performance/k6/k6.local.env` | 사용자가 바로 실행할 수 있는 k6 전용 로컬 환경변수 파일. git에는 올리지 않음 |
| `performance/k6/.gitignore` | `k6.local.env` git 추적 제외 |
| `performance/k6/results/.gitignore` | k6 summary 결과 파일을 로컬 산출물로만 보관 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | k6 브랜치 진행 상태 반영 |
| `docs/11_implementation_log/133_K6_Vaccination_Queue_Load_Scenarios_구현_기록.md` | 구현 기록 |
| `docs/11_implementation_log/134_K6_Vaccination_Queue_Load_Scenarios_PR_작성안.md` | PR 작성안 |

## 6. 기본 시나리오

```text
직원 로그인
  ↓
예방접종 업무 유형 조회
  ↓
현장 접수 생성
  ↓
대기열 조회
  ↓
일부 대기표 call/start/complete
```

기본값:

| 항목 | 값 |
|---|---:|
| VUS | 5 |
| DURATION | 1m |
| QUEUE_ACTION_RATE | 0.3 |
| 실패율 threshold | 1% 미만 |
| 조회 p95 threshold | 1초 이내 |
| 쓰기 p95 threshold | 1.5초 이내 |

## 7. 검증 체크리스트

- [x] 스크립트 정적 확인
- [x] `git diff --check`
- [ ] 로컬 Docker Compose backend 실행
- [x] Docker k6로 로컬 backend 대상 실행
- [ ] 로컬 설치 k6로 로컬 backend 대상 실행
- [x] VM/배포 서버 대상 실행
- [ ] Grafana request rate/latency/error rate 확인
- [ ] Loki error log 확인

2026-05-23 로컬 Docker k6 실행 결과:

| 지표 | 결과 | 판단 |
|---|---:|---|
| checks | 100.00% (532/532) | 통과 |
| http_req_failed | 0.00% (0/266) | 통과 |
| 전체 http_req_duration p95 | 70.93ms | 기준 1초/1.5초 대비 매우 양호 |
| auth_login p95 | 58.55ms | 양호 |
| service_types p95 | 65.79ms | 양호 |
| walk_in p95 | 73.97ms | 양호 |
| queue_list p95 | 59.63ms | 양호 |
| queue_action p95 | 75.96ms | 양호 |
| http_reqs | 266 requests, 5.23/s | 로컬 기본 부하 확인 |
| iterations | 114 iterations, 2.24/s | 로컬 기본 부하 확인 |

결론: 로컬 기본 부하 기준에서는 예방접종 현장 접수, 대기열 조회, 일부 대기 상태 변경 흐름이 실패 없이 안정적으로 통과했다. 다음 확인은 VM 대상 짧은 smoke 실행 또는 로컬 강도 상향 테스트다.

2026-05-23 로컬 Docker k6 추가 실행 결과:

| 지표 | 결과 | 판단 |
|---|---:|---|
| checks | 100.00% (550/550) | 통과 |
| http_req_failed | 0.00% (0/275) | 통과 |
| 전체 http_req_duration p95 | 65.36ms | 기준 1초/1.5초 대비 매우 양호 |
| auth_login p95 | 45.25ms | 양호 |
| service_types p95 | 39.80ms | 양호 |
| walk_in p95 | 66.10ms | 양호 |
| queue_list p95 | 61.76ms | 양호 |
| queue_action p95 | 66.16ms | 양호 |
| http_reqs | 275 requests, 5.40/s | 로컬 기본 부하 확인 |
| iterations | 114 iterations, 2.24/s | 로컬 기본 부하 확인 |
| vus_max | 3 | 강도 상향 시도 시 `k6.local.env` 적용값 재확인 필요 |

결론: 두 번째 로컬 실행도 실패 없이 통과했고, p95 응답 시간은 첫 실행보다 소폭 더 낮았다. 다만 출력상 `vus_max=3`으로 표시되어, `VUS=5` 강도 상향 테스트를 의도했다면 `performance/k6/k6.local.env`의 `VUS` 값 저장 여부와 `--env-file performance/k6/k6.local.env` 적용 여부를 다시 확인해야 한다.

2026-05-23 로컬 Docker k6 강도 상향 실행 결과:

| 지표 | 결과 | 판단 |
|---|---:|---|
| checks | 100.00% (1818/1818) | 통과 |
| http_req_failed | 0.00% (0/909) | 통과 |
| 전체 http_req_duration p95 | 79.98ms | 기준 1초/1.5초 대비 매우 양호 |
| auth_login p95 | 39.58ms | 양호 |
| service_types p95 | 28.41ms | 양호 |
| walk_in p95 | 69.16ms | 양호 |
| queue_list p95 | 83.36ms | 양호 |
| queue_action p95 | 82.02ms | 양호 |
| http_reqs | 909 requests, 11.24/s | 로컬 VUS 5 부하 확인 |
| iterations | 311 iterations, 3.85/s | 로컬 VUS 5 부하 확인 |
| vus_max | 5 | 강도 상향 적용 확인 |

결론: `VUS=5`, 약 1분 20초 실행에서도 예방접종 현장 접수, 대기열 조회, 일부 대기 상태 변경 흐름이 실패 없이 통과했다. `queue_list`와 `queue_action` p95가 80ms 초반으로 늘었지만 기준값보다 충분히 낮다.

2026-05-23 VM 도메인 대상 Docker k6 강도 상향 실행 결과:

| 지표 | 결과 | 판단 |
|---|---:|---|
| checks | 100.00% (1400/1400) | 통과 |
| http_req_failed | 0.00% (0/700) | 통과 |
| 전체 http_req_duration p95 | 244.67ms | 기준 1초/1.5초 대비 양호 |
| auth_login p95 | 368.71ms | 양호 |
| service_types p95 | 175.93ms | 양호 |
| walk_in p95 | 181.96ms | 양호 |
| queue_list p95 | 278.78ms | VM 도메인 경유 기준 양호 |
| queue_action p95 | 174.97ms | 양호 |
| http_reqs | 700 requests, 8.64/s | VM VUS 5 부하 확인 |
| iterations | 247 iterations, 3.05/s | VM VUS 5 부하 확인 |
| vus_max | 5 | 강도 상향 적용 확인 |

결론: VM 도메인 대상 `VUS=5`, 약 1분 20초 실행에서도 실패 없이 통과했다. 로컬 테스트보다 p95 응답 시간은 증가했지만, 도메인/TLS/네트워크 경유가 포함된 결과이며 문서 기준인 조회 1초와 쓰기 1.5초보다 충분히 낮다. 가장 느린 구간은 `queue_list` p95 278.78ms이고, 전체 p95도 244.67ms로 포트폴리오 시연용 기준에서는 안정적이다.

2026-05-23 VM 도메인 대상 Docker k6 3단계 실행 결과:

| 지표 | 결과 | 판단 |
|---|---:|---|
| checks | 100.00% (6900/6900) | 통과 |
| http_req_failed | 0.00% (0/3450) | 통과 |
| 전체 http_req_duration p95 | 380.60ms | 기준 1초/1.5초 대비 양호 |
| auth_login p95 | 312.10ms | 양호 |
| service_types p95 | 131.84ms | 양호 |
| walk_in p95 | 247.18ms | 양호 |
| queue_list p95 | 473.06ms | 기준 내 통과, 병목 후보 |
| queue_action p95 | 271.45ms | 양호 |
| http_reqs | 3450 requests, 17.11/s | VM VUS 10 부하 확인 |
| iterations | 1169 iterations, 5.80/s | VM VUS 10 부하 확인 |
| vus_max | 10 | 3단계 강도 적용 확인 |

결론: VM 도메인 대상 `VUS=10`, 약 3분 20초 실행에서도 checks 100%, 실패율 0%로 통과했다. 전체 p95는 380.60ms로 기준값보다 충분히 낮다. 다만 `queue_list` p95가 473.06ms로 가장 높아, 이후 데이터를 더 늘리거나 VUS를 더 올릴 경우 대기열 조회 SQL과 응답 payload가 1차 병목 후보가 될 수 있다.

2026-05-23 VM 도메인 대상 Docker k6 4단계 실행 결과:

| 지표 | 결과 | 판단 |
|---|---:|---|
| checks | 100.00% (14122/14122) | 기능 흐름 통과 |
| http_req_failed | 0.00% (0/7061) | HTTP 실패 없음 |
| 전체 http_req_duration p95 | 1.90s | 기준 초과 |
| auth_login p95 | 346.64ms | 양호 |
| service_types p95 | 132.26ms | 양호 |
| walk_in p95 | 499.48ms | 기준 내 통과 |
| queue_list p95 | 2.31s | threshold 초과, 병목 확인 |
| queue_action p95 | 501.05ms | 기준 내 통과 |
| http_reqs | 7061 requests, 21.97/s | VM VUS 20 부하 확인 |
| iterations | 2043 iterations, 6.36/s | VM VUS 20 부하 확인 |
| vus_max | 20 | 4단계 강도 적용 확인 |

k6 threshold 결과:

```text
thresholds on metrics 'http_req_duration{api:queue_list}' have been crossed
```

결론: VM 도메인 대상 `VUS=20`, 약 5분 20초 실행에서 기능 흐름은 모두 성공했고 HTTP 실패도 없었다. 하지만 `queue_list` p95가 2.31초로 조회 기준 1초를 초과했다. 이 결과는 장애라기보다 대기열 조회 API가 높은 부하에서 가장 먼저 느려지는 병목 후보임을 확인한 것이다. 다음 성능 개선 후보는 `GET /api/queues`의 조회 범위, 인덱스, 정렬, 응답 payload, 페이지네이션이다.

## 8. 사용자 확인 방법

### 8.1 1단계: 로컬에서 테스트 대상 서버 켜기

먼저 로컬 Docker Compose로 PostgreSQL과 backend가 떠 있어야 한다.

권장 흐름:

```powershell
docker compose --env-file .env up -d postgres backend
```

확인할 것:

```powershell
docker compose --env-file .env ps
```

`backend`와 `postgres`가 실행 중이면 다음 단계로 간다.

브라우저 또는 Swagger로 `http://localhost:8080/swagger-ui/index.html`에 접속해 백엔드가 살아있는지 확인한다. Swagger 확인이 어렵다면 `http://localhost:8080/actuator/health`가 응답하는지만 봐도 1차 확인에는 충분하다.

### 8.2 2단계: Docker k6로 로컬 backend 때려보기

PowerShell에서 프로젝트 루트 기준으로 실행한다.

```powershell
docker run --rm `
  --env-file performance/k6/k6.local.env `
  -v "${PWD}/performance/k6:/scripts" `
  grafana/k6:0.54.0 run /scripts/vaccination-queue-flow.js
```

이 명령의 의미:

| 부분 | 의미 |
|---|---|
| `--env-file performance/k6/k6.local.env` | k6 실행에 필요한 환경변수를 한 번에 읽는 옵션 |
| `BASE_URL` | k6가 요청을 보낼 백엔드 주소. 로컬 Docker k6에서는 `http://host.docker.internal:8080` |
| `host.docker.internal` | Docker 컨테이너에서 내 PC의 localhost로 접근하기 위한 이름 |
| `VUS=3` | 동시에 3명이 접수하는 것처럼 테스트 |
| `DURATION=30s` | 30초 동안만 짧게 확인 |
| `QUEUE_ACTION_RATE=0.1` | 접수된 대기표 중 10% 정도만 호출/시작/완료까지 진행 |

### 8.3 3단계: 결과 읽기

k6 실행이 끝나면 콘솔에 결과가 나온다. 처음에는 아래 3개만 본다.

| 볼 항목 | 좋게 나온 상태 |
|---|---|
| `checks` | 95% 이상 통과 |
| `http_req_failed` | 1% 미만 |
| `http_req_duration`의 `p(95)` | 조회는 1초 근처, 쓰기는 1.5초 근처 또는 그 이하 |

예를 들어 `http_req_failed`가 높으면 API 오류가 많이 난 것이다. `checks`가 낮으면 로그인 실패, 업무 유형 조회 실패, 현장 접수 실패 같은 시나리오 조건이 깨진 것이다.

### 8.4 4단계: 로컬에서 강도 조금 올리기

1단계가 성공하면 아래처럼 조금 올려본다.

```powershell
# performance/k6/k6.local.env에서 VUS=5, DURATION=1m, QUEUE_ACTION_RATE=0.3으로 수정한 뒤 실행

docker run --rm `
  --env-file performance/k6/k6.local.env `
  -v "${PWD}/performance/k6:/scripts" `
  grafana/k6:0.54.0 run /scripts/vaccination-queue-flow.js
```

### 8.5 5단계: VM/배포 서버 대상으로 실행

로컬에서 명령과 결과를 이해한 뒤에만 `BASE_URL`을 VM 주소로 바꾼다.

```powershell
# performance/k6/k6.local.env에서 BASE_URL만 VM 주소로 변경한 뒤 실행

docker run --rm `
  --env-file performance/k6/k6.local.env `
  -v "${PWD}/performance/k6:/scripts" `
  grafana/k6:0.54.0 run /scripts/vaccination-queue-flow.js
```

VM 대상으로 실행하면 VM DB에 테스트 방문자가 생성된다. 그래서 처음부터 큰 값으로 실행하지 않고 `VUS=3`, `DURATION=30s`로 먼저 확인한다.

이 단계는 VM 안에서 실행하는 것이 아니라, 내 PC에서 k6 Docker를 실행하고 VM의 공개 API 주소로 요청을 보내는 방식이다.

```text
내 PC의 k6 Docker -> VM 공개 URL -> VM backend -> VM PostgreSQL
```

그래서 VM에는 `performance/k6/k6.local.env`가 없어도 된다. Jenkins가 배포에 사용하는 루트 `.env`는 그대로 두고, 내 PC의 `performance/k6/k6.local.env`에서 `BASE_URL`만 VM 주소로 바꾼다.

확인 기준:

- `checks`가 대부분 통과한다.
- `http_req_failed`가 1% 미만이다.
- `http_req_duration` p95가 과도하게 튀지 않는다.
- Grafana에서 backend request rate와 latency가 증가했다가 정상으로 돌아온다.
- Loki에 대량 오류 로그가 남지 않는다.

## 9. 남은 위험

- 이 시나리오는 대상 DB에 실제 `K6Vaccine...` 방문/대기 데이터를 생성한다. 로컬이면 로컬 DB에, VM이면 VM DB에 쌓인다.
- 기본 직원 계정이 비활성화되어 있거나 비밀번호가 다르면 setup 단계에서 실패한다.
- `QUEUE_ACTION_RATE`를 높이면 상태 변경 API 부하가 커지고 미처리 대기표 수는 줄어든다.
- 예약 생성 동시성 테스트는 슬롯 정원과 seed 충돌 관리가 필요하므로 이번 브랜치에서 제외했다.
- Docker/k6 실행은 로컬 Docker 상태, 네트워크, VM 상태가 필요하므로 사용자가 직접 확인한다.

## 9.1 Grafana/Prometheus/Loki 확인 방법

k6 콘솔은 요청 성공 여부를 보여주고, Grafana는 backend가 그 부하를 받는 동안 어떤 상태였는지 보여준다.

| 도구 | 역할 | 확인할 것 |
|---|---|---|
| k6 | 부하 생성과 요청 결과 출력 | checks, 실패율, p95 응답 시간 |
| Prometheus | backend 메트릭 수집 | 요청량, 평균 응답 시간, JVM heap |
| Loki | backend 로그 수집 | ERROR 로그, 예외 반복 여부 |
| Grafana | Prometheus와 Loki 화면 조회 | 대시보드와 Explore |

Grafana에서 확인하는 순서:

1. `Health Center Backend Overview` 대시보드를 연다.
2. 시간 범위를 k6 실행 시간에 맞춘다. 예: `Last 15 minutes`
3. `HTTP Request Rate`가 k6 실행 중 올라갔다가 내려가는지 본다.
4. `Average HTTP Latency`가 계속 상승하지 않고 안정적인지 본다.
5. `JVM Heap Memory`가 급격히 계속 증가하지 않는지 본다.
6. `Backend Error Logs`에 반복 ERROR가 없는지 본다.

현재 dashboard의 Loki 기본 쿼리:

```logql
{service="backend"} |= "ERROR"
```

에러가 없으면 패널이 비어 있을 수 있다. 전체 backend 로그를 보고 싶으면 Grafana `Explore`에서 datasource를 `Loki`로 선택하고 아래 쿼리를 실행한다.

```logql
{service="backend"}
```

컨테이너 label이 다르게 보이면 아래처럼 넓게 조회한다.

```logql
{container=~".*backend.*"}
```

Loki 포트를 VM NAT로 따로 열 필요는 없다. Grafana datasource가 `access: proxy`, `url: http://loki:3100`으로 설정되어 있어 Grafana 컨테이너가 Docker 내부 네트워크에서 Loki를 조회한다.

결과 해석:

| k6 결과 | Grafana 결과 | 판단 |
|---|---|---|
| 실패율 0%, p95 낮음 | Request Rate 상승, Error Logs 없음 | 안정적 |
| 실패율 0%, p95 증가 | Latency도 증가 | 처리 가능하지만 느려지는 상태 |
| 실패율 증가 | Error Logs에 예외 반복 | API 오류 원인 확인 필요 |
| k6 요청이 있는데 Request Rate 변화 없음 | Prometheus target 또는 scrape 설정 확인 필요 |
| Error Logs가 항상 비어 있음 | 에러가 없거나 Promtail 수집 문제. Explore에서 전체 로그 확인 |

이번 VM VUS 5 결과는 k6 기준으로 안정적이다. Grafana에서는 같은 시간대의 Request Rate 상승과 Error Logs 반복 여부만 확인하면 된다.

## 10. 후속 작업

| 후보 | 이유 |
|---|---|
| Jenkins 수동 파라미터 기반 k6 smoke stage | 배포 후 짧은 부하 테스트를 자동화하기 위함 |
| k6 summary JSON 저장 | PR이나 포트폴리오에 결과를 남기기 위함 |
| 예약 생성 동시성 시나리오 | 정원 초과와 중복 예약 방지를 부하 상황에서 확인하기 위함 |
| 테스트 데이터 정리 스크립트 | VM DB에 남는 K6 데이터를 정리하기 위함 |

## 11. 커밋 메시지 초안

```text
test: 예방접종 대기 흐름 k6 부하 테스트 추가

- 직원 로그인 기반 예방접종 현장 접수 시나리오 작성
- 대기열 조회와 호출/시작/완료 상태 변경 부하를 환경변수로 조절
- Docker와 로컬 k6 실행 방법 문서화
- 부하 테스트 지표와 데이터 주의사항 정리
```
