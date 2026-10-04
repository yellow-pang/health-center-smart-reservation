# Mac CI/CD 공개 검증 HTTP 403 오류 수정 기록

작성일: 2026-10-04 (KST). 작업 브랜치: `dev`.

## 실제 실행과 실패 범위

[Deploy production 실행 37199699624](https://github.com/yellow-pang/health-center-smart-reservation/actions/runs/37199699624)의 SHA는 `912e9534d66658fa6490388cd7bef41aedcb8c9f`다. CI, 두 ARM64 이미지 발행, Mac registry 로그인, 이미지 다운로드, 배포 전 DB 백업과 앱 교체가 진행됐고 내부 readiness·배포 버전 검사가 통과했다. 앞선 [150 Keychain 오류](150_Mac_CICD_Docker_Keychain_오류_수정_기록.md)는 이 실행에서 재발하지 않았다.

마지막 `verify_public()`이 공개 URL 검사를 60초 동안 통과하지 못해 workflow는 실패했다. 공개 검사 전에 정상 release가 current로 기록되는 구조이므로 새 앱은 유지되며, 공개 검사 실패만으로 앱을 자동 복구하지 않는다. 이는 [운영 가이드](../08_deploy/14_GitHub_Actions_Mac_CICD.md)의 기존 처리 기준이다.

## 원인 확인

실패 후 같은 Mac 호스트에서 배포 코드와 같은 `urllib` 및 `ProxyHandler({})`를 사용해 요청을 비교했다. 아래 결과는 후속 직접 검사이며 실패한 Actions 실행의 성공 결과로 기록하지 않는다.

| 공개 검사 대상 | Python 기본 User-Agent | `HealthCenter-Deployment/1.0` | `curl/8.7.1` |
|---|---|---|---|
| `https://api.healthq.store/actuator/health` | HTTP 403, Cloudflare 오류 1010 | HTTP 200, UP 확인 | HTTP 200 |
| `https://api.healthq.store/actuator/info` | HTTP 403, Cloudflare 오류 1010 | HTTP 200, 실행 SHA 일치 | HTTP 200 |
| `https://demo.healthq.store/` | HTTP 403, Cloudflare 오류 1010 | HTTP 200 | HTTP 200 |

기본 Python 3.9.6의 `Python-urllib/3.9`도 HTTP 403이었다. runner가 사용하는 `/opt/homebrew/bin/python3` 3.14.7에서도 같은 opener로 health를 확인했으며 기본 `Python-urllib/3.14`는 HTTP 403·오류 1010, `HealthCenter-Deployment/1.0`은 HTTP 200·UP이었다. 같은 URL과 프록시 조건에서 배포용 식별 User-Agent로 정상 응답해 Cloudflare가 Python 기본 User-Agent 요청을 차단한 것이 공개 검사 실패 원인임을 확인했다. 응답 본문, 토큰과 운영 비밀값은 기록하지 않는다.

[Cloudflare 공식 오류 1010 설명](https://developers.cloudflare.com/support/troubleshooting/http-status-codes/cloudflare-1xxx-errors/error-1010/)은 클라이언트의 브라우저 식별 특성에 따른 접근 차단을 나타낸다. Dashboard의 해당 규칙을 직접 확인한 것은 아니므로 특정 Browser Integrity Check 설정 상태까지 확인했다고 기록하지 않는다.

기존 Cloudflare 문서의 Access 보호 대상은 `/actuator/prometheus`이며 일반 API·Frontend로 확대하지 않았다. `/actuator/health`와 `/actuator/info`는 앱의 공개 검사 대상이다. 이번 대응은 Cloudflare 보호 설정을 유지하고 자동 검사 요청의 목적을 식별하는 방식으로 진행한다.

## 수정과 검증 범위

- 배포 HTTP 검사에 `HealthCenter-Deployment/1.0` User-Agent를 적용해 배포 검사용 요청임을 식별한다.
- 공개 검사 실패 시 endpoint별 HTTP 상태나 실패 종류를 남긴다. 응답 본문·인증정보·운영 설정은 로그에 포함하지 않는다.
- health의 UP, info의 배포 SHA, frontend 정상 응답 검증을 유지한다. HTTP 403을 성공으로 처리하거나 공개 검사를 건너뛰지 않는다.

| 검증 | 현재 상태 |
|---|---|
| 실패 Actions 실행의 단계 확인 | CI·발행·로그인·백업·내부 readiness/버전 통과, 공개 검사 실패 |
| 같은 호스트의 User-Agent 대조 | 세 공개 URL에서 기본 UA 403, 식별 UA 200 확인 |
| 수정 코드의 회귀 테스트 | 기존 28건과 새 공개 검사 5건 포함 배포 테스트 33건 통과. UA·HTTP/JSON/TLS 진단·비밀값 미노출·일시 실패 후 회복 및 공개 실패 시 정상 앱 유지 확인 |
| 수정된 공개 검사 함수의 직접 실행 | 같은 Mac의 Python 3.14.7에서 `verify_public()`만 읽기 전용 실행. health UP·정확한 실행 SHA·frontend 응답 모두 통과 (1.5초). 컨테이너·DB 변경 없음 |
| 변경 형식 | `git diff --check` 통과 |
| 수정 후 원격 Actions 전체 성공 | 아직 미확인 |

직접 URL 대조 성공은 수정된 workflow의 첫 전체 성공과 별도다. 백업 archive 검증을 실제 데이터 복원 검증 완료로 취급하지 않으며, 관측성 수집과 외부 metrics 보호 유지 여부도 운영 완료 항목에서 따로 확인한다.

## 재개와 완료 기준

1. 회귀 검증과 직접 공개 검사를 통과한 수정본을 커밋하고 dev에 push한다.
2. dev/PR CI 확인 후 dev → main에 반영하고 최신 main SHA의 배포 실행을 확인한다.
3. 공개 health UP·info SHA·frontend 응답과 workflow 전체 성공을 확인한다. 기존 데이터·관측성·metrics 보호 유지도 별도로 확인한다.

이전 실행의 Re-run은 이전 코드를 사용하므로 새 수정 적용 방법으로 취급하지 않는다. 이미 앱 교체와 내부 검사가 통과한 실행을 초기 로그인 실패 상태로 취급하거나 최초 준비부터 반복하지 않는다.

- [x] 실제 실행의 성공 단계와 공개 검사 실패 분리
- [x] 같은 호스트·URL에서 User-Agent 대조로 HTTP 403 원인 확인
- [x] 수정 코드 회귀 검증 33건 통과
- [x] 수정된 공개 검사 함수만 실제 URL에 읽기 전용 실행해 통과
- [ ] 수정 후 최신 main의 공개 검사 및 workflow 전체 성공

관련 문서: [14 운영 가이드](../08_deploy/14_GitHub_Actions_Mac_CICD.md), [150 Keychain 수정 기록](150_Mac_CICD_Docker_Keychain_오류_수정_기록.md), [13 Cloudflare 설정 기록](../08_deploy/13_Cloudflare_Mac_Tunnel_설정_기록.md).
