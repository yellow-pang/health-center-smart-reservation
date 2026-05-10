# eGovFrame 레거시 인증 샘플 제거 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/egov-auth-sample-cleanup` |
| base 브랜치 | `main` 예정 |
| 작업 성격 | 신규 Auth/Member로 대체된 eGovFrame 인증 샘플 제거 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | `mvn -q -DskipTests compile` 성공 |
| 테스트 확인 | `mvn -q test-compile` 성공 |
| API/Swagger 확인 | 사용자가 직접 확인할 명령 작성 |
| GitNexus 확인 | MCP 리소스 없음, CLI 모듈 없음으로 실패, `rg` 대체 확인 |

## PR 제목

```text
refactor: eGovFrame 레거시 인증 샘플 제거
```

## PR 본문

```markdown
## 개요

신규 보건소 Auth/Member API로 대체된 eGovFrame 로그인, SNS 로그인, 관리자 인증 샘플 기능을 제거했습니다.

이번 PR은 전자정부프레임워크 기반 구조를 제거하는 작업이 아닙니다. Maven, MyBatis, eGovFrame 공통 설정, Security/JWT 필터 골격은 유지하고, 보건소 MVP와 무관한 샘플 API 묶음만 정리했습니다.

## 변경 내용

- eGovFrame 로그인 샘플 Controller, Service, DAO 제거
- SNS 로그인 샘플 Controller, Utils, VO 제거
- 관리자 인증 샘플 Controller, Service, DAO 제거
- 레거시 로그인 API 테스트 제거
- 제거된 `/auth/*` 샘플 API 인터셉터 등록 정리
- SNS 샘플 properties 제거
- 구현 기록과 전체 체크리스트 갱신

## 유지한 eGovFrame 기반

- `egovframework.com.config`
- Maven 구조
- MyBatis 기반 Mapper 구조
- Spring Security/JWT 필터 골격
- `LoginVO`, `EgovJwtTokenUtil`, `JwtAuthenticationFilter`
- eGovFrame 공통 보안/유틸 클래스

## 검증

- [x] `rg` 기반 레거시 로그인/SNS/관리자 샘플 참조 확인
- [x] `git diff --check`
- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [ ] 사용자 직접 Swagger UI에서 레거시 Controller 미노출 확인
- [ ] 사용자 직접 신규 `/api/auth/**` 확인

## 미검증 사유

- 서버 기동, API 런타임 호출, Swagger 브라우저 확인은 사용자 직접 수행 기준에 따라 에이전트가 실행하지 않았습니다.
- GitNexus MCP 리소스가 현재 세션에 노출되지 않았고, CLI도 로컬 모듈을 찾지 못해 detect_changes는 수행하지 못했습니다.

## 후속 작업

- 사용자 직접 Auth API와 Swagger UI 확인 결과 반영
- `JwtAuthenticationFilter`의 레거시 `LoginVO` fallback 유지 여부 별도 판단
- `EgovJwtTokenUtil`, `LoginVO`, eGov 보안 유틸 사용 범위 재점검
```

## 변경 파일 요약

| 파일 | 내용 |
|---|---|
| `backend/src/main/java/egovframework/let/uat/uia/**` | eGovFrame 로그인 샘플 제거 |
| `backend/src/main/java/egovframework/com/sns/**` | SNS 로그인 샘플 제거 |
| `backend/src/main/java/egovframework/let/uat/esm/**` | 관리자 인증 샘플 제거 |
| `backend/src/test/java/egovframework/let/uat/uia/web/EgovLoginApiControllerTest.java` | 레거시 로그인 API 테스트 제거 |
| `backend/src/main/java/egovframework/com/config/EgovConfigWebDispatcherServlet.java` | 레거시 `/auth/*` 인터셉터 등록 제거 |
| `backend/src/main/resources/application.properties` | SNS 샘플 설정 제거 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 샘플 정리 진행 상태 갱신 |
| `docs/11_implementation_log/19_eGovFrame_레거시_인증_샘플_제거_기록.md` | 구현 기록 추가 |
| `docs/11_implementation_log/20_eGovFrame_레거시_인증_샘플_제거_PR_작성안.md` | PR 작성안 추가 |

## 커밋 메시지 초안

```text
refactor: eGovFrame 레거시 인증 샘플 제거

- eGovFrame 로그인/SNS/관리자 샘플 API 제거
- 레거시 로그인 Service/DAO와 SNS 유틸 제거
- SNS 샘플 설정과 레거시 로그인 테스트 정리
- 전자정부 공통 보안 골격은 유지
```
