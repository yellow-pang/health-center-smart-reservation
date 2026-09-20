# Mac mini + OrbStack 이전 진행 기록

## 실행 기준

| 항목 | 현재 상태 |
|---|---|
| 기준 Analysis | `08_Mac_mini_OrbStack_이전_사전_분석.md` |
| 기준 Plan | `09_Mac_mini_OrbStack_이전_실행_Plan.md` |
| branch | `docs/mac-orbstack-deployment-analysis` |
| 실행 시작 HEAD | `fed9104f754b02b1d993cef2175c0779b97585e0` |
| 현재 Phase / Step | Phase 2 Step 2-2, Mac 전용 임시 Tunnel 외부 경로 검증 중 |
| 완료된 Stop Point | Stop Point 0, Stop Point 0-B, Stop Point 1 |
| 상태 | Mac 전용 Tunnel·Health Center 임시 route·metrics Access 보호 생성 및 1차 검증 완료 |
| 다음 시작 Step | 공개 URL/CORS를 반영한 Frontend 재빌드 전 현재 `.env` 계약 확인 |

## 최신 방향 — 2026-09-20

- 이전 대상은 Health Center뿐 아니라 RWR/SmartDrain을 포함한 개인 서버 전체다.
- Mac 호스트의 remotely-managed Tunnel 하나에 프로젝트별 route를 연결한다. 프로젝트별 Tunnel을 만들지 않는다.
- [11 다중 프로젝트 공통 운영 기준](11_Mac_mini_다중_프로젝트_공통_운영_기준.md)에 사용자 확정 방향, 장기 설계 제안, 결제 화면 강제 중단 규칙을 기록했다.
- 사용자의 명시적 재개 승인으로 Mac 전용 Tunnel과 Health Center 임시 route 설정을 진행했다. 관측성 분리·CI 전환은 여전히 자동 실행하지 않는다.
- 과거 검증 결과는 아래에 보존한다. 다른 세션이 Cloudflare와 다른 프로젝트를 변경할 수 있으므로 외부 설정 재개 전 실제 상태를 재확인한다.
- 09의 Phase 3/4 제외 조건은 이번 문서화만으로 해제되지 않는다. 새 운영 설계 적용은 별도 실행 범위로 정한다.

## 실행 판단

- 후속 읽기 조사와 [12 공통 운영 전환 1차 Plan](12_공통_운영_전환_1차_실행_Plan.md) 작성 완료. 실제 설정은 변경하지 않았다.
- 호스트 직접 확인: FileVault Off, AC sleep=1, autorestart=0, OrbStack 2.2.3. 무인 복구 PASS를 뜻하지 않으며 실제 sleep assertion/기동 방식은 추가 확인한다.
- Docker 직접 확인: RWR 검증 스택 두 개가 8090/8091을 IPv4/IPv6 전체 interface에 publish한다. 운영 대상 선정과 localhost 제한은 RWR 담당 세션에서 처리한다.
- SmartDrain 진행 기록상 동일 운영 best.pt 부재로 Gate A BLOCKED, 실제 Compose는 Alembic migrate/선택 seed와 모델 mount를 사용한다. RWR 기록상 CI/GHCR 전환은 이미 다른 세션에서 진행 중이므로 재구현하지 않는다.
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

## 수정한 파일/설정

- 기존 실행 중 Mac에 cloudflared 실행 파일을 설치했다. 앱 코드·Compose·실제 `.env`는 수정하지 않았다.
- 이번 문서화에서 11 공통 운영 기준 생성, 10 진행 기록 및 루트/docs README 연결을 갱신했다.
- Cloudflare에 `mac-mini-prod` Tunnel, Health Center 임시 published application route 2개, metrics 경로 보호 Access application 1개를 생성했다.
- Mac에 cloudflared system LaunchDaemon을 설치했다. 기존 VM Tunnel·운영 hostname·기존 Access application은 수정하지 않았다.

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

## Rollback 상태

- 기존 VM, Jenkins, 기존 DB, 기존 Tunnel 및 Docker volume 변경 없음.
- 기존 VM Tunnel·운영 DNS·운영 Access 설정은 변경하지 않았다. 새 Mac Tunnel과 `mac-*` 임시 DNS/route만 추가했다.
- Mac에 생성한 Health Center 컨테이너·named volume·관측성 스택은 Stop Point 0/0-B 통과 상태로 계속 실행 중이며 삭제하지 않음.
- Rollback이 필요하면 먼저 새 임시 route/Access/Tunnel의 데이터 보존 영향이 없음을 확인한 뒤 제거하고, Mac LaunchDaemon을 공식 uninstall 절차로 해제한다. 기존 VM Tunnel은 그대로 유지되어 운영 rollback 대상이 아니다.

## 보류 항목

- Phase 3 Jenkins/CI 개선.
- Phase 4 후속 유지보수.
- npm audit 경고 조치는 runtime/Frontend 의존성 검토와 함께 Phase 4에서 수행.
- 재개 전 소유자가 기존 VM에서 실행 위치, systemd/manual/container 여부, 서비스·config 위치, hostname→localhost route를 비밀 값 없이 확인할 수 있게 해야 함.
- Cloudflare 로그인으로 확인한 항목과 미확인 항목을 구분하고, 재개 시 DNS·Access 상세·원래 설정 복원본 및 다른 세션의 변경 내용을 재확인한다.
- 임시 Frontend는 아직 Phase 0의 localhost API target 번들이므로 외부 UI의 실제 API target/CORS 검증은 미완료다. `.env`의 공개 URL 계약을 비밀값 출력 없이 확인한 뒤 Frontend만 재빌드해야 한다.
