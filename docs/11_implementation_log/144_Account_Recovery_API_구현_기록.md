# Account Recovery API 구현 기록

## 1. 작업 목표

- `/find-id`, `/reset-password` 프론트 mock 화면을 실제 API로 연결하기 전에 백엔드 계정 복구 API 기반을 먼저 만든다.
- 아이디 찾기는 이름과 휴대폰 번호로 가입 이메일을 마스킹해 반환한다.
- 비밀번호 찾기는 서버 저장 토큰 기반 2단계 재설정 방식으로 구현한다.
- 실제 SMS/Email 발송은 이번 브랜치에서 제외하고, 개발/포트폴리오 확인용 토큰 응답을 설정으로 제어한다.

## 2. 작업 범위

포함:

- [x] `POST /api/auth/find-id` 추가
- [x] `POST /api/auth/password-reset/request` 추가
- [x] `POST /api/auth/password-reset/confirm` 추가
- [x] `password_reset_tokens` 테이블 추가
- [x] 계정 복구 DTO 추가
- [x] `AccountRecoveryController`, `AccountRecoveryService` 추가
- [x] `MemberMapper`와 MyBatis XML query 추가
- [x] 비밀번호 재설정 성공 시 회원 refresh token 전체 폐기
- [x] Security whitelist에 공개 계정 복구 API 추가
- [x] API/DB/체크리스트/후속작업 문서 갱신

제외:

- [ ] `/find-id`, `/reset-password` 프론트 실제 API 연동
- [ ] SMS/Email 발송 채널 연동
- [ ] rate limit 또는 IP 기반 남용 방지
- [ ] 서버 기동, Swagger, 브라우저 런타임 확인
- [ ] Maven build/test 실행

## 3. 변경 내용

| 파일 | 변경 내용 |
|---|---|
| `backend/src/main/java/egovframework/healthcenter/member/api/AccountRecoveryController.java` | 계정 복구 공개 API 3종 추가 |
| `backend/src/main/java/egovframework/healthcenter/member/application/AccountRecoveryService.java` | 아이디 찾기, 재설정 토큰 발급, 비밀번호 변경 정책 구현 |
| `backend/src/main/java/egovframework/healthcenter/member/dto/*` | 계정 복구 request/response DTO 추가 |
| `backend/src/main/java/egovframework/healthcenter/member/mapper/MemberMapper.java` | 계정 복구용 조회/수정 mapper method 추가 |
| `backend/src/main/resources/egovframework/mapper/healthcenter/member/Member_SQL_postgresql.xml` | 이름/휴대폰, 이메일/휴대폰, 토큰 조회와 비밀번호 변경 SQL 추가 |
| `backend/src/main/resources/db/postgresql/schema.sql` | `password_reset_tokens` 테이블과 인덱스 추가 |
| `backend/src/main/resources/application.properties` | 계정 복구 토큰 만료 시간, 개발용 토큰 노출 설정 추가 |
| `.env.example` | 계정 복구 환경변수 예시 추가 |
| `backend/src/main/java/egovframework/com/security/SecurityConfig.java` | 계정 복구 공개 API whitelist 추가 |
| `backend/src/main/java/egovframework/healthcenter/common/exception/ErrorCode.java` | 비밀번호 재설정 오류 코드 추가 |
| `docs/04_api/01_API_명세서.md` | Account Recovery API 명세 추가 |
| `docs/03_database/01_ERD_및_테이블_명세서.md` | `password_reset_tokens` 명세 추가 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 계정 복구 API 진행 상태 갱신 |
| `docs/14_deferred_cleanup/*` | 후속작업 상태 갱신 |

## 4. 구현 정책

아이디 찾기:

- `name + phone`으로 활성 회원을 조회한다.
- 휴대폰 번호는 하이픈 유무와 관계없이 숫자 기준으로 비교한다.
- 조회 성공 시 이메일 local-part 앞 1~2자만 남기고 마스킹한다.
- 조회 실패 시 `found=false`, `maskedEmail=null`을 반환한다.

비밀번호 재설정 요청:

- `email + phone`으로 활성 회원을 조회한다.
- 회원이 없더라도 계정 존재 여부 노출을 줄이기 위해 `accepted=true`를 반환한다.
- 회원이 있으면 기존 미사용 재설정 토큰을 사용 처리하고 새 토큰을 발급한다.
- 토큰 원문은 저장하지 않고 SHA-256 해시만 저장한다.
- 기본 만료 시간은 `ACCOUNT_RECOVERY_RESET_TOKEN_EXPIRE=1800`초이다.
- `ACCOUNT_RECOVERY_EXPOSE_DEVELOPMENT_TOKEN=true`일 때만 응답에 `developmentResetToken`을 포함한다.

비밀번호 재설정 완료:

- 사용하지 않았고 만료되지 않은 토큰만 허용한다.
- 새 비밀번호는 8자 이상이며 확인값과 일치해야 한다.
- 기존 eGovFrame 방식인 `EgovFileScrty.encryptPassword(rawPassword, email)`로 암호화한다.
- 성공 시 사용한 재설정 토큰을 사용 처리하고 회원의 refresh token을 모두 폐기한다.

## 5. 검증 결과

| 검증 | 결과 |
|---|---|
| `git diff --check` | 통과. CRLF 변환 경고만 표시 |
| Maven compile/test | 미수행. 사용자 요청에 따라 학습 PC에서는 빌드/테스트를 실행하지 않음 |
| 서버 기동 | 미수행 |
| Swagger 확인 | 미수행 |
| 브라우저 확인 | 미수행 |

## 6. 남은 위험과 후속 작업

- 운영 환경에서는 `ACCOUNT_RECOVERY_EXPOSE_DEVELOPMENT_TOKEN=false`로 설정해야 한다.
- 실제 운영 흐름에는 SMS/Email 발송 또는 별도 본인확인 채널이 필요하다.
- 계정 복구 API는 공개 API이므로 rate limit, captcha, IP/계정 기준 시도 제한을 후속으로 검토한다.
- 프론트 `/find-id`, `/reset-password`는 아직 mock 화면이므로 실제 API 연동이 필요하다.
- 재설정 토큰 정책에 대한 단위 테스트 또는 통합 테스트가 필요할 수 있다.

## 7. 커밋 메시지 초안

```text
feat: 계정 찾기 백엔드 API 추가

- 아이디 찾기 이메일 마스킹 API 추가
- 서버 저장 토큰 기반 비밀번호 재설정 API 추가
- password_reset_tokens 테이블과 MyBatis 매핑 추가
- 계정 복구 API 명세와 체크리스트 갱신
```
