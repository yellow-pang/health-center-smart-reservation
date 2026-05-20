# JwtAuthenticationFilter 레거시 LoginVO fallback 정리 기록

## 1. 작업 목표

- `JwtAuthenticationFilter`에서 신규 보건소 Auth/Member 토큰 검증 실패 시 레거시 `LoginVO` 토큰으로 다시 인증하는 fallback을 제거한다.
- 보건소 API 인증 principal은 `MemberPrincipal` 기준으로 단일화한다.
- `EgovJwtTokenUtil`, `LoginVO`, eGovFrame 사용자 인증 유틸 자체 제거는 다음 작은 브랜치로 분리한다.

## 2. 관련 항목 확인

전체 체크리스트 기준:

- `docs/13_schedule/02_전체_작업_체크리스트.md`
- `12.5 MVP 이후 고도화 후보`의 잔여 eGovFrame 샘플/보류 항목 재점검

보류 정리 목록 기준:

- `DC-001`: `JwtAuthenticationFilter`의 레거시 `LoginVO` fallback
- `DC-002`: `EgovJwtTokenUtil`
- `DC-003`: `LoginVO`와 eGovFrame 사용자 인증 유틸

## 3. 작업 범위

포함한다.

- [x] `JwtAuthenticationFilter`의 `EgovJwtTokenUtil` 의존성 제거
- [x] `JwtAuthenticationFilter`의 `LoginVO` fallback 제거
- [x] 신규 토큰 검증 실패 시 인증 객체를 만들지 않도록 유지
- [x] `JwtAuthenticationFilterTest`를 `MemberPrincipal` 기준으로 수정
- [x] 보류 정리 목록과 전체 체크리스트 갱신
- [x] PR 작성안 작성

제외한다.

- [ ] `EgovJwtTokenUtil` 삭제
- [ ] `EgovJwtTokenUtilTest` 삭제 또는 전환
- [ ] `LoginVO` 삭제
- [ ] `EgovUserDetailsHelper`, `AuthenticInterceptor`, `CustomAuthenticationPrincipalResolver` 정리
- [ ] Spring Security filter 단계 401/403 공통 응답 통합

## 4. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `backend/src/main/java/egovframework/com/jwt/JwtAuthenticationFilter.java` | `LoginVO` import, `EgovJwtTokenUtil` 주입, `setLegacyAuthentication`, `isAdmin` 제거 |
| `backend/src/test/java/egovframework/com/jwt/JwtAuthenticationFilterTest.java` | 레거시 `LoginVO` 토큰 테스트를 `HealthcenterJwtTokenProvider`와 `MemberPrincipal` 기준으로 변경 |
| `docs/14_deferred_cleanup/01_보류_정리_목록.md` | `DC-001` 정리 완료, `DC-002` 후속 브랜치 조건 갱신 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 잔여 eGovFrame 보류 항목 중 JWT filter fallback 정리 진행 상태 반영 |

## 5. 설계 판단

- 레거시 토큰 파싱 실패를 신규 토큰 파싱 실패와 섞지 않도록 인증 경로를 단순화했다.
- 인증 필터는 `HealthcenterJwtTokenProvider`가 만든 `MemberPrincipal`만 SecurityContext에 저장한다.
- 기존 eGovFrame 공통 골격과 직접 연결된 `LoginVO`, `EgovJwtTokenUtil` 삭제는 영향 범위를 따로 확인해야 하므로 이번 브랜치에서 제외했다.
- 잘못된 토큰이 들어오면 기존처럼 SecurityContext를 비워 둔다. 실제 401 응답 처리는 Spring Security 인증 엔트리포인트 흐름에 맡긴다.

## 6. 검증 체크리스트

- [x] `rg`로 `JwtAuthenticationFilter`와 해당 테스트에 `LoginVO`, `EgovJwtTokenUtil`, `setLegacyAuthentication` 참조가 남지 않았는지 확인
- [x] `git diff --check`로 변경 파일 공백 오류 확인
- [ ] `mvn.cmd -q -DskipTests compile`
- [ ] `mvn.cmd -q test-compile`
- [ ] Swagger 인증 대표 흐름 확인

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
- 레거시 `LoginVO` 기반 토큰 또는 잘못된 토큰이면 인증 객체가 만들어지지 않고 인증 실패 흐름으로 처리된다.

추가 Happy/Edge/Bad 케이스는 PR 문서 체크리스트에 남긴다.

## 8. 남은 위험과 후속 작업

- `EgovJwtTokenUtil`과 `EgovJwtTokenUtilTest`는 이번 브랜치에서 유지했다. 다음 브랜치에서 실제 사용처가 테스트뿐인지 다시 확인한 뒤 삭제 또는 문서상 유지 후보로 결정한다.
- `LoginVO`, `EgovUserDetailsHelper`, `AuthenticInterceptor`, `CustomAuthenticationPrincipalResolver`는 eGovFrame 공통 골격과 연결되어 있으므로 별도 브랜치에서 정리 범위를 다시 판단한다.
- 기존 운영 환경에서 레거시 eGovFrame JWT를 계속 사용하던 클라이언트가 있다면 이번 변경으로 인증되지 않는다. 현재 프로젝트 방향은 신규 Auth/Member 토큰만 사용하는 것으로 정리했다.

## 9. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] PR 문서 작성
- [x] 보류 정리 목록 갱신
- [x] 전체 체크리스트 갱신
- [x] 정적 확인 결과 기록
- [x] 미실행 검증과 사용자 확인 방법 기록
- [x] 커밋 메시지 정리
