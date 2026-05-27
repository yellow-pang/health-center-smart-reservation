# Account Recovery 구현 방식 검토 기록

## 1. 작업 목표

- 현재 프로젝트 상태에서 아이디 찾기와 비밀번호 찾기를 어떻게 구현하는 것이 적절한지 확인한다.
- 기존 Auth/Member 구조, 프론트 보조 화면, DB 스키마, 후속 고도화 목록을 대조한다.
- 이번 작업은 구현 전 검토와 체크리스트 갱신만 수행하고, API/DB/프론트 코드는 변경하지 않는다.

## 2. 확인한 현재 상태

- 백엔드 Auth API는 `POST /api/auth/login`, `POST /api/auth/reissue`, `POST /api/auth/logout` 중심으로 구현되어 있다.
- Member API는 현재 사용자 조회 중심이며, 아이디 찾기/비밀번호 재설정 API는 아직 없다.
- `members` 테이블은 `email`, `password`, `name`, `phone`, `role`, `active`를 보유한다.
- 프론트에는 `/find-id`, `/reset-password` 화면이 이미 있으나 mock 완료 상태만 표시한다.
- `docs/14_deferred_cleanup/01_보류_정리_목록.md`의 DC-025에 휴대폰 인증 기반 본인확인이 고도화 후보로 남아 있다.
- 현재 MVP와 배포 기본선은 완료 상태이며, 계정 찾기는 MVP 차단 작업이 아니라 인증 UX 고도화 후보로 보는 것이 맞다.

## 3. 권장 구현 방향

1순위는 "운영 안전형 2단계 재설정"이다.

- 아이디 찾기:
  - `POST /api/auth/find-id`
  - 입력값은 `name`, `phone`
  - 응답은 가입 이메일을 전부 노출하지 않고 마스킹한다. 예: `ci***@example.com`
  - 존재하지 않는 회원도 동일한 형태의 중립 메시지를 반환할지 결정한다.

- 비밀번호 찾기:
  - `POST /api/auth/password-reset/request`
  - 입력값은 `email`, `phone`
  - 회원 검증 후 재설정 토큰을 발급한다.
  - MVP 포트폴리오 환경에서는 실제 SMS/Email 발송 대신 개발용 응답 또는 서버 로그로 토큰 확인 방식을 둘 수 있다.
  - 운영 전에는 발송 채널, 토큰 저장 방식, rate limit, 감사 로그를 확정한다.

- 비밀번호 재설정:
  - `POST /api/auth/password-reset/confirm`
  - 입력값은 `resetToken`, `newPassword`, `newPasswordConfirm`
  - 성공 시 비밀번호를 eGovFrame 기존 방식과 동일하게 `EgovFileScrty.encryptPassword(raw, email)`로 저장한다.
  - 해당 회원의 refresh token은 모두 폐기한다.

## 4. 구현 시 필요한 변경 후보

백엔드:

- `AuthController` 또는 별도 `AccountRecoveryController`에 공개 API 추가
- `AccountRecoveryService` 추가
- DTO 추가: `FindIdRequest`, `FindIdResponse`, `PasswordResetRequest`, `PasswordResetConfirmRequest`
- `MemberMapper`에 이름/휴대폰 기준 조회, 이메일/휴대폰 기준 조회, 비밀번호 변경, 회원 refresh token 전체 폐기 query 추가
- `password_reset_tokens` 테이블 추가 검토
- 오류 코드는 `ACCOUNT_RECOVERY_*` 또는 `AUTH_PASSWORD_RESET_*` 계열로 추가

프론트엔드:

- `frontend/src/lib/auth-api.ts` 또는 계정 복구 전용 API client 추가
- `/find-id` mock 결과를 실제 API 응답으로 교체
- `/reset-password`는 "요청"과 "새 비밀번호 입력" 단계를 분리
- 토큰 URL 진입 방식(`/reset-password/confirm?token=...`) 또는 같은 화면 2단계 입력 방식을 선택

문서:

- `docs/04_api/01_API_명세서.md`에 Account Recovery API 추가
- `docs/03_database/01_ERD_및_테이블_명세서.md`에 토큰 테이블 추가 여부 반영
- `docs/13_schedule/02_전체_작업_체크리스트.md`의 인증 UX 고도화 항목 갱신

## 5. 구현 방식 후보

### 후보 A. 개발용 단순 구현

- 아이디 찾기는 `name + phone`으로 이메일 마스킹 반환
- 비밀번호 찾기는 `email + phone` 확인 후 즉시 새 비밀번호를 받는다.
- 장점: DB 변경이 적고 빠르게 연결 가능
- 단점: 운영 보안 기준으로는 약하다.
- 판단: 포트폴리오 데모 한정이면 가능하지만 권장 1순위는 아니다.

### 후보 B. 서버 저장 토큰 기반 2단계 재설정

- `password_reset_tokens` 테이블에 해시된 토큰, 만료시각, 사용 여부를 저장한다.
- 요청 API는 토큰을 만들고, 확인 API는 토큰 검증 후 비밀번호를 변경한다.
- 장점: 실제 서비스에 가까운 구조이고, refresh token 폐기/감사 로그/rate limit 확장이 쉽다.
- 단점: DB 스키마와 API가 추가된다.
- 판단: 이 프로젝트에는 가장 균형이 좋다.

### 후보 C. SMS/Email 인증번호 연동

- SMS 또는 Email 발송 서비스를 붙이고 인증번호 검증 후 재설정한다.
- 장점: 사용자 본인확인 UX가 자연스럽다.
- 단점: 외부 서비스, 비용, 운영 설정, rate limit, 발송 실패 대응이 필요하다.
- 판단: 운영 고도화 후보로 두고, 먼저 후보 B 구조를 만든 뒤 발송 채널을 교체하는 편이 좋다.

## 6. 이번 작업에서 변경하지 않은 것

- API 구현
- DB 스키마 변경
- 프론트 mock 화면의 실제 API 연동
- SMS/Email 발송 연동
- 서버 기동, Swagger, 브라우저 확인

## 7. 다음 작업 후보

1. `feat/account-recovery-api`: 서버 저장 토큰 기반 아이디 찾기/비밀번호 재설정 API 구현
2. `feat/account-recovery-frontend`: `/find-id`, `/reset-password` 실제 API 연동
3. `feat/account-recovery-verification`: SMS/Email 인증번호 또는 발송 채널 연동 검토
