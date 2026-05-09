# eGovFrame 레거시 인증 샘플 비노출 기록

## 1. 작업 목표

이번 단계의 목표는 보건소 Auth/Member API가 구현된 뒤에도 남아 있는 eGovFrame 로그인, SNS 로그인, 관리자 샘플 API가 Swagger와 공개 인증 경로에서 MVP API처럼 보이지 않도록 정리하는 것이다.

기존 코드는 바로 삭제하지 않고, 보안/token 골격과 향후 소셜/간편 인증 검토 여지를 위해 보류한다.

## 2. 작업 범위

### 포함

- [x] 기존 `/auth/login-jwt`, `/auth/logout` 공개 인증 예외 제거
- [x] 기존 `/login/**` SNS 로그인 공개 인증 예외 제거
- [x] `EgovLoginApiController` Swagger 비노출 처리
- [x] `SnsLoginApiController` Swagger 비노출 처리
- [x] `EgovSiteManagerApiController` Swagger 비노출 처리
- [x] 구현 기록과 PR 문서 초안 작성
- [x] 전체 체크리스트 갱신

### 제외

- [x] eGovFrame 로그인/SNS/관리자 샘플 코드 삭제 제외
- [x] SNS 설정 properties 삭제 제외
- [x] 기존 eGovFrame JWT 유틸 삭제 제외
- [x] 서버 기동, API 런타임 호출, Swagger 브라우저 확인 제외
- [x] 실제 커밋, push, 배포 제외

## 3. 영향 확인

GitNexus MCP 리소스가 현재 세션에 노출되지 않았고, `npm.cmd exec -- gitnexus status`는 로컬 `node_modules`의 gitnexus CLI 모듈을 찾지 못해 실패했다.

대체 확인은 `rg`와 파일 직접 확인으로 수행했다.

| 대상 | 영향 범위 | 판단 |
|---|---|---|
| `SecurityConfig` | 인증 예외 경로 | MEDIUM, 레거시 샘플 공개 경로 제거 |
| `EgovLoginApiController` | `/auth/login-jwt`, `/auth/logout` | LOW, 코드 삭제 없이 Swagger 비노출 |
| `SnsLoginApiController` | `/login/kakao`, `/login/naver` 계열 | LOW, 코드 삭제 없이 Swagger 비노출 |
| `EgovSiteManagerApiController` | `/jwtAuthAPI`, `/admin/password` | LOW, 코드 삭제 없이 Swagger 비노출 |

HIGH 또는 CRITICAL 위험은 확인되지 않았다. 다만 레거시 샘플 API를 의도적으로 공개 경로에서 제외했으므로, 이 API를 사용하는 외부 클라이언트가 있다면 신규 `/api/auth/**`로 전환해야 한다.

## 4. 구현 내용

### Security

아래 경로를 `AUTH_WHITELIST`에서 제거했다.

- `/login/**`
- `/auth/login-jwt`
- `/auth/logout`

보건소 MVP 인증 경로는 유지한다.

- `/api/auth/login`
- `/api/auth/reissue`

### Swagger/OpenAPI

아래 레거시 샘플 Controller에 `@Hidden`을 추가했다.

- `EgovLoginApiController`
- `SnsLoginApiController`
- `EgovSiteManagerApiController`

## 5. 변경 파일

| 파일 | 내용 |
|---|---|
| `backend/src/main/java/egovframework/com/security/SecurityConfig.java` | 레거시 로그인/SNS 공개 인증 예외 제거 |
| `backend/src/main/java/egovframework/let/uat/uia/web/EgovLoginApiController.java` | Swagger 비노출 처리 |
| `backend/src/main/java/egovframework/com/sns/SnsLoginApiController.java` | Swagger 비노출 처리 |
| `backend/src/main/java/egovframework/let/uat/esm/web/EgovSiteManagerApiController.java` | Swagger 비노출 처리 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 샘플 정리/Auth 진행 상태 갱신 |
| `docs/11_implementation_log/16_eGovFrame_레거시_인증_샘플_비노출_기록.md` | 구현 기록 추가 |
| `docs/11_implementation_log/17_eGovFrame_레거시_인증_샘플_비노출_PR_작성안.md` | PR 작성안 추가 |

## 6. 검증

- [x] `rg` 기반 레거시 로그인/SNS/관리자 샘플 노출 경로 확인
- [x] `git diff --check`
- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [ ] 사용자 직접 Swagger UI에서 레거시 Controller 비노출 확인
- [ ] 사용자 직접 `/api/auth/login`, `/api/auth/reissue`, `/api/auth/logout` 확인
- [ ] `gitnexus detect_changes`

## 7. 사용자 직접 확인 방법

서버는 사용자가 직접 실행한다.

```powershell
cd C:\Dev\health-center-smart-reservation\backend
mvn spring-boot:run
```

Swagger UI에서 아래 Controller가 보이지 않는지 확인한다.

```text
http://localhost:8080/swagger-ui/index.html
```

확인 항목:

- `EgovLoginApiController` 미노출
- `SnsLoginApiController` 미노출
- `EgovSiteManagerApiController` 미노출
- `AuthController`, `MemberController`, `CommonCodeController` 노출

## 8. 미검증 사유

- 서버 기동, API 런타임 호출, Swagger 브라우저 확인은 사용자가 직접 수행하는 운영 기준에 따라 에이전트가 실행하지 않는다.
- GitNexus MCP 리소스가 노출되지 않았고 CLI 실행도 실패해 `gitnexus detect_changes`는 수행하지 못했다.

## 9. 후속 작업

1. 사용자 직접 Swagger UI 비노출 확인 결과 반영
2. SNS/레거시 로그인 샘플을 유지할지 완전 삭제할지 별도 브랜치에서 결정
3. Reservation Context에서 객체 권한 정책 구현

## 10. 커밋 메시지 초안

```text
refactor: eGovFrame 레거시 인증 샘플 비노출 처리

- 레거시 로그인과 SNS 샘플 공개 인증 예외 제거
- eGovFrame 로그인/SNS/관리자 샘플 Controller를 Swagger에서 숨김
- 신규 Auth Member API 기준으로 인증 노출 범위 정리
```
