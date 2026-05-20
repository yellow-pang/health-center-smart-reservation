# JwtAuthenticationFilter 레거시 LoginVO fallback 정리 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/remove-legacy-auth` |
| base 브랜치 | `dev` |
| 작업 트리 | `JwtAuthenticationFilter` 레거시 `LoginVO` fallback 정리 |
| 관련 전체 체크리스트 | MVP 이후 고도화 후보의 잔여 eGovFrame 샘플/보류 항목 재점검 |
| 관련 보류 항목 | `DC-001` |
| 정적 확인 | `rg`, `git diff --check` 확인 |
| 빌드/테스트 | 이번 요청 기준상 실행하지 않음. 사용자 직접 수행 필요 |
| 실행/API 확인 | 사용자가 Swagger에서 대표 인증 흐름 확인 필요 |

## PR 제목

```text
refactor: JWT 필터의 레거시 LoginVO fallback 제거
```

## PR 본문

```markdown
## 개요

`JwtAuthenticationFilter`의 인증 경로를 보건소 Auth/Member 토큰 기준으로 단일화합니다.

기존에는 신규 `HealthcenterJwtTokenProvider` 토큰 파싱에 실패하면 `EgovJwtTokenUtil`로 레거시 `LoginVO` 토큰을 다시 파싱해 인증 객체를 만들었습니다. 이번 PR에서는 해당 fallback을 제거하고, 필터가 `MemberPrincipal`만 SecurityContext에 저장하도록 정리합니다.

`EgovJwtTokenUtil`, `LoginVO`, eGovFrame 사용자 인증 유틸 자체 삭제는 영향 범위가 다르므로 다음 작은 브랜치로 분리합니다.

## 변경 내용

- `JwtAuthenticationFilter`에서 `EgovJwtTokenUtil` 의존성 제거
- `JwtAuthenticationFilter`에서 `LoginVO` fallback 메서드 제거
- 신규 토큰 검증 실패 시 인증 객체를 생성하지 않도록 유지
- `JwtAuthenticationFilterTest`를 `MemberPrincipal` 기준으로 변경
- 보류 정리 목록의 `DC-001` 상태 갱신
- 전체 체크리스트에 이번 정리 상태 반영

## 검증

- [x] `rg`로 변경 대상의 레거시 fallback 참조 제거 확인
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
- [ ] Edge: `Authorization` 헤더가 없는 상태에서 보호 API 호출 시 인증 실패
- [ ] Bad: 임의 문자열 토큰으로 보호 API 호출 시 인증 실패
- [ ] Bad: 레거시 `LoginVO` 기반 토큰이 더 이상 인증 principal을 만들지 않는지 확인
- [ ] 권한: STAFF/ADMIN 보호 API가 신규 role claim 기반 권한으로 통과하는지 확인

## 후속 작업

- `EgovJwtTokenUtil`과 `EgovJwtTokenUtilTest` 사용처 재점검 후 삭제 여부 결정
- `LoginVO`, `EgovUserDetailsHelper`, `AuthenticInterceptor`, `CustomAuthenticationPrincipalResolver` 유지/정리 범위 결정
- Spring Security filter 단계 401/403 응답을 공통 오류 응답과 맞출지 검토
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] `DC-002`, `DC-003` 후속 브랜치 생성 여부 결정

## 커밋 메시지 초안

제목:

```text
refactor: JWT 필터의 레거시 LoginVO fallback 제거
```

본문:

```text
- JwtAuthenticationFilter에서 EgovJwtTokenUtil 기반 fallback 제거
- 인증 principal을 Healthcenter MemberPrincipal 기준으로 단일화
- JwtAuthenticationFilterTest를 신규 토큰 provider 기준으로 수정
- 보류 정리 목록과 전체 체크리스트에 DC-001 정리 결과 반영
- EgovJwtTokenUtil/LoginVO 자체 정리는 후속 브랜치로 분리
```
