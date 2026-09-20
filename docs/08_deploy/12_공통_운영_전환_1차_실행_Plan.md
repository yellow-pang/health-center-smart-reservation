# 공통 운영 전환 1차 실행 Plan

> 실행 담당자는 `superpowers:executing-plans`를 참고하여 아래 작업을 순서대로 진행한다. 사용자 지시와 각 저장소 지침이 우선한다. 문서 작성은 외부 설정 변경 완료를 뜻하지 않는다.

**목표:** 세 프로젝트의 현재 작업을 보존하면서 Mac 공통 호스트 준비와 단일 Tunnel 전환의 선행조건을 검증한다.

**구조:** Mac host cloudflared 하나, 프로젝트별 독립 Compose, localhost HTTP endpoint. 공통 관측성/CI 전체 전환은 독립 후속 작업으로 분리한다.

**기술:** macOS, OrbStack 2.2.3, Docker Compose, cloudflared 2026.9.1, Cloudflare remotely-managed Tunnel.

**기준:** [11 공통 운영 기준](11_Mac_mini_다중_프로젝트_공통_운영_기준.md), [09 Health Center Plan](09_Mac_mini_OrbStack_이전_실행_Plan.md), [10 진행 기록](10_Mac_mini_OrbStack_이전_진행기록.md). 작성일 2026-09-20, 시작 HEAD `f0d2748`.

## 1. 이번 조사 결과

| 대상 | 이번 직접 확인 또는 출처 | 판정 |
|---|---|---|
| Health Center | Docker에서 7개 container 실행, HTTP publish는 127.0.0.1 | 이전 API PASS 증거 유지. 이번에는 API 재시험 안 함 |
| RWR | `rwr-phase0` 8090, `rwr-phase1` 8091; 모두 IPv4/IPv6 전체 interface publish | 검증용 두 스택 중 운영 대상 선정과 localhost 제한 필요 |
| SmartDrain | 실제 Compose에 migrate/seed/AI 모델 mount 있음. 진행 기록은 Gate A BLOCKED | Health Center SQL init 절차를 그대로 적용하면 안 됨 |
| FileVault | 호스트 권한으로 `fdesetup status`: Off | 디스크 암호화 해제 작업 불필요. 자동 로그인/무인 복구 성공을 의미하지 않음 |
| 전원 | `pmset -g custom`: AC sleep=1, displaysleep=10, autorestart=0 | 서버 sleep/정전 복구 설정 검토 필요; assertion에 의한 실제 sleep 억제 여부는 미확인 |
| cloudflared | 실행 파일 version 2026.9.1 | 이번에는 서비스 등록/연결 상태 재검증 안 함 |
| OrbStack | CLI version 2.2.3 | 로그인 전 Engine 복구는 미검증 |

샌드박스 내 `fdesetup status`는 volume 오류를 냈으나 호스트 권한 재조회는 정상 응답했다. 이 오류를 macOS 장애로 판단하지 않는다.

다른 프로젝트 근거(읽기 전용 확인, 실시간 진행 중이므로 재개 때 재확인):

- RWR: `/Users/tro/dev/RWR-mini-project/docker-compose.yml`, `docs/34-mac-mini-deployment-analysis/steps/step-34-phase-2-ci-ghcr.md`. 기록상 CI/GHCR 준비 및 PR 검증은 진행됐으나 main image publish는 남아 있다. 원격 상태를 이 세션에서 재검증한 것은 아니다.
- SmartDrain: `/Users/tro/dev/smartdrain-agent-upgrade/docker-compose.yml`, `docs/steps/step-03-mac-mini-orbstack-migration.md`. 기록상 동일 운영 `best.pt` 부재로 YOLO 검증이 막혀 있다. 모델 파일이나 실제 env 값은 이번에 열지 않았다.

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
- [ ] 다른 세션의 Cloudflare 변경 여부와 완료 시점을 인계받고 Dashboard에서 실제 Tunnel/route/DNS/Access 상태를 재확인한다.
- [ ] 기존 route 복원본이 비공개로 확보됐는지 확인한다. 목록 조회만으로 rollback 준비 완료라고 표시하지 않는다.

**성공:** 공통 설정의 동시 수정이 배제되고 최신 origin/route와 복원 대상이 특정됨. **실패:** 해당 공통 설정 쓰기 중단. **Rollback:** 읽기 전용이므로 없음.

## 5. Task 2 — 프로젝트별 로컬 준비 Gate

**입력:** 담당 프로젝트 최신 실행 기록. **산출:** 각 프로젝트의 단일 운영 대상과 임시 hostname 연결 가능 여부.

- [ ] Health Center는 09 Stop Point 0/0-B 증거를 재사용한다. image/config가 바뀐 부분만 재검증하고 명령별 override를 운영 `.env`에 정착시키는 시점을 Task 4와 맞춘다.
- [ ] RWR는 두 검증 스택 중 어느 DB/버전을 운영으로 사용할지 해당 세션 기록을 확인한다. 현재 8090을 최종 대상이라고 단정하지 않는다.
- [ ] RWR 담당 세션에서 `NGINX_PORT=127.0.0.1:<선정한 포트>` 적용 후 바인딩과 주요 API를 확인한다. 이 표기는 선택 규칙이며 미선정 상태로 실행할 명령이 아니다.
- [ ] SmartDrain은 동일 운영 모델을 비공개 전달하고 크기/SHA256 일치, ARM64 inference, migration, seed, REST/WebSocket/callback E2E Gate를 해당 Plan에서 순서대로 통과시킨다.
- [ ] 프로젝트별 DB volume과 이름을 기록한다. 새 이름을 쓰면 새 volume이 생길 수 있으므로 임의 project rename을 금지한다.

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

**입력:** Task 1 인계·복원 준비, 대상 프로젝트 Task 2 PASS, 호스트 Task 3 지원 확인. **산출:** Mac 임시 HTTPS 경로와 검증된 production 전환.

- [ ] 기존 Mac Tunnel이 다른 세션에서 생성됐는지 먼저 확인하고 중복 생성하지 않는다. 없다면 서버 단위 이름 `mac-mini-prod`로 단일 remotely-managed Tunnel 생성 절차를 진행한다.
- [ ] 11 결제 규칙과 UI 권한 확인 규칙을 적용한다. 기존 Tunnel token은 재사용/회전하지 않는다.
- [ ] Mac host connector를 지원되는 서비스 방식으로 연결한다. token을 채팅·문서·shell history에 남기지 않는다.
- [ ] 대상 프로젝트의 `mac-*` hostname 충돌 여부를 확인하고 11의 localhost 매핑을 등록한다. 내부 DB/관측성 route는 만들지 않는다.
- [ ] Backend 관리 경로 차단을 공개와 함께 준비한다. 외부 `/actuator/prometheus`는 거부되며 내부 target은 UP인지 확인한다. 유료 기능이 필수인 것으로 나타나면 중단하고 무료 대안을 설명한다.
- [ ] Health Center URL/CORS 및 Frontend build는 09 Step 2-2를 사용한다. 다른 프로젝트는 각 Plan의 domain/WebSocket/외부 API 허용 origin 조건을 확인한다.
- [ ] 외부 네트워크에서 로그인/조회·브라우저 API target과 Mac 로그를 대조한다. 임시 URL 확인을 production 성공으로 간주하지 않는다.
- [ ] 생산 hostname 전환은 09 Step 2-3의 쓰기 통제·rollback 기준을 프로젝트별 적용한다. Health Center 두 hostname은 함께 검증한다.

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

다음 실행 위치는 Task 1의 Cloudflare 작업 인계와 Task 3의 OrbStack 자동 기동 지원 확인이다. 공통 인프라 변경을 시작하기 전에 본 Plan의 범위와 다른 세션 담당을 확인한다.
