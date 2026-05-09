# Auth/Member 역할별 접근 규칙 구현 기록

## 1. 작업 목표

이번 단계의 목표는 API 명세서의 권한 표를 기준으로 `SecurityConfig`에 보건소 MVP 역할별 접근 규칙을 1차 반영하는 것이다.

서버 기동, API 런타임 호출, Swagger 브라우저 확인은 사용자가 직접 수행하는 기준에 따라 에이전트는 정적 확인과 Maven compile/test-compile까지만 수행한다.

## 2. 작업 범위

### 포함

- [x] 공개 API 경로 정리
- [x] 관리자 API 경로 `ADMIN` 제한
- [x] 직원 운영 API 경로 `STAFF`, `ADMIN` 제한
- [x] 예약 API 경로 1차 권한 제한
- [x] 로그인 사용자 API 경로 인증 요구
- [x] 구현 기록과 PR 문서 초안 작성
- [x] 전체 체크리스트 갱신

### 제외

- [x] 예약자 본인 여부 같은 객체 소유자 정책 구현 제외
- [x] 아직 구현되지 않은 Office/Reservation/Visit/Queue/Dashboard Controller 구현 제외
- [x] 기존 eGovFrame 로그인/관리자/SNS 샘플 삭제 제외
- [x] 서버 기동, API 런타임 호출, Swagger 브라우저 확인 제외
- [x] 실제 커밋, push, 배포 제외

## 3. 영향 확인

GitNexus MCP 리소스가 현재 세션에 노출되지 않았고, `npm.cmd exec -- gitnexus status`는 로컬 `node_modules`의 gitnexus CLI 모듈을 찾지 못해 실패했다.

대체 확인은 `rg`와 파일 직접 확인으로 수행했다.

| 대상 | 영향 범위 | 판단 |
|---|---|---|
| `SecurityConfig` | 모든 HTTP 요청의 인증/인가 진입점 | MEDIUM, 인증 핵심 경로이나 URL 규칙 추가 중심 |
| `JwtAuthenticationFilter` | `ROLE_CITIZEN`, `ROLE_GUARDIAN`, `ROLE_STAFF`, `ROLE_ADMIN` 권한 주입 | 변경 없음, 기존 동작 사용 |
| API 명세 권한 표 | SecurityConfig 규칙의 기준 | 문서 기준과 1차 정합성 확보 |

HIGH 또는 CRITICAL 위험은 확인되지 않았다. 다만 객체 소유자 판정은 URL 규칙만으로 처리할 수 없어 후속 Service 정책으로 남긴다.

## 4. 구현 내용

### 공개 API

- `/api/auth/login`
- `/api/auth/reissue`
- `/api/common-codes`
- `/api/common-codes/**`
- `/api/service-types`
- `/api/congestion/current`
- Swagger와 정적 리소스

### 역할별 API

| 경로 | 권한 |
|---|---|
| `/api/admin/**` | `ADMIN` |
| `/api/dashboard/**` | `ADMIN` |
| `/api/visits/**` | `STAFF`, `ADMIN` |
| `/api/queues/**` | `STAFF`, `ADMIN` |
| `POST /api/reservations` | `CITIZEN`, `GUARDIAN`, `STAFF`, `ADMIN` |
| `DELETE /api/reservations/*` | `CITIZEN`, `GUARDIAN`, `ADMIN` |
| `/api/reservation-slots/**` | 로그인 사용자 |
| `/api/members/me` | 로그인 사용자 |
| `/api/auth/logout` | 로그인 사용자 |

`GET /api/reservations/me`와 `GET /api/reservations/*`는 로그인 사용자로 제한했다. 예약자 본인, 직원, 관리자 세부 권한은 Reservation Context 구현 시 Service 정책으로 보강한다.

## 5. 변경 파일

| 파일 | 내용 |
|---|---|
| `backend/src/main/java/egovframework/com/security/SecurityConfig.java` | 보건소 MVP 공개/역할별 API 접근 규칙 추가 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | Auth/Member 진행 상태 갱신 |
| `docs/11_implementation_log/14_Auth_Member_역할별_접근_규칙_구현_기록.md` | 구현 기록 추가 |
| `docs/11_implementation_log/15_Auth_Member_역할별_접근_규칙_PR_작성안.md` | PR 작성안 추가 |

## 6. 검증

- [x] `rg` 기반 Security/Auth/권한 참조 확인
- [x] `git diff --check`
- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [ ] 사용자 직접 역할별 API 접근 확인
- [ ] 사용자 직접 Swagger UI 확인
- [ ] `gitnexus detect_changes`

## 7. 사용자 직접 확인 방법

서버는 사용자가 직접 실행한다.

```powershell
cd C:\Dev\health-center-smart-reservation\backend
mvn spring-boot:run
```

역할별 확인은 각 seed 계정으로 로그인한 뒤 같은 API에 접근해 200/403을 비교한다.

```powershell
$admin = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"admin@test.com","password":"password1234"}'
$staff = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"staff@test.com","password":"password1234"}'
$citizen = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"citizen@test.com","password":"password1234"}'
```

예상 기준:

- `ADMIN`은 `/api/admin/**`, `/api/dashboard/**` 접근 가능
- `STAFF`는 `/api/visits/**`, `/api/queues/**` 접근 가능
- `CITIZEN`은 직원/관리자 API 접근 시 403
- 공개 API인 `/api/service-types`, `/api/congestion/current`, `/api/common-codes/**`는 토큰 없이 접근 가능

아직 구현되지 않은 API는 404가 날 수 있으므로, 실제 Controller가 추가된 뒤 역할별 접근 검증을 다시 수행한다.

## 8. 미검증 사유

- 서버 기동, API 런타임 호출, Swagger 브라우저 확인은 사용자가 직접 수행하는 운영 기준에 따라 에이전트가 실행하지 않는다.
- GitNexus MCP 리소스가 노출되지 않았고 CLI 실행도 실패해 `gitnexus detect_changes`는 수행하지 못했다.
- 아직 구현되지 않은 API는 역할 규칙만 선반영했고 런타임 권한 검증은 후속 Controller 구현 뒤 재확인한다.

## 9. 후속 작업

1. Reservation Context에서 예약자 본인/직원/관리자 객체 권한 정책 구현
2. 기존 eGovFrame 로그인/관리자/SNS 샘플 비노출 또는 삭제 판단
3. 사용자 직접 역할별 접근 확인 결과를 PR 문서에 반영

## 10. 커밋 메시지 초안

```text
feat: Auth Member 역할별 접근 규칙 추가

- 보건소 MVP 공개 API와 인증 필요 API 경로 정리
- 관리자, 직원, 예약 API 역할별 Security 규칙 추가
- 역할별 접근 확인 방법과 남은 객체 권한 정책 문서화
```
