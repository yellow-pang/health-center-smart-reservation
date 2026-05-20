# eGovFrame JWT/LoginVO 인증 보조 유틸 정리 기록

## 1. 작업 목표

- `JwtAuthenticationFilter`의 레거시 fallback 제거 이후 남은 `EgovJwtTokenUtil`과 `LoginVO` 기반 인증 보조 코드를 정리한다.
- 보건소 Auth/Member 인증 흐름은 `HealthcenterJwtTokenProvider`와 `MemberPrincipal` 기준으로 유지한다.
- 전자정부프레임워크 Simple Backend Template 기반, Maven, MyBatis, `egovframework.healthcenter` 신규 도메인 구조는 유지한다.

## 2. 관련 항목 확인

전체 체크리스트 기준:

- `docs/13_schedule/02_전체_작업_체크리스트.md`
- `12.5 MVP 이후 고도화 후보`의 잔여 eGovFrame 샘플/보류 항목 재점검

보류 정리 목록 기준:

- `DC-002`: `EgovJwtTokenUtil`
- `DC-003`: `LoginVO`와 eGovFrame 사용자 인증 유틸

## 3. 작업 범위

포함한다.

- [x] `EgovJwtTokenUtil` 제거
- [x] `EgovJwtTokenUtilTest` 제거
- [x] `LoginVO` 제거
- [x] `EgovUserDetailsHelper` 제거
- [x] `AuthenticInterceptor` 제거
- [x] `EgovUserDetailsService`와 구현체 제거
- [x] `CustomAuthenticationPrincipalResolver` 제거
- [x] `WebMvcConfig`의 레거시 `LoginVO` principal resolver 등록 제거
- [x] 보류 정리 목록과 전체 체크리스트 갱신
- [x] PR 작성안 작성

제외한다.

- [ ] `SecurityConfig`, `JwtAuthenticationFilter`, `JwtAuthenticationEntryPoint` 제거
- [ ] 보건소 Auth/Member 토큰 발급/검증 흐름 변경
- [ ] eGovFrame 공통 설정, Maven, MyBatis 구조 변경
- [ ] Spring Security filter 단계 401/403 공통 응답 통합

## 4. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `backend/src/main/java/egovframework/com/jwt/EgovJwtTokenUtil.java` | 레거시 `LoginVO` JWT 생성/파싱 유틸 제거 |
| `backend/src/test/java/egovframework/com/jwt/EgovJwtTokenUtilTest.java` | 제거된 유틸 테스트 제거 |
| `backend/src/main/java/egovframework/com/cmm/LoginVO.java` | 레거시 인증 VO 제거 |
| `backend/src/main/java/egovframework/com/cmm/util/EgovUserDetailsHelper.java` | `LoginVO` 기반 SecurityContext helper 제거 |
| `backend/src/main/java/egovframework/com/cmm/interceptor/AuthenticInterceptor.java` | `LoginVO` 기반 MVC 인증 인터셉터 제거 |
| `backend/src/main/java/egovframework/com/cmm/service/EgovUserDetailsService.java` | 레거시 사용자 상세 서비스 인터페이스 제거 |
| `backend/src/main/java/egovframework/com/cmm/service/impl/EgovUserDetailsSessionServiceImpl.java` | 세션 기반 레거시 사용자 상세 서비스 제거 |
| `backend/src/main/java/egovframework/com/cmm/service/impl/EgovTestUserDetailsServiceImpl.java` | 테스트 사용자 상세 서비스 제거 |
| `backend/src/main/java/egovframework/com/security/CustomAuthenticationPrincipalResolver.java` | `@AuthenticationPrincipal LoginVO` resolver 제거 |
| `backend/src/main/java/egovframework/com/security/WebMvcConfig.java` | 제거된 resolver 등록 제거 |

## 5. 설계 판단

- `rg` 확인 결과 보건소 도메인 API는 `MemberPrincipal`과 `AuthenticatedPrincipal` helper를 사용하고 있었다.
- `LoginVO` 기반 클래스는 레거시 인증 보조 코드 내부 참조만 남아 있어 묶음 단위로 제거했다.
- `WebMvcConfig`의 HTML escaping converter는 API 응답 보안 설정이므로 유지했다.
- eGovFrame 공통 패키지 전체를 지우지 않고, 현재 보건소 MVP 인증 흐름과 충돌하는 `LoginVO` 인증 보조 묶음만 제거했다.

## 5.1 포트폴리오 관점의 매핑

이번 정리는 단순 삭제가 아니라 eGovFrame 템플릿의 샘플 인증을 보건소 도메인 인증으로 치환한 작업이다.

| 제거한 요소 | 현재 대체 구현 | 의미 |
|---|---|---|
| `LoginVO` | `MemberPrincipal` | 보건소 회원, 권한, 보건소 식별자 중심 principal로 전환 |
| `EgovJwtTokenUtil` | `HealthcenterJwtTokenProvider` | 보건소 Auth/Member claim 기반 JWT 발급/검증으로 전환 |
| `EgovUserDetailsHelper` | `AuthenticatedPrincipal.require(authentication)` | 보건소 API에서 필요한 principal 추출 방식으로 단순화 |
| `CustomAuthenticationPrincipalResolver` | Controller의 `Authentication` 주입과 `MemberPrincipal` 추출 | 레거시 `@AuthenticationPrincipal LoginVO` 의존 제거 |
| `AuthenticInterceptor` | `SecurityConfig`의 URL/role 기반 접근 규칙 | REST API에 맞는 Spring Security 권한 규칙으로 전환 |

포트폴리오 설명은 `docs/12_portfolio/01_포트폴리오_구현_스토리라인.md`의 `eGovFrame 템플릿 전환 스토리` 섹션을 기준으로 한다.

## 6. 검증 체크리스트

- [x] `rg`로 `EgovJwtTokenUtil`, `LoginVO`, `EgovUserDetailsHelper`, `AuthenticInterceptor`, `CustomAuthenticationPrincipalResolver` 코드 참조 제거 확인
- [x] `git diff --check`
- [ ] `mvn.cmd -q -DskipTests compile`
- [ ] `mvn.cmd -q test-compile`
- [ ] Swagger 대표 인증 흐름 확인

이번 요청 기준상 실제 개발 환경이 아니므로 Maven 빌드와 테스트 컴파일은 실행하지 않았다. 위 Maven 명령은 사용자 직접 수행 검증 항목으로 남긴다.

## 7. 사용자 확인 안내

서버 기동 후 Swagger에서 아래 대표 흐름을 확인한다.

대표 예시:

```text
GET /api/members/me
Authorization: Bearer {POST /api/auth/login으로 발급받은 accessToken}
```

기대 결과:

- 유효한 보건소 access token이면 `success: true`와 현재 사용자 정보가 반환된다.
- 레거시 `LoginVO` 기반 토큰 또는 잘못된 토큰은 인증 principal을 만들지 않는다.

추가 Happy/Edge/Bad 케이스는 PR 문서 체크리스트에 남긴다.

## 8. 남은 위험과 후속 작업

- 과거 문서에는 `LoginVO`와 `EgovJwtTokenUtil`이 유지 대상이었다는 기록이 남아 있다. 이번 정리 결과 이후 최신 기준 문서는 이 기록과 PR 작성안을 우선한다.
- Spring Security filter 단계 인증/인가 실패 응답은 아직 공통 오류 응답과 완전히 통합하지 않았다.
- 향후 eGovFrame 공통 패키지의 다른 미사용 샘플 유틸을 정리할 때도 보건소 도메인 참조 여부를 먼저 확인한다.

## 9. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] PR 문서 작성
- [x] 보류 정리 목록 갱신
- [x] 전체 체크리스트 갱신
- [x] 정적 확인 결과 기록
- [x] 미실행 검증과 사용자 확인 방법 기록
- [x] 커밋 메시지 정리
