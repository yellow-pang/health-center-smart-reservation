# Social Login OAuth 구현 기록

## 1. 작업 목표

- eGovFrame Simple Backend Template에 있었던 SNS 로그인 샘플의 외부 OAuth 호출 흐름을 참고해 카카오, 네이버, 구글 로그인을 현재 Auth/Member 구조에 맞게 다시 연결한다.
- 로그인 화면의 mock 소셜 로그인 페이지를 제거하고, 카카오/네이버/구글 버튼에서 백엔드 OAuth 시작 엔드포인트로 이동하게 한다.
- 실제 Client ID/Secret은 코드에 넣지 않고 환경변수로 주입한다.

## 2. 작업 범위

- 포함:
  - [x] 제거된 eGovFrame `SnsLoginApiController`, `SnsUtils`, `SnsVO` git 이력 확인
  - [x] 현재 `AuthCommandService`, `HealthcenterJwtTokenProvider`, `MemberMapper`, `SecurityConfig` 확인
  - [x] `GET /api/auth/social/{provider}/authorize` 추가
  - [x] `GET /api/auth/social/{provider}/callback` 추가
  - [x] 카카오/네이버/구글 token/profile 조회 흐름 추가
  - [x] `social_accounts` 테이블과 MyBatis 매핑 추가
  - [x] 소셜 계정 매핑 또는 이메일 기준 기존 회원 조회, 없으면 `CITIZEN` 회원 생성
  - [x] 현재 프로젝트 JWT/Refresh Token 발급 흐름 재사용
  - [x] 로그인 페이지 소셜 버튼을 백엔드 OAuth 시작 URL로 연결
  - [x] `/login/social/callback` 프론트 콜백 화면 추가
  - [x] 기존 `/social-login` mock 페이지 제거
  - [x] Maven compile/test-compile, TypeScript/Next build 확인

- 제외:
  - [ ] 실제 카카오/네이버/구글 개발자 콘솔 앱 생성
  - [ ] 실제 Client ID/Secret 입력
  - [ ] 서버 기동, 브라우저 OAuth 왕복 검증
  - [ ] provider별 추가 동의항목 심사/운영 정책 문서화

## 3. 영향 범위

GitNexus impact:

| 대상 | risk | direct callers | affected processes |
|---|---|---:|---:|
| `AuthController` | LOW | 0 | 0 |
| `AuthCommandService` | LOW | 1 | 0 |
| `MemberMapper` | LOW | 1 | 0 |
| `SecurityConfig` | LOW | 0 | 0 |
| `LoginPage` | LOW | 0 | 0 |
| `SocialLoginPage` | LOW | 0 | 0 |

HIGH/CRITICAL 위험은 없었다.

## 4. 구현 내용

수정/추가 파일:

- `backend/src/main/java/egovframework/healthcenter/member/api/SocialAuthController.java`
- `backend/src/main/java/egovframework/healthcenter/member/application/SocialLoginService.java`
- `backend/src/main/java/egovframework/healthcenter/member/application/AuthCommandService.java`
- `backend/src/main/java/egovframework/healthcenter/member/mapper/MemberMapper.java`
- `backend/src/main/resources/egovframework/mapper/healthcenter/member/Member_SQL_postgresql.xml`
- `backend/src/main/resources/db/postgresql/schema.sql`
- `backend/src/main/java/egovframework/com/security/SecurityConfig.java`
- `backend/src/main/resources/application.properties`
- `docker-compose.yml`
- `.env.example`
- `frontend/app/login/page.tsx`
- `frontend/app/login/social/callback/page.tsx`
- `frontend/app/social-login/page.tsx` 제거
- `frontend/README.md`
- `docs/04_api/01_API_명세서.md`
- `docs/13_schedule/02_전체_작업_체크리스트.md`

구현 요약:

- provider별 authorize URL을 백엔드에서 생성해 302 redirect한다.
- provider callback에서 authorization code로 provider access token을 받고 프로필을 조회한다.
- `social_accounts(provider, provider_user_id)`로 기존 연결을 찾고, 없으면 이메일 기준 회원을 찾는다.
- 기존 회원이 없으면 `CITIZEN` 역할의 소셜 회원을 생성한다.
- 프로젝트 내부 access token/refresh token은 기존 `HealthcenterJwtTokenProvider`와 `refresh_tokens` 테이블을 재사용한다.
- 프론트 콜백은 URL fragment의 token을 받아 자동 로그인 체크 여부에 따라 localStorage/sessionStorage에 저장한다.

## 5. 환경변수

```text
OAUTH_FRONTEND_CALLBACK_URL=http://localhost:3000/login/social/callback
KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=
KAKAO_REDIRECT_URI=http://localhost:8080/api/auth/social/kakao/callback
NAVER_CLIENT_ID=
NAVER_CLIENT_SECRET=
NAVER_REDIRECT_URI=http://localhost:8080/api/auth/social/naver/callback
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/social/google/callback
```

## 6. 검증 결과

| 검증 | 결과 |
|---|---|
| `mvn.cmd -q -DskipTests compile` | 통과 |
| `mvn.cmd -q test-compile` | 통과 |
| `npm.cmd run build` | 통과 |
| `.\node_modules\.bin\tsc.cmd --noEmit` | 통과 |
| 서버 기동 | 미수행. 사용자 직접 수행 범위 |
| OAuth 브라우저 왕복 | 미수행. 실제 provider 키 입력 후 사용자 직접 수행 |

## 7. 사용자 직접 확인 방법

1. `.env`에 provider별 Client ID/Secret/Redirect URI를 입력한다.
2. 카카오/네이버/구글 개발자 콘솔에도 같은 Redirect URI를 등록한다.
3. 백엔드와 프론트엔드를 실행한다.
4. `/login`에서 카카오/네이버/구글 버튼을 클릭한다.
5. provider 인증 후 `/login/social/callback`을 거쳐 역할별 첫 화면으로 이동하는지 확인한다.

## 8. 남은 위험

- `state` 값은 현재 난수 생성 후 provider로 전달하지만 서버 저장 검증은 하지 않는다. 운영 보안 강화를 위해 서명 state 또는 별도 저장소 검증을 후속으로 검토해야 한다.
- 소셜 신규 회원은 기본 `CITIZEN`으로 생성한다. 직원/관리자 소셜 계정 연결 정책은 후속 결정이 필요하다.
- provider가 이메일을 주지 않으면 `{provider}_{id}@social.local` 형태의 내부 이메일을 사용한다.

## 9. 커밋 메시지 초안

```text
feat: 카카오 네이버 구글 소셜 로그인 연동 추가

- 소셜 로그인 authorize/callback API 추가
- provider 프로필 조회 후 기존 JWT 발급 흐름 재사용
- social_accounts 테이블과 MyBatis 매핑 추가
- 로그인 화면 소셜 버튼을 백엔드 OAuth 시작 URL로 연결
- 소셜 로그인 콜백 화면 추가 및 mock 소셜 로그인 페이지 제거
```

