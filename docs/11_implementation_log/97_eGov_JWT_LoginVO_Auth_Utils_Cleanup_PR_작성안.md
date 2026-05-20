# eGovFrame JWT/LoginVO 인증 보조 유틸 정리 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/egov-jwt-util-cleanup` |
| base 브랜치 | `dev` |
| 작업 트리 | `EgovJwtTokenUtil`과 `LoginVO` 기반 인증 보조 유틸 정리 |
| 관련 전체 체크리스트 | MVP 이후 고도화 후보의 잔여 eGovFrame 샘플/보류 항목 재점검 |
| 관련 보류 항목 | `DC-002`, `DC-003` |
| 정적 확인 | `rg`, `git diff --check` 확인 |
| 빌드/테스트 | 이번 요청 기준상 실행하지 않음. 사용자 직접 수행 필요 |
| 실행/API 확인 | 사용자가 Swagger에서 대표 인증 흐름 확인 필요 |

## PR 제목

```text
refactor: eGovFrame LoginVO 인증 보조 유틸 정리
```

## PR 본문

```markdown
## 개요

`JwtAuthenticationFilter`의 레거시 `LoginVO` fallback 제거 이후 남은 `EgovJwtTokenUtil`과 `LoginVO` 기반 인증 보조 코드를 정리합니다.

보건소 Auth/Member 인증 흐름은 `HealthcenterJwtTokenProvider`와 `MemberPrincipal` 기준으로 유지합니다. 전자정부프레임워크 Simple Backend Template 기반 구조, Maven, MyBatis, `SecurityConfig`, `JwtAuthenticationFilter` 골격은 유지하고, 보건소 MVP 인증 경로와 충돌하는 레거시 인증 보조 묶음만 제거합니다.

## 변경 내용

- `EgovJwtTokenUtil` 제거
- `EgovJwtTokenUtilTest` 제거
- `LoginVO` 제거
- `EgovUserDetailsHelper` 제거
- `AuthenticInterceptor` 제거
- `EgovUserDetailsService`와 구현체 제거
- `CustomAuthenticationPrincipalResolver` 제거
- `WebMvcConfig`의 `LoginVO` principal resolver 등록 제거
- 보류 정리 목록의 `DC-002`, `DC-003` 상태 갱신
- 전체 체크리스트에 이번 정리 상태 반영

## 검증

- [x] `rg`로 제거 대상 코드 참조 제거 확인
- [x] `git diff --check`
- [ ] `mvn.cmd -q -DskipTests compile`
- [ ] `mvn.cmd -q test-compile`
- [ ] Swagger 대표 인증 흐름 확인

## 미검증 사유

- 이번 작업 요청에서 실제 개발 환경이 아니므로 빌드 및 실행하지 말고 테스트 문서로 남기도록 지정했습니다.
- 서버 기동, Docker 실행, Swagger 런타임 호출은 프로젝트 운영 기준상 사용자가 직접 확인합니다.

## Swagger 대표 확인

현재 사용자 조회:

```http
GET /api/members/me
Authorization: Bearer {accessToken}
```

기대 응답:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "...",
    "name": "...",
    "role": "..."
  },
  "error": null
}
```

## 추가 테스트 체크리스트

- [ ] Happy: `POST /api/auth/login`으로 받은 access token으로 `GET /api/members/me` 성공
- [ ] Happy: STAFF access token으로 `GET /api/queues` 접근 가능
- [ ] Happy: ADMIN access token으로 `GET /api/dashboard/summary` 접근 가능
- [ ] Edge: `Authorization` 헤더가 없는 상태에서 보호 API 호출 시 인증 실패
- [ ] Bad: 임의 문자열 토큰으로 보호 API 호출 시 인증 실패
- [ ] Bad: 레거시 `LoginVO` 기반 토큰이 더 이상 인증 principal을 만들지 않는지 확인

## 후속 작업

- Spring Security filter 단계 401/403 응답을 공통 오류 응답과 맞출지 검토
- eGovFrame 공통 패키지의 다른 미사용 샘플 유틸이 남아 있는지 별도 브랜치에서 점검
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 보류 정리 목록에 `DC-002`, `DC-003` 정리 완료 반영 확인

## 커밋 메시지 초안

제목:

```text
refactor: eGovFrame LoginVO 인증 보조 유틸 정리
```

본문:

```text
- EgovJwtTokenUtil과 관련 테스트 제거
- LoginVO 기반 인증 helper, interceptor, principal resolver 제거
- WebMvcConfig의 레거시 principal resolver 등록 제거
- 보건소 Auth/Member 인증 흐름은 MemberPrincipal 기준으로 유지
- 보류 정리 목록과 전체 체크리스트에 DC-002/DC-003 정리 결과 반영
```
