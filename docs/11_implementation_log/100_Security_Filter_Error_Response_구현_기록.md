# Security Filter 401/403 공통 오류 응답 구현 기록

## 1. 작업 목표

- Spring Security filter 단계에서 직접 반환되는 401/403 응답을 신규 보건소 API의 공통 오류 응답 형식으로 맞춘다.
- 인증 실패는 `AUTH_REQUIRED`, 권한 부족은 `FORBIDDEN` 오류 코드로 응답한다.
- URL 권한 규칙과 JWT 토큰 검증 로직은 변경하지 않는다.

## 2. 작업 범위

- [x] 현재 브랜치와 작업 트리 확인
- [x] `JwtAuthenticationEntryPoint` 레거시 `ResultVO` 응답 확인
- [x] 401 인증 실패 응답을 `ApiResponse.failure(ErrorCode.AUTH_REQUIRED)`로 변경
- [x] 403 권한 부족 응답을 처리하는 `JwtAccessDeniedHandler` 추가
- [x] `SecurityConfig`에 access denied handler 연결
- [x] 보류 목록과 전체 체크리스트 갱신
- [x] PR 문서 초안 작성

제외한다.

- [ ] URL 권한 매핑 변경
- [ ] JWT token parsing/claims 정책 변경
- [ ] 객체 단위 권한 정책 보강
- [ ] 서비스/정책 계층의 `BusinessException` 점진 교체

## 3. 영향 범위 확인

GitNexus 상태:

- `gitnexus status` 결과 인덱스가 stale 상태였다.
- 현재 세션의 GitNexus MCP impact 도구는 노출되어 있지 않았다.
- 직전 문서 브랜치에서 `npx gitnexus analyze`, `gitnexus analyze` 모두 `Not inside a git repository`로 실패했으므로 이번 브랜치에서는 `rg`, 소스 확인, Maven compile/test-compile로 보완했다.

Blast radius:

| 대상 | 영향 | 위험도 |
|---|---|---|
| `JwtAuthenticationEntryPoint` | 보호 API 인증 실패 401 응답 본문이 레거시 `ResultVO`에서 `ApiResponse`로 변경 | MEDIUM |
| `JwtAccessDeniedHandler` | 역할 부족 등 인가 실패 403 응답 본문을 `ApiResponse`로 통일 | MEDIUM |
| `SecurityConfig` | access denied handler 연결 추가 | MEDIUM |

직접 영향을 받는 흐름:

- 인증 없이 보호 API 호출
- CITIZEN/GUARDIAN 사용자가 STAFF/ADMIN API 호출
- STAFF 사용자가 ADMIN API 호출

유지한 것:

- HTTP status 401/403
- URL별 역할 접근 규칙
- JWT 토큰 추출과 principal 생성 방식
- 성공 응답 구조

## 4. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `backend/src/main/java/egovframework/com/jwt/JwtAuthenticationEntryPoint.java` | `ResultVO` 기반 401 응답을 `ApiResponse.failure(ErrorCode.AUTH_REQUIRED)`로 교체 |
| `backend/src/main/java/egovframework/com/jwt/JwtAccessDeniedHandler.java` | 403 권한 부족 응답을 `ApiResponse.failure(ErrorCode.FORBIDDEN)`로 반환하는 핸들러 추가 |
| `backend/src/main/java/egovframework/com/security/SecurityConfig.java` | `exceptionHandling`에 `accessDeniedHandler` 연결 |
| `docs/14_deferred_cleanup/01_보류_정리_목록.md` | DC-006 후속 범위에서 filter 401/403 공통 응답 정리 완료 반영 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | Security filter 401/403 공통 오류 응답 정리 항목 추가 |

## 5. 검증 체크리스트

- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `git diff --check`
- [ ] Swagger 런타임 대표 401 응답 확인
- [ ] Swagger 런타임 대표 403 응답 확인

## 6. Swagger 확인 안내

서버 기동과 Swagger 확인은 사용자가 직접 수행한다.

대표 401 예시:

```text
GET /api/reservations/me
Authorization 헤더 없음
Expected: 401
```

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

추가 확인 후보:

- CITIZEN 토큰으로 `GET /api/queues` 호출: `403 FORBIDDEN`
- STAFF 토큰으로 `GET /api/dashboard/summary` 호출: `403 FORBIDDEN`

## 7. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| Security filter 응답 정리 중 | 객체 단위 권한 정책 보강 | URL 역할 접근과 별도로 예약/방문/대기 데이터별 접근 조건을 다시 점검할 필요가 있다. | DC-007 후속 브랜치 후보로 유지 |
| Security filter 응답 정리 중 | `IllegalArgumentException`의 `BusinessException` 점진 교체 | 메시지 기반 오류 코드 매핑 의존을 줄이기 위함 | 후속 리팩터링 후보로 유지 |

## 8. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] 보류 목록 갱신
- [x] PR 문서 작성
- [x] 빌드와 테스트 컴파일 확인
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리

