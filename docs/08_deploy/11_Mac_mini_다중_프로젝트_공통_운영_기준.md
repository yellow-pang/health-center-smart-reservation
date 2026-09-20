# Mac mini 다중 프로젝트 공통 운영 기준

작성일: 2026-09-20

## 1. 문서 상태와 적용 범위

이 문서는 Windows 노트북의 Linux VM에서 운영하던 개인 프로젝트 서버를 Mac mini 16GB + OrbStack으로 점진 이전하기 위한 **설계 제안과 운영 원칙 기록**이다. 설정 적용 완료나 전체 구현 승인을 뜻하지 않는다.

- 사용자 확정 방향: Docker Compose 프로젝트 3개, Mac 호스트 cloudflared, Mac 전용 remotely-managed Tunnel 1개, localhost HTTP publish, 기존 VM/Tunnel과 임시 도메인 병행.
- 제안 사항: 공통 관측성 스택, CI 이미지 빌드/Registry 배포, 보관기간·백업주기·고정 운영 디렉터리 표준. 실제 적용 범위는 후속 실행 계획에서 정한다.
- 미검증 사항: RWR/SmartDrain 실제 저장소와 배포 설정, SmartDrain AI 최대 자원 사용량, Mac 로그인 전 OrbStack 기동, 무인 재부팅 복구, 전체 외부 전환.
- 이번 기록 작업에서는 Cloudflare·Docker·macOS·CI 설정을 변경하지 않는다. Secret과 개인정보를 기록하지 않는다.

관련 문서:

- [08 사전 분석](08_Mac_mini_OrbStack_이전_사전_분석.md): Health Center 기존 구조의 정적 근거.
- [09 실행 Plan](09_Mac_mini_OrbStack_이전_실행_Plan.md): Health Center 기존 구조 재현·이전 절차.
- [10 진행 기록](10_Mac_mini_OrbStack_이전_진행기록.md): 실제 실행 증거와 재개 위치.

본 문서는 08/09를 소급해서 다시 쓰거나 기존 Phase 3/4를 자동 실행하는 근거가 아니다. 다중 프로젝트 운영 설계가 추가되었으므로 구현 재개 전에 09와의 차이를 정리한다. 특히 프로젝트 전용 Tunnel 이름, 공통 관측성 분리, CI 변경은 별도 적용 판단이 필요하다.

## 2. 기존 환경과 근거 구분

사용자 제공 정보:

| 프로젝트 | 구성 | 기존 실제 운영 경로 |
|---|---|---|
| Health Center | frontend, backend, PostgreSQL/pgvector, Grafana, Prometheus, Loki, Promtail | Jenkins workspace |
| RWR Mini Project | nginx, server, PostgreSQL | self-hosted runner의 `_work` |
| SmartDrain | nginx, frontend, backend, AI service, PostgreSQL | `/home/yp/apps/smart-drain` |

기존 VM 할당은 RAM 약 8GB/디스크 100GB, 컨테이너 전체 RAM 사용량은 약 2.2GB였다. 서버에서 build하며 이미지 약 34GB와 build cache 약 8.7GB가 누적됐다. 이 수치는 사용자 제공 당시 관측값이며 최대 부하 보장은 아니다.

이전 방식은 clean clone → 비공개 `.env` 이전 → 새 Docker 환경 → 새 DB → 서비스 재구축이다. 기존 데이터 없이 시작해도 된다는 결정과 향후 생성 데이터의 보존 정책은 별개다.

앞선 세션 실확인: Health Center 기본 서비스와 기존 observability 스택 ARM64 기동·API·로그/지표 검증 통과. Cloudflare 기존 Tunnel 하나에 route 4개와 Linux amd64 connector 1개가 있었다. 당시 공개 `/actuator/prometheus`는 무인증 HTTP 200이었다. 이들은 과거 확인 결과이며 전환 직전에 다시 확인한다.

## 3. 추천 최종 구조와 책임

```text
GitHub 프로젝트별 소스 / Dockerfile / Compose
  └─ CI 테스트·Linux ARM64 이미지 build → Registry의 버전별 이미지
       └─ Mac에서 선택한 버전 pull / Compose 적용 / 검증

Mac mini
├─ 고정 운영 디렉터리: health-center / rwr / smartdrain / observability
├─ OrbStack
│  ├─ Health Center Compose → 전용 네트워크 / DB volume
│  ├─ RWR Compose           → 전용 네트워크 / DB volume
│  ├─ SmartDrain Compose    → 전용 네트워크 / DB volume
│  └─ 공통 관측성 Compose   → Grafana / Prometheus / Loki / Alloy
└─ 호스트 cloudflared → Mac 전용 Tunnel 1개 → hostname별 localhost 포트
```

| 구성 | 책임 |
|---|---|
| GitHub / CI | 테스트, 이미지 생성, 배포 버전 식별 |
| 배포 절차 | 버전 선택, pull, Compose 적용, 검증과 rollback |
| OrbStack / Compose | 컨테이너, 네트워크, volume, 프로세스 재시작 |
| Cloudflare / cloudflared | 외부 HTTPS ingress와 지정 origin 전달 |
| 프로젝트 DB volume | 데이터 영속성. 백업 자체는 아님 |
| 공통 관측성 | 지표·로그·알림. 앱 실행의 필수 의존성으로 만들지 않음 |

장기적으로 운영 Mac에서는 완성된 이미지를 실행한다. CI build와 배포는 분리하고 초기에는 수동 배포로도 운영 가능하다. 이미지 tag는 commit SHA/릴리스로 식별하고 배포 digest와 직전 검증 버전을 기록한다. `latest` 자동 추적만으로 운영하지 않는다. 이미지 rollback이 DB schema/data 변경까지 되돌리지는 않는다.

고정 운영 경로 예시는 `/Users/<운영계정>/services/<프로젝트>`다. 실제 계정·경로는 구현 시 정한다. 개발 checkout, Jenkins workspace, runner `_work`, iCloud 동기화 경로에 운영 실행을 의존시키지 않는다. 운영 소스는 이미지 안에 포함하고 개발용 source bind mount를 제거하는 방향을 검토한다.

Kubernetes, Swarm, 별도 CD 서버, 프로젝트별 Tunnel, 불필요한 공통 reverse proxy, 과도한 secret manager, 근거 없는 HA는 도입하지 않는다. 단일 Mac/네트워크/Tunnel 장애는 여러 프로젝트에 함께 영향을 준다는 운영 한계는 유지된다.

## 4. 프로젝트 추가 규칙

프로젝트 대장에 아래 항목을 등록한다.

- 고유한 Compose project name, 운영 디렉터리, 저장소, 이미지 버전/digest.
- 전용 네트워크, DB 계정과 named volume, 필요 HTTP endpoint.
- host port, 운영/검증 hostname, health 확인 방법.
- 백업 대상·주기·복원 방법, 로그 label, 담당 배포 절차.

DB는 프로젝트별로 유지한다. 내부 PostgreSQL 포트는 모두 5432여도 네트워크가 분리되어 충돌하지 않는다. staging/임시 스택을 병행한다면 project name·host port·volume을 구분한다. 고정 container_name의 충돌 여부도 확인한다.

애플리케이션은 다른 프로젝트 DB에 직접 의존하지 않는다. 자원 제한은 실측 peak와 여유분을 근거로 결정한다. 특히 SmartDrain AI는 추론 지연, 동시 요청, 메모리 peak, ARM64 dependency를 따로 검증한다.

## 5. 포트와 네트워크 기준

| 대상 | Mac publish 기준 | Cloudflare route |
|---|---|---|
| Health Center frontend | `127.0.0.1:3000` | 있음 |
| Health Center backend | `127.0.0.1:8080` | 있음 |
| RWR nginx | `127.0.0.1:8090` | 있음 |
| SmartDrain nginx | `127.0.0.1:8099` | 있음 |
| 공통 Grafana | `127.0.0.1:3001` | 기본 없음 |
| 프로젝트 PostgreSQL | 기본 publish 없음 | 없음 |
| Prometheus / Loki | 기본 관측성 내부 네트워크 | 없음 |
| SmartDrain 내부 backend / AI | 프로젝트 내부 네트워크 | 없음 |

현재 포트를 대장에 예약하고 새 프로젝트는 빈 포트를 배정한다. 관리·진단용 publish가 필요하면 localhost에 한정하고 용도를 기록한다. `0.0.0.0`/IPv6/LAN 노출 여부를 실제로 검증한다. OrbStack의 LAN 포트 노출 설정도 함께 점검한다.

각 프로젝트의 nginx는 해당 프로젝트 내부 routing 책임을 유지한다. cloudflared 때문에 Compose 네트워크들을 연결하지 않는다.

## 6. Cloudflare hostname / route / 이전 기준

Mac Tunnel 이름은 서버 단위인 `mac-mini-prod`를 제안한다. 이름은 미확정이며 이 문서 작성 시 생성하지 않는다. cloudflared는 Mac host의 공통 서비스로 한 번만 운영한다.

| 프로젝트 endpoint | 임시 검증 hostname 예시 | 운영 hostname | Mac target |
|---|---|---|---|
| Health Center frontend | `mac-demo.healthq.store` | `demo.healthq.store` | `http://127.0.0.1:3000` |
| Health Center backend | `mac-api.healthq.store` | `api.healthq.store` | `http://127.0.0.1:8080` |
| RWR | `mac-rwr.healthq.store` | `rwr.healthq.store` | `http://127.0.0.1:8090` |
| SmartDrain | `mac-smartdrain.healthq.store` | `smartdrain.healthq.store` | `http://127.0.0.1:8099` |

임시 hostname의 사용 가능 여부는 실제 DNS/route에서 확인한다. 인증서 범위가 달라지는 다단계 이름 대신 zone 바로 아래 한 단계 subdomain을 사용한다.

기존 Tunnel은 운영 hostname을, Mac Tunnel은 임시 hostname을 담당하며 병행한다. 프로젝트마다 검증 후 운영 hostname을 옮긴다. Health Center frontend/API는 한 쌍으로 다룬다. DNS/cache 때문에 원자적 전환을 가정하지 않고 전환 중 쓰기를 통제한다. 다른 프로젝트 route는 건드리지 않는다.

서로 다른 DB 상태의 VM/Mac을 기존 Tunnel replica로 함께 연결하지 않는다. rollback은 원래 hostname/route/DNS/Access 복원이며 Mac 데이터의 VM 복원을 뜻하지 않는다. 전환 전 원래 설정과 Mac 새 데이터 보존 방침을 확보한다.

공개 endpoint의 앱 인증·권한 검사는 별도 책임이다. `/actuator/prometheus` 등 내부 관리 경로는 공개 전 정확한 hostname/path 정책으로 차단하고 내부 scrape 유지와 외부 거부를 함께 검증한다. Access 목록만 보고 wildcard·추가 destination까지 보호 여부를 단정하지 않는다. 무료 범위에서 적용 가능한 실제 정책은 구현 전에 확인한다.

## 7. 결제 관련 강제 중단 규칙

사용자 명시 조건이며 모든 Cloudflare Dashboard 작업과 다음 세션에 적용한다.

다음 화면/요구가 나타나면 다음 조작을 즉시 멈춘다: Upgrade, Purchase, Subscribe, Checkout, Add payment method, 유료 가격·Add-on, Advanced Certificate, Load Balancer, 유료 Access 좌석, 한도 초과 과금, trial 이후 자동 과금.

중단 시 사용자에게 다음만 설명하고 **현재 화면을 직접 확인하도록 안내**한다.

1. 현재 화면 위치와 설정하려던 기능.
2. 어떤 항목 때문에 결제 안내가 나타났는지.
3. 실제 결제 필수인지 또는 선택적 광고인지에 대한 확인 결과. 미확인이면 미확인으로 표시.
4. 무료 대안과 그 차이.

명시적 승인 전 결제수단 등록, 플랜 변경, 유료 기능 활성화를 하지 않는다. 화면이 바뀌었거나 가격을 확인하지 못했다면 추정으로 진행하지 않는다. 단순 Tunnel/Published Application route 생성을 유료 작업으로 간주하지 않고 공식 문서와 실제 화면으로 판단한다.

이 규칙은 에이전트/운영자 절차다. 자동 화면 감지·과금 차단 프로그램이 설치된 것은 아니다. CI/Registry 등 다른 서비스의 무료 한도·유료 runner도 별도로 확인한다.

## 8. 환경변수와 Secret

- 프로젝트별 `.env`를 Git에서 제외하고 파일/디렉터리 권한을 운영 계정에 한정한다.
- `.env.example`에는 key와 비민감 예시만 둔다. 컨테이너마다 필요한 변수만 전달한다.
- DB 비밀번호·JWT 키는 프로젝트별로 분리한다. Frontend/Grafana에 불필요한 DB/OAuth Secret을 전달하지 않는다.
- `NEXT_PUBLIC_*`는 공개되는 값이다. build 시 고정되는 URL과 이미지 환경을 함께 기록한다.
- Secret을 Docker build argument, 이미지 layer, CI 로그, Notion, 진행 기록에 넣지 않는다.
- Tunnel token은 호스트 공통 인프라 자격증명으로 관리한다. 구현 시 지원되는 서비스 등록 방식과 파일 권한·로그 노출을 확인한다.
- Notion에는 보관 위치/소유자/교체·복원 방법만 적고 실제 값은 암호화된 별도 보관소에 둔다.

`.env`/Compose 변경은 restart만으로 적용됐다고 판단하지 않고 재생성 여부를 확인한다. 실제 파일을 shell에 무조건 source하거나 전체 config/env를 출력하지 않는다.

## 9. 공통 로그·관측성·공간 관리 제안

장기 목표는 Health Center 관측성을 독립 Compose로 옮겨 세 프로젝트를 관측하는 것이다. 기존 검증 스택은 공통 스택과 dashboard/datasource/로그 동등성을 확인한 후 정리한다. 동시에 두 수집 경로를 영구 운영하지 않는다.

신규 기준: Grafana, Prometheus, Loki, Alloy. Promtail은 2026-03-02 EOL이므로 장기 표준에는 Alloy를 제안한다. 관련 이미지도 지원 중인 버전을 선택하되 변경 단위별로 검증한다. 이는 기존 Mac 재현 성공을 부정하거나 즉시 업그레이드를 실행한다는 뜻이 아니다.

- 로그: stdout/stderr, INFO 기본, 임시 DEBUG, 민감정보 마스킹.
- 공통 label: project/service/environment. 사용자 ID·token 등 고 cardinality/민감값을 label로 넣지 않는다.
- Alloy는 허용한 Compose project만 수집한다. Docker socket mount의 `:ro`는 Docker API 읽기 전용 권한을 보장하지 않는다.
- 내부 scrape가 필요할 때만 프로젝트별 수집 네트워크에 수집기와 대상 endpoint를 연결한다. 프로젝트 DB나 다른 앱끼리 공통망에 합치지 않는다.
- host 네트워크/관리 endpoint 인증 등 세부 연결은 기존 앱 설정 확인 후 정한다. 관측성 때문에 공개 API의 관리 경로를 무인증으로 열지 않는다.

초기 보관값 제안:

| 항목 | 기준 |
|---|---|
| Docker 로그 | 컨테이너당 10MB × 3개 등 명시적 회전 |
| Loki | 7일; compactor/삭제 설정까지 검증 |
| Prometheus | 15일 + disk budget에 맞는 용량 상한 |
| 이미지 | 현재 및 직전 검증 릴리스 보존 |
| build cache | 서버 build 축소, 용량 확인 후 대상 한정 정리 |

Docker 로그와 Loki 저장량은 별개로 제한한다. 디스크 여유, image/cache/volume 증가, 반복 재시작/OOM, 백업 실패를 관찰한다. `down -v`, volume prune, system prune을 자동 정리 수단으로 사용하지 않는다. Mac 내부 모니터링으로 Mac 전체 정전을 감지할 수 없으므로 외부 장애 알림은 별도 수단·비용을 확인한 후 선택한다.

## 10. 재부팅·절전·자동 복구 기준

완료 조건: Mac 부팅 → 디스크 접근 가능 → OrbStack Engine → 컨테이너/DB/API → Tunnel → 외부 URL까지 사람 개입 없이 복구. 로그인 후 복구와 무인 복구를 구분한다.

- cloudflared는 지원되는 macOS 시스템 서비스 방식 사용. remotely-managed token의 실제 등록 방식은 구현 시 검증.
- 장기 실행 container는 `restart: unless-stopped` 기본. 수동 stop한 서비스의 미복구는 의도된 동작이다.
- healthcheck의 unhealthy만으로 Docker가 재시작하지 않는다. DB 지연·재접속 시 앱 회복도 확인한다.
- Compose 최초 기동의 depends_on 순서가 Engine 재시작에도 그대로 적용된다고 가정하지 않는다.
- 화면 꺼짐은 허용하고 서버 sleep은 방지한다. 유선 네트워크 우선, 업데이트는 복구 확인 가능한 시간에 수행한다.
- 일반 reboot, 정전 이후 부팅, FileVault unlock, 로그인 전 OrbStack 지원을 구분해서 검증한다.
- FileVault가 켜져 있으면 자동 로그인이 제한된다. 임의로 보안을 낮추거나 root wrapper/LaunchDaemon/custom boot script를 만들지 않는다.
- 지원되는 구성에서 무인 복구가 불가능하면 플랫폼/보안/가용성 선택을 사용자에게 보고한다. 목표를 수동 로그인 복구로 조용히 낮추지 않는다.

컨테이너별 restart, Mac reboot, Tunnel restart, 데이터 보존과 외부 URL 복구를 검증한다. 24시간 관찰은 시작 시간·자원 기준값을 기록하고 다음 세션에 종료 검증한다. 시간이 지나기 전 완료로 표시하지 않는다.

## 11. 백업과 복원 기준

| 대상 | 기준 |
|---|---|
| PostgreSQL | 보존할 데이터가 있으면 논리 백업과 복원 검증 |
| 사용자 upload/첨부 | 원본 백업 |
| 자체 학습 모델/결과 | 재생성 가능성과 비용에 따라 백업 |
| env/인증서/자격증명 | 암호화된 별도 보관 |
| Compose/설정/dashboard | Git 관리 |
| Grafana UI에서만 만든 설정 | Git export 또는 데이터 백업 |
| Registry 이미지 | Registry 보존정책 확인; Mac 캐시 복사 불필요 |
| build/package cache | 재생성, 백업 불필요 |
| 다운로드 가능한 모델 | 버전·위치·검증값 기록 |
| Loki/Prometheus 이력 | 기본 재생성 허용, 요구가 있으면 백업 |

초기 제안: DB 매일 백업, 일간 7개/주간 4개, 월 1회 별도 DB로 복원 시험. 최대 24시간 데이터 손실을 허용하는 정책이므로 프로젝트별 합의가 필요하다. DB dump 외에도 복원에 필요한 role/extension/버전을 기록한다. 실행 중 DB volume의 단순 파일 복사를 백업 표준으로 삼지 않는다.

최소 한 사본은 Mac 밖에 둔다. 같은 SSD의 다른 폴더는 디스크 고장 대비가 아니다. 백업 저장소 구매·구독은 별도 판단한다. 비공개 백업 내용과 개인정보는 공개 문서에 넣지 않는다.

## 12. 기존 Windows VM 종료·제거

세 프로젝트의 외부 경로, 데이터 보존, 재부팅 복구와 관찰을 통과하고 사용자가 이전 완료를 승인해야 종료를 판단한다.

1. Jenkins/runner 배포 job의 VM 재배포를 중단한다.
2. 모든 운영 route 전환 확인 후 기존 앱과 cloudflared를 중지한다.
3. 다른 프로젝트 영향 확인 후 기존 배포 자격증명을 회수한다.
4. 합의한 rollback 보관기간 후 기존 Tunnel/DNS 잔여 항목을 정리한다.
5. 필요한 설정 보관 후 Jenkins home, runner `_work`, 기존 배포 디렉터리를 정리한다.
6. 백업 복원 확인과 명시적 삭제 승인 후 DB volume, 이미지/cache, VM 디스크를 정리한다.

중지와 삭제를 분리한다. Health Center 한 프로젝트 완료는 공유 Tunnel과 VM 종료 조건이 아니다. 이 문서는 삭제 실행을 승인하지 않는다.

## 13. Notion 기록 구조와 다음 세션 재개

Notion 권장 페이지: 서버 개요 / 프로젝트 대장 / 포트·hostname / 배포·rollback / Secret 보관 위치 / 백업·복원 / 재부팅 검증 / 변경 기록. 설정 원본은 Git에 두고 Notion에는 설명·결정·문서 링크를 기록한다. 이번 작업에서 Notion에 직접 작성하지 않았다.

다음 세션에서는 08 → 09 → 10 → 본 문서를 읽고 아래부터 확인한다.

1. 다른 프로젝트 세션에서 변경한 Tunnel·DNS·호스트 서비스 현황을 읽기 전용 재확인.
2. 본 문서의 사용자 확정 방향과 설계 제안 구분, 기존 Plan과 실행 범위 차이 정리.
3. RWR/SmartDrain 실제 Compose·env key·데이터 경로·ARM64 지원 확인.
4. Mac 무인 복구 지원 조건과 비용 없는 공개 관리 경로 보호 방법 확인.
5. 공통 기준의 적용 순서와 프로젝트별 검증/rollback 계획을 정한 뒤 구현 재개.

신규 Tunnel 생성, CI 전환, 공통 관측성 변경을 문서 작성 요청만으로 자동 실행하지 않는다.

## 14. 공식 참고 근거

2026-09-20 대화에서 확인한 자료. 제품 지원·요금·설치 절차는 실제 적용 전에 재확인한다.

- [Cloudflare 공개 애플리케이션과 paid Access 필요 여부](https://developers.cloudflare.com/cloudflare-one/networks/connectors/cloudflare-tunnel/routing-to-tunnel/)
- [Cloudflare Tunnel 생성 및 hostname 주의사항](https://developers.cloudflare.com/cloudflare-one/networks/connectors/cloudflare-tunnel/get-started/create-remote-tunnel/)
- [Cloudflare macOS 서비스 문서: local-managed 기준이므로 관리 방식 혼용 금지](https://developers.cloudflare.com/tunnel/features/locally-managed-tunnels/as-a-service/macos/)
- [OrbStack 네트워크와 LAN 노출](https://docs.orbstack.dev/docker/network)
- [Docker 운영용 Compose](https://docs.docker.com/compose/how-tos/production/)
- [Compose restart 및 dependency 의미](https://docs.docker.com/reference/compose-file/services/)
- [Docker 로그 관리](https://docs.docker.com/engine/logging/configure/)
- [Promtail EOL 및 Alloy 이전](https://grafana.com/docs/grafana-cloud/observe-and-act/send-data/alloy/set-up/migrate/from-promtail/)
- [Apple 자동 로그인과 FileVault 조건](https://support.apple.com/en-nz/102316)
- [GitHub Actions 무료 범위와 과금](https://docs.github.com/en/billing/concepts/product-billing/github-actions)
