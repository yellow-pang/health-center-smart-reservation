# Mac mini + OrbStack 이전 진행 기록

## 실행 기준

| 항목 | 현재 상태 |
|---|---|
| 기준 Analysis | `08_Mac_mini_OrbStack_이전_사전_분석.md` |
| 기준 Plan | `09_Mac_mini_OrbStack_이전_실행_Plan.md` |
| branch | `docs/mac-orbstack-deployment-analysis` |
| 실행 시작 HEAD | `fed9104f754b02b1d993cef2175c0779b97585e0` |
| 현재 Phase / Step | Phase 2-S, S-3 Mac reboot 승인 Gate |
| 완료된 Stop Point | Stop Point 0, Stop Point 0-B, Stop Point 1, Stop Point 2-A, Stop Point 2-B |
| 상태 | 세 프로젝트 운영 전환 PASS. S-1 실무 필수 설정과 S-2 컨테이너·DB·cloudflared 재시작 PASS. 로그인 전 OrbStack 복구와 S-3 reboot는 미검증 |
| 다음 시작 Step | 사용자 별도 승인 후 S-3 정상 reboot와 로그인 전/후 외부 복구 판정 |

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

- 현재 checkout은 전용 문서 브랜치이며 실제 `.env`가 있는 실행 경로이므로 별도 worktree를 만들지 않는다.
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

## Rollback 상태

- 기존 VM, Jenkins, 기존 DB, 기존 Tunnel connector 및 Docker volume은 삭제하거나 변경하지 않았다. 기존 VM Tunnel의 네 운영 route만 프로젝트별 승인과 검증 후 제거했다.
- RWR·SmartDrain·Health Center 운영 route를 Mac Tunnel로 전환했다. rollback은 대상 Mac route를 제거한 직후 기존 VM Tunnel에 원래 hostname과 `localhost:8090`, `localhost:8099`, 또는 Health Center의 `localhost:3000`/`localhost:8080` 매핑을 재생성하고 운영 UI/API를 재검증하는 순서다.
- Health Center는 Frontend/Backend 두 hostname을 하나의 rollback 단위로 취급한다. Mac의 새 DB는 보존하고 기존 VM DB와 자동 병합된다고 가정하지 않는다. Mac Tunnel의 `mac-*` 임시 route 4개도 유지 중이다.
- Mac에 생성한 Health Center 컨테이너·named volume·관측성 스택은 Stop Point 0/0-B 통과 상태로 계속 실행 중이며 삭제하지 않음.
- 전체 Mac Tunnel을 철회해야 할 때는 임시 route·Access·데이터 보존 영향을 먼저 확인하고, Mac LaunchDaemon은 공식 uninstall 절차로 해제한다. RWR만 되돌릴 때는 위의 프로젝트 단위 route 복원 절차를 사용한다.

## 보류 항목

- Phase 3 Jenkins/CI 개선.
- Phase 4 후속 유지보수.
- npm audit 경고 조치는 runtime/Frontend 의존성 검토와 함께 Phase 4에서 수행.
- cloudflared LaunchDaemon 재시작과 외부 자동 회복 검증은 PASS했다.
- Mac의 서버용 sleep 설정은 AC system sleep `0`, display sleep `10`으로 적용·검증했다.
- OrbStack은 로그인 시 시작하도록 바꿨지만 자동 로그인이 없어 로그인 전 완전 무인 복구 목표는 아직 충족하지 않는다. 보안 약화나 임의 system daemon 대신 실제 S-3 결과와 운영 계약을 사용자와 결정한다.
- RWR의 실제 실패 유도 rollback과 runner/stack 재부팅 복구, SmartDrain의 서버 수명주기 검증은 최종 공통 재부팅 시점에 함께 확인한다. RWR runner는 사용자 로그인 후 동작하는 LaunchAgent이므로 system LaunchDaemon인 cloudflared와 복구 조건을 분리 판정한다.
- 모든 다른 세션이 종료되고 사용자가 재부팅 시점을 승인하기 전에는 Mac reboot를 실행하지 않는다.
- 공통 작업 재개 순서: 별도 승인 후 S-3 reboot → 로그인 전/후 복구 판정 → 조건이 합의되면 S-4 24시간 관찰 시작.
- 의도치 않게 생성된 `health-center-smart-reservation` 기본 project의 미사용 network와 PostgreSQL volume은 사용자 승인 후 제거 완료했다. 실제 `health-center` network·volume·컨테이너는 변경하지 않았다.
