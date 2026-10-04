# Cloudflare Mac Tunnel 설정 기록

## 목적과 범위

2026-09-20 Health Center를 기존 VM 운영 경로와 분리해 Mac mini에서 검증하기 위해 Mac 전용 remotely-managed Tunnel과 임시 hostname을 구성했다. 이 최초 검증 단계에서는 기존 VM Tunnel, 운영 hostname, 운영 DB와 Jenkins를 변경하지 않았고, 검증 후 프로젝트별 사용자 승인에 따라 운영 route만 순차 전환했다.

## 비용 및 보안 Gate

- 계정 표시 플랜: Zero Trust Free.
- Cloudflare 공식 문서와 Dashboard에서 Tunnel 생성 및 일반 published application route에 Upgrade·Purchase·Checkout 요구가 없음을 확인했다.
- `mac-demo.healthq.store`, `mac-api.healthq.store`는 한 단계 서브도메인이므로 화면에 표시된 다단계 서브도메인용 Advanced Certificate Manager 대상이 아니다.
- Load Balancer, Advanced Certificate, Argo, 유료 Access 좌석과 기타 유료 Add-on은 선택하지 않았다.
- Tunnel token, 전체 설치 명령, connector 식별자, 원본 IP는 문서와 캡처에 기록하지 않았다.

## 실행 순서

1. Cloudflare Dashboard에서 `mac-mini-prod` Tunnel을 생성했다.
2. macOS ARM64 connector 설치 명령은 Dashboard에서 직접 복사하고 사용자가 관리자 암호를 입력해 실행했다.
3. token이 일반 shell history에 남지 않도록 임시 zsh history를 사용했다.

```zsh
fc -p /dev/null
# Dashboard에서 복사한 Install as service 명령 실행 — 문서에 token을 기록하지 않음
fc -P
```

4. 설치 결과는 system LaunchDaemon 성공과 boot 시 실행 안내였다. 로그 경로는 `/Library/Logs/com.cloudflare.cloudflared.out.log`, `/Library/Logs/com.cloudflare.cloudflared.err.log`이다.
5. Dashboard에서 connector Healthy와 macOS `darwin_arm64`, cloudflared `2026.9.1`을 확인했다.
6. 다음 published application route를 추가했다.

| 임시 hostname | Mac origin | 목적 |
|---|---|---|
| `mac-demo.healthq.store` | `http://localhost:3000` | Health Center Frontend 검증 |
| `mac-api.healthq.store` | `http://localhost:8080` | Health Center Backend 검증 |

7. DB와 관측성 포트에는 public route를 만들지 않았다.
8. Backend route 생성 직후 `/actuator/prometheus` 외부 HTTP 200을 확인해 보안 blocker로 분류했다.
9. Zero Trust Access self-hosted application `Health Center metrics block`을 `mac-api.healthq.store/actuator/prometheus`에 생성했다. Allow policy가 없는 Access 기본 거부를 사용하며 다른 API 경로에는 적용하지 않았다.

## 검증 결과

| 검증 | 결과 | 검출하려는 실패 |
|---|---|---|
| Tunnel 목록 | `mac-mini-prod` Healthy, replica 1 | connector 미연결 |
| LaunchDaemon | system domain `running`, boot daemon 경로 확인 | 로그인 세션에만 의존하는 실행 |
| Frontend 임시 URL | HTTP 307 | DNS/Tunnel/origin 연결 실패 |
| Backend health | HTTP 200 | Backend route 또는 origin 실패 |
| 외부 metrics 보호 전 | HTTP 200 | 관리 endpoint 공개 재현 |
| 외부 metrics 보호 후 | HTTP 302 Access 인증 경로 | 경로 단위 보호 미적용 |
| 내부 Prometheus | Backend target `UP`, Prometheus target `UP` | 외부 차단이 내부 scrape까지 차단 |
| cloudflared 오류 로그 | 최근 error/fatal/failed 없음 | connector 반복 실패 |

HTTP 302는 metrics가 origin으로 직접 전달되지 않고 Cloudflare Access에서 가로채졌다는 증거다. 완료 판단에서는 redirect URL이나 인증용 metadata를 기록하지 않는다.

## 브라우저 캡처 기준

실행 세션에서 다음 비민감 화면을 캡처해 확인했다.

- Tunnel 목록: 기존 Tunnel과 분리된 `mac-mini-prod` Healthy 상태.
- `mac-demo.healthq.store` route 생성 성공 화면.
- `mac-api.healthq.store` route 생성 성공 화면.
- Access applications 목록의 `Health Center metrics block`과 보호 destination.

Tunnel token이 표시되는 설치 화면과 connector 원본 IP·식별자 화면은 캡처 근거에서 제외했다. 캡처는 계정 이메일 등 UI 개인정보가 포함될 수 있으므로 저장소 파일로 자동 저장하지 않았으며, 공개 문서로 옮길 때 계정 영역을 잘라내거나 가린다.

## 현재 미완료 항목

- Health Center 임시 URL/CORS 반영, Frontend 재빌드, 로그인·업무 유형·예약 슬롯·실제 Backend target 검증은 완료했다.
- RWR 운영 hostname은 Mac Tunnel로 전환하고 실제 Mac origin 도착까지 검증했다.
- SmartDrain과 Health Center 운영 hostname 전환을 완료했다. Health Center production metrics Access 보호와 내부 수집 회귀 검증도 완료했다.
- Mac 재부팅 후 OrbStack, Compose, cloudflared, public URL의 무인 복구 검증은 Phase 2-S에서 수행한다.

## 세 프로젝트 확장 인계

2026-09-20 RWR과 SmartDrain의 최신 저장소 상태를 확인했고, 두 프로젝트의 Cloudflare 작업을 이 Health Center 세션에서 단일 관리하기로 인계받았다. 기존 `mac-mini-prod` Tunnel과 system LaunchDaemon을 재사용하며 추가 Tunnel이나 connector를 만들지 않는다.

| 프로젝트 | 임시 hostname | Mac origin | 현재 상태 | 외부 성공 조건 |
|---|---|---|---|---|
| Health Center Frontend | `mac-demo.healthq.store` | `http://localhost:3000` | 생성 완료, 공개 Gate PASS | 로그인 후 업무 유형·예약 슬롯 조회, 브라우저 `mac-api` 호출 PASS |
| Health Center Backend | `mac-api.healthq.store` | `http://localhost:8080` | 생성 완료, 공개 Gate PASS | health·업무 API·CORS PASS, metrics는 Access 보호 |
| RWR | `mac-rwr.healthq.store` | `http://localhost:8090` | 생성 완료, 공개 Gate PASS | UI·health·Kakao REST/ORS 코스·즐겨찾기·JavaScript SDK 임시 domain PASS |
| SmartDrain | `mac-smartdrain.healthq.store` | `http://localhost:8099` | 생성 완료, 공개 E2E PASS | UI·REST·image·WSS·실제 분석 callback/DB/UI·Kakao 지도 PASS |

- RWR은 nginx가 UI와 `/api`를 함께 제공하므로 hostname 하나만 사용한다. server와 PostgreSQL route는 만들지 않는다.
- SmartDrain은 nginx가 UI, REST, image와 WebSocket upgrade를 중계하므로 hostname 하나만 사용한다. Backend, AI service와 PostgreSQL route는 만들지 않는다.
- RWR/SmartDrain route 생성 직전에 localhost origin과 대표 health/API를 재확인한다. 실패하면 Cloudflare 설정을 우회하지 않고 해당 프로젝트 localhost와 외부 결과를 비교한다.
- 프로젝트별 route 생성과 검증 결과는 10 진행 기록에 반영한다. 임시 경로는 모두 통과했으며 남은 production hostname은 프로젝트별 사용자 승인 전까지 변경하지 않는다.
- Upgrade, Purchase, Subscribe, Checkout, Add payment method, 유료 가격, Advanced Certificate, Load Balancer 또는 유료 Access 좌석 안내가 나타나면 저장 없이 중단하고 사용자에게 화면과 무료 대안을 설명한다.

## 다중 프로젝트 route 실행 결과

- Dashboard는 RWR과 SmartDrain route 저장 후 각각 설정 저장과 DNS record 생성 성공을 표시했다.
- Tunnel의 published application route는 Health Frontend/Backend, RWR, SmartDrain 총 4개이며 catch-all은 기존 `http_status:404`를 유지한다.
- RWR은 외부 HTTPS에서 UI와 health 200, seed 코스, Kakao REST/ORS 주소 코스, 테스트 즐겨찾기 CRUD가 통과했다.
- SmartDrain은 외부 HTTPS에서 dashboard REST, sample image와 WSS 연결이 동작했다. 실제 분석 요청 뒤 두 callback, DB completed, history와 브라우저 실시간 갱신이 통과했다.
- SmartDrain 빌드의 JavaScript 키는 별도 SmartDrain 앱이 아니라 RWR 앱에 속한 키였다. 키 원문을 문서나 로그에 기록하지 않고 해시 지문으로 대조했다.
- RWR 앱의 기존 운영·localhost domain을 보존하고 `mac-rwr.healthq.store`, `mac-smartdrain.healthq.store`를 추가했다. SmartDrain 공개 페이지에서 실제 Kakao 지도, 마커, Kakao Maps 링크를 확인했고, RWR은 임시 hostname Referer로 SDK HTTP 200과 SDK signature를 확인했다.
- 초기에 별도 SmartDrain 앱에 추가한 임시 domain은 실제 사용 키와 무관했다. 사용자 승인 후 그 항목만 제거했고 기존 localhost domain은 유지했다.
- Health Center는 임시 공개 URL/CORS를 `.env`에 반영하고 Frontend를 재빌드했다. 외부 API와 실제 브라우저 로그인, 업무 유형·예약 슬롯, 실제 bundle target이 `mac-api`임을 확인했다.
- Kakao JavaScript domain 검증 후 RWR과 SmartDrain 운영 hostname을 Mac Tunnel로 전환했다. 이어서 사용자 승인과 복원 정보 재확인 후 Health Center Frontend/Backend 운영 hostname도 같은 전환 단위로 Mac Tunnel에 옮겼다.
- Kakao Developers에서 결제·유료 API·Upgrade 화면은 열지 않았고 유료 기능을 활성화하지 않았다.

## Production 전환 직전 복원 기준

2026-09-20 읽기 전용 재확인 결과다. 기존 VM Tunnel은 Healthy였고 다음 매핑을 그대로 유지했다.

| 운영 hostname | 기존 VM origin | 전환 직전 응답 |
|---|---|---|
| `demo.healthq.store` | `http://localhost:3000` | `/login` redirect 후 HTTP 200 |
| `api.healthq.store` | `http://localhost:8080` | `/actuator/health` HTTP 200 |
| `rwr.healthq.store` | `http://localhost:8090` | `/` HTTP 200 |
| `smartdrain.healthq.store` | `http://localhost:8099` | `/` HTTP 200 |

- 복원 시에는 전환한 hostname만 위 기존 VM route로 되돌린다.
- Tunnel·connector 식별자, token, origin IP는 문서에 기록하지 않는다.
- 각 프로젝트의 production 전환을 사용자가 명시적으로 승인하기 전에는 해당 route나 기존 VM connector를 변경하지 않는다. RWR은 승인과 검증을 마쳤다.

## RWR production 전환 결과

- 사용자 승인 후 `rwr.healthq.store` 하나만 첫 production 전환 단위로 선택했다.
- 기존 DNS가 있는 상태에서 Mac Tunnel에 중복 route를 먼저 생성하려는 시도는 Cloudflare가 충돌로 거부했고 저장되지 않았다.
- 기존 VM Tunnel에서 RWR route만 제거한 직후 Mac Tunnel에 `rwr.healthq.store -> http://localhost:8090`을 생성했다. Dashboard의 route 저장과 DNS record 생성 성공을 확인했다.
- 운영 UI, health, 저장 코스, 랜덤 코스가 모두 HTTP 200이었고 랜덤 코스 계약이 PASS했다. 검증 요청이 Mac `rwr-production-nginx` 로그에 기록돼 실제 Mac origin 도착을 확인했다.
- RWR 운영 Referer의 Kakao Maps SDK는 HTTP 200과 SDK signature를 반환했고 Safari 운영 홈 화면이 정상 로드됐다.
- rollback은 Mac Tunnel의 RWR route를 제거한 뒤 기존 VM Tunnel의 동일 hostname/origin 매핑을 재생성하고 UI·health를 확인하는 순서다.
- 이 RWR 전환 시점에는 결제·유료 기능을 열거나 활성화하지 않았고 Health Center와 SmartDrain production route는 기존 VM Tunnel에 남겨두었다. 이후 SmartDrain은 아래 절차로 전환했다.

## SmartDrain production 전환 결과

- 사용자 승인 후 `smartdrain.healthq.store` 하나만 두 번째 production 전환 단위로 선택했다.
- 전환 직전 localhost·임시·기존 운영 hostname의 UI와 dashboard API가 모두 HTTP 200이었다.
- 기존 VM Tunnel에서 SmartDrain route만 제거한 직후 Mac Tunnel에 `smartdrain.healthq.store -> http://localhost:8099`를 생성했다. Dashboard의 route 저장과 DNS record 생성 성공을 확인했다.
- 운영 UI, `/api/dashboard/summary`, `/api/drains`가 모두 HTTP 200이었고 검증 요청이 Mac `smartdrain-mac-nginx` 로그에 기록돼 실제 Mac origin 도착을 확인했다.
- Safari 운영 화면에서 실제 Kakao 지도·마커, 대시보드 데이터와 WebSocket의 `실시간 연결됨` 상태를 확인했다.
- rollback은 Mac Tunnel의 SmartDrain route를 제거한 뒤 기존 VM Tunnel의 동일 hostname과 `http://localhost:8099` 매핑을 재생성하고 UI·대표 API를 확인하는 순서다.
- 결제·유료 기능은 열거나 활성화하지 않았다. 이 시점 이후 Health Center는 아래 절차로 전환했다.

## Health Center production 전환 결과

- 사용자 승인 후 Health Center Frontend/Backend 두 hostname을 하나의 전환 단위로 처리했다.
- `.env`의 공개 Frontend URL과 Backend API target을 운영 hostname으로 반영하고 Frontend image를 재빌드했다. CORS에는 기존 임시·운영 Origin을 유지했으며 앱 코드와 Compose는 수정하지 않았다.
- 기존 VM Tunnel에서 `api.healthq.store -> http://localhost:8080`, `demo.healthq.store -> http://localhost:3000`을 제거하고 Mac Tunnel에 같은 hostname/origin 매핑을 생성했다. 두 route 모두 Dashboard에서 설정 저장과 DNS record 생성 성공을 확인했다.
- 공개 Frontend·Backend health·업무 유형·운영 Origin CORS가 HTTP 200이었다. Safari에서 일반 시민 로그인, 업무 유형 3개, 다음 날 예약 슬롯 14개를 확인했고 같은 요청이 Mac Backend 로그에 HTTP 200으로 기록됐다.
- 운영 `/actuator/prometheus`가 Access 보강 전 HTTP 200으로 공개됨을 확인했다. 사용자 승인 후 기존 `Health Center metrics block`에 `api.healthq.store/actuator/prometheus` destination만 추가했다. 운영·임시 metrics는 HTTP 302, 일반 API는 HTTP 200, 내부 Prometheus target은 `UP`이었다.
- 이 Access application은 별도 2중 인증 체계를 모든 서비스에 강제하기 위한 것이 아니라, 외부 공개가 불필요한 metrics 경로를 origin 도달 전에 막는 최소 경계로 유지한다. 일반 API, Frontend, RWR, SmartDrain에는 확대하지 않았고 새 policy도 만들지 않았다.
- Dashboard에 Upgrade·Purchase·Checkout·유료 좌석 또는 Add-on 요구가 없었고 계정의 Zero Trust Free 범위에서 저장됐다.
- rollback은 Mac Tunnel의 Health Frontend/Backend route를 제거한 뒤 기존 VM Tunnel에 `demo.healthq.store -> http://localhost:3000`, `api.healthq.store -> http://localhost:8080`을 함께 복원하고 로그인·API를 확인하는 순서다. Mac DB는 보존한다.

## Rollback 경계

- 세 프로젝트 운영 hostname은 Mac Tunnel에 전환됐다. 각 production 전환 결과의 프로젝트 단위 복원 절차를 사용하며 Health Center Frontend/Backend는 함께 복원한다.
- rollback 시 새 Access application과 임시 route를 먼저 검토하고, Tunnel/LaunchDaemon 제거는 별도 명시적 판단 후 수행한다.
- Mac의 새 DB 데이터는 기존 VM DB와 자동 병합되지 않는다.
