# Security Filter 401/403 공통 오류 응답 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `fix/security-filter-error-response` |
| base 브랜치 | `dev` |
| 작업 트리 | Security filter 인증/인가 실패 응답 정리 진행 |
| GitNexus | `gitnexus status` 결과 stale. MCP impact 도구 미노출로 `rg`, 소스 확인, Maven compile/test-compile로 보완 |
| 정적 확인 | `mvn.cmd -q -DskipTests compile`, `mvn.cmd -q test-compile`, `git diff --check` 통과 |
| 실행/API 확인 | 사용자가 Docker 실행 환경에서 Swagger 대표 401/403 응답 확인 필요 |

## PR 제목

```text
fix: 인증 인가 실패 응답 형식 통일
```

## PR 본문

```markdown
## 개요

Spring Security filter 단계에서 직접 반환하던 인증/인가 실패 응답을 신규 보건소 API의 공통 응답 형식으로 통일합니다.

기존 `JwtAuthenticationEntryPoint`는 eGovFrame 샘플의 `ResultVO` 구조를 반환하고 있었기 때문에 Controller 이후의 공통 예외 응답과 형식이 달랐습니다. 이번 변경으로 인증 실패 401은 `AUTH_REQUIRED`, 권한 부족 403은 `FORBIDDEN` 코드로 `success + data + error` 형식을 반환합니다.

## 변경 내용

- `JwtAuthenticationEntryPoint`의 401 응답을 `ApiResponse.failure(ErrorCode.AUTH_REQUIRED)`로 변경
- `JwtAccessDeniedHandler` 추가
- `SecurityConfig`에 403 access denied handler 연결
- DC-006 보류 목록과 전체 체크리스트에 filter 단계 응답 정리 완료 반영

## 검증

- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `git diff --check`
- [ ] Swagger 대표 401 응답 확인
- [ ] Swagger 대표 403 응답 확인

## Swagger 대표 확인

401:

```http
GET /api/reservations/me
```

Authorization 헤더 없이 호출한다.

기대 응답:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "AUTH_REQUIRED",
    "message": "로그인이 필요합니다.",
    "traceId": "..."
  }
}
```

403:

```http
GET /api/queues
Authorization: Bearer {citizenAccessToken}
```

기대 응답:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "FORBIDDEN",
    "message": "요청을 처리할 권한이 없습니다.",
    "traceId": "..."
  }
}
```

## 미검증 사유

- 서버 기동, Docker 실행, Swagger 런타임 호출은 프로젝트 운영 기준상 사용자가 직접 확인합니다.
- GitNexus MCP impact 도구가 현재 세션에 없어 `rg`, 소스 확인, Maven compile/test-compile로 변경 범위를 보완했습니다.

## 후속 작업

- DC-007 객체 권한 정책 보강
- 서비스/정책 계층의 주요 `IllegalArgumentException`을 `BusinessException(ErrorCode.X)`로 점진 교체
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 다음 보류 항목 브랜치 생성 여부 결정

## 커밋 메시지 초안

제목:

```text
fix: 인증 인가 실패 응답 형식 통일
```

본문:

```text
- JwtAuthenticationEntryPoint의 401 응답을 ApiResponse 형식으로 변경
- JwtAccessDeniedHandler를 추가해 403 응답을 공통 오류 형식으로 반환
- SecurityConfig에 access denied handler 연결
- 보류 목록과 전체 체크리스트에 filter 단계 응답 정리 완료 반영
```

