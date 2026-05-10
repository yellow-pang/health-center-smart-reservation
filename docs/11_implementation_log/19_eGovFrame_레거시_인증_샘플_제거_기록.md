# eGovFrame 레거시 인증 샘플 제거 기록

## 1. 작업 목표

이번 단계의 목표는 신규 보건소 Auth/Member API로 대체된 eGovFrame 로그인, SNS 로그인, 관리자 인증 샘플 기능을 제거하는 것이다.

전자정부프레임워크 기반 구조는 유지한다. `egovframework.com.config`, Maven, MyBatis, Security/JWT 필터 골격, `LoginVO`, `EgovJwtTokenUtil`, `JwtAuthenticationFilter`는 이번 삭제 범위에서 제외한다.

## 2. 작업 범위

### 포함

- [x] eGovFrame 로그인 샘플 Controller, Service, DAO 제거
- [x] SNS 로그인 샘플 Controller, Utils, VO 제거
- [x] 관리자 인증 샘플 Controller, Service, DAO 제거
- [x] 레거시 로그인 API 테스트 제거
- [x] `/auth/*` 레거시 인터셉터 등록 제거
- [x] SNS 샘플 properties 제거
- [x] 전체 체크리스트 갱신

### 제외

- [x] eGovFrame 공통 설정 제거 제외
- [x] `LoginVO` 제거 제외
- [x] `EgovJwtTokenUtil` 제거 제외
- [x] `JwtAuthenticationFilter` 레거시 fallback 제거 제외
- [x] `AuthenticInterceptor`, `EgovUserDetailsHelper`, `CustomAuthenticationPrincipalResolver` 제거 제외
- [x] 서버 기동, API 런타임 호출, Swagger 브라우저 확인 제외
- [x] 실제 커밋, push, 배포 제외

## 3. 영향 확인

GitNexus MCP 리소스가 현재 세션에 노출되지 않았고, `npm.cmd exec -- gitnexus status`는 로컬 `node_modules`의 gitnexus CLI 모듈을 찾지 못해 실패했다.

대체 확인은 `rg`와 파일 직접 확인으로 수행했다.

| 대상 | 영향 범위 | 판단 |
|---|---|---|
| `EgovLoginApiController`, `EgovLoginService`, `LoginDAO` | `/auth/login-jwt`, `/auth/logout` 샘플 | 제거 가능, 신규 `/api/auth/**`로 대체 |
| `SnsLoginApiController`, `SnsUtils`, `SnsVO` | Kakao/Naver 샘플 로그인 | MVP 제외, 샘플 설정과 함께 제거 가능 |
| `EgovSiteManagerApiController`, `EgovSiteManagerService`, `SiteManagerDAO` | `/jwtAuthAPI`, `/admin/password` 샘플 | 신규 Member/Admin API에서 재설계 대상이므로 제거 가능 |
| `EgovConfigWebDispatcherServlet` | `/auth/*` 레거시 인터셉터 등록 | 대상 Controller 제거로 등록 제거 가능 |
| `LoginVO`, `EgovJwtTokenUtil`, `JwtAuthenticationFilter` | eGovFrame 보안 골격 및 레거시 fallback | 이번 단계 보류 |

## 4. 제거 파일

| 파일 | 처리 |
|---|---|
| `backend/src/main/java/egovframework/let/uat/uia/web/EgovLoginApiController.java` | 제거 |
| `backend/src/main/java/egovframework/let/uat/uia/service/EgovLoginService.java` | 제거 |
| `backend/src/main/java/egovframework/let/uat/uia/service/impl/EgovLoginServiceImpl.java` | 제거 |
| `backend/src/main/java/egovframework/let/uat/uia/service/impl/LoginDAO.java` | 제거 |
| `backend/src/main/java/egovframework/com/sns/SnsLoginApiController.java` | 제거 |
| `backend/src/main/java/egovframework/com/sns/SnsUtils.java` | 제거 |
| `backend/src/main/java/egovframework/com/sns/SnsVO.java` | 제거 |
| `backend/src/main/java/egovframework/let/uat/esm/web/EgovSiteManagerApiController.java` | 제거 |
| `backend/src/main/java/egovframework/let/uat/esm/service/EgovSiteManagerService.java` | 제거 |
| `backend/src/main/java/egovframework/let/uat/esm/service/impl/EgovSiteManagerServiceImpl.java` | 제거 |
| `backend/src/main/java/egovframework/let/uat/esm/service/impl/SiteManagerDAO.java` | 제거 |
| `backend/src/test/java/egovframework/let/uat/uia/web/EgovLoginApiControllerTest.java` | 제거 |

## 5. 변경 파일

| 파일 | 내용 |
|---|---|
| `backend/src/main/java/egovframework/com/config/EgovConfigWebDispatcherServlet.java` | 제거된 `/auth/*` 레거시 인터셉터 등록 정리 |
| `backend/src/main/resources/application.properties` | SNS 샘플 설정 제거 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 샘플 정리 진행 상태 갱신 |
| `docs/11_implementation_log/19_eGovFrame_레거시_인증_샘플_제거_기록.md` | 이번 제거 기록 추가 |
| `docs/11_implementation_log/20_eGovFrame_레거시_인증_샘플_제거_PR_작성안.md` | PR 작성안 추가 |

## 6. 검증

- [x] `rg` 기반 레거시 로그인/SNS/관리자 샘플 참조 확인
- [x] `git diff --check`
- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [ ] 사용자 직접 Swagger UI에서 레거시 Controller 미노출 확인
- [ ] 사용자 직접 신규 `/api/auth/**` 확인
- [ ] `gitnexus detect_changes`

`rg` 확인 결과 백엔드 코드와 설정에서 제거 대상 레거시 샘플 참조는 남지 않았다.

## 7. 미검증 사유

- 서버 기동, API 런타임 호출, Swagger 브라우저 확인은 사용자가 직접 수행하는 운영 기준에 따라 에이전트가 실행하지 않는다.
- GitNexus MCP 리소스가 노출되지 않았고 CLI 실행도 실패해 `gitnexus detect_changes`는 수행하지 못했다.

## 8. 후속 작업

1. 사용자 직접 Auth API와 Swagger UI 확인 결과 반영
2. `JwtAuthenticationFilter`의 레거시 `LoginVO` fallback 유지 여부를 별도 브랜치에서 판단
3. `EgovJwtTokenUtil`, `LoginVO`, eGov 보안 유틸 사용 범위 재점검

2~3번 항목은 이번 브랜치에서 제거하지 않고 `docs/14_deferred_cleanup/01_보류_정리_목록.md`로 이관했다.

## 9. 커밋 메시지 초안

```text
refactor: eGovFrame 레거시 인증 샘플 제거

- eGovFrame 로그인/SNS/관리자 샘플 API 제거
- 레거시 로그인 Service/DAO와 SNS 유틸 제거
- SNS 샘플 설정과 레거시 로그인 테스트 정리
- 전자정부 공통 보안 골격은 유지
```
