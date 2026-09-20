# Cloudflare Mac Tunnel 설정 기록

## 목적과 범위

2026-09-20 Health Center를 기존 VM 운영 경로와 분리해 Mac mini에서 검증하기 위해 Mac 전용 remotely-managed Tunnel과 임시 hostname을 구성했다. 기존 VM Tunnel, 운영 hostname, 운영 DB, Jenkins 및 기존 Access application은 변경하지 않았다.

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

- Frontend image는 아직 Phase 0의 localhost Backend API target을 포함한다.
- `.env`의 `NEXT_PUBLIC_APP_URL`, `NEXT_PUBLIC_API_BASE_URL`, `CORS_ALLOWED_ORIGINS` 계약을 확인하고 Frontend만 재빌드해야 한다.
- 외부 브라우저 로그인·업무 유형·예약 슬롯과 실제 Backend target 대조가 남아 있다.
- Mac 재부팅 후 OrbStack, Compose, cloudflared, public URL의 무인 복구 검증은 Phase 2-S에서 수행한다.
- 임시 hostname 검증 전에는 운영 `demo.healthq.store`, `api.healthq.store`를 전환하지 않는다.

## Rollback 경계

- 기존 VM Tunnel과 운영 hostname은 유지 중이므로 임시 경로 실패가 기존 운영 서비스로 전파되지 않는다.
- rollback 시 새 Access application과 임시 route를 먼저 검토하고, Tunnel/LaunchDaemon 제거는 별도 명시적 판단 후 수행한다.
- Mac의 새 DB 데이터는 기존 VM DB와 자동 병합되지 않는다.
