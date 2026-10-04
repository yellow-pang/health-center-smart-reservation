# Mac mini + OrbStack 이전 진행 기록

## 실행 기준

| 항목 | 현재 상태 |
|---|---|
| 기준 Analysis | `08_Mac_mini_OrbStack_이전_사전_분석.md` |
| 기준 Plan | `09_Mac_mini_OrbStack_이전_실행_Plan.md` |
| 현재 branch / HEAD | `dev` / `323f73e` |
| 실행 시작 HEAD | `fed9104f754b02b1d993cef2175c0779b97585e0` |
| 현재 Phase / Step | Phase 2-S 완료, Phase 3/4 후속 작업 보류 |
| 완료된 Stop Point | Stop Point 0, Stop Point 0-B, Stop Point 1, Stop Point 2-A, Stop Point 2-B, Stop Point S(운영 제약 수용) |
| 상태 | 세 프로젝트 Mac runtime 이전과 24시간 관찰 PASS. 사용자가 로그인 후 자동 복구 조건과 기존 VM DB 미이전을 수용해 이전 완료로 판정. 자동 CI/CD는 RWR만 완료 |
| 다음 시작 Step | 별도 SmartDrain Mac CI/CD 작업 후 Health Center Phase 3 CI/CD 전환 검토. 기존 VM 삭제는 CI/credential 의존성 확인 후 사용자 별도 수행 |

## 최신 방향 — 2026-09-20

- 이전 대상은 Health Center뿐 아니라 RWR/SmartDrain을 포함한 개인 서버 전체다.
- Mac 호스트의 remotely-managed Tunnel 하나에 프로젝트별 route를 연결한다. 프로젝트별 Tunnel을 만들지 않는다.
- [11 다중 프로젝트 공통 운영 기준](11_Mac_mini_다중_프로젝트_공통_운영_기준.md)에 사용자 확정 방향, 장기 설계 제안, 결제 화면 강제 중단 규칙을 기록했다.
- 사용자의 명시적 재개 승인으로 Mac 전용 Tunnel과 Health Center 임시 route 설정을 진행했다. 관측성 분리·CI 전환은 여전히 자동 실행하지 않는다.
- 과거 검증 결과는 아래에 보존한다. 다른 세션이 Cloudflare와 다른 프로젝트를 변경할 수 있으므로 외부 설정 재개 전 실제 상태를 재확인한다.
- 09의 Phase 3/4 제외 조건은 이번 문서화만으로 해제되지 않는다. 새 운영 설계 적용은 별도 실행 범위로 정한다.

## 세 프로젝트 인계 스냅샷 — 2026-09-20

다른 두 프로젝트 세션의 최신 Git 상태와 진행 문서를 읽기 전용으로 재확인했다. 이 표를 Cloudflare 공통 작업의 재개 기준으로 사용하며, 각 프로젝트 저장소의 코드·설정은 이 세션에서 수정하지 않는다.

| 프로젝트 | branch / HEAD | 로컬 준비 상태 | Mac origin 계약 | Cloudflare 상태와 다음 검증 |
|---|---|---|---|---|
| Health Center | `docs/mac-orbstack-deployment-analysis` / `5c3ea17` | Stop Point 0/0-B/1 PASS, 새 DB와 observability 실행 검증 완료 | Frontend `http://127.0.0.1:3000`, Backend `http://127.0.0.1:8080` | 임시 route 2개, metrics Access 보호, 공개 URL/CORS·로그인·업무 유형·예약 슬롯·실제 API target PASS |
| RWR | `dev` / `4d77fc4` | GHCR 기반 main 자동 배포와 production localhost 검증 완료. 실제 실패 rollback·Mac reboot 검증은 보류 | nginx `http://127.0.0.1:8090`, health `/api/health`; server/DB는 host 미공개 | `mac-rwr.healthq.store` route와 UI·health·코스·즐겨찾기 PASS. Kakao JavaScript SDK 임시 domain 응답 PASS |
| SmartDrain | `dev` / `dc1c060` | 이관 PR 병합 완료. ARM 실제 추론, 새 DB, 전체 Compose, restart, localhost REST/WebSocket/callback/UI Gate PASS | nginx `http://127.0.0.1:8099`; Backend/AI/DB는 host 미공개 | `mac-smartdrain.healthq.store` route와 공개 분석 E2E, 실제 Kakao 지도 PASS |

- RWR의 과거 `cloudflared 미등록`, 8090/8091 검증 스택 병행 기록은 현재 운영 대상 확정 전의 이력이다. 현재 공통 기준은 `rwr-production`의 loopback 8090이다.
- SmartDrain 문서 일부에 남은 이전 HEAD 또는 `best.pt` 차단 표기는 해결 전 이력이다. 이관 브랜치는 `dev`의 `dc1c060`으로 병합됐고 모델을 사용한 ARM inference와 localhost Gate가 통과했다.
- 세 저장소의 작업 트리는 이 인계 확인 시점에 깨끗했다.
- Cloudflare 공통 설정의 단일 작업 소유자는 이 Health Center 세션이다. 다른 프로젝트 세션에서는 Tunnel, route, DNS, Access를 동시에 변경하지 않는다.
- 임시 hostname 검증은 모두 통과했다. 이후 RWR, SmartDrain, Health Center 순서로 운영 hostname을 Mac Tunnel에 전환했고 Health Center production metrics Access 보호도 검증했다.

## 공통 Cloudflare Task 4 실행 — 2026-09-20

- 세 프로젝트 최신 상태를 재확인했다. Health Center는 문서 branch `5c3ea17`, RWR은 `dev` `4d77fc4`, SmartDrain은 `dev` `dc1c060`이며 세 작업 트리는 확인 시점에 깨끗했다.
- localhost 기준 Health Frontend/Backend, RWR UI/health, SmartDrain UI/dashboard summary가 모두 HTTP 200이었다.
- 기존 `mac-mini-prod` Tunnel은 Healthy였고 가격·Upgrade·Checkout 안내 없이 다음 route와 DNS record를 추가했다.

| 임시 hostname | Mac origin | 결과 |
|---|---|---|
| `mac-demo.healthq.store` | `http://localhost:3000` | 기존 route 유지, 공개 Frontend 200 |
| `mac-api.healthq.store` | `http://localhost:8080` | 기존 route 유지, health 200, metrics Access 302 |
| `mac-rwr.healthq.store` | `http://localhost:8090` | 신규 생성, UI/health/코스/즐겨찾기 PASS |
| `mac-smartdrain.healthq.store` | `http://localhost:8099` | 신규 생성, UI/REST/image/WSS/실제 분석 E2E PASS |

- RWR 공개 검증: seed 랜덤 코스 200, Kakao REST와 ORS를 사용하는 주소 순환 코스 200, 테스트 UUID의 즐겨찾기 생성 201·조회 200·삭제 200·삭제 후 0건을 확인했다.
- SmartDrain 공개 검증: 대시보드와 Mac 데이터, sample image, `실시간 연결됨`을 확인했다. `DR-002` 분석이 `processing`으로 수락된 뒤 DB `completed`, YOLO 결과 연결, YOLO/XGBoost callback 각각 200, 외부 history 결과와 브라우저 최신 시각 갱신을 확인했다.
- SmartDrain 빌드가 사용하는 JavaScript 키와 Kakao 앱을 키 원문 노출 없이 해시 지문으로 대조했다. 실제 소유 앱은 RWR 앱이었으며, 이 앱의 기존 운영·localhost domain을 보존하고 `mac-rwr.healthq.store`, `mac-smartdrain.healthq.store`를 추가했다.
- 잘못 추가했던 별도 SmartDrain 앱의 Mac domain은 사용자 승인 후 제거했고 기존 `localhost` domain은 보존했다. 결제·유료 API·Upgrade 화면은 열지 않았다.
- production 전환 Stop Point 직전에 기존 VM Tunnel이 Healthy이고 운영 route 4개가 각각 `localhost:3000`, `localhost:8080`, `localhost:8090`, `localhost:8099`를 가리키는 것을 Dashboard에서 재확인했다. Tunnel·connector 식별자는 기록하지 않았다.
- 기존 운영 URL 4개는 첫 전환 직전 모두 HTTP 200으로 응답했다. Health Frontend는 `/login`으로 정상 redirect된 뒤 200이었다.
- 사용자 승인 후 RWR을 첫 production 전환 단위로 수행했다. Mac Tunnel에 동일 hostname을 먼저 추가하려는 시도는 기존 DNS 충돌로 저장되지 않았고, 기존 VM Tunnel의 RWR route 하나만 제거한 직후 Mac Tunnel에 `rwr.healthq.store -> http://localhost:8090`을 생성했다.
- Dashboard에서 Mac Tunnel route 저장과 DNS record 생성 성공을 확인했다. 기존 VM Tunnel의 Health Center 두 route와 SmartDrain route, Mac Tunnel의 임시 route 4개는 변경하지 않았다.
- Health Center `.env`는 실제 원문이나 Secret을 출력하지 않고 `NEXT_PUBLIC_APP_URL`, `NEXT_PUBLIC_API_BASE_URL`, `CORS_ALLOWED_ORIGINS`을 Mac 임시 hostname 계약으로 반영했다. 기존 CORS origin은 보존했다.
- Health Center Frontend만 재빌드했고 Backend/Frontend만 재생성했다. 공개 로그인 API, 업무 유형 3건, 예약 슬롯 14건, CORS, 배포 bundle의 `mac-api` target을 확인했으며 localhost API 문자열은 배포 bundle에 없었다.
- Health Center의 PostgreSQL과 observability는 재생성하지 않았다. Backend 재생성 후 Prometheus target `up`, 외부 metrics Access 302를 재확인했다.
- `.env`의 PostgreSQL/Backend/Frontend/Prometheus/Loki/Grafana publish 값을 loopback으로 저장해 명령 override 없이 Compose를 재검증했다.
- 공개 브라우저에서 테스트 시민 계정으로 로그인해 `mac-demo.healthq.store/citizen/reservations/new` 이동, 업무 유형 3개와 다음 날 예약 슬롯 14개 표시를 확인했다. Safari 암호 저장은 선택하지 않았다.

## 실행 판단

- 후속 읽기 조사와 [12 공통 운영 전환 1차 Plan](12_공통_운영_전환_1차_실행_Plan.md) 작성 완료. 실제 설정은 변경하지 않았다.
- 호스트 직접 확인: FileVault Off, AC sleep=1, autorestart=0, OrbStack 2.2.3. 무인 복구 PASS를 뜻하지 않으며 실제 sleep assertion/기동 방식은 추가 확인한다.
- 과거 Docker 직접 확인에서는 RWR 검증 스택 두 개가 8090/8091에 publish됐으나, 이후 RWR 세션에서 production 대상을 loopback 8090으로 확정하고 검증 스택을 정리했다. volume은 보존됐다.
- 과거 SmartDrain 진행 기록의 `best.pt` 부재 Gate는 이후 해결됐다. 최신 인계에서는 실제 모델 기반 ARM inference와 localhost E2E가 통과했다. 두 프로젝트의 변경을 이 저장소에서 재구현하지 않는다.
- 다른 프로젝트 파일·전원 설정·Docker·Cloudflare는 이번 조사에서 수정하지 않았다. 재부팅 전에 모든 프로젝트 작업 상태와 사용자 시점을 확인한다.

- 초기 실행은 전용 문서 브랜치와 실제 `.env`가 있는 실행 경로에서 진행했고, 관련 PR 병합 후 현재 `dev`에서 진행 기록을 이어간다.
- Phase 0/0-B는 애플리케이션 코드를 변경하지 않고 Plan의 runtime 검증을 우선한다.
- 코드 변경이 실제 blocker 때문에 발생할 경우에만 관련 regression을 검출하는 테스트를 먼저 추가한다.

## 비민감 핵심 증거

- 작업 경로: `/Users/tro/dev/health-center-smart-reservation`
- Analysis/Plan 문서 존재 확인.
- Docker context: `orbstack`.
- Docker Compose: `v5.1.2`.
- OrbStack Docker Engine: `aarch64`, 할당 메모리 약 7.8 GiB.
- 실행 전 Health Center 컨테이너·볼륨·이미지 및 3000/8080/5432/3001/9090/3100 포트 충돌 없음.
- `.env` 존재와 필수 변수 관계를 값 출력 없이 확인. 기존 profile은 `dev`이며 임의로 `prod`를 강제하지 않음.
- 로컬 재현에 필요한 DB host/port, frontend/backend URL, CORS, localhost bind는 저장소 파일 수정 없이 명령 단위 비민감 override로 적용.
- Compose config validation PASS. `DOCKER_DEFAULT_PLATFORM` 및 셸 환경의 `SPRING_PROFILES_ACTIVE`는 강제하지 않음.
- Backend/Frontend 이미지 build PASS, 두 이미지 모두 `linux/arm64`.
- PostgreSQL 새 named volume 생성, container healthy. Spring SQL init으로 핵심 table과 업무 유형·예약 슬롯 seed 생성 확인.
- Backend health 3회 HTTP 200, Frontend `/login` HTTP 200.
- CORS preflight HTTP 200이며 `Access-Control-Allow-Origin: http://localhost:3000` 확인.
- 실제 API 흐름: 일반 시민 로그인, 업무 유형 3건, 예약 슬롯 14건 조회 PASS.
- Safari에서 로그인 후 시민 예약 신청 화면과 업무 유형 3건 표시 확인.
- 실행 중인 Frontend bundle의 API target은 `http://localhost:8080`이고, 동일 시점 Backend log에 브라우저 로그인·업무 유형 요청이 기록됨.
- 기존 `observability` profile의 Compose validation·image build·기동 PASS. Prometheus/Grafana/Loki/Promtail 이미지 모두 `linux/arm64`.
- Prometheus Backend target 1개 `UP`; JVM heap 3 series와 HTTP request rate 8 series 조회 PASS.
- Grafana health PASS, provisioned datasource 2개(Prometheus/Loki) 모두 health PASS, 기존 `Health Center Backend Overview` dashboard 탐색 PASS.
- Loki readiness PASS. Promtail Docker discovery로 서비스 label이 생성되고 Loki에서 Backend 20 lines, Frontend 8 lines의 실제 로그 조회 PASS.
- Promtail의 Docker socket과 container log path는 read-only mount이며 최근 오류 로그 없음.
- 7개 Health Center 컨테이너 현재 메모리 합계 약 0.9 GiB. OrbStack 할당 7.8 GiB 대비 각 container 0.55~5.40%, OOM·재시작 없음.
- macOS 전체 메모리 free 53%, swap 156.69 MiB 사용. Docker image 7.527 GB, volume 179.3 MB, build cache 5.916 GB; Data volume 228 GiB 중 50 GiB 사용.

## 실제로 발견된 문제

- 일반 샌드박스 명령에서 OrbStack이 publish한 localhost 포트 접속이 거부됨. 컨테이너·포트·로그는 정상이었고 호스트 네트워크 권한으로 재검증해 서비스 문제가 아닌 도구 격리 문제로 판정.
- Frontend build 중 기존 npm audit 경고 6건(1 moderate, 4 high, 1 critical) 확인. Mac 이전 blocker가 아니므로 runtime upgrade·의존성 변경 없이 Phase 4로 보류.
- 최초 Phase 2 Step 2-1에서는 기존 VM 접속 정보와 인증된 Cloudflare Dashboard 세션이 없어 Stop Point 2-A에서 중단했다. 이후 사용자 로그인으로 Dashboard 읽기 확인을 진행했다.
- 최초 확인 시 Mac에 `cloudflared` 실행 파일과 서비스가 없었다. 이후 실행 파일만 설치했으며 아래 설치 결과를 참조한다.
- 저장소에는 Cloudflare 가이드만 있고 실제 Tunnel config·ID·ingress가 없으며, `.env`에도 Cloudflare/Tunnel 식별 key가 없음.
- Cloudflare 로그인 후 읽기 전용 확인: 기존 Tunnel은 healthy, Linux amd64 connector 1개이며 Health Center와 다른 서비스를 포함한 published application route 4개를 함께 담당함. 기존 Tunnel/token/connector 변경은 다른 서비스에도 영향을 줄 수 있음.
- Health Center 기존 route는 `demo.healthq.store -> http://localhost:3000`, `api.healthq.store -> http://localhost:8080`으로 확인. Tunnel ID·replica ID·원본 IP는 기록하지 않음.
- 당시 Access 목록에 표시된 application 이름/대표 destination은 다른 서비스용이었다. 추가 destination과 wildcard까지 모두 확인한 증거는 없으므로 Health Center에 적용되는 정책 전체가 없다고 단정하지 않는다. 외부 지표 endpoint 응답은 별도 확인했다.
- 외부 비인증 호출 검증: Frontend root HTTP 307, Backend health HTTP 200, Backend `/actuator/prometheus` HTTP 200, Frontend origin CORS preflight HTTP 200/PASS. 지표 endpoint가 현재 외부에 무인증 공개되어 있어 Phase 2 전환 전 보호 방법 확정이 필요함.
- Cloudflare 계정은 Zero Trust Free plan이며 공식 문서상 public application Tunnel은 paid Access plan 없이 사용 가능함. 결제·유료 plan·구독·Load Balancer는 선택하지 않음.
- Homebrew로 `cloudflared 2026.9.1` ARM64 실행 파일만 설치. 서비스 시작·launchd 등록·설정 디렉터리·Tunnel token은 없음.
- 2026-09-20 Tunnel 생성 재개 전 공식 문서와 Dashboard를 재확인했다. Tunnel 생성과 일반 published application route는 Free 범위이며 생성 화면에 가격·Upgrade·Checkout 문구가 없었다.
- `mac-mini-prod` 이름 입력까지 완료했으나 생성 직전 Safari에 다른 작업의 Google QR 로그인 시트가 활성화됐다. 공유 UI 충돌을 피하기 위해 시트를 조작하지 않았고 `Create Tunnel`도 실행되지 않았다. 현재 환경에는 분리 가능한 in-app browser가 없어 이 지점에서 중단했다.
- 사용자가 Google 로그인을 완료한 뒤 `mac-mini-prod` remotely-managed Tunnel을 생성했다. macOS ARM64 connector 1개가 Healthy 상태이며 기존 Tunnel의 connector/token은 재사용하거나 변경하지 않았다.
- Mac host에 `cloudflared` system LaunchDaemon 설치 완료. `/Library/LaunchDaemons/com.cloudflare.cloudflared.plist`가 `running`이고 설치 직후 프로세스는 종료 이력 없이 active였다. token과 전체 명령은 기록하지 않았다.
- 임시 route `mac-demo.healthq.store -> http://localhost:3000`, `mac-api.healthq.store -> http://localhost:8080`을 생성했다. PostgreSQL·Prometheus·Loki·Grafana route는 만들지 않았다.
- Backend 임시 route 생성 직후 외부 `/actuator/prometheus`가 HTTP 200으로 노출되는 것을 재현했다. Zero Trust Free의 경로 단위 Access application `Health Center metrics block`을 생성해 해당 경로만 default-deny로 보호했다.
- Health 재생성 첫 시도에서 현재 스택의 Compose project 이름 `health-center`를 생략해 기존 container name과 충돌했다. 기존 컨테이너는 변경되지 않았지만 기본 project 이름의 미사용 network와 PostgreSQL volume이 생성됐다. 이후 사용자 승인에 따라 소유 project와 연결 컨테이너 0개를 재확인하고 두 리소스만 제거했다.
- 다음 재생성에서 존재하지 않는 `BACKEND_HOST_PORT`/`FRONTEND_HOST_PORT`를 사용해 Backend/Frontend가 잠시 전체 인터페이스에 publish됐다. 실제 Compose 변수 `BACKEND_PORT`/`FRONTEND_PORT`를 확인해 즉시 loopback으로 재생성했고, 이후 여섯 publish 값을 `.env`에 영속 반영했다.

## 수정한 파일/설정

- 기존 실행 중 Mac에 cloudflared 실행 파일을 설치했다. 앱 코드와 Compose는 수정하지 않았다.
- 이번 문서화에서 11 공통 운영 기준 생성, 10 진행 기록 및 루트/docs README 연결을 갱신했다.
- Cloudflare에 `mac-mini-prod` Tunnel, Health Center 임시 published application route 2개, metrics 경로 보호 Access application 1개를 생성했다.
- Mac에 cloudflared system LaunchDaemon을 설치했다. 설치 단계에서는 기존 VM Tunnel·운영 hostname을 수정하지 않았고, 이후 프로젝트별 승인에 따라 운영 route만 순차 전환했다.
- Cloudflare `mac-mini-prod`에 RWR·SmartDrain 임시 route와 DNS record를 추가했다. 결제·유료 기능은 선택하지 않았다.
- Kakao Developers의 실제 사용 JavaScript 키 앱에 RWR·SmartDrain Mac 임시 domain을 추가했다. 기존 운영·localhost domain은 보존했고 비밀 키와 앱 식별자는 문서에 기록하지 않았다.
- 실제 `.env`의 Health 공개 URL/CORS와 여섯 host publish 값만 수정했다. 앱 코드·Compose 파일은 수정하지 않았다.
- Health Center의 공개 Frontend/API URL을 production hostname으로 반영해 Frontend를 재빌드했고, 기존 VM의 Health route 두 개를 제거한 뒤 Mac Tunnel에 `demo.healthq.store -> http://localhost:3000`, `api.healthq.store -> http://localhost:8080`을 생성했다.

## 재검증 결과

- Stop Point 0 PASS: PostgreSQL, Backend, Frontend 기본 재현과 실제 브라우저/API 계약 통과.
- Phase 1 수정 필요 없음. Phase 0-B로 진행.
- Stop Point 0-B PASS: 지표·datasource·기존 dashboard·두 서비스 로그·자원 상태 통과.
- Stop Point 1 PASS: Phase 0/0-B에 Mac 실행 blocker가 없어 저장소 설정·코드 수정 없이 Phase 2로 진행.
- Stop Point 2-A FAIL/STOP: 실제 VM Tunnel 계약과 Cloudflare 복원 대상을 확인할 권한·운영 정보 부족. Phase 2-2, 2-3, Phase 2-S는 시작하지 않음.
- Cloudflare 로그인 후 route·connector와 Access 목록을 읽기 확인했다. 기존 DNS 전체 연결, Access 추가 destination, 비공개 복원본 확보와 VM 서비스 관리 방식은 아직 모두 검증하지 않았다. Stop Point 2-A 완전 통과로 취급하지 않고 재개 시 누락 항목을 확인한다.
- Stop Point 2-UI BLOCKED: 다른 세션이 사용하는 Safari 인증 시트 때문에 Cloudflare 쓰기 작업을 안전하게 분리할 수 없었다. 시트가 종료되고 Cloudflare Tunnel 화면의 독점 조작이 가능할 때 `mac-mini-prod` 생성 직전부터 재개한다.
- 사용자 로그인 완료 후 Stop Point 2-UI 해소. Tunnel/connector와 임시 route 생성 PASS.
- 외부 1차 검증: Frontend root HTTP 307, Backend health HTTP 200. metrics 보호 전 HTTP 200, Access 생성 후 HTTP 302 Access 인증 경로로 전환됨.
- 내부 회귀 검증: Prometheus의 `health-center-backend` target과 자기 target 모두 `UP`; metrics 외부 보호가 내부 scrape를 막지 않음. cloudflared 최근 오류 없음.
- 브라우저 증거 캡처: Tunnel 목록의 `mac-mini-prod` Healthy, Frontend/Backend route 생성 성공, Access application 생성 성공 화면. token이 표시되는 설치 화면은 의도적으로 캡처하지 않았다. 상세 절차는 [13 Cloudflare Mac Tunnel 설정 기록](13_Cloudflare_Mac_Tunnel_설정_기록.md)을 참조한다.
- RWR 임시 공개 Gate PASS: UI, health, seed 코스, 외부 Kakao REST/ORS 코스 생성, 즐겨찾기 CRUD 통과.
- SmartDrain 임시 공개 Gate PASS: UI, REST, image, WSS 연결, 실제 분석→callback→DB→history→브라우저 갱신과 실제 Kakao 지도·마커 로드 통과.
- RWR Kakao JavaScript SDK 임시 domain 검증 PASS: 배포 키가 속한 앱의 허용 domain을 확인했고, `mac-rwr.healthq.store` Referer로 SDK HTTP 200과 SDK signature를 확인했다. 주소 선택 iframe을 포함한 전체 코스 UI는 기존 API 검증 증거를 재사용하며 불필요하게 재생성하지 않았다.
- Health 임시 공개 Gate PASS: Frontend/Backend 200, login token 발급, 업무 유형 3건, 예약 슬롯 14건, CORS와 번들 API target 통과. 실제 브라우저 로그인과 예약 신청 화면의 업무 유형·다음 날 슬롯 표시도 통과.
- production 전환 전 복원 Gate PASS: 기존 VM Tunnel Healthy, 운영 hostname 4개의 기존 localhost origin 매핑 확인, 운영 URL 4개 HTTP 200. 이 증거를 rollback 기준으로 사용한다.
- RWR production 전환 PASS: `rwr.healthq.store` UI, `/api/health`, 저장 코스, 랜덤 코스가 모두 HTTP 200이고 랜덤 코스 응답 계약이 PASS했다. 고유 User-Agent로 보낸 네 요청이 Mac `rwr-production-nginx` 최근 로그에 모두 남아 실제 Mac 도착을 확인했다.
- RWR 운영 Referer로 Kakao Maps JavaScript SDK HTTP 200과 SDK signature를 확인했고 Safari에서 운영 홈 화면을 확인했다.
- 전환 후 재확인에서도 RWR UI·health와 기존 VM에 남은 Health Center Frontend·Backend health·SmartDrain 운영 URL이 모두 HTTP 200이었다.
- SmartDrain 전환 직전 localhost·임시·기존 운영 hostname의 UI와 `/api/dashboard/summary`가 모두 HTTP 200이었다. 기존 VM Tunnel의 SmartDrain route 하나를 제거한 직후 Mac Tunnel에 `smartdrain.healthq.store -> http://localhost:8099`를 생성했고 Dashboard의 route 저장과 DNS record 생성 성공을 확인했다.
- SmartDrain 운영 UI, `/api/dashboard/summary`, `/api/drains`가 모두 HTTP 200이었고 고유 User-Agent 요청 3개가 Mac `smartdrain-mac-nginx` 로그에 기록됐다. Safari 운영 화면에서 실제 Kakao 지도·마커와 `실시간 연결됨`을 확인했다.
- Health Center 전환 직전 localhost·임시·기존 운영 Frontend/Backend health가 모두 HTTP 200이었다. `.env`의 Frontend 공개 URL과 API target을 운영 hostname으로 바꾸고 CORS의 기존 임시·운영 Origin 목록은 유지했다.
- Frontend image 재빌드는 PASS했고 Backend/Frontend 적용 과정에서 Compose 의존 서비스인 PostgreSQL도 같은 named volume으로 재생성됐다. PostgreSQL healthy, 기존 volume 연결, 업무 유형 4건과 예약 슬롯 630건 보존을 확인했다.
- 적용 후 localhost Frontend·Backend health와 운영 Origin CORS가 HTTP 200이었고, 실행 중 Frontend bundle은 운영 API hostname을 포함하며 임시 API hostname은 포함하지 않았다.
- Health Center production route 저장과 DNS record 생성 성공을 Dashboard에서 확인했다. 공개 Frontend·Backend health·업무 유형·운영 Origin CORS가 HTTP 200이었다.
- Safari production Frontend에서 일반 시민 로그인이 성공했고 업무 유형 3개와 다음 날 예약 슬롯 14개를 표시했다. 같은 로그인·업무 유형·예약 슬롯 요청이 Mac Backend 로그에 각각 HTTP 200으로 기록돼 실제 Mac origin과 production API target을 확인했다.
- production `/actuator/prometheus`는 보호 적용 전에 HTTP 200으로 확인됐다. 기존 `Health Center metrics block`에 `api.healthq.store/actuator/prometheus` destination을 추가 입력했으며, Access 권한 변경에 대한 사용자 확인 전에는 저장하지 않았다. 유료 기능이나 결제 안내는 나타나지 않았다.
- 사용자 승인 후 기존 Access application에 production destination만 저장했다. 운영·임시 metrics는 모두 HTTP 302, 일반 health·업무 API·Frontend는 HTTP 200, 내부 Prometheus Backend target은 `UP`이었다. 새 policy, 별도 인증 계층, 유료 기능은 추가하지 않았다.
- S-1 확인 결과 cloudflared는 `RunAtLoad`/`KeepAlive`인 system LaunchDaemon이고 세 프로젝트의 모든 실행 컨테이너는 `unless-stopped`였다. Health Compose는 새 터미널의 `.env`로 config validation을 통과했다.
- OrbStack 2.2.3의 공식 `app.start_at_login`은 최초 `false`였으며 지원되는 최소 설정으로 `true`로 변경했다. FileVault는 Off지만 자동 로그인은 구성되지 않아 이 설정은 사용자 로그인 이후에만 유효하다. 임의 root wrapper/LaunchDaemon은 만들지 않았다.
- AC 전원의 system sleep 값이 `1`로 확인돼 display sleep 뒤 상시 서비스가 중단될 위험이 있다. 서버용 sleep 방지 설정은 아직 변경하지 않았으며 사용자 확인이 필요한 Stop Point로 남겼다.
- S-2에서 Backend, Frontend, PostgreSQL을 순차 재시작했다. 각 public URL/API는 HTTP 200으로 복구됐고 Backend는 PostgreSQL 재시작 후 자체적으로 DB에 재연결했다. 기존 PostgreSQL named volume과 비시연 지속 데이터 건수는 유지됐고 Prometheus target은 `UP`이었다.
- cloudflared 재시작 직전 세 프로젝트 운영 URL은 모두 HTTP 200이었다. `sudo` 관리자 암호가 필요해 재시작 명령은 실행되지 않았으며 암호 우회나 기록은 하지 않았다.
- 사용자가 로컬 Terminal에서 관리자 암호를 직접 입력해 cloudflared LaunchDaemon을 재시작했다. 재시작 횟수가 2로 증가하고 `running` 상태였으며 Health Frontend/Backend, RWR, SmartDrain 운영 URL이 모두 HTTP 200으로 자동 회복했다.
- 사용자가 AC 전원의 system sleep을 `1`에서 `0`으로 변경했다. display sleep은 `10`으로 유지해 화면은 꺼질 수 있지만 시스템 idle sleep은 방지한다. 복원값은 AC system sleep `1`이다.
- 관리자 작업 후 Health production metrics는 계속 HTTP 302, 내부 Prometheus Backend target은 `UP`, OrbStack은 `Running`이고 `app.start_at_login=true`였다. 세 프로젝트의 15개 컨테이너가 모두 실행 상태였다.

## S-3 Mac 재부팅 검증 — 2026-09-20

### 사용자 외부 관찰

- Mac mini를 정상 재부팅한 뒤 로그인하지 않은 상태로 약 10분간 Health Center Frontend/Backend, RWR, SmartDrain 운영 URL을 확인했으나 모두 HTTP 502가 지속됐다.
- 로그인 전에도 system LaunchDaemon인 cloudflared는 시작됐지만 OrbStack/Docker Engine과 origin 컨테이너는 시작되지 않아 요청을 전달할 수 없었다.
- 사용자는 암호를 입력해 로그인만 수행했고 `orb start`, `docker compose up`, 컨테이너·OrbStack·cloudflared 수동 시작/재시작은 수행하지 않았다.
- 로그인 후 OrbStack과 Docker Engine이 시작되고 세 프로젝트 운영 URL이 모두 자동 복구됐다.

### 로그인 후 상태 증거

- host boot 시각은 `2026-09-20 18:03 KST`, 15개 컨테이너의 시작 시각은 모두 약 `18:11 KST`였다. 모든 컨테이너가 `running`, 설정은 `unless-stopped`, restart count는 0이었다.
- Health Center PostgreSQL은 기존 named volume `health-center_health-center-postgres-data`를 다시 연결했고 `refresh_tokens=5`, `service_types=4`, `reservation_slots=630`으로 재부팅 전 검증 데이터가 유지됐다.
- Health Center Backend health, RWR UI/API, SmartDrain UI/API는 HTTP 200이었다. Health Frontend root의 localhost 307은 로그인 화면 redirect 계약이며 공개 URL은 redirect 후 HTTP 200이었다.
- cloudflared LaunchDaemon은 부팅 직후 DNS resolver 준비 전 1회 종료했지만 `KeepAlive`로 6초 뒤 자동 재시작했다. 이후 QUIC 연결 4개와 connectivity pre-check PASS가 기록됐고 수동 조작 없이 운영 URL이 복구됐다.
- Prometheus의 `health-center-backend`와 자기 target은 모두 `UP`이었다. 외부 `api.healthq.store/actuator/prometheus`는 HTTP 302로 Cloudflare Access 로그인 경로로 전환돼 차단 정책이 유지됐다.
- Loki는 재부팅 시 정상 종료 후 WAL 복구를 완료했고 `/ready` HTTP 200, Promtail 로그 수신/flush가 재개됐다. 기동 직후의 일시적 readiness 관찰에는 수정하지 않았다.

### S-3 판정

- Plan의 엄격한 성공 조건인 **로그인 전 10분 이내 무인 복구는 FAIL**이다.
- 현재 지원되는 실제 운영 계약인 **사용자 로그인 후 수동 명령 없는 자동 복구는 PASS**다.
- 자동 로그인, FileVault 변경, root wrapper, custom LaunchDaemon/boot script는 추가하지 않았다.
- 이 조건을 개인 포트폴리오 서버 기준으로 수용할지는 S-4 종료 후 별도 사용자 판단으로 남긴다. S-4 진행은 이 제한을 해소하거나 S-3를 완전 PASS로 바꾸지 않는다.

## S-4 24시간 운영 관찰 시작 — 2026-09-20 18:18 KST

- 종료 측정 가능 시각: `2026-09-21 18:18 KST` 이후. 그 전에는 S-4 완료로 표시하지 않는다.
- 시작 시점 15개 컨테이너 메모리 합계는 약 1.1 GiB이며 모두 실행 중이다. macOS swap in/out은 0이었다.
- Docker 기준값은 image 27.96 GB, container writable layer 8.004 MB, local volume 287.7 MB, build cache 18.73 GB다. macOS Data volume은 228 GiB 중 68 GiB 사용, 139 GiB 여유다.
- Health Center 주요 volume 기준값은 PostgreSQL 67.39 MB, Prometheus 9.391 MB, Grafana 14.6 MB, Loki 2.149 MB다.
- Health Center 7개 컨테이너의 Docker log driver는 `json-file`, `max-size=20m`, `max-file=5`로 확인됐다.
- 시작 시 공개 Health Center Frontend/Backend, RWR, SmartDrain은 redirect 포함 최종 HTTP 200이었다. 외부 metrics는 redirect를 따라가지 않은 상태에서 HTTP 302였고 내부 Prometheus target은 `UP`이었다.
- 종료 시 같은 상태·자원·disk 지표를 재측정하고 display sleep 중 외부 접근 유지, container restart/OOM, volume 증가량을 비교한다. 자동 cleanup이나 retention 변경은 이번 관찰 중 선제 적용하지 않는다.

## S-4 24시간 운영 관찰 종료 — 2026-09-21 18:37 KST

### 서비스와 절전 상태

- 시작 후 24시간 19분이 경과한 시점에 종료 측정을 수행했다.
- 전원 로그상 display는 `2026-09-20 18:32:33 KST`부터 `2026-09-21 18:34:07 KST`까지 꺼져 있었고, 이 구간에 system sleep 진입 기록은 없었다. AC 설정은 system sleep `0`, display sleep `10`으로 유지됐다.
- OrbStack은 공식 CLI `orb`와 `orbctl`에서 모두 `Running`, `app.start_at_login=true`였다. 일반 샌드박스에서 한 차례 `Stopped`로 보인 값은 호스트 권한 교차 확인과 실행 중 Docker 상태가 일치하지 않아 도구 격리 관찰로 판정했다.
- 15개 컨테이너 모두 시작 시각이 `2026-09-20 18:11 KST`로 유지됐고 restart count 0, OOM false였다. healthcheck가 있는 PostgreSQL과 SmartDrain 서비스는 모두 healthy였다.
- Health Center PostgreSQL은 같은 named volume을 사용했고 `refresh_tokens=5`, `service_types=4`, `reservation_slots=630`으로 기준 데이터가 유지됐다.
- localhost Health Center Backend, RWR UI/API, SmartDrain UI/API는 HTTP 200이었다. Health Frontend root는 정상 로그인 redirect인 HTTP 307이었다.
- 공개 Health Center Frontend/Backend, RWR, SmartDrain은 redirect 포함 최종 HTTP 200이었다. 외부 metrics는 HTTP 302 Access 보호를 유지했다.
- Prometheus의 Backend와 자기 target은 모두 `UP`, Loki `/ready`는 HTTP 200, 최근 Backend 로그 20건 조회가 가능했고 Promtail의 관찰 구간 warn/error는 0건이었다.
- Loki warn 1건은 종료 검증의 최근 로그 조회가 끝나며 발생한 `context canceled`로, 같은 요청은 결과를 반환했고 readiness와 수집 상태에는 영향이 없었다.
- cloudflared는 같은 PID와 launchd 실행 횟수 3을 유지하고 현재 HA connection 4개가 연결돼 있었다. 관찰 중 DNS resolver refresh timeout 1건이 있었으나 프로세스 재시작·origin 연결 오류는 없었고 종료 시 공개 URL은 모두 정상 응답했다.

### 자원·디스크 비교

| 항목 | 시작 | 종료 | 증가량/판정 |
|---|---:|---:|---|
| 15개 컨테이너 메모리 합계 | 약 1.1 GiB | 약 1.13 GiB | 유의한 급증 없음 |
| Docker images | 27.96 GB | 27.97 GB | 약 0.01 GB 증가 |
| Container writable layer | 8.004 MB | 18.46 MB | 약 10.46 MB 증가 |
| Local volumes | 287.7 MB | 303 MB | 약 15.3 MB 증가 |
| Build cache | 18.73 GB | 18.73 GB | 변화 없음 |
| Health PostgreSQL volume | 67.39 MB | 67.39 MB | 변화 없음 |
| Health Prometheus volume | 9.391 MB | 23.72 MB | 약 14.33 MB 증가 |
| Health Grafana volume | 14.6 MB | 14.6 MB | 변화 없음 |
| Health Loki volume | 2.149 MB | 3.165 MB | 약 1.02 MB 증가 |
| macOS Data volume | 68/228 GiB 사용 | 68/228 GiB 사용 | 표시 단위 내 변화 없음, 139 GiB 여유 |
| Swap I/O | 0/0 | 0/0 | swap in/out 없음 |

### S-4 판정과 남은 Gate

- display sleep 상태의 24시간 실행, 서비스 접근, DB 보존, 관측성, 자원 여유 기준은 **PASS**다. 이 결과는 장기간 무장애나 backup 복구 가능성을 증명하지 않는다.
- 현재 증가 속도에서 즉시 디스크 고갈 징후는 없으므로 관찰 중 cleanup·retention 변경은 하지 않았다. Prometheus/Loki retention과 image/build cache 정리는 Phase 4 후속 개선으로 유지한다.
- 사용자는 `로그인 후 자동 복구`를 개인 포트폴리오 서버의 현재 운영 조건으로 수용했다. 따라서 로그인 전 무인 복구는 미충족 제약으로 명시하되 Phase 2-S runtime 이전은 완료로 판정한다.
- 기존 Windows 노트북의 Linux VM에 있는 DB 데이터는 이전하지 않으며, 사용자가 향후 VM과 함께 삭제할 예정이다. 이는 의도한 데이터 폐기이므로 Mac 이전 blocker나 backup 작업으로 취급하지 않는다.
- Mac에서 새로 생성되는 데이터의 장기 backup/RPO는 runtime 이전과 분리한 Phase 4 선택사항이다. 복원 검증 없이 backup 완료로 표시하지 않는다.
- 기존 VM 완전 삭제 전에는 데이터 외에 Jenkins, self-hosted runner, credential, VM에만 남은 `.env`가 향후 배포에 필요한지 확인한다. 현재 Mac 서비스 실행은 이 리소스에 의존하지 않는다.

## 현재 배포와 CI/CD 경계 — 2026-09-21

세 프로젝트의 **Mac runtime 배포 완료**와 **main 반영 자동 배포 완료**는 같은 의미가 아니다. 현재 실행 상태와 저장소의 실제 배포 설정을 대조한 결과는 다음과 같다.

| 프로젝트 | 현재 Mac 실행 방식 | `main` 반영 시 Mac 자동 배포 | 판정 |
|---|---|---|---|
| Health Center | 이 저장소 checkout에서 Compose로 build한 local image 실행 | Mac용 GitHub Actions/runner/Jenkins 없음. 기존 `Jenkinsfile`은 Windows 노트북의 Linux VM Jenkins 계약 | runtime 이전 완료, CI/CD 미완료 |
| RWR | GHCR의 commit SHA image와 `/Users/tro/services/rwr/releases/<SHA>` release 사용 | `main` push가 hosted runner의 검증·multi-arch image publish 후 Mac 전용 self-hosted runner 배포를 실행 | runtime 및 Mac CI/CD 완료 |
| SmartDrain | `/Users/tro/dev/smartdrain-agent-upgrade`의 `dev` checkout에서 Compose local build 실행 | GitHub Actions workflow 없음. 기존 `Jenkinsfile`은 VM의 `/home/yp/apps/smart-drain`, `dev`, `smartdrain-dev` 계약 | runtime 이전 완료, CI/CD 미완료 |

- 따라서 세 프로젝트 코드를 `main`에 반영해도 현재 Mac에 자동 배포되는 것은 RWR뿐이다.
- Health Center와 SmartDrain은 수동 Compose로 검증된 현재 runtime을 유지한다. CI/CD 작업 전까지 `main` 반영만으로 실행 컨테이너가 갱신된다고 가정하지 않는다.
- SmartDrain Mac CI/CD를 다음 별도 작업으로 진행하고, Health Center의 Jenkins 제거/대체는 기존 Plan의 Phase 3으로 유지한다.
- RWR workflow와 runner는 검증된 참고 구현일 뿐이며 SmartDrain/Health Center에 파일을 복사하거나 같은 runner가 다른 저장소 job을 자동 수신한다고 가정하지 않는다.
- CI/CD 후속 작업에서도 Cloudflare route, DB volume, 운영 Compose project 이름을 불필요하게 변경하지 않는다.

## Rollback 상태

- 기존 VM, Jenkins, 기존 DB, 기존 Tunnel connector 및 Docker volume은 삭제하거나 변경하지 않았다. 기존 VM Tunnel의 네 운영 route만 프로젝트별 승인과 검증 후 제거했다.
- RWR·SmartDrain·Health Center 운영 route를 Mac Tunnel로 전환했다. rollback은 대상 Mac route를 제거한 직후 기존 VM Tunnel에 원래 hostname과 `localhost:8090`, `localhost:8099`, 또는 Health Center의 `localhost:3000`/`localhost:8080` 매핑을 재생성하고 운영 UI/API를 재검증하는 순서다.
- Health Center는 Frontend/Backend 두 hostname을 하나의 rollback 단위로 취급한다. Mac의 새 DB는 보존하고 기존 VM DB와 자동 병합된다고 가정하지 않는다. Mac Tunnel의 `mac-*` 임시 route 4개도 유지 중이다.
- Mac에 생성한 Health Center 컨테이너·named volume·관측성 스택은 Stop Point 0/0-B 통과 상태로 계속 실행 중이며 삭제하지 않음.
- 전체 Mac Tunnel을 철회해야 할 때는 임시 route·Access·데이터 보존 영향을 먼저 확인하고, Mac LaunchDaemon은 공식 uninstall 절차로 해제한다. RWR만 되돌릴 때는 위의 프로젝트 단위 route 복원 절차를 사용한다.

## 보류 항목

- Health Center Phase 3 Jenkins/CI 개선.
- SmartDrain Mac용 CI/CD 구성. 현재 local Compose runtime과 기존 VM Jenkins 경로를 혼합하지 않고 별도 분석·계획·검증 후 진행한다.
- Phase 4 후속 유지보수.
- npm audit 경고 조치는 runtime/Frontend 의존성 검토와 함께 Phase 4에서 수행.
- cloudflared LaunchDaemon 재시작과 외부 자동 회복 검증은 PASS했다.
- Mac의 서버용 sleep 설정은 AC system sleep `0`, display sleep `10`으로 적용·검증했다.
- OrbStack은 로그인 시 시작하도록 구성됐고 S-3에서 로그인 전 복구 실패, 로그인 후 자동 복구를 확인했다. 보안 약화나 임의 system daemon을 추가하지 않고 이 운영 조건의 수용 여부를 사용자와 결정한다.
- 공통 재부팅에서 세 프로젝트의 컨테이너·DB·Tunnel 복구를 함께 확인했고 S-4 24시간 관찰도 종료했다. 별도의 실제 배포 실패 rollback과 CI runner 검증은 이번 Phase 2-S 범위에 포함하지 않는다.
- 공통 작업 재개 순서: 로그인 후 자동 복구 조건 수용 여부 결정 → 운영 DB backup/RPO 결정 → Mac 이전 완료 및 기존 VM 종료 여부 판단. 기존 VM·DB·Jenkins home·volume 삭제는 별도 승인 전 수행하지 않는다.
- 의도치 않게 생성된 `health-center-smart-reservation` 기본 project의 미사용 network와 PostgreSQL volume은 사용자 승인 후 제거 완료했다. 실제 `health-center` network·volume·컨테이너는 변경하지 않았다.
