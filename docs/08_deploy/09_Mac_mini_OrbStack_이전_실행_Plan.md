# Mac mini + OrbStack 이전 구현·검증 Plan

> 실행 담당자: 이 문서는 실행 승인이나 실행 완료 기록이 아니다. 구현을 시작할 때 `superpowers:executing-plans` 등 실행 절차를 적용하더라도 아래 범위·Stop Point·사용자 지시를 우선한다. 체크박스는 실제 결과를 확인한 뒤 갱신한다.

**목표:** 기존 Health Center Compose를 Mac에서 새 DB로 재현하고, 관측성·Cloudflare 외부 접근·재부팅 복구까지 검증하여 상시 운영 가능 여부를 판정한다.

**구조:** 현재 Dockerfile/Compose/서비스 계약을 유지한다. 환경 차이는 환경변수로 처리하고 실제 blocker가 확인될 때만 기존 설정을 최소한으로 수정한다. 기존 VM은 rollback 대상으로 보존한다.

**기술:** Apple Silicon Mac mini, OrbStack, Docker Compose, 기존 Java 17/Maven·Node 20/Next.js, PostgreSQL/pgvector, Prometheus/Grafana/Loki/Promtail, Cloudflare Tunnel.

**기준:** [08_Mac_mini_OrbStack_이전_사전_분석.md](08_Mac_mini_OrbStack_이전_사전_분석.md), 기준 HEAD `c300bf0342ee2b8558776639a4c3a19af72a659c`, 작성일 2026-09-20.

## 1. Analysis 검토 결과와 실행 전제

### Analysis clarification / execution assumption

기존 Analysis는 Plan 작성에 충분하다. 배포 구조·DB·이미지 지원을 다시 분석하지 않는다. 추가로 필요한 것은 실제 실행 단계에서 수집할 환경 상태와 운영 설정이다. Analysis 파일은 수정하지 않는다.

| 보정 항목 | Plan에서 적용할 기준 |
|---|---|
| `DOCKER_DEFAULT_PLATFORM=linux/arm64` | Phase 0 필수값으로 사용하지 않는다. Apple Silicon OrbStack의 기본 선택으로 실행한다. 기존 shell 강제가 있으면 원인을 확인하고 해당 세션에서 해제한다. 잘못된 아키텍처 선택이 실제 확인된 경우에만 진단 옵션으로 사용한다. |
| `SPRING_PROFILES_ACTIVE=prod` | 강제 export하지 않는다. 기존 `.env`와 운영 profile을 사용자가 확인한다. Compose의 미설정/빈 값 기본은 `prod`, 애플리케이션 properties 기본은 `dev`이므로 실제 Compose 경로의 적용값을 기준으로 판단한다. 운영값과 다르면 이유를 확인한 뒤 결정한다. |
| 과거의 깨끗한 Git 상태 | 현재 작업 트리에는 이전에 만든 Analysis와 README 변경이 있다. 이를 사용자 작업으로 보존한다. 실행 직전 상태를 다시 기록한다. |
| 임시 Compose project name | 이전 Analysis의 `health-center-mac-check`는 예시다. 이 Plan은 Mac에서 `health-center`를 일관되게 사용한다. 예시를 이미 실행했다면 기존 컨테이너/volume의 소유 project를 먼저 확인하고 이름을 임의로 바꾸지 않는다. |
| shell override | 최초 로컬 검증에만 사용 가능하다. 검증된 설정은 상시 운영 전에 Mac의 기존 `.env`에 반영하고 shell override 없이 재생성 가능한지 확인한다. |
| 재부팅 복구 | 컨테이너 `restart: unless-stopped`만으로 Mac 부팅·OrbStack 시작·Tunnel 복구가 보장되지 않는다. 로그인 전 복구와 로그인 후 복구를 분리해서 판정한다. |

관련 설정만 좁게 확인했다: [Compose](../../docker-compose.yml), [prod properties](../../backend/src/main/resources/application-prod.properties), [dev properties](../../backend/src/main/resources/application-dev.properties). 기존 분석의 핵심 결론을 뒤집는 사실은 발견하지 않았다.

**완료 기준:** 기본 서비스와 기존 관측성이 정상이고, 공개 Frontend가 공개 Backend를 호출하며, Mac 재부팅 후 사용자 로그인·수동 `orb start`·수동 `compose up` 없이 합의한 복구 시간 안에 외부 서비스가 돌아와야 한다. 본 Plan의 초기 검증 한도는 10분이다. 로그인/디스크 잠금 해제가 필요하면 그 조건을 기록하고 무인 복구 미달로 판정한다. 목표를 조용히 낮추지 않는다.

## 2. 공통 제약과 변경 분류

### Global Constraints

- 이번 문서 작성에서는 Plan 파일 하나만 생성한다. 코드, Analysis, README, 실행 환경은 변경하지 않는다.
- 이후 실행 시에도 앞 Phase의 성공 조건이 충족되기 전 다음 변경을 시작하지 않는다.
- 실제 `.env`·Token·비밀번호·개인 데이터는 출력·문서화하지 않는다. 환경 계약 확인은 소유자가 비공개로 하고 결과만 기록한다.
- `source .env`, `set -x`, 전체 `env`, 무필터 `docker inspect`, `compose config` 전체 출력은 사용하지 않는다. 인증 응답·HAR·로그 원문도 저장소에 붙이지 않는다.
- 기존 VM/DB/Jenkins home/Tunnel/volume/배포 경로는 이전 완료 판단 전 삭제·정리하지 않는다.
- `down -v`, volume prune, system prune, 무차별 image prune, 자동 cleanup은 이 Plan에 없다.
- runtime upgrade, 서비스 로직 변경, CI/CD 전환은 기본 재현의 선행 조건이 아니다.
- 새 profile, 중복 helper, wrapper, 배포 framework를 선제 추가하지 않는다.
- 명령은 해당 Step에서만 실행한다. 문서 전체를 일괄 복사 실행하지 않는다. 실패하면 멈추고 분류한다.

| 분류 | 의미 | 대상 |
|---|---|---|
| A. 반드시 해야 함 | 이전 완료 판정에 필요 | 사전 충돌 확인, 기존 Compose 실행, 새 DB/실제 API, 기존 관측성, Tunnel 실구성 조사·이전, 재부팅/절전/복구, 데이터 보존 정책 결정 |
| B. 실제 실패 시에만 수정 | 증거가 있는 blocker만 해결 | native 의존성, image, mount/path, 환경값, CORS, 기동 순서·자동 복구 장애 |
| C. 이전 완료 후 개선 | 기본 migration과 분리 | Jenkins 제거·새 CI/CD, runtime upgrade, 이미지 최적화, seed/migration 체계, backup/retention 자동화 |
| D. 하지 않음 | 현재 목표 밖 | Kubernetes/MSA, 새 config/script framework, 업무 로직 재설계, 반복 unit test, 미래를 위한 compatibility 경로 |

### 파일·설정 책임 경계

| 대상 | 역할과 변경 조건 |
|---|---|
| `docker-compose.yml`, 기존 Dockerfile 7개 | Phase 0/0-B 읽기만. Phase 1에서 실제 blocker에 해당하는 기존 파일만 변경 |
| 루트 `.env` | Secret 공급과 환경별 차이. Phase 0 소유자 비공개 확인, Phase 2 운영값 정착. Git에 추가하지 않음 |
| `infra/observability/promtail/promtail-config.yml` 등 | 수집 실패의 근거가 있을 때만 기존 설정 수정 |
| Backend/Frontend 서비스 코드 | 원칙적으로 변경 없음. 필요하면 Phase 1의 별도 계약 검토를 통과해야 함 |
| Mac OrbStack/전원 설정 | 서버 검증 단계에서 확인 후 필요한 항목만 변경 |
| Mac cloudflared 서비스 및 설정 | Phase 2에서 실제 관리 방식에 맞춰 구성. Linux systemd를 Mac에 복제하지 않음 |
| `Jenkinsfile`, `infra/jenkins/*`, CI workflow | Phase 3 이전에는 변경 없음 |

### Review Focus

1. 다른 Compose project라도 고정 container/image 이름이 충돌할 수 있다 → Step 0-1에서 소유권 확인.
2. 새 DB 초기화 계정과 Backend 접속 계정의 불일치, shell override 잔존 → Step 0-2/0-3, Step S-1에서 확인.
3. 브라우저 번들이 옛 VM/API 주소를 계속 사용할 수 있다 → Step 0-4/2-2에서 실제 Network 요청 확인.
4. 같은 Tunnel의 VM/Mac connector가 서로 다른 DB로 요청을 분산할 수 있다 → Step 2-1/2-3에서 전환 경로 분리.
5. 로그인·FileVault·수동 중지 상태가 재부팅 복구를 막을 수 있다 → Step S-1/S-3에서 외부 관찰로 판정.

## 3. Phase 순서와 공통 검증 규칙

```text
Phase 0 기본 3종 재현
→ 기본 서비스 blocker가 있으면 Phase 1에서 해당 문제만 해결 후 실패 Step 재검증
→ Phase 0-B 기존 Observability
→ 관측성 blocker가 있으면 Phase 1에서 해당 문제만 해결 후 실패 Step 재검증
→ Phase 1 변경 필요 없음 또는 blocker 해결 확인
→ Phase 2 실제 Cloudflare 조사·외부 접근 전환
→ Phase 2-S 서버 운영·재부팅 검증
→ 이전 완료 판정·기존 VM 종료 여부 별도 결정
→ Phase 3 Jenkins/CI 개선(별도 작업)
→ Phase 4 후속 유지보수
```

Phase 1은 예방적 변경 묶음이 아니라 실패한 Step을 해결하는 조건부 경로다. 실패가 없으면 수정 없이 통과한다.

검증 결과에는 Step ID, branch/HEAD, 실행 시각, 성공/실패, 검출한 실패, 비민감 증거, 변경한 변수 이름, rollback 상태만 기록한다. 체크리스트 통과에 Secret이나 전체 로그는 필요 없다.

Health는 상태를 확인하며 기다린다. 컨테이너/API 2분, 새 DB 최초 준비 3분, build 15분을 초기 조사 한도로 사용하되 정상 진행 중인 다운로드를 실패로 단정하지 않는다. 한도 초과 시 진행 상태와 원인을 조사한다. 무조건 긴 fixed sleep을 넣거나 자동으로 전체 build를 반복하지 않는다.

공통 명령의 작업 경로는 저장소 루트다. 아래 명령은 Mac에서만 실행하며 Docker 대상은 항상 `--context orbstack`, project는 `-p health-center`, env는 `--env-file .env`로 고정한다. 다른 터미널을 열면 환경 계약을 다시 확인한다.

## 4. Phase 0 — 기존 구조 그대로 재현

### Step 0-1 [A] 실행 대상과 충돌 확인

**목적/근거:** 기존 Mac 리소스나 다른 Docker Engine에 영향을 주지 않고 출발점을 확정한다. Analysis의 리소스 조회 결과는 과거 시점의 결과다.

**변경/대상 파일:** 없음. Git·Engine·리소스 metadata 읽기만.

- [ ] 다음 명령으로 branch/HEAD, 변경 파일, context, 현재 리소스를 확인한다.

```bash
pwd
git branch --show-current
git rev-parse HEAD
git status --short --untracked-files=all
docker context show
docker --context orbstack compose version
docker --context orbstack info --format 'OS={{.OperatingSystem}} Arch={{.Architecture}} Memory={{.MemTotal}}'
docker --context orbstack ps -a --filter name=health-center --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'
docker --context orbstack volume ls --filter name=health-center
docker --context orbstack image ls --filter 'reference=health-center-*'
lsof -nP -iTCP:3000 -iTCP:8080 -iTCP:5432 -iTCP:3001 -iTCP:9090 -iTCP:3100 -sTCP:LISTEN
test -f .env && test -r .env
```

`lsof`가 결과 없이 종료 코드 1이면 해당 조회에서 listener를 찾지 못한 것이다. 권한 때문에 보이지 않는 경우와 구분한다. build 직전 기존 local image가 있으면 ID와 사용 컨테이너를 확인한다. project name만 다르게 지정해도 `health-center-frontend:local` 등의 tag 충돌은 피할 수 없다.

**성공 조건/검출 실패:** OrbStack 대상 확인, 변경 파일 보존, 고정 이름·tag·포트·volume 충돌 없음. Docker context 착오와 기존 리소스 덮어쓰기를 검출한다.

**실패 시:** 소유 project·사용자·포트 용도를 확인한다. 기존 리소스를 삭제하거나 프로세스를 종료하지 않는다. 다른 포트를 쓰면 URL/CORS도 함께 맞춘다. 이전 검증용 volume이 있으면 보존/재사용 여부를 결정하고 ‘새 DB 검증’과 구분한다.

**Rollback/보류:** 읽기만이므로 불필요. cleanup 자동화는 Phase 4.

### Step 0-2 [A] 환경 계약 확인과 Compose validation

**목적/근거:** 옛 VM으로 연결하거나 잘못된 계정으로 새 DB를 초기화하는 것을 방지한다. `.env`를 실행 코드로 source하지 않는다.

**변경/대상 파일:** 저장소 파일 변경 없음. 소유자가 `.env`의 관계만 비공개 확인. 필요한 비민감 로컬 값만 전용 터미널에서 override.

- [ ] 아래 관계의 통과/실패만 기록한다.

| 변수/조건 | 확인할 계약 |
|---|---|
| `POSTGRES_DB` ↔ `DB_NAME` | 동일 DB |
| `POSTGRES_USER` ↔ `DB_USERNAME` | 동일 또는 적절한 schema 생성 권한을 가진 접속 계정 |
| `POSTGRES_PASSWORD` ↔ `DB_PASSWORD` | 해당 계정 인증 일치. 값을 기록하지 않음 |
| `DB_HOST`, `DB_PORT` | 새 Compose의 `postgresql:5432`. VM/외부 DB를 가리키면 시작 중지 |
| `SPRING_PROFILES_ACTIVE` | 기존 운영 profile 확인. 미설정이면 Compose 기본 prod. shell의 의도치 않은 override 없는지 확인 |
| `SPRING_SQL_INIT_*`, 기타 추가 설정 | 초기화 비활성화·외부 설정 override가 없는지 소유자 확인 |
| JWT/OAuth/Grafana 관련 Secret | 필요한 값의 존재·유효성만 확인. 예제 fallback을 운영 Secret으로 채택하지 않음 |
| `NEXT_PUBLIC_*`, CORS | 로컬 Frontend/Backend 주소 계약 일치 |
| `APP_TIME_ZONE`, `DB_TIME_ZONE` | seed 날짜와 API 날짜 조회 기준 일치 |
| shell/Compose 설정 | platform, profiles, file 목록, URL override가 의도하지 않게 남아 있지 않은지 확인 |

운영 profile을 아직 알 수 없으면 소유자 확인에서 멈춘다. `prod`를 추정하여 강제하지 않는다. 현재 `.env`에 맞지 않는 DB 계정이 있으면 비공개 설정만 바로잡은 뒤 시작하며 코드 변경으로 우회하지 않는다.

- [ ] 최초 실행에서는 platform을 지정하지 않는다. 해당 터미널에 기존 `DOCKER_DEFAULT_PLATFORM`이 설정된 경우에만 해제한다.
- [ ] 기본 포트를 사용할 수 있을 때 아래 비민감 override를 적용한다. 이미 같은 값이어도 검증 대상 주소를 명확히 하기 위한 세션 설정이다.

```bash
export DB_HOST=postgresql DB_PORT=5432
export NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
export NEXT_PUBLIC_APP_URL=http://localhost:3000
export CORS_ALLOWED_ORIGINS=http://localhost:3000
export POSTGRES_PORT=127.0.0.1:5432
export BACKEND_PORT=127.0.0.1:8080
export FRONTEND_PORT=127.0.0.1:3000
docker --context orbstack compose -p health-center --env-file .env config --quiet
```

`SPRING_PROFILES_ACTIVE`와 `DOCKER_DEFAULT_PLATFORM` export는 의도적으로 없다. localhost bind는 기존 port 변수로 처리하며 Compose 재설계가 아니다.

**성공 조건/검출 실패:** 변수 관계 확인 완료, `config --quiet` 종료 코드 0. 구문·보간 오류를 검출하지만 계정 인증과 이미지 호환성까지 검증하지는 않는다.

**실패 시:** 누락 변수·값 형식·shell 우선순위만 조사. 전체 config를 로그에 출력하지 않는다.

**Rollback/보류:** 전용 터미널 종료로 임시 override 해제. OAuth 공개 URL 검증은 Phase 2, Secret 공급 자동화는 Phase 3.

### Step 0-3 [A] 이미지 build, 새 PostgreSQL, Backend 순차 기동

**목적/근거:** 이미지/build 오류, DB readiness, Spring schema/data 초기화를 서로 구분한다.

**변경/대상 파일:** 저장소 변경 없음. Mac에 이미지·network·새 named volume·컨테이너가 생성된다. Backend 기동은 DB 쓰기를 포함한다.

- [ ] 기존 tag 충돌이 없음을 확인한 뒤 기존 Dockerfile로 한 번 build한다.

```bash
docker --context orbstack compose -p health-center --env-file .env build backend frontend
docker --context orbstack image inspect health-center-backend:local health-center-frontend:local --format '{{.Id}} {{.Os}}/{{.Architecture}}'
docker --context orbstack compose -p health-center --env-file .env up -d postgresql
docker --context orbstack inspect health-center-postgres --format '{{.State.Status}} {{if .State.Health}}{{.State.Health.Status}}{{end}}'
docker --context orbstack inspect health-center-postgres --format '{{range .Mounts}}{{.Type}} {{.Name}} {{.Destination}}{{println}}{{end}}'
```

- [ ] DB가 `healthy`가 된 뒤 Backend를 기동한다. volume 이름과 `/var/lib/postgresql` mount를 기록하고 기존 VM volume이 아닌 새 Mac volume임을 확인한다.

```bash
docker --context orbstack compose -p health-center --env-file .env up -d --no-build backend
curl --fail --silent --show-error --max-time 5 http://localhost:8080/actuator/health
docker --context orbstack compose -p health-center --env-file .env exec -T postgresql sh -c 'exec psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1' <<'SQL'
SELECT to_regclass('public.members'), to_regclass('public.service_types'), to_regclass('public.reservation_slots');
SELECT COUNT(*) AS service_type_count FROM service_types;
SELECT COUNT(*) AS reservation_slot_count FROM reservation_slots;
SQL
```

SQL은 존재·건수만 읽는다. 계정·비밀번호·회원 데이터를 출력하지 않는다. Backend health는 준비 전 실패할 수 있으므로 상태를 보며 재확인한다. 오류 로그가 필요하면 소유자가 로컬에서 해당 서비스의 제한된 최근 구간을 확인하고 비민감 오류만 요약한다.

**성공 조건/검출 실패:** build 성공, 선택된 이미지가 native arm64, PostgreSQL healthy, Backend `UP`, 세 테이블 존재·기준 데이터 생성. 의존성 다운로드 실패, 잘못된 이미지 선택, DB 인증·DDL/seed 실패를 검출한다.

**실패 시:** 표 1의 원인 분류로 이동. volume 삭제로 초기화 실패를 숨기지 않는다. 반쯤 적용된 SQL이 있다면 범위를 확인한다. 로컬에서 성공했더라도 호스트 JDK/npm으로 다시 중복 build하지 않는다.

**Rollback/보류:** 이번 project에서 만든 컨테이너만 `compose stop`으로 멈출 수 있다. volume은 남긴다. 이 동작은 DB 쓰기를 되돌리지 않는다. migration framework와 image 최적화는 Phase 4.

### Step 0-4 [A] Frontend와 기본 사용자 흐름 확인

**목적/근거:** `Up` 상태와 실제 사용 가능성을 구분한다. 화면만 열리고 옛 API를 호출하는 경우도 실패다.

**변경/대상 파일:** 저장소 변경 없음. Frontend 기동, 로그인에 따른 새 DB 토큰 데이터 생성 가능.

```bash
docker --context orbstack compose -p health-center --env-file .env up -d --no-build frontend
docker --context orbstack compose -p health-center --env-file .env ps
curl --silent --show-error --output /dev/null --write-out '%{http_code}\n' --max-time 5 http://localhost:3000/login
```

- [ ] 브라우저에서 `/login`을 열고 시연용 일반 계정으로 로그인한다. 비밀번호·Token은 기록하지 않는다.
- [ ] 업무 유형을 조회하고 반환된 실제 `serviceTypeId`를 선택한다.
- [ ] 예약 화면에서 생성된 기간의 날짜를 선택하여 슬롯을 조회한다.
- [ ] Network에서 로그인·업무·슬롯 요청이 `http://localhost:8080`을 향하며 CORS 오류가 없는지 확인한다.

Swagger로 확인할 경우 대표 조회 하나만 사용한다: `GET /api/reservation-slots`, `serviceTypeId`는 업무 조회 결과, `date`는 앱 시간대 기준 내일의 `YYYY-MM-DD`. 예상 결과는 성공 응답과 슬롯 목록이다. 숫자 ID·과거 날짜를 고정하지 않는다. 화면 검증과 같은 계약을 새 자동 테스트로 중복 작성하지 않는다.

**성공 조건/검출 실패:** 로그인 및 두 조회가 성공하고 API target·CORS가 맞음. 잘못된 번들 URL, 인증 설정, seed 날짜, CORS 문제를 검출한다.

**실패 시:** Network status/target → Backend health → 인증·CORS·seed 순으로 좁힌다. URL 변경이면 Frontend만 재빌드한다. 업무 로직부터 고치지 않는다.

**Rollback/보류:** 새 Mac 컨테이너만 정지 가능, VM 영향 없음. 소셜 로그인/외부 도메인은 Phase 2.

**Stop Point 0:** 0-1~0-4가 모두 통과해야 기본 재현 성공이다. blocker가 있으면 Phase 1에서 해당 원인만 해결하고 이 Step으로 돌아온다. 통과하면 Phase 0-B로 진행한다.

## 5. Phase 0-B — 기존 Observability 검증

### Step OB-1 [A] 기존 profile 기동과 지표 검증

**목적/근거:** 기존 profile을 그대로 사용하여 운영 지표 수집을 확인한다. 기본 서비스 정상 상태를 전제로 해야 관측성 오류를 분리할 수 있다.

**변경/대상 파일:** 저장소 변경 없음. 모니터링 image/volume/container 생성. scrape 허용을 위해 Backend가 재생성될 수 있으며 seed가 다시 적용된다.

```bash
export OBSERVABILITY_PROMETHEUS_SCRAPE_ENABLED=true
export PROMETHEUS_PORT=127.0.0.1:9090
export GRAFANA_PORT=127.0.0.1:3001
export LOKI_PORT=127.0.0.1:3100
export GRAFANA_ROOT_URL=http://localhost:3001
docker --context orbstack compose -p health-center --env-file .env --profile observability config --quiet
docker --context orbstack compose -p health-center --env-file .env --profile observability build prometheus grafana loki promtail
docker --context orbstack compose -p health-center --env-file .env --profile observability up -d --no-build postgresql backend frontend prometheus grafana loki promtail
```

- [ ] Prometheus `http://localhost:9090/targets`에서 Backend target `UP` 확인.
- [ ] Grafana `http://localhost:3001` 로그인, 두 datasource 연결 확인.
- [ ] 기존 `Health Center Backend Overview`에서 기본 API 사용 후 요청률·JVM 지표 확인.

**성공 조건/검출 실패:** 컨테이너 존재뿐 아니라 scrape 성공과 datasource 조회 성공. endpoint 권한·DNS·잘못된 datasource·label 불일치를 검출한다. 트래픽 없는 요청률의 0값은 실패가 아니다.

**실패 시:** Backend health/metrics 접근, scrape 옵션·exposure 설정, 내부 DNS, datasource UID와 metric label 순으로 확인한다.

**Rollback/보류:** 새 관측성 서비스만 정지하고 기본 서비스 유지 가능. 외부 Actuator 경로 보호는 Step 2-2, profile 재설계는 하지 않는다.

### Step OB-2 [A, 실패 수정은 B] Loki·Promtail 로그와 자원 확인

**목적/근거:** OrbStack에서 기존 Docker socket/log 접근이 실제 동작하는지 검증한다.

**변경/대상 파일:** 기본적으로 없음. 로그가 부족할 때만 해당 앱 컨테이너를 한 번 재시작하여 기존 startup 로그로 확인한다. 새 logging code를 추가하지 않는다.

```bash
curl --fail --silent --show-error --max-time 5 http://localhost:3100/ready
docker --context orbstack inspect health-center-promtail --format '{{range .Mounts}}{{.Source}} -> {{.Destination}} (RW={{.RW}}){{println}}{{end}}'
docker --context orbstack stats --no-stream health-center-postgres health-center-backend health-center-frontend health-center-prometheus health-center-grafana health-center-loki health-center-promtail
docker --context orbstack system df -v
df -h /System/Volumes/Data
```

- [ ] Grafana Explore에서 `{service="backend"}`, `{service="frontend"}`를 최근 시간 범위로 각각 조회한다. 실제 컨테이너 로그의 시각·메시지와 대응시킨다. Secret 포함 로그는 복사하지 않는다.
- [ ] Frontend는 요청별 로그를 반드시 남긴다고 가정하지 않는다. 기존 startup 로그를 사용한다. 필요해서 수행한 재시작은 S-2의 같은 컨테이너 재시작 증거로 재사용한다.
- [ ] Mac Activity Monitor의 memory pressure, swap 변화와 Docker 자원 사용을 기록한다. `stats`는 build peak를 보여주지 않으므로 이미 본 build 중 메모리 압력도 함께 기록한다.

**성공 조건/검출 실패:** 두 서비스의 로그가 Loki에 들어오고 Mac이 OOM·지속적인 메모리 압력 없이 기본 화면/지표를 제공한다. socket 권한, Docker API 호환성, discovery/label, log driver/path, Loki 전송 실패를 검출한다.

**실패 시:** Promtail 자체 오류 → Docker discovery/API → 실제 logging driver/필요 mount → Loki 전송 → query label/time range 순으로 조사한다. 특정 경로가 있다는 이유만으로 파일 scraping만 수행한다고 단정하지 않는다. Promtail이 실제로 사용하는 수집 방식을 먼저 확인한다. 그때만 기존 mount/config의 최소 수정을 Phase 1로 넘긴다.

**Rollback/보류:** 관측성만 정지 가능. 수집 이력 손실과 기존 업무 서비스 장애를 구분한다. agent 교체·retention 자동화는 Phase 4.

**Stop Point 0-B:** 지표·두 서비스 로그·자원 상태가 통과해야 관측성 재현 성공이다. 실패한 채 제거하고 이전 완료로 처리하지 않는다.

## 6. Phase 1 — 실제 blocker만 수정

### Step 1-1 [B] 실패 분류와 최소 변경 심사

**목적/근거:** 환경 이전을 애플리케이션 재설계로 확대하지 않는다. Phase 0/0-B에서 실패가 없으면 ‘변경 불필요’로 기록하고 통과한다.

| 표 1: 실패 분류 | 먼저 확인할 증거 | 수정 후보/대상 | 재검증 |
|---|---|---|---|
| 환경값 | 적용값 관계, shell 우선순위, 접속 대상 | 기존 `.env`의 해당 값 | 실패한 config/접속 검사 |
| 포트 충돌 | listener와 소유자 | 기존 port 변수·관련 URL/CORS | 바인딩+실제 브라우저 요청 |
| image/build | 실패 단계·registry/npm/Maven/font 오류 | 네트워크/인증 우선, 필요 시 해당 기존 Dockerfile | 해당 image build만 |
| ARM64 | Engine·image architecture, native loader 오류 | 원인 패키지/태그; platform 강제는 진단용으로만 | 해당 build·기동·관련 API |
| DB 초기화 | 인증/권한/SQL 오류와 적용 범위 | 계정 설정 또는 입증된 기존 SQL 오류 | 영향받은 schema/seed/API |
| Application | 환경이 정상인데 같은 API가 실패하는 증거 | 별도 계약 검토 후 기존 코드 | 기존 관련 테스트+실제 실패 API |
| OrbStack/path | mount·socket·Engine 오류 | 지원되는 기존 설정/경로 | 영향받은 수집/컨테이너 |

- [ ] 변경 전 증상, 원인, 가장 단순한 기존 구조 활용안, 대상 파일, 외부 계약 영향, rollback을 한 항목으로 기록한다.
- [ ] 기존 코드를 수정할 수 있는지 먼저 확인한다. compatibility 요구가 없으면 구/신 경로를 동시에 만들지 않는다.
- [ ] 작은 diff라도 임시 hardcode나 재부팅 후 사라지는 우회면 채택하지 않는다.
- [ ] 서비스 코드가 필요하면 ‘Mac 이전과의 인과관계 / 환경으로 해결 불가한 이유 / 기존 동작 / 변경 후 계약’을 별도로 설명한다. 예약·로그인·API 응답·DB 업무 규칙·화면 흐름 변경은 이 Plan의 일반 수정 권한에 포함하지 않는다.

**명령/검증:** 원인에 따라 위 표의 해당 기존 명령만 재실행한다. 새 unit test는 기존 테스트가 잡지 못하는 구체적인 regression이 있을 때만 추가한다. 내부 함수 호출 순서·private helper·class 모양·일시적 config를 계약으로 고정하지 않는다.

**성공 조건:** 원래 실패가 해소되고 관련 외부 계약이 유지됨. 전체 테스트·전체 build를 이유 없이 반복하지 않음.

**Rollback:** 변경한 설정과 image ID를 보존하고 해당 파일/설정만 이전 상태로 복원한다. SQL 쓰기는 코드 revert로 취소되지 않으므로 DB 변경 전 보존 방법을 별도로 정한다. 기존 VM은 유지한다.

**보류:** Java/Node upgrade, Spring/Frontend 구조 개편, migration framework, Jenkins 제거, Actions/GHCR, Compose/profile 재설계, 대규모 Dockerfile 최적화는 수행하지 않는다. 실제 실행 blocker임이 입증되면 필요한 좁은 변경만 별도 근거와 함께 검토한다.

**Stop Point 1:** 0/0-B의 blocker가 없거나 해결되어야 Phase 2로 간다. Phase 2·서버 검증에서 새 blocker가 나와도 해당 원인만 이 심사로 되돌린다.

## 7. Phase 2 — Cloudflare와 외부 접근 이전

### Step 2-1 [A] 기존 VM의 실제 Tunnel 계약 확인

**목적/근거:** 문서의 도메인 예제를 운영 사실로 취급하지 않고 rollback 가능한 전환 대상을 특정한다.

**변경/대상 파일:** 아직 없음. 기존 VM과 Cloudflare dashboard를 소유자가 읽기 전용 확인한다. Secret-bearing 파일·서비스 명령줄 원문을 agent 출력에 전달하지 않는다.

- [ ] 실행 위치가 VM host, container, Windows 중 어디인지 확인.
- [ ] Tunnel 이름/ID, connector ID, 관리 방식(remote/local), ingress/hostname/path와 Frontend·Backend route를 확인.
- [ ] DNS record의 Tunnel 연결, Access application/policy와 API preflight 영향, VM localhost target을 확인.
- [ ] systemd/manual/container 여부, 서비스 이름, 설정 파일 위치, 재시작 방법을 확인. Token은 확인 결과에 포함하지 않는다.

VM에서 systemd 사용 여부를 확인하는 좁은 명령 예시:

```bash
systemctl is-active cloudflared
systemctl is-enabled cloudflared
systemctl show cloudflared --property=FragmentPath --property=User
```

실제 unit 이름이 다르면 dashboard·운영자 확인으로 식별한 이름만 사용한다. `systemctl cat`, process 전체 args, 전체 inspect는 Token을 노출할 수 있으므로 그대로 출력하지 않는다. 로컬 관리 설정은 소유자가 비공개로 확인하고 hostname→service 및 정책만 전달한다.

**성공 조건/검출 실패:** 실제 route·실행 위치·관리 방식·복원 대상이 특정됨. 예제와 운영 구조의 불일치를 검출한다.

**실패 시/Stop Point 2-A:** 접근 권한이나 운영 정보가 없으면 외부 전환을 중단한다. 로컬 검증 완료 상태는 유지한다. 추정 ingress로 운영 route를 덮어쓰지 않는다.

**Rollback/보류:** 아직 변경 없음. 기존 설정의 비공개 복원본과 비민감 route 목록 확보가 전환의 선행 조건이다.

### Step 2-2 [A] Mac 전용 경로로 외부 접근 준비·검증

**목적/근거:** 기존 VM을 유지하면서 새 Mac origin을 구분해서 검증한다. 기본 방안은 Mac 전용 Tunnel과 임시 Frontend/API hostname이다. 기존 Tunnel 삭제나 load balancer 구축은 하지 않는다.

같은 Tunnel ID에 VM/Mac connector를 동시에 붙이면 어느 DB로 요청이 갈지 통제할 수 없다. 서로 다른 DB의 migration 검증에 replica 방식을 사용하지 않는다. 근거: [Cloudflare replica 문서](https://developers.cloudflare.com/tunnel/configuration/), [routing 문서](https://developers.cloudflare.com/tunnel/concepts/routing/).

**변경/대상:** Mac cloudflared 설치·서비스 설정, Mac 전용 Tunnel/임시 route, Mac `.env`의 URL/CORS. 기존 앱 소스/Compose는 원칙적으로 변경 없음.

- [ ] Step 2-1의 관리 방식과 계정 권한을 기준으로 Mac 호스트에 cloudflared를 설치한다. 이미 설치되어 있으면 재설치하지 않는다.
- [ ] Dashboard에서 Mac 전용 named Tunnel과 사용 가능한 임시 hostname 2개를 생성한다. 소유한 실제 zone에서 충돌 없는 이름을 선택한다.
- [ ] Mac host connector 기준 Frontend service를 `http://localhost:3000`, Backend를 `http://localhost:8080`으로 연결한다. 다른 포트를 선택했다면 검증된 포트를 사용한다.
- [ ] DB·Prometheus·Loki용 public route는 만들지 않는다. 이들 publish는 우선 기존 port 변수로 localhost에 제한하고 전체 port 재설계는 미룬다. Grafana는 로컬 관리 접근을 유지한다.
- [ ] 공개 Backend의 `/actuator/prometheus` 접근 정책을 확인한다. scrape를 켠 상태의 무인증 공개를 묵인하지 않는다. 확인된 Tunnel/Access의 경로 단위 정책으로 외부 지표 접근만 차단하고 내부 scrape는 유지한다. 이 좁은 보호가 현재 방식으로 불가능하면 외부 전환을 멈추고 최소 대안을 판단한다.
- [ ] 공개해도 되는 시연 데이터/계정 범위와 기존 접근 정책을 소유자가 확인한다. 공개 범위 확대를 임의로 승인하지 않는다.

Mac host cloudflared를 선택하면 Linux의 systemd 대신 macOS 서비스 방식을 사용한다. Local-managed 구성은 공식 문서에 따라 `/etc/cloudflared/config.yml`의 Tunnel/credential 경로를 준비하고 boot daemon으로 등록한다. Remote-managed는 해당 방식의 공식 설치 절차를 사용한다. Token을 저장소·명령 기록·출력에 남기지 않는 전달 방식을 먼저 확인하고 적용한다. 서로 다른 두 관리 방식을 혼용하지 않는다.

```bash
cloudflared --version
# Local-managed 구성 준비와 권한 확인을 마친 경우에만:
sudo cloudflared service install
sudo launchctl start com.cloudflare.cloudflared
```

위 설치 명령은 기존 cloudflared 서비스가 없는지 확인한 뒤 사용한다. 서비스가 있다면 덮어쓰기 전에 소유권·설정·기존 Tunnel을 확인한다. 로그인 시 agent와 boot daemon은 다르다. 근거: [Cloudflare macOS 서비스 문서](https://developers.cloudflare.com/cloudflare-one/networks/connectors/cloudflare-tunnel/do-more-with-tunnels/local-management/as-a-service/macos/).

- [ ] 실제 임시 HTTPS Frontend/API origin을 `.env`의 `NEXT_PUBLIC_APP_URL`, `NEXT_PUBLIC_API_BASE_URL`, `CORS_ALLOWED_ORIGINS`에 반영한다. 이미 검증한 DB/port/관측성 값도 유지한다. OAuth 사용 시 Frontend callback와 provider redirect URI까지 일치시키고 기존 VM callback을 먼저 제거하지 않는다.
- [ ] Phase 0/0-B에서 export한 URL·port·DB host·scrape·Grafana override를 모두 해제한 새 터미널에서 `.env`만으로 config를 검증한다. Secret을 shell에 source하지 않는다.

```bash
docker --context orbstack compose -p health-center --env-file .env --profile observability config --quiet
docker --context orbstack compose -p health-center --env-file .env build frontend
docker --context orbstack compose -p health-center --env-file .env --profile observability up -d --no-build
```

Frontend의 공개 URL이 번들에 반영되어야 하므로 해당 build만 수행한다. runtime 설정 변경에는 `restart`만 사용하지 않는다. `up`으로 변경된 환경을 적용한다. Backend 재생성 시 seed 재실행을 예상한다.

**검증/검출 실패:** Mac 외부 기기·네트워크에서 임시 Frontend 로그인, 공개 Backend health, 업무·슬롯 조회 및 브라우저의 실제 HTTPS API target 확인. 외부에서 localhost/VM을 호출하는 번들, CORS/Access preflight, 잘못된 origin route를 검출한다. Loki에서 Mac Backend의 해당 시각 요청을 확인한다. API origin 직접 접근이 로컬 DB에 연결됨도 로컬 검증 결과와 대조한다.

**성공 조건:** Frontend와 Backend가 모두 Mac 경로로 동작하고 내부 지표는 수집되며 외부 Actuator 정책도 의도대로 적용됨.

**Rollback:** 기존 production route는 그대로다. Mac 임시 route를 비활성화하고 보관한 로컬 설정/이미지로 돌아갈 수 있다. Mac에서 작성한 데이터는 VM에 복제되지 않는다.

**보류:** DNS 자동화, load balancer, reverse proxy 신설, monitoring 외부 공개는 하지 않는다.

### Step 2-3 [A] Production hostname 전환과 되돌리기

**목적/근거:** Frontend/API를 하나의 서비스 쌍으로 전환하고 두 DB에 쓰기가 분산되지 않도록 한다.

**변경/대상:** 확인된 실제 production hostname의 DNS/Tunnel route, Mac `.env` 공개 URL, Frontend image. VM·DB·Jenkins home·기존 Tunnel은 유지.

- [ ] 소유자가 짧은 전환 시간, 허용 중단, 새 DB 시작과 기존 계정/데이터가 이어지지 않음을 확인한다. 새 데이터도 잃어도 되는지 별도로 결정한다.
- [ ] 원래 DNS/route/Access 정책과 Mac 전환 후 되돌릴 위치를 비공개로 보존한다. 외부 DNS cache와 이미 열린 브라우저 세션 때문에 두 route가 원자적으로 바뀌지 않음을 고려한다.
- [ ] Mac Frontend를 최종 production API/Frontend URL로 한 번 재빌드하고 Backend CORS/OAuth를 맞춘다. 검증 중 기존 VM으로 API가 갈 수 있으므로 production 전환 전 이 번들로 쓰기 테스트하지 않는다.
- [ ] 전환 시간 동안 기존 접근 경로의 쓰기를 제한할 수 있는 운영 수단을 사용한다. 포트폴리오 서비스에 실제 사용자가 없다면 그 상태를 확인한다. 제한 수단이 없고 동시 쓰기를 배제할 수 없으면 전환을 멈춘다. 새 maintenance framework는 만들지 않는다.
- [ ] 확인된 production Frontend/API 두 route를 Mac Tunnel로 전환한다. 기존 VM의 connector를 Mac Tunnel의 replica로 추가하지 않는다.
- [ ] 외부 로그인·조회·API target을 다시 확인하고, 이후 운영에 사용할 비시연 검증 레코드 한 개를 기존 UI로 만들어 Mac 데이터 보존 확인에 사용한다. 회원 정보나 레코드 내용은 공개 기록에 남기지 않는다.

**성공 조건/검출 실패:** 최종 URL 두 개가 Mac을 향하고 browser API target/CORS/Access가 정상. URL 쌍의 반쪽 전환, 오래된 bundle/cache, VM으로의 잔여 쓰기를 검출한다. 검증 중 상태 변경은 통제된 계정으로만 수행한다.

**Rollback 절차:** 전환 실패 시 새 쓰기를 중지 → Mac 새 데이터 보존 필요 판단 및 필요한 경우 비공개 백업 → 원래 Frontend/API DNS·route·Access를 함께 복원 → 외부 URL과 실제 VM API target 확인 → Mac 컨테이너/volume을 삭제하지 않고 유지. 단순 DNS rollback은 Mac 데이터의 VM 복원을 뜻하지 않는다. DB 병합·역방향 migration을 자동 수행하지 않는다.

**Stop Point 2-B:** 외부 접근 성공만으로 이전 완료가 아니다. Phase 2-S를 통과하기 전 VM 종료·Jenkins 제거로 진행하지 않는다.

## 8. Phase 2-S — 상시 서버 운영 검증

### Step S-1 [A] 부팅·절전·설정 지속성 계약 확정

**목적/근거:** 개발용 수동 기동과 상시 서버를 구분한다. `unless-stopped`는 Docker Engine이 시작된 뒤의 컨테이너 정책이며 Mac 부팅 정책이 아니다.

**변경/대상:** 검증된 `.env`, 필요한 OrbStack 자동 시작·macOS Energy 설정·cloudflared 서비스만. 업무 코드 변경 없음.

```bash
sw_vers
pmset -g custom
fdesetup status
docker --context orbstack inspect health-center-postgres health-center-backend health-center-frontend health-center-prometheus health-center-grafana health-center-loki health-center-promtail --format '{{.Name}} {{.HostConfig.RestartPolicy.Name}} {{.State.Status}}'
```

- [ ] 설치된 OrbStack 버전의 자동 시작 설정과 실행 사용자, 로그인 item인지 로그인 전 boot 지원인지 확인한다. CLI headless 지원은 확인됐지만 그것만으로 boot service 지원을 단정하지 않는다. [OrbStack 공식 CLI 문서](https://docs.orbstack.dev/headless).
- [ ] FileVault 상태와 부팅 중 디스크 unlock 필요 여부를 확인한다. 자동 로그인/FileVault 해제를 기본 해결책으로 적용하지 않는다. Apple은 FileVault 사용 시 자동 로그인이 불가능하다고 설명한다. [Apple 자동 로그인 문서](https://support.apple.com/en-gb/102316).
- [ ] 무인 시작이 현재 설치 방식에서 지원되지 않으면 정확한 한계를 기록하고 여기서 운영 완료를 보류한다. 지원되는 부팅 방법 또는 로그인 후 복구를 허용하는 운영 계약은 소유자 판단이 필요하다. 임의의 root 실행 wrapper나 LaunchDaemon으로 OrbStack을 강제 구동하지 않는다.
- [ ] cloudflared가 boot daemon인지 확인한다. localhost 앱보다 먼저 시작될 수 있으므로 앱이 준비된 뒤 연결이 자동 회복되는지 재부팅에서 검증한다.
- [ ] 검증된 환경을 `.env`에 지속시키고 새 터미널에서 config validation을 통과시킨다. 운영 경로는 Jenkins workspace를 복제하지 않고 현재 checkout을 유지한다. 경로 이동은 별도 필요가 없으면 하지 않는다.
- [ ] Mac Energy의 ‘디스플레이가 꺼져 있을 때 자동 잠자기 방지’를 서버 운용에 맞춘다. display sleep과 화면 잠금은 허용한다. network wake만으로 지속 서비스가 보장된다고 가정하지 않는다. 기존 설정을 기록하여 되돌릴 수 있게 한다. [Apple 잠자기 설정 문서](https://support.apple.com/guide/mac-help/set-sleep-and-wake-settings-mchle41a6ccd/mac).

**성공 조건/검출 실패:** 자동 시작 경로·실행 사용자·지속 설정이 확인되고, 화면이 꺼져도 호스트 sleep으로 서비스가 끊기지 않음. 로그인 의존성과 임시 shell 환경 의존성을 검출한다.

**Rollback/보류:** Energy/자동 시작 설정을 기록한 이전 값으로 복원 가능. 전원 차단 강제 실험·광범위한 macOS tweak는 하지 않는다. 정전 뒤 자동 전원 복귀 옵션이 해당 Mac에 제공되는지 확인하고 필요할 때만 별도 적용한다. 재부팅 성공을 정전/콜드 부팅 성공으로 확대 해석하지 않는다.

### Step S-2 [A] 최소 재시작 검증과 데이터 보존

**목적/근거:** 컨테이너 재시작과 DB 연결 회복을 검증한다. UI에서 만든 비시연 레코드를 사용하며 매 기동마다 재생성되는 seed의 건수만으로 영속성을 판정하지 않는다.

**변경/대상:** Mac 컨테이너 재시작. DB 데이터는 보존. 순차 수행하며 앞 서비스 복구 전 다음 재시작을 하지 않는다.

| 검증 | 실행 명령 | 검출하려는 실패와 성공 조건 |
|---|---|---|
| Backend restart | `docker --context orbstack compose -p health-center --env-file .env restart backend` | SQL init 재실행 오류·기동 실패 검출. health/인증 조회 복구, 비시연 레코드 유지 |
| Frontend restart | `docker --context orbstack compose -p health-center --env-file .env restart frontend` | runtime 기동·정적 파일 누락 검출. 로그인 화면과 실제 API 호출 복구 |
| PostgreSQL restart | `docker --context orbstack compose -p health-center --env-file .env restart postgresql` | 기존 Backend connection pool의 DB 재연결 실패 검출. DB healthy 뒤 Backend를 수동 재시작하지 않아도 조회 복구 |
| cloudflared restart | boot daemon을 확인한 뒤 `sudo launchctl kickstart -k system/com.cloudflare.cloudflared` | 서비스 관리·credential 접근·Tunnel 재접속 실패 검출. 외부 Frontend/API 복구 |

다른 관리 방식/서비스 label이면 확인된 해당 manager의 재시작 명령만 사용한다. 명령의 label을 추정하지 않는다. OB-2에서 동일 버전·설정으로 실시한 앱 재시작은 증거를 재사용한다. 그 뒤 image/config가 바뀌었다면 영향받은 서비스만 다시 확인한다.

- [ ] 매 재시작 전후 해당 public URL과 필요한 조회 하나만 확인한다. 전체 사용자 시나리오를 매번 반복하지 않는다.
- [ ] 비시연 레코드의 존재와 같은 DB volume 이름을 확인한다. 개인 정보·Token을 기록하지 않는다.
- [ ] 관측성 수집이 재개되는지 확인한다. 정책상 시연 seed 재설정은 예상 동작으로 별도 기록한다.

**실패 시:** container status, 해당 서비스 오류, DB readiness·pool 재연결, Tunnel manager 순으로 해당 원인만 조사한다. 수동 재시작으로 살렸다면 ‘자동 복구 성공’으로 표시하지 않는다.

**Rollback/보류:** 필요 시 Mac 서비스를 수동으로 복구하고 자동 복구 항목은 실패로 남긴다. public 서비스 영향이 크면 Step 2-3 rollback 적용. 이 테스트는 SIGKILL·정전·스토리지 손상 내성을 검증하는 chaos test가 아니다.

### Step S-3 [A] Mac reboot 및 외부 서비스 자동 복구

**목적/근거:** 실제 서버 완료 조건을 검증한다. host 재부팅은 Docker의 재시작 순서·volume·Tunnel·로그인 의존성을 동시에 드러낸다.

**변경/대상:** 소유자가 승인한 시간에 Mac을 정상 재부팅한다. 다른 Mac 작업을 저장한다. 기존 VM은 보존한다.

- [ ] 재부팅 직전 모든 대상 서비스가 실행 중인지 확인한다. `compose stop/down`을 먼저 실행하지 않는다. `unless-stopped`는 수동으로 중지한 컨테이너의 자동 복원을 보장하지 않는다. [Docker restart 정책](https://docs.docker.com/engine/containers/start-containers-automatically/).
- [ ] 비시연 레코드·volume 이름·정상 public URL·복구 측정 시작 시각을 비민감하게 기록한다.
- [ ] Mac 메뉴의 정상 재시작을 실행한다. 로그인·`orb start`·`compose up`을 하지 않고 다른 기기에서 최대 10분간 주기적으로 public URL 상태를 확인한다. 처음 발생하는 502/연결 실패와 최종 복구 시간을 구분한다.
- [ ] 복구 후 외부 Frontend가 실제 Backend를 호출하고 비시연 데이터가 남았는지, 지표·로그가 다시 수집되는지 확인한다.
- [ ] 무인 복구가 안 되면 로그인 전 상태와 로그인 후 상태를 각각 기록한다. 사람이 로그인하거나 CLI를 실행해서 복구됐다면 무인 복구 실패다.

**성공 조건/검출 실패:** 사람 개입 없이 Engine→컨테이너→DB/API→Tunnel→public URL이 10분 이내 복구하고 데이터 유지. Engine 부팅, container 시작 순서, credential 경로, 수동 중지, FileVault 잠금으로 인한 실패를 검출한다. `depends_on`의 초기 Compose 순서가 Engine 재시작에도 동일하게 재실행된다고 가정하지 않는다.

**Rollback:** Mac을 수동 복구하거나 Step 2-3의 VM route 복귀를 사용한다. 데이터는 삭제하지 않는다. 무인 시작이 지원되지 않으면 목표 미달로 멈추고 운영 방식 결정을 요청한다.

### Step S-4 [A/C] 제한된 운영 관찰과 저장공간·backup 판단

**목적/근거:** 최초 기동 성공 후 잠자기·로그 누적·자원 부족으로 곧 중단되는 것을 확인한다. 복잡한 자동 cleanup 시스템은 만들지 않는다.

**변경/대상:** 처음에는 측정과 운영 정책 결정만. 실제 보존 대상 데이터가 있으면 기본 백업 확보가 필요하다.

- [ ] 최소 하룻밤을 포함한 24시간을 초기 관찰 구간으로 둔다. 시작/종료 시 Step OB-2의 `stats`, `system df -v`, `df`를 재사용하고 OrbStack UI의 disk 사용량과 비교한다.
- [ ] 화면 잠금·display sleep 상태에서 외부 접근이 유지되는지 다른 기기에서 확인한다. 모니터를 계속 켜 놓는 우회로 통과시키지 않는다.
- [ ] 아래 항목의 기준값과 증가량을 별도로 기록한다. `system df` 하나로 모든 Docker json log 사용량이 설명된다고 가정하지 않는다.

| 공간 항목 | 지금 해야 할 확인 | 이후 개선 |
|---|---|---|
| image/build cache | tag/ID·사용 여부·총량. 동일 source의 불필요한 rebuild 없음 | 검증된 미사용 대상만 수동 정리 후 필요 시 정책화 |
| PostgreSQL volume | 비시연 데이터 유지, 용량·보존 요구 | 정기 backup·restore drill |
| Prometheus | 실제 volume 증가·현재 retention 적용 상태 | retention 크기/기간 조정 |
| Loki | 실제 volume 증가·명시 retention 부재의 영향 | retention 설정 |
| Docker json logs | OrbStack/호스트 disk와 로그 증가량, driver | log rotation |

여유 공간이 빠르게 줄거나 OOM/지속적인 memory pressure가 발생하면 24시간을 채우기 위해 방치하지 않는다. 해당 원인을 B로 올려 운영 완료 전에 해결한다. 보존 데이터가 없는 데모라도 디스크 고갈을 정상 운영으로 승인하지 않는다.

- [ ] 새 DB 데이터도 재생성 가능한지 소유자가 결정한다. 버릴 수 있으면 그 한계와 RPO를 기록한다. 새 데이터 보존이 필요하면 전환 후/VM 종료 판단 전 native `pg_dump`로 비공개 백업을 확보하고 복원 가능성을 확인한다. 백업 경로는 저장소와 DB volume 밖, Secret/개인 데이터 접근이 제한된 위치로 정한다.
- [ ] 복원 검증은 별도 승인된 임시 DB에서만 수행한다. 운영 DB나 VM DB를 덮어쓰지 않는다. 복원까지 검증하지 않은 파일은 ‘복구 보장’으로 표시하지 않는다. 백업 자동화·복잡한 rotation은 Phase 4다.

**성공 조건:** 잠자기 때문에 중단되지 않고 24시간 동안 자원 여유·데이터 보존 정책·복구 방법이 설명 가능함. 장기간 무장애를 증명했다고 표현하지 않는다.

**Rollback/보류:** 자원 문제가 해결되지 않으면 VM으로 트래픽 복귀 가능. 자동 cleanup/backup framework는 만들지 않는다.

**Stop Point S:** S-1~S-4와 최종 외부 흐름을 통과해야 Mac 이전 완료를 제안한다. 소유자의 명시적 이전 완료 판단 후에만 기존 VM 종료 여부를 논의한다. 우선 종료와 삭제를 분리하며 VM 디스크·DB·Jenkins home·옛 Tunnel은 합의한 rollback 보관 기간 동안 보존한다. 이 문서는 자동 삭제를 승인하지 않는다.

## 9. Phase 3 — Jenkins 제거 및 CI/CD 개선 [C]

### Step 3-1 기존 역할의 대체 책임 결정

**목적/근거:** 정상화된 수동 Mac 운영과 Jenkins 제거를 분리한다. 새 CI/CD는 선택이며 수동 배포 유지도 가능하다.

**변경/대상:** 이 migration 실행의 기본 범위에서는 없음. 별도 작업을 승인하면 `Jenkinsfile`, `infra/jenkins/*`, 루트 `.github/workflows/` 등 실제 채택 대상만 결정한다. 지금 workflow나 GHCR 설정을 생성하지 않는다.

| 기존 Jenkins 기능 | 수동 Mac 운영에서의 책임 | Actions/GHCR를 선택할 경우 대체할 책임 |
|---|---|---|
| checkout | 실행할 commit을 명시한 로컬 checkout | CI checkout. 배포할 digest와 commit 연결 필요 |
| SCM polling | 소유자가 변경 확인·배포 결정 | workflow trigger. CI가 배포까지 하는지는 별도 결정 |
| Backend pre-build | Dockerfile build로 패키징. 기존 테스트 실행은 필요한 범위만 | CI 검증과 image build 중복 여부 정리 |
| Frontend pre-build | Dockerfile build 및 실제 API target 검증 | 공개 URL build args와 CI 검증 책임 |
| `.env` credential 공급 | Mac의 접근 제한된 `.env` | registry image에 Secret 포함 금지. 배포 host의 Secret 공급 경로 유지 |
| compose config | Mac에서 `config --quiet` | CI 검증과 host 실효값 검증 분리 |
| compose build | Mac Engine | image build/push를 택하면 runner architecture와 runtime architecture 연결 |
| compose up | Mac에서 직접 실행 | GHCR push만으로 Mac deploy가 되지 않음. host update 주체를 명확히 결정 |
| image prune | 자동 수행하지 않음 | 사용 image·rollback image를 보존하는 별도 정책 |

**검증/성공:** 선택한 방식으로 변경 감지→정확한 버전 build/deploy→Secret 공급→health 확인이 이어지고 수동 rollback이 가능함. 단순 registry 업로드를 배포 완료로 오인하는 실패를 검출한다.

**Rollback/보류:** 새 방식의 배포 검증 전 Jenkins job/home은 보존한다. 자동 trigger 중단, Jenkins 컨테이너 중단, home 삭제는 각각 분리한다. 완전 삭제는 별도 확인이 필요하다. 멀티아키텍처 CI·자동 rollback 등은 필요가 입증될 때만 추가한다.

**Stop Point 3:** Mac 이전 완료와 선택한 대체 책임의 검증 후에만 Jenkins 제거를 결정한다. Mac으로 옮기기 위해 Jenkins를 먼저 제거하지 않는다.

## 10. Phase 4 — 후속 유지보수 [C]

각 항목은 별도 필요·위험·검증 범위로 선정한다. 아래는 선행 구현 목록이 아니다.

| 후보 | 대상 | 착수 근거와 목표 검증 |
|---|---|---|
| Java/Node upgrade | 기존 Dockerfile·의존성 | 지원/보안 요구 검토 후 해당 build·중요 외부 계약만 회귀 확인 |
| 중복 build·image 최적화 | Jenkinsfile·Dockerfile | 배포 시간/용량 측정, 동일 실행 결과 확인 |
| seed 정책·migration 체계 | 기존 SQL·설정·pom | 기존 데이터 보존, 빈 DB와 기존 DB 변경의 차이 검증 |
| healthcheck | Compose | 실제 준비 전 false positive와 준비 후 false negative 검출 |
| rollback 개선 | 배포 절차/image 식별 | 실제 이전 버전 복구, DB 역호환 한계 확인 |
| log rotation·retention | Compose·Loki·Prometheus | 디스크 증가 제한과 필요한 관측 기간 유지 |
| DB backup 자동화 | 운영 절차 | 복원 성공과 허용 데이터 손실 시간 검증 |
| publish/Actuator 정리 | 기존 Compose·접근 정책 | 내부 통신 유지, 불필요한 외부 접근 차단 |
| CI/CD 최적화 | Phase 3에서 채택한 기존 경로 | 중복 trigger/build 제거, 배포 버전 추적 |

이미 Phase 2의 안전한 공개나 S 단계의 운영 안정성에 꼭 필요한 최소 조치를 수행했다면 이를 다시 다른 경로로 구현하지 않는다. 예컨대 localhost bind를 적용했다면 같은 목적의 proxy layer를 추가하지 않는다.

## 11. 목표 기반 검증과 증거 재사용

| 검증 ID | 담당 Step | 검출하는 실패 | 통과 증거 |
|---|---|---|---|
| V0 | 0-1/0-2 | 잘못된 Engine·충돌·잘못된 환경 관계·Compose 구문 | 대상 확인과 quiet validation 성공 |
| V1 | 0-3 | image/native build·DB 인증/초기화 실패 | build 성공·native image·DB healthy·테이블/건수·health UP |
| V2 | 0-4 | 로그인·업무/슬롯 API·CORS·옛 API target | 한 사용자 흐름과 Network 결과 |
| V3 | OB-1 | scrape 권한·datasource·dashboard 연결 실패 | target UP·Grafana 실제 조회 |
| V4 | OB-2 | Docker log 접근·전송·조회 label 실패 | Backend/Frontend 실제 로그 수신 |
| V5 | 2-2/2-3 | Tunnel/DNS/Access·공개 번들 URL·반쪽 전환 | 외부 URL 쌍·로그인/조회·Mac target |
| V6 | S-2 | 앱 재시작/DB 재연결/Tunnel 재시작 실패 | 영향 서비스 복구·비시연 데이터 유지 |
| V7 | S-3 | 로그인 의존·boot/volume/시작 순서 실패 | 무인 재부팅 후 외부 서비스 복구와 소요 시간 |
| V8 | S-4 | sleep·자원 누적·복구 정책 공백 | 24시간 관찰·증가량·데이터 보존 결정 |

새 테스트 수를 성공 지표로 삼지 않는다. 같은 image/config로 이미 검증한 계약은 증거를 재사용한다. URL 변경·재시작·DB 재연결처럼 실패 조건이 달라질 때만 관련 검증을 다시 한다. Application unit test 대량 추가, 내부 구조 고정, 실환경 검증을 대신하는 mock test는 하지 않는다.

## 12. Stop Point 요약과 완료 판정

| Stop Point | 다음 단계로 가기 위한 조건 | 실패 시 |
|---|---|---|
| 0 | 기본 3종·로그인·두 조회·API target 정상 | Phase 1에서 해당 blocker만 해결 |
| 0-B | 기존 관측성 지표·두 서비스 로그·자원 정상 | 수집 문제만 추가 분석 |
| 1 | blocker 없음 또는 해당 재검증 통과 | Cloudflare 변경 시작 금지 |
| 2-A | 실제 VM Tunnel/route/복원 방법 확인 | 문서 예제로 전환하지 않음 |
| 2-B | 최종 public Frontend/API가 Mac으로 동작 | 기존 route rollback |
| S | 무인 reboot·restart·volume·Tunnel·운영 관찰 통과 | VM 유지, 미달 조건과 선택 필요 사항 기록 |
| 이전 완료 판단 | 소유자가 결과·데이터 보존·rollback 기간 확인 | 기존 VM 종료/정리 금지 |
| 3 | Jenkins 대체 책임 검증 | Jenkins home/기존 배포 경로 보존 |

Plan 작성 시점에는 위 runtime 검증을 하나도 실행하지 않았다. 현재 파일 생성은 이전 완료를 의미하지 않는다.

## 13. Self Review

- [x] Analysis를 불필요하게 반복하지 않았는가? — 기존 문서를 전제로 하고 profile과 새 서버 운영 조건만 보완했다.
- [x] 기존 Compose를 먼저 실행하는가? — Phase 0은 저장소 변경 없이 기본 3종부터 확인한다.
- [x] 실제 실패 전부터 구조를 변경하지 않는가? — Phase 1은 실패 증거가 없으면 생략한다.
- [x] Jenkins 제거가 너무 일찍 들어가지 않았는가? — 운영 완료 뒤 Phase 3로 분리했다.
- [x] Java/Node upgrade가 섞이지 않았는가? — Phase 4이며 실제 blocker 예외만 좁게 허용한다.
- [x] Observability를 근거 없이 제거하지 않았는가? — 기존 profile 전체를 검증한다.
- [x] 새로운 profile/script/abstraction을 불필요하게 추가하지 않았는가? — 기존 환경변수·Compose·공식 서비스 방식을 우선한다.
- [x] 기존 코드를 그대로 둔 채 유사 코드를 새로 추가하는 설계가 없는가? — 기존 파일 수정 우선, 이중 경로 금지.
- [x] 중복 테스트가 없는가? — 검증 ID와 증거 재사용 기준으로 재시작·UI 검증 중복을 제한했다.
- [x] 테스트가 내부 구현을 지나치게 고정하지 않는가? — health/API/데이터 보존/외부 접근 계약만 검증한다.
- [x] 단순한 작은 diff 때문에 목표 달성에 실패하는 설계는 아닌가? — 로그인 후 수동 복구를 무인 서버 완료로 취급하지 않는다.
- [x] 인프라 이전 때문에 서비스 로직이 변경되지 않는가? — 계약 변경은 별도 검토 없이 진행하지 않는다.
- [x] 시간 복잡도/빌드 시간/불필요한 반복 작업을 고려했는가? — 최초 build 1회, 공개 URL 변경 시 Frontend만 rebuild, fixed sleep 대신 상태 확인.
- [x] Docker image/cache/log/DB/Observability의 공간 증가를 고려했는가? — S-4에서 각각 측정하며 실제 고갈 위험은 완료 전 해소한다.
- [x] 향후 유지보수가 기존보다 어려워지지 않는가? — 임시 shell 설정을 운영 계약으로 남기지 않고 기존 `.env`에 정착시킨다.
- [x] Mac 재부팅 이후까지 검증하는가? — 로그인·수동 시작 없이 외부 기기에서 확인한다.
- [x] Cloudflare 복구까지 포함해 실제 '서버' 상태를 검증하는가? — process restart와 Mac reboot 모두 public URL을 확인한다.
- [x] 기존 VM rollback 경로를 유지하는가? — 기존 리소스 보존, route 복원과 DB 데이터 복원을 구분한다.

검수 중 보완한 사항:

1. Analysis 예시의 platform/profile 강제는 최초 실행 필수값에서 제외했다.
2. 로그인 item과 boot daemon을 같은 자동 복구로 취급할 위험을 제거했다. OrbStack의 무인 부팅 가능 여부는 실제 Stop Point로 남겼다.
3. 같은 Tunnel replica로 서로 다른 DB에 트래픽을 보내는 위험을 피하도록 Mac 전용 검증 경로를 사용했다.
4. 일시적 shell override가 재부팅 뒤 사라지는 문제를 방지하기 위해 운영 `.env` 정착을 추가했다.
5. 매 기동 seed가 생긴다는 이유만으로 DB 영속성을 통과시키지 않도록 비시연 레코드를 사용한다.
6. 관측성·서버 검증의 동일 재시작 테스트는 조건이 같으면 재사용한다.
7. backup/retention 자동화는 후속으로 두되, 현재 데이터 보존 결정과 실제 disk 고갈 문제는 상시 운영 완료 전에 처리한다.

기존 Analysis의 대규모 수정은 필요 없다. 향후 예시 명령을 정리한다면 platform/profile 강제가 선택적임을 짧게 보정할 수 있으나, 현재는 이 Plan의 실행 전제를 우선하면 충분하다.
