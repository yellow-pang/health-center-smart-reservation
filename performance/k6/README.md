# k6 부하 테스트 가이드

## 목적

이 폴더는 보건소 API에 예방접종 현장 접수와 대기 처리 부하를 걸기 위한 k6 스크립트를 보관한다.

부하 테스트는 "많은 사용자가 동시에 요청했을 때 API가 어느 정도까지 안정적인지"를 확인하는 실험이다. 이번 시나리오는 고령층 방문이 몰릴 수 있는 예방접종 업무를 기준으로 한다.

처음부터 배포 VM에 부하를 걸지 않는다. 먼저 로컬 Docker Compose로 backend와 PostgreSQL을 띄운 뒤, k6 Docker 컨테이너로 로컬 backend를 테스트한다. 로컬에서 명령과 결과를 이해한 다음 `BASE_URL`만 VM 주소로 바꿔 배포 환경을 확인한다.

```text
로컬 Docker Compose backend
  <- k6 Docker 컨테이너가 요청
  <- 결과를 콘솔에서 확인
  ↓
익숙해지면 BASE_URL만 VM 주소로 변경
```

## 이번 시나리오

`vaccination-queue-flow.js`는 직원 계정으로 로그인한 뒤 아래 흐름을 반복한다.

1. `POST /api/auth/login`
2. `GET /api/service-types`에서 `VACCINATION` 업무 유형 ID 확인
3. `POST /api/visits/walk-in`으로 예방접종 현장 접수
4. `GET /api/queues?serviceTypeId={id}&status=WAITING`으로 대기열 조회
5. 일부 요청은 `call -> start -> complete` 상태 변경까지 진행

기본값은 작게 잡았다.

| 항목 | 기본값 | 의미 |
|---|---:|---|
| VUS | 5 | 동시에 움직이는 가상 사용자 수 |
| DURATION | 1m | 부하 유지 시간 |
| QUEUE_ACTION_RATE | 0.3 | 접수된 대기표 중 호출/시작/완료까지 처리하는 비율 |
| ERROR_RATE_THRESHOLD | 0.01 | 전체 요청 실패율 1% 미만 |
| READ_P95_MS | 1000 | 조회 API 95%가 1초 이내 |
| WRITE_P95_MS | 1500 | 생성/상태변경 API 95%가 1.5초 이내 |

## Docker 방식

기본 권장 방식이다. k6를 PC에 설치하지 않아도 된다.

환경변수 예시는 `performance/k6/k6.env.example`에 있고, 실제 실행용 로컬 파일은 `performance/k6/k6.local.env`이다. `k6.local.env`는 git에 올리지 않는 개인 실행 파일이며, 루트 `.env`와 섞지 않는다.

로컬 연습용 기본값은 이미 `k6.local.env`에 들어 있다.

```text
BASE_URL=http://host.docker.internal:8080
VUS=3
DURATION=30s
QUEUE_ACTION_RATE=0.1
```

VM/배포 서버를 테스트할 때는 `k6.local.env`의 `BASE_URL`만 VM 주소로 바꾸고, 처음에는 `VUS=3`, `DURATION=30s`를 유지한다.

중요한 구분:

| 파일 | 위치 | 용도 |
|---|---|---|
| 루트 `.env` | 프로젝트 루트 또는 VM/Jenkins 배포 작업공간 | backend/frontend/postgres를 실행하기 위한 앱 설정 |
| `performance/k6/k6.local.env` | 내 PC의 k6 폴더 | k6가 어느 API 주소로 몇 명의 가짜 사용자를 보낼지 정하는 테스트 설정 |

Jenkins가 읽는 루트 `.env`는 앱 컨테이너를 띄우기 위한 파일이다. k6 테스트용 값은 앱 실행 설정이 아니므로 Jenkins 배포 `.env`에 넣지 않는다.

### 1단계: 로컬 테스트 대상 서버 켜기

프로젝트 루트에서 로컬 Docker Compose를 먼저 실행한다.

```powershell
docker compose --env-file .env up -d postgres backend
```

실행 상태를 확인한다.

```powershell
docker compose --env-file .env ps
```

`backend`와 `postgres`가 실행 중이면 k6를 실행할 수 있다.

### 2단계: Docker k6로 로컬 backend 테스트

PowerShell에서 프로젝트 루트 기준으로 아래 한 번만 실행한다.

```powershell
docker run --rm `
  --env-file performance/k6/k6.local.env `
  -v "${PWD}/performance/k6:/scripts" `
  grafana/k6:0.54.0 run /scripts/vaccination-queue-flow.js
```

Docker 컨테이너 안에서 `localhost`는 컨테이너 자기 자신이다. 그래서 Windows/Mac에서 Docker k6가 내 PC의 backend로 접근하려면 `http://host.docker.internal:8080`을 사용한다.

### 3단계: VM/배포 서버 대상으로 실행

로컬에서 성공한 뒤 `performance/k6/k6.local.env`의 `BASE_URL`만 VM 주소로 바꾼다.

```text
BASE_URL=https://your-vm-domain.example.com
VUS=3
DURATION=30s
QUEUE_ACTION_RATE=0.1
```

그 다음 같은 명령을 다시 실행한다.

```powershell
docker run --rm `
  --env-file performance/k6/k6.local.env `
  -v "${PWD}/performance/k6:/scripts" `
  grafana/k6:0.54.0 run /scripts/vaccination-queue-flow.js
```

VM 대상으로 실행하면 VM DB에 테스트 방문자가 생성된다. 처음에는 `VUS=3`, `DURATION=30s`로 짧게 확인한 뒤 올린다.

이 방식은 "내 PC에서 k6를 실행하고, 요청만 VM의 공개 API 주소로 보내는 방식"이다. VM 안에 `k6.local.env` 파일이 없어도 된다. 배포 VM에는 Jenkins가 앱을 띄울 때 쓰는 루트 `.env`만 있으면 된다.

```text
내 PC의 k6 Docker
  -> https://your-vm-domain.example.com/api/...
  -> VM의 backend
  -> VM의 PostgreSQL
```

결과 JSON을 파일로 남기려면 아래처럼 `--summary-export`를 붙인다.

```powershell
docker run --rm `
  --env-file performance/k6/k6.local.env `
  -v "${PWD}/performance/k6:/scripts" `
  grafana/k6:0.54.0 run `
  --summary-export /scripts/results/vaccination-queue-summary.json `
  /scripts/vaccination-queue-flow.js
```

VM 내부에서 k6 Docker를 실행하는 방식은 선택이다. 이 경우에는 VM에 repo checkout과 `performance/k6/k6.local.env`가 있어야 하므로, 처음에는 권장하지 않는다. 꼭 VM 내부에서 실행한다면 `performance/k6/k6.env.example`을 복사해 `performance/k6/k6.local.env`를 만들고 `BASE_URL`을 VM 내부에서 접근 가능한 backend 주소로 설정한다.

```bash
cp performance/k6/k6.env.example performance/k6/k6.local.env
# k6.local.env의 BASE_URL 수정 후 실행

docker run --rm \
  --env-file performance/k6/k6.local.env \
  -v "$PWD/performance/k6:/scripts" \
  grafana/k6:0.54.0 run /scripts/vaccination-queue-flow.js
```

## 로컬 설치 방식

k6를 직접 설치했다면 Docker k6 대신 아래처럼 실행한다. 이 경우 k6가 PC에서 직접 실행되므로 로컬 backend 주소는 `http://localhost:8080`을 사용한다.

PowerShell:

```powershell
$env:BASE_URL="http://localhost:8080" # k6 직접 설치 방식에서는 localhost 사용
k6 run performance/k6/vaccination-queue-flow.js
```

Bash:

```bash
BASE_URL=http://localhost:8080 \
STAFF_EMAIL=staff@test.com \
STAFF_PASSWORD=password1234 \
VUS=3 \
DURATION=30s \
QUEUE_ACTION_RATE=0.1 \
k6 run performance/k6/vaccination-queue-flow.js
```

## 강도 조절

처음에는 아래 순서로 올린다.

| 단계 | VUS | DURATION | QUEUE_ACTION_RATE | 목적 |
|---:|---:|---|---:|---|
| 1 | 3 | 30s | 0.1 | 연결과 인증 확인 |
| 2 | 5 | 1m | 0.3 | 기본 실전형 확인 |
| 3 | 10 | 3m | 0.3 | VM이 안정적인지 확인 |
| 4 | 20 | 5m | 0.5 | 병목 후보 확인 |

실패율이 오르거나 p95 응답 시간이 크게 튀면 더 올리지 않는다.

## Grafana에서 결과 확인하기

k6 콘솔은 "테스트 요청 자체가 성공했는지"를 보여준다. Grafana는 "테스트를 받는 서버가 그 순간 어떤 상태였는지"를 보여준다.

역할은 이렇게 나뉜다.

| 도구 | 보는 것 | 이번 확인에서의 의미 |
|---|---|---|
| k6 콘솔 | 실패율, 응답 시간, checks | 부하 테스트 요청이 성공했는지 |
| Prometheus | backend 메트릭 수집 | API 요청량, 평균 응답 시간, JVM 메모리 |
| Loki | backend 로그 수집 | 부하 중 에러 로그가 남았는지 |
| Grafana | Prometheus와 Loki를 화면으로 조회 | 메트릭과 로그를 한 곳에서 확인 |

Loki는 브라우저에서 직접 열 필요가 없다. Grafana가 Docker 내부 네트워크에서 `http://loki:3100`으로 대신 조회한다. 따라서 VM NAT/포트포워딩은 Grafana 접속 포트만 열려 있어도 Grafana 안에서 Loki 로그를 볼 수 있다.

### 1단계: Grafana 대시보드 열기

1. Grafana에 접속한다.
2. `Dashboards` 메뉴를 연다.
3. `Health Center Backend Overview` 대시보드를 연다.
4. 오른쪽 위 시간 범위를 k6 실행 시간에 맞춘다.
   - 방금 실행했다면 `Last 15 minutes`
   - 조금 전에 실행했다면 `Last 30 minutes`
5. 새로고침 간격은 `10s` 정도로 둔다.

### 2단계: Prometheus 패널 보기

`Health Center Backend Overview`에서 아래 패널을 본다.

| 패널 | 무엇을 보는가 | k6 중 정상적으로 보이는 모습 |
|---|---|---|
| HTTP Request Rate | 초당 API 요청 수 | k6 실행 중 그래프가 올라갔다가 종료 후 내려감 |
| Average HTTP Latency | API 평균 응답 시간 | 튀더라도 계속 상승하지 않고 안정적으로 유지 |
| JVM Heap Memory | backend heap 메모리 | 완만히 움직이고 급격히 계속 증가하지 않음 |

해석 기준:

- Request Rate가 올라가면 k6 요청이 backend까지 도착한 것이다.
- Latency가 k6 콘솔 p95보다 낮거나 다르게 보일 수 있다. Grafana 기본 패널은 평균값이고, k6는 p95를 따로 보여주기 때문이다.
- JVM Heap이 테스트가 끝난 뒤에도 계속 올라가기만 하면 메모리 누수 후보를 의심한다. 짧은 테스트에서는 보통 완만한 변화만 확인하면 충분하다.

### 3단계: Loki 로그 보기

대시보드 아래의 `Backend Error Logs` 패널을 본다.

현재 기본 쿼리는 아래와 같다.

```logql
{service="backend"} |= "ERROR"
```

정상적인 결과:

- k6 실행 중 새로운 `ERROR` 로그가 거의 없거나 없다.
- `walk-in`, `queue`, `auth` 관련 예외가 반복해서 쌓이지 않는다.

로그가 안 보일 때 확인할 점:

- 에러가 없으면 `Backend Error Logs` 패널이 비어 있는 것이 정상일 수 있다.
- 전체 backend 로그를 보고 싶으면 Grafana `Explore`에서 datasource를 `Loki`로 고르고 아래 쿼리를 실행한다.

```logql
{service="backend"}
```

컨테이너 label이 다르게 잡힌 경우에는 더 넓게 본다.

```logql
{container=~".*backend.*"}
```

그래도 아무것도 안 나오면 Promtail이 Docker 로그를 못 읽는 상태일 수 있다. 이때는 VM에서 `health-center-promtail`, `health-center-loki`, `health-center-backend` 컨테이너가 떠 있는지 확인한다.

### 4단계: k6 콘솔과 Grafana를 함께 해석하기

| k6 결과 | Grafana 결과 | 판단 |
|---|---|---|
| 실패율 0%, p95 낮음 | Request Rate 상승, Error Logs 없음 | 가장 좋은 상태 |
| 실패율 0%, p95 증가 | Latency도 같이 증가 | 부하는 처리하지만 느려지는 상태 |
| 실패율 증가 | Error Logs에 예외 반복 | API 오류 원인부터 확인 |
| k6 요청 수가 있는데 Grafana Request Rate 변화 없음 | Prometheus scrape 설정 문제 가능 | `/actuator/prometheus`와 Prometheus target 확인 |
| Error Logs가 항상 비어 있음 | 실제 에러가 없거나 Promtail 문제 | `Explore`에서 `{service="backend"}`로 전체 로그 확인 |

이번 VM VUS 5 테스트 결과는 k6 기준으로는 안정적이다.

```text
checks: 100%
http_req_failed: 0%
전체 p95: 244.67ms
walk_in p95: 181.96ms
queue_list p95: 278.78ms
queue_action p95: 174.97ms
```

Grafana에서는 같은 시간대에 Request Rate가 올라갔다가 내려가고, Backend Error Logs에 반복 ERROR가 없는지 확인하면 된다.

## k6 콘솔에서 확인할 지표

k6 콘솔에서 본다.

- `http_req_failed`: 요청 실패율
- `http_req_duration`: 전체 응답 시간
- `checks`: 시나리오별 성공 조건 통과율

Grafana에서 본다.

- API request rate
- API latency p95
- error rate
- JVM heap
- backend error log

## 데이터 주의

이 시나리오는 대상 DB에 `K6Vaccine{VU}_{ITER}` 방문자를 생성한다. 로컬 `BASE_URL`이면 로컬 DB에, VM `BASE_URL`이면 VM DB에 쌓인다. 포트폴리오 검증용 VM에서만 실행하고, 운영 데이터와 섞이면 안 되는 환경에서는 실행하지 않는다.

테스트 후 미처리 대기표가 많이 남으면 관리자 화면의 대기 마감 기능이나 `POST /api/queues/admin/close-pending`으로 정리한다.

## 다음 자동화 방향

1. Jenkins에서 수동 파라미터로 k6 smoke 실행
2. k6 결과를 `--summary-export` JSON으로 저장
3. Grafana 대시보드와 함께 PR 체크리스트에 결과 캡처 첨부
4. 예약 생성 동시성 시나리오는 별도 브랜치에서 seed 충돌 방지 전략을 정한 뒤 추가
