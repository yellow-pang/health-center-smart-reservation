# Social Login OAuth Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/login-social-auth` |
| base 브랜치 | `main` 추정 |
| 주요 커밋 | 아직 없음 |
| Maven compile | `mvn.cmd -q -DskipTests compile` 통과 |
| Maven test-compile | `mvn.cmd -q test-compile` 통과 |
| Next build | `npm.cmd run build` 통과 |
| TypeScript 확인 | `.\node_modules\.bin\tsc.cmd --noEmit` 통과 |
| 실행/OAuth 확인 | 미수행. 실제 provider 키 입력 후 사용자 직접 확인 필요 |

## PR 제목

```text
feat: 카카오 네이버 구글 소셜 로그인 연동 추가
```

## PR 본문

```markdown
## 개요

eGovFrame Simple Backend Template에 있었던 SNS 로그인 샘플 흐름을 현재 보건소 Auth/Member 구조에 맞게 재구성합니다.

카카오, 네이버, 구글 OAuth authorize/callback 엔드포인트를 추가하고, provider 프로필을 기존 회원 또는 신규 시민 회원으로 연결한 뒤 현재 프로젝트의 JWT access token과 refresh token을 발급합니다.

## 변경 내용

- `GET /api/auth/social/{provider}/authorize` 추가
- `GET /api/auth/social/{provider}/callback` 추가
- 카카오/네이버/구글 provider token/profile 조회 구현
- `social_accounts` 테이블 추가
- 소셜 계정과 `members` 매핑 MyBatis SQL 추가
- 기존 JWT/Refresh Token 발급 흐름을 소셜 로그인에서 재사용
- OAuth 환경변수와 Docker Compose 전달값 추가
- 로그인 페이지 소셜 버튼을 백엔드 OAuth 시작 URL로 연결
- `/login/social/callback` 프론트 콜백 화면 추가
- provider 이메일 미제공 시 `/login/social/complete` 추가 정보 입력 화면으로 이동
- `POST /api/auth/social/signup`으로 추가 정보 입력 후 소셜 회원가입 완료
- 기존 `/social-login` mock 페이지 제거

## 검증

- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `npm.cmd run build`
- [x] `.\node_modules\.bin\tsc.cmd --noEmit`
- [ ] 실제 카카오 Client ID/Secret 입력 후 브라우저 로그인 확인
- [ ] 실제 네이버 Client ID/Secret 입력 후 브라우저 로그인 확인
- [ ] 실제 구글 Client ID/Secret 입력 후 브라우저 로그인 확인
- [ ] 카카오 이메일 미제공 계정으로 추가 정보 입력 화면 이동 확인
- [ ] 추가 정보 입력 후 신규 시민 회원 생성 및 로그인 확인
- [ ] 이미 가입된 이메일 입력 시 오류 처리 확인
- [ ] 자동 로그인 체크 여부에 따라 localStorage/sessionStorage 저장 확인

## 대표 확인

1. `.env`에 `KAKAO_CLIENT_ID`, `KAKAO_REDIRECT_URI` 등을 입력한다.
2. provider 개발자 콘솔 Redirect URI를 백엔드 callback URL과 동일하게 맞춘다.
3. `/login`에서 소셜 로그인 버튼을 클릭한다.
4. provider 인증 후 `/login/social/callback`을 거쳐 역할별 첫 화면으로 이동하는지 확인한다.
5. provider가 이메일을 제공하지 않는 경우 `/login/social/complete`에서 이메일과 이름을 입력해 가입 완료되는지 확인한다.

## 미검증 사유

- 서버 기동, OAuth provider 왕복, 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- 실제 provider 앱 키는 로컬 환경변수로 주입해야 하며 코드/문서에는 비워둡니다.

## 남은 위험

- OAuth `state` 서버 검증은 후속 보안 고도화 대상입니다.
- 추가 정보 입력 완료 토큰은 10분 만료 서명 토큰이지만 서버 저장형 일회성 토큰은 아닙니다.
- 추가 정보 입력 이메일은 기존 계정 임의 연결을 막기 위해 이미 가입된 이메일을 거부합니다.
- 신규 소셜 회원은 기본 `CITIZEN`으로 생성합니다.
- 직원/관리자 소셜 계정 연결 정책은 별도 관리 화면 또는 운영 절차가 필요합니다.
```

## 커밋 메시지 초안

```text
feat: 카카오 네이버 구글 소셜 로그인 연동 추가

- 소셜 로그인 authorize/callback API 추가
- provider 프로필 조회 후 기존 JWT 발급 흐름 재사용
- 이메일 미제공 소셜 계정의 추가 정보 입력 흐름 추가
- social_accounts 테이블과 MyBatis 매핑 추가
- 로그인 화면 소셜 버튼을 백엔드 OAuth 시작 URL로 연결
- 소셜 로그인 콜백 화면 추가 및 mock 소셜 로그인 페이지 제거
```
