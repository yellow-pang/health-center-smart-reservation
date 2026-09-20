# 공통 운영 전환 1차 실행 Plan

> 실행 담당자는 `superpowers:executing-plans`를 참고하여 아래 작업을 순서대로 진행한다. 사용자 지시와 각 저장소 지침이 우선한다. 문서 작성은 외부 설정 변경 완료를 뜻하지 않는다.

**목표:** 세 프로젝트의 현재 작업을 보존하면서 Mac 공통 호스트 준비와 단일 Tunnel 전환의 선행조건을 검증한다.

**구조:** Mac host cloudflared 하나, 프로젝트별 독립 Compose, localhost HTTP endpoint. 공통 관측성/CI 전체 전환은 독립 후속 작업으로 분리한다.

**기술:** macOS, OrbStack 2.2.3, Docker Compose, cloudflared 2026.9.1, Cloudflare remotely-managed Tunnel.

**기준:** [11 공통 운영 기준](11_Mac_mini_다중_프로젝트_공통_운영_기준.md), [09 Health Center Plan](09_Mac_mini_OrbStack_이전_실행_Plan.md), [10 진행 기록](10_Mac_mini_OrbStack_이전_진행기록.md). 작성일 2026-09-20, 시작 HEAD `f0d2748`.

## 1. 최신 인계 결과

| 대상 | 이번 직접 확인 또는 출처 | 판정 |
|---|---|---|
| Health Center | HEAD `5c3ea17`; 7개 container, 기본 기능·관측성·새 DB 검증 PASS | 임시 route 생성 완료. 공개 URL/CORS와 브라우저 기능 검증 필요 |
| RWR | HEAD `4d77fc4`; main 자동 배포와 production localhost 검증 PASS | `rwr-production`, nginx loopback 8090을 임시 route 대상으로 확정 |
| SmartDrain | `dev` HEAD `dc1c060`; 이관 PR 병합, ARM inference와 전체 Compose/새 DB/localhost E2E PASS | nginx loopback 8099를 임시 route 대상으로 확정 |
| FileVault | 호스트 권한으로 `fdesetup status`: Off | 디스크 암호화 해제 작업 불필요. 자동 로그인/무인 복구 성공을 의미하지 않음 |
| 전원 | `pmset -g custom`: AC sleep=1, displaysleep=10, autorestart=0 | 서버 sleep/정전 복구 설정 검토 필요; assertion에 의한 실제 sleep 억제 여부는 미확인 |
| cloudflared | version 2026.9.1, system LaunchDaemon running, `mac-mini-prod` connector Healthy | 공통 connector 준비 완료. 중복 Tunnel/service 생성 금지 |
| OrbStack | CLI version 2.2.3 | 로그인 전 Engine 복구는 미검증 |

샌드박스 내 `fdesetup status`는 volume 오류를 냈으나 호스트 권한 재조회는 정상 응답했다. 이 오류를 macOS 장애로 판단하지 않는다.

다른 프로젝트 근거(읽기 전용 확인, 외부 설정 쓰기 전 Git/localhost 상태 재확인):

- RWR: `/Users/tro/dev/RWR-mini-project`, 최신 HEAD `4d77fc4`. GHCR arm64 image를 사용하는 main 자동 배포와 `127.0.0.1:8090`의 UI/API health가 검증됐다. Cloudflare 작업은 본 세션으로 인계됐다.
- SmartDrain: `/Users/tro/dev/smartdrain-agent-upgrade`, `dev` 최신 HEAD `dc1c060`. 실제 모델 기반 ARM 추론과 `127.0.0.1:8099`의 REST/WebSocket/callback/UI Gate가 검증됐고 이관 PR이 병합됐다. Cloudflare 작업은 본 세션으로 인계됐다.
- 각 프로젝트 문서에 남은 이전 상태의 HEAD, RWR 검증 스택, SmartDrain 모델 차단 표기는 시점 기록으로 보존하되 현재 실행 판단에는 위 최신 인계를 사용한다.

## 2. 공통 제약과 검수 포인트

- 다른 프로젝트 저장소·DB·검증 container를 이 세션에서 임의 변경하지 않는다.
- Cloudflare 변경 담당 세션은 한 번에 하나다. 다른 세션의 변경 종료/인계 상태를 확인한다.
- 11 문서의 결제 화면 강제 중단 규칙을 모든 Dashboard 조작에 적용한다.
- Secret 원문, 전체 env/config/inspect/process args, token 표시 화면을 기록하지 않는다.
- 기존 VM/Tunnel은 유지하고 Mac을 기존 Tunnel replica로 연결하지 않는다.
- 재부팅은 세 프로젝트 작업에 영향을 준다. 실행 전 사용자와 다른 세션의 작업 종료 및 시점을 확인한다.
- volume 삭제, 자동 prune, 기존 VM 정리, runtime upgrade는 이번 1차 범위에 없다.
- 문서/설정 작업 때문에 Application unit test를 새로 추가하지 않는다.

| 검수 포인트 | 검출 작업 |
|---|---|
| 다른 세션이 동일 Tunnel/Compose를 변경 | Task 1의 소유권·상태 인계 |
| 검증용 DB와 운영용 DB 혼동 | Task 2의 project/volume 대조 |
| 로그인 후 복구를 무인 복구로 오판 | Task 3의 로그인 전/후 분리 |
| DNS만 옮기고 Frontend가 VM API 사용 | Task 4의 실제 브라우저 target·Mac 요청 증거 |
| 지표 공개 또는 유료 기능 오활성화 | Task 4의 외부 관리 경로 거부·결제 중단 |

## 3. 파일과 변경 책임

| 대상 | 책임/변경 시점 |
|---|---|
| 본 문서, 10 진행 기록, docs README | 조정·검증 결과 기록 |
| Health Center 기존 `.env` / Compose | 실제 적용 단계에 필요한 설정만, 09 기준 |
| RWR 기존 `.env`의 `NGINX_PORT` | 해당 세션에서 운영 대상 결정 후 localhost 제한 |
| SmartDrain 기존 `.env`의 `NGINX_HTTP_PORT`, `SMARTDRAIN_YOLO_MODEL_PATH` | 해당 세션에서 포트·모델 공급 검증 |
| macOS 전원/OrbStack 설정 | Task 3에서 지원·영향 확인 후 명시된 항목만 |
| Cloudflare Tunnel/DNS/Access, 호스트 서비스 | Task 4에서 선행 Gate와 작업 인계 완료 후 |

## 4. Task 1 — 공통 작업 소유권 확정

**입력:** 11 운영 기준, 세 프로젝트 최신 진행 기록. **산출:** 프로젝트별 담당 작업·현재 Gate·배포 대상·공통 인프라 담당 세션.

- [x] 실제 세 저장소의 Compose와 진행 기록 위치를 확인한다.
- [x] Docker container 이름과 publish, FileVault/전원 설정을 읽기 확인한다.
- [x] RWR·SmartDrain 세션에서 Cloudflare 작업을 본 Health Center 세션으로 인계받았다.
- [x] `mac-mini-prod` Tunnel, Health Center 임시 route 2개, metrics Access application의 실제 생성 상태를 확인했다.
- [ ] RWR·SmartDrain route를 쓰기 직전에 Dashboard의 Tunnel/route/DNS/Access 상태를 다시 확인한다.
- [ ] 기존 route 복원본이 비공개로 확보됐는지 확인한다. 목록 조회만으로 rollback 준비 완료라고 표시하지 않는다.

**성공:** 공통 설정의 동시 수정이 배제되고 최신 origin/route와 복원 대상이 특정됨. **실패:** 해당 공통 설정 쓰기 중단. **Rollback:** 읽기 전용이므로 없음.

## 5. Task 2 — 프로젝트별 로컬 준비 Gate

**입력:** 담당 프로젝트 최신 실행 기록. **산출:** 각 프로젝트의 단일 운영 대상과 임시 hostname 연결 가능 여부.

- [x] Health Center는 09 Stop Point 0/0-B 증거를 재사용한다. image/config가 바뀐 부분만 재검증하고 공개 URL/CORS 반영은 Task 4에서 수행한다.
- [x] RWR 운영 대상은 `rwr-production`, nginx `127.0.0.1:8090`, health `/api/health`로 확정됐다. server/DB는 host에 publish하지 않는다.
- [x] SmartDrain은 실제 모델 기반 ARM64 inference, migration, 새 DB, REST/WebSocket/callback/UI localhost Gate를 통과했다. nginx `127.0.0.1:8099`만 공개한다.
- [ ] 프로젝트별 DB volume과 이름을 기록한다. 새 이름을 쓰면 새 volume이 생길 수 있으므로 임의 project rename을 금지한다.
- [ ] route 생성 직전에 세 origin과 대표 health/API를 재확인한다. 기존 검증과 동일한 image/config이면 전체 build/test는 반복하지 않는다.

**성공:** 해당 프로젝트가 localhost에서 실제 외부 계약을 통과함. 한 프로젝트 실패가 이미 검증된 다른 프로젝트의 임시 route 준비를 막지는 않는다. **실패:** 해당 프로젝트 route 추가 보류. **Rollback:** 기존 검증 stack/volume 보존; 삭제 정리 없음.

## 6. Task 3 — 호스트 복구 지원 확인과 전원 기준

**입력:** FileVault Off, sleep=1, autorestart=0 관측값. **산출:** 지원되는 자동 기동 경로와 무인 복구 가능/불가 판정.

읽기 명령:

```bash
fdesetup status
pmset -g custom
pmset -g assertions
orb version
docker --context orbstack ps --format '{{.Names}}|{{.Status}}|{{.Ports}}'
```

- [ ] 설치된 OrbStack의 자동 시작 설정과 공식 지원 방식에서 로그인 전 실행 가능 여부를 확인한다.
- [ ] macOS 전원 설정에서 시스템 sleep 방지와 정전 복구 가능 항목을 확인한다. 화면 sleep은 유지 가능하다. 변경 전 값과 복원 방법을 기록하고 지원되는 설정만 적용한다.
- [ ] cloudflared의 boot service와 OrbStack Engine 복구가 독립적임을 확인한다. token 전달·서비스 파일 권한은 공식 remotely-managed 절차 기준으로 준비한다.
- [ ] 지원되는 방법으로 무인 복구가 불가능하면 사용자에게 제한을 보고하고 중단한다. 자동 로그인/보안 약화/custom root script를 임의 적용하지 않는다.

**성공:** 지원 방식이 확인돼 재부팅 시험을 설계할 수 있음. 실제 복구 성공 판정은 Task 4 이후 09 S-3에서 수행한다. **실패:** 무인 서버 완료 판정 보류. **Rollback:** 변경한 전원/자동 시작 항목만 기록한 값으로 복원; 다른 프로젝트 서비스는 건드리지 않음.

## 7. Task 4 — 단일 Mac Tunnel 임시 공개와 프로젝트별 전환

**입력:** Task 1 인계·복원 준비, 대상 프로젝트 Task 2 PASS. Task 3의 최종 reboot 조건은 임시 공개를 막지 않지만 production 전환 완료 판정 전에 반드시 해결한다. **산출:** Mac 임시 HTTPS 경로와 검증된 production 전환.

- [x] 서버 단위 remotely-managed Tunnel `mac-mini-prod`와 Mac connector가 생성됐으며 Healthy다. Tunnel과 connector를 추가 생성하지 않는다.
- [x] 11 결제 규칙과 UI 권한 확인 규칙을 적용했고 기존 Tunnel token을 재사용/회전하지 않았다.
- [x] Mac host connector를 system LaunchDaemon으로 연결했다. token을 채팅·문서·일반 shell history에 남기지 않았다.
- [x] 대상 프로젝트의 `mac-*` hostname 충돌 여부를 확인하고 네 개의 localhost 매핑을 등록했다. 내부 DB/관측성 route는 만들지 않았다.
- [x] Health Center `mac-demo`, `mac-api` route와 `/actuator/prometheus` 경로 단위 Access 보호를 생성했다. 외부 보호 후 내부 Prometheus target `UP`을 확인했다.
- [x] Health Center의 공개 URL/CORS, API 로그인·업무 유형·예약 슬롯·실제 Backend target과 브라우저 폼 로그인·예약 화면을 검증했다.
- [x] RWR `mac-rwr.healthq.store -> http://localhost:8090` 등록, `/`, `/api/health`, 코스 생성, 즐겨찾기가 PASS했다. 실제 배포 JavaScript 키의 소유 앱에 임시 domain을 추가하고 Referer를 포함한 SDK 응답을 검증했다. 별도 Backend route는 만들지 않았다.
- [x] SmartDrain `mac-smartdrain.healthq.store -> http://localhost:8099` 등록, `/`, dashboard REST, image, WSS, 실제 분석→callback→DB→UI가 PASS했다. 배포 JavaScript 키가 RWR 앱 소유임을 비밀 값 노출 없이 확인해 해당 앱에 임시 domain을 추가했고, 공개 브라우저에서 실제 Kakao 지도·마커 로드를 검증했다. Backend/AI/DB route는 만들지 않았다.
- [x] Health Center production `/actuator/prometheus`의 외부 HTTP 200 노출을 확인해 기존 Access application에 production destination만 추가했다. 운영·임시 외부 metrics 302, 일반 API 200, 내부 Prometheus `UP`을 재검증했다. 새 policy나 유료 기능은 추가하지 않았다.
- [x] Health Center URL/CORS 및 Frontend build를 09 Step 2-2 기준으로 production hostname에 적용했다. 앱 코드와 Compose는 수정하지 않았다.
- [x] 외부 네트워크에서 Health Center production 로그인·업무 유형·예약 슬롯과 브라우저 API target을 Mac Backend 로그와 대조했다.
- [x] 생산 hostname 전환에 09 Step 2-3의 쓰기 통제·rollback 기준을 프로젝트별 적용했다. Health Center 두 hostname은 같은 전환 단위로 옮기고 검증했다.
- [x] RWR production hostname을 기존 VM Tunnel에서 Mac Tunnel로 전환하고 UI·health·저장/랜덤 코스·Kakao SDK·Mac nginx 도착을 검증했다. 나머지 프로젝트는 별도 승인 전에 전환하지 않는다.
- [x] SmartDrain production hostname을 기존 VM Tunnel에서 Mac Tunnel로 전환하고 UI·dashboard·drains API·실제 Kakao 지도·WebSocket 상태·Mac nginx 도착을 검증했다.
- [x] Health Center production Frontend/Backend hostname을 Mac Tunnel로 전환하고 health·CORS·로그인·업무 유형·예약 슬롯·Mac Backend 도착과 production metrics Access 보호를 검증했다.

### Task 4 Stop Point

1. Health Center 임시 기능 검증 PASS 전에는 RWR/SmartDrain 설정과 원인을 섞어 수정하지 않는다.
2. RWR 임시 route 검증 실패 시 RWR만 중단하고 localhost와 외부 결과를 비교한다. Health Center 설정을 되돌리지 않는다.
3. SmartDrain 임시 route 검증 실패 시 SmartDrain만 중단하고 REST·WSS·callback 중 실패 경계를 찾는다. 다른 프로젝트 route를 바꾸지 않는다.
4. 세 임시 hostname이 모두 PASS한 뒤 현재 production route 복원 정보를 재확인하고 사용자 승인을 받는다.
5. production 전환은 프로젝트 하나씩 수행한다. Health Center의 frontend/backend 두 hostname만 같은 전환 단위로 취급한다.

**성공:** Mac origin의 실제 서비스 계약, 외부 관리 경로 차단, 내부 관측성 유지. **실패:** 실패한 프로젝트 전환 중지; 쓰기와 데이터 보존 판단 후 원래 route 복원. **Rollback:** 기존 Tunnel/VM 유지, Mac 새 데이터 필요 시 별도 보관. DNS 복원으로 DB 데이터가 돌아간다고 가정하지 않음.

## 8. Task 5 — 전체 서버 검증 및 후속 작업 분리

- [ ] 모든 작업 세션 종료·재부팅 시점을 확인한 뒤 09 S-1~S-3 기준으로 컨테이너/DB/Tunnel/reboot 복구를 검증한다. 합격 기준은 로그인·수동 기동 없이 10분 이내 외부 복구와 데이터 보존이다.
- [ ] 세 프로젝트가 준비되면 동일 reboot에서 모두 확인해 반복 시험을 줄인다. 별도 시점에 검증한 프로젝트는 나중에 최종 조합 영향만 다시 확인한다.
- [ ] S-4 시작 시각, 자원·디스크 기준값과 외부 URL 상태를 10에 기록한다. 24시간 후 종료 검증 전 완료 표시하지 않는다.
- [ ] 기존 VM 전체 종료는 세 프로젝트 모두 검증된 뒤 사용자 판단을 받는다. 삭제는 별도 승인이다.

다음은 이번 1차 Plan의 자동 실행 대상이 아니다. 각각 별도 변경 단위와 검증을 가진다.

| 후속 단위 | 독립 완료 조건 |
|---|---|
| 공통 관측성 분리 / Alloy 전환 | 기존 dashboard·지표·세 프로젝트 로그 동등성, 단일 수집 경로, 내부 접근 경계 |
| CI/Registry 배포 표준 | 프로젝트별 image 게시·pull·digest 기록·실패 rollback. RWR의 기존 진행 작업 재사용 |
| 백업/retention 운영화 | 별도 DB 복원 성공, Mac 외부 사본 확인, 실제 로그 저장량 감소 |

## 9. 검수와 재개 위치

- [x] 09의 기존 runtime 증거를 재사용하고 전체 build/test를 반복하지 않는다.
- [x] 다른 프로젝트 기록을 재현 성공으로 과장하지 않는다.
- [x] model 부재·LAN publish·절전 설정을 실제 증거와 연결했다.
- [x] 공통 관측성/CI 전체 재설계를 이번 1차 작업에 묶지 않았다.
- [x] 결제 중단, 다른 세션 인계, 기존 VM 보호, 무인 복구 기준을 유지했다.
- [x] 실제 Secret·개인정보를 기록하지 않았다.

Task 4는 PASS했다. S-1에서 OrbStack의 공식 로그인 시작을 활성화하고 AC system sleep을 `0`으로 적용했으며 display sleep `10`은 유지했다. 자동 로그인은 없어 로그인 전 OrbStack 복구 여부는 아직 미검증이다. S-2의 Health 컨테이너·DB 재시작과 cloudflared LaunchDaemon 재시작, 세 프로젝트 외부 자동 회복은 PASS했다. 다음 실행 위치는 S-3 Mac reboot 승인 Stop Point다.
