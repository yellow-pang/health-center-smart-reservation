# eGovFrame 레거시 인증 샘플 비노출 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/auth-member-domain` |
| base 브랜치 | `main` 예정 |
| 작업 트리 | SecurityConfig, 레거시 샘플 Controller, 문서 변경 있음 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | `mvn -q -DskipTests compile` 성공 |
| 테스트 확인 | `mvn -q test-compile` 성공 |
| API/Swagger 확인 | 사용자가 직접 확인할 명령 작성 |
| GitNexus 확인 | MCP 리소스 없음, CLI 모듈 없음으로 실패, `rg` 대체 확인 |

## PR 제목

```text
refactor: eGovFrame 레거시 인증 샘플 비노출 처리
```

## PR 본문

```markdown
## 개요

보건소 Auth/Member API 구현 이후에도 남아 있는 eGovFrame 로그인, SNS 로그인, 관리자 샘플 API가 MVP API처럼 노출되지 않도록 정리했습니다.

이번 PR에서는 샘플 코드를 삭제하지 않고 Swagger 비노출과 공개 인증 예외 제거만 수행했습니다.

## 변경 내용

- `/login/**` 공개 인증 예외 제거
- `/auth/login-jwt`, `/auth/logout` 공개 인증 예외 제거
- `EgovLoginApiController` Swagger 비노출 처리
- `SnsLoginApiController` Swagger 비노출 처리
- `EgovSiteManagerApiController` Swagger 비노출 처리
- 샘플 보류/비노출 기준과 사용자 확인 방법 문서화

## 검증

- [x] `rg` 기반 레거시 로그인/SNS/관리자 샘플 노출 경로 확인
- [x] `git diff --check`
- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [ ] 사용자 직접 Swagger UI에서 레거시 Controller 비노출 확인
- [ ] 사용자 직접 신규 `/api/auth/**` 확인

## 미검증 사유

- 서버 기동, API 런타임 호출, Swagger 브라우저 확인은 사용자 직접 수행 기준에 따라 에이전트가 실행하지 않았습니다.
- GitNexus MCP 리소스가 현재 세션에 노출되지 않았고, CLI도 로컬 모듈을 찾지 못해 detect_changes는 수행하지 못했습니다.

## 후속 작업

- 사용자 직접 Swagger UI 비노출 확인 결과 반영
- SNS/레거시 로그인 샘플 완전 삭제 여부 별도 판단
- Reservation Context 객체 권한 정책 구현
```

## 변경 파일 요약

| 파일 | 내용 |
|---|---|
| `backend/src/main/java/egovframework/com/security/SecurityConfig.java` | 레거시 로그인/SNS 공개 인증 예외 제거 |
| `backend/src/main/java/egovframework/let/uat/uia/web/EgovLoginApiController.java` | Swagger 비노출 처리 |
| `backend/src/main/java/egovframework/com/sns/SnsLoginApiController.java` | Swagger 비노출 처리 |
| `backend/src/main/java/egovframework/let/uat/esm/web/EgovSiteManagerApiController.java` | Swagger 비노출 처리 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 샘플 정리/Auth 진행 상태 갱신 |
| `docs/11_implementation_log/16_eGovFrame_레거시_인증_샘플_비노출_기록.md` | 구현 기록 추가 |
| `docs/11_implementation_log/17_eGovFrame_레거시_인증_샘플_비노출_PR_작성안.md` | PR 작성안 추가 |

## 커밋 메시지 초안

```text
refactor: eGovFrame 레거시 인증 샘플 비노출 처리

- 레거시 로그인과 SNS 샘플 공개 인증 예외 제거
- eGovFrame 로그인/SNS/관리자 샘플 Controller를 Swagger에서 숨김
- 신규 Auth Member API 기준으로 인증 노출 범위 정리
```
