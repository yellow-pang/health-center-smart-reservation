# Account Recovery API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/account-recovery-api` |
| base 브랜치 | `dev` |
| 작업 트리 | 계정 복구 백엔드 API, DB schema, MyBatis 매핑, 설정, API/DB/체크리스트 문서 갱신 |
| 주요 커밋 | 커밋 전 |
| 빌드 확인 | 미수행. 사용자 요청에 따라 학습 PC에서는 빌드/테스트를 실행하지 않음 |
| 테스트 확인 | `git diff --check` 통과. CRLF 변환 경고만 표시 |
| 실행/API 확인 | 서버 기동, Swagger, 브라우저 확인은 사용자 직접 확인 필요 |

## PR 제목

```text
feat: 계정 찾기 백엔드 API 추가
```

## PR 본문

```markdown
## 개요

프론트에 이미 존재하는 아이디 찾기와 비밀번호 찾기 mock 화면을 실제 API로 연결하기 위한 백엔드 계정 복구 API를 추가합니다.

아이디 찾기는 이름과 휴대폰 번호로 활성 회원을 조회하고 가입 이메일을 마스킹해 반환합니다. 비밀번호 찾기는 서버 저장 토큰 기반 2단계 재설정 방식으로 구현했습니다.

실제 SMS/Email 발송 연동은 이번 브랜치에서 제외하고, 개발/포트폴리오 확인용 재설정 토큰 응답은 환경변수로 제어합니다.

## 변경 내용

- 계정 복구 API 추가
  - `POST /api/auth/find-id`
  - `POST /api/auth/password-reset/request`
  - `POST /api/auth/password-reset/confirm`
- `password_reset_tokens` 테이블 추가
  - 토큰 원문 미저장
  - SHA-256 해시 저장
  - 만료 시각과 사용 시각 관리
- 계정 복구 서비스 정책 추가
  - 이메일 마스킹 반환
  - 하이픈 유무를 무시한 휴대폰 번호 비교
  - 새 토큰 발급 시 기존 미사용 토큰 사용 처리
  - 비밀번호 변경 성공 시 refresh token 전체 폐기
- 공개 API 보안 경로 추가
- 계정 복구 환경변수 추가
  - `ACCOUNT_RECOVERY_RESET_TOKEN_EXPIRE`
  - `ACCOUNT_RECOVERY_EXPOSE_DEVELOPMENT_TOKEN`
- API 명세, DB 명세, 전체 체크리스트, 후속작업 문서 갱신

## 검증

- [x] `git diff --check`
- [ ] `mvn.cmd -q -DskipTests compile`
- [ ] `mvn.cmd -q test-compile`
- [ ] Swagger `POST /api/auth/find-id`
- [ ] Swagger `POST /api/auth/password-reset/request`
- [ ] Swagger `POST /api/auth/password-reset/confirm`
- [ ] 비밀번호 변경 후 기존 refresh token 재사용 실패 확인
- [ ] 프론트 `/find-id`, `/reset-password` 실제 API 연동 확인

## 미검증 사유

현재 작업 PC는 학습용 환경이라 사용자 요청에 따라 Maven 빌드와 테스트를 실행하지 않았습니다. 서버 기동, Swagger, 브라우저 런타임 확인도 사용자 직접 확인 항목으로 남깁니다.

## Swagger 확인 예시

아이디 찾기:

```json
{
  "name": "홍길동",
  "phone": "010-0000-0001"
}
```

비밀번호 재설정 요청:

```json
{
  "email": "citizen@example.com",
  "phone": "010-0000-0001"
}
```

비밀번호 재설정 완료:

```json
{
  "resetToken": "developmentResetToken 값",
  "newPassword": "newPassword1234",
  "newPasswordConfirm": "newPassword1234"
}
```

## 운영 주의사항

- 운영 환경에서는 `ACCOUNT_RECOVERY_EXPOSE_DEVELOPMENT_TOKEN=false`로 설정해야 합니다.
- 실제 운영 계정 복구에는 SMS/Email 발송 또는 별도 본인확인 흐름이 필요합니다.
- 공개 API 특성상 rate limit, captcha, IP/계정 기준 시도 제한을 후속으로 검토합니다.

## 후속 작업

- `/find-id`, `/reset-password` 프론트 실제 API 연동
- SMS/Email 발송 채널 또는 인증번호 검증 흐름 검토
- 계정 복구 정책 테스트 추가
- 공개 계정 복구 API 남용 방지 정책 검토
```

## Merge 후 브랜치 정리 기준

- [ ] API 명세와 Swagger 노출 확인
- [ ] 개발 환경에서 `developmentResetToken`으로 비밀번호 재설정 확인
- [ ] 운영 환경변수에서 `ACCOUNT_RECOVERY_EXPOSE_DEVELOPMENT_TOKEN=false` 반영 여부 확인
- [ ] 다음 브랜치 후보를 `feat/account-recovery-frontend`로 연결

## 커밋 메시지 초안

제목:

```text
feat: 계정 찾기 백엔드 API 추가
```

본문:

```text
- 아이디 찾기 이메일 마스킹 API 추가
- 서버 저장 토큰 기반 비밀번호 재설정 API 추가
- password_reset_tokens 테이블과 MyBatis 매핑 추가
- 계정 복구 API 명세와 체크리스트 갱신
```
