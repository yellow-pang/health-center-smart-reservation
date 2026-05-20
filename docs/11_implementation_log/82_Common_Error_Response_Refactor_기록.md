# 공통 예외/오류 응답 일관화 기록

## 1. 작업 목표

- Controller별 반복 `try/catch`와 문자열 기반 오류 응답 생성을 줄인다.
- 실패 응답은 `success + data + error` 형식을 유지하면서 `code`, `message`, `traceId`를 일관되게 제공한다.
- 오류 원인 추적은 서버 로그와 `traceId`로 보존하고, 클라이언트 응답에는 안전한 사용자 메시지를 유지한다.

## 2. 영향 범위 확인

GitNexus MCP 도구는 현재 세션에 노출되어 있지 않았다.

GitNexus CLI 기준:

- 사용자 로컬 환경에서는 프로젝트에 설치된 GitNexus CLI를 직접 실행한다.
- 관계 파악이 필요한 경우 `gitnexus analyze`를 우선 실행한다.
- 현재 설치 버전에는 `detect-change` 계열 명령이 없으므로 GitNexus 기반 변경 감지는 수행 대상에서 제외한다.
- 정확한 MCP 그래프 기반 impact 분석은 현재 세션에서 수행하지 못했다.

대체 확인:

- `rg`로 `ApiResponse.failure`, `try/catch`, `IllegalArgumentException`, Controller 참조 범위를 확인했다.
- 변경 대상은 `egovframework.healthcenter` 하위 신규 보건소 API Controller와 공통 응답/예외 모듈로 제한했다.

Blast radius:

| 구분 | 영향 |
|---|---|
| 직접 영향 | Auth, Member, Office, Reservation, Visit, Queue, Dashboard Controller 실패 응답 |
| 유지한 계약 | `success`, `data`, `error.code`, `error.message` |
| 추가한 계약 | `error.traceId` 선택 필드 |
| 위험도 | MEDIUM. 전체 API 오류 응답에 영향이 있으나 성공 응답과 URL/API 기능 계약은 유지 |

## 3. 작업 범위

포함한다.

- [x] `ErrorCode` enum 추가
- [x] `BusinessException` 추가
- [x] `GlobalExceptionHandler` 추가
- [x] 인증 Principal 추출 helper 추가
- [x] Controller 반복 `try/catch` 제거
- [x] `ApiError`에 `traceId` 추가
- [x] 프론트 API error type에 `traceId` 선택 필드 추가

제외한다.

- [ ] 서비스/정책 계층의 모든 `IllegalArgumentException`을 도메인 예외로 전면 교체
- [ ] 에러 메시지 다국어화
- [ ] 프론트 화면에 traceId 표시
- [ ] Spring Security filter 단계 401/403 응답 통합

## 4. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `backend/src/main/java/egovframework/healthcenter/common/exception/ErrorCode.java` | 공통/도메인별 오류 코드, HTTP status, 기본 메시지, 기존 예외 메시지 매핑 추가 |
| `backend/src/main/java/egovframework/healthcenter/common/exception/BusinessException.java` | 명시적 비즈니스 예외 타입 추가 |
| `backend/src/main/java/egovframework/healthcenter/common/exception/GlobalExceptionHandler.java` | 공통 실패 응답 변환과 서버 로그 기록 추가 |
| `backend/src/main/java/egovframework/healthcenter/common/security/AuthenticatedPrincipal.java` | Controller의 인증 principal 추출 반복 제거 |
| `backend/src/main/java/egovframework/healthcenter/common/response/ApiError.java` | `traceId` 필드 추가 |
| `backend/src/main/java/egovframework/healthcenter/common/response/ApiResponse.java` | `ErrorCode` 기반 failure factory와 MDC traceId 주입 추가 |
| `backend/src/main/java/egovframework/healthcenter/*/api/*Controller.java` | 반복 `try/catch`, `resolveErrorCode`, `resolveStatus`, 수동 인증 실패 응답 제거 |
| `frontend/src/lib/api-client.ts` | `ApiErrorBody.traceId` 선택 필드 추가 |

## 5. 설계 판단

- 공통화 대상은 응답 변환과 오류 코드 매핑으로 제한했다.
- 서비스 계층의 예외를 한 번에 전부 교체하지 않고, 기존 메시지 기반 동작을 `ErrorCode.fromMessage` 한 곳으로 모았다.
- Controller는 성공 흐름만 표현하고 실패 응답은 `GlobalExceptionHandler`가 담당하게 했다.
- 원인 추적은 응답에 stack trace를 노출하지 않고 `traceId`와 서버 로그로 연결한다.

## 6. 검증 체크리스트

- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `npm.cmd run build`
- [x] Controller의 반복 `try/catch` 제거 확인
- [x] `frontend/next-env.d.ts` 생성 변경 원복
- [x] GitNexus `detect-change` 계열 명령 미제공 확인 및 대체 검증 기준 정리
- [x] Swagger 런타임 대표 오류 응답 확인

GitNexus 변경 범위 확인:

- 현재 설치된 GitNexus CLI 버전에는 `detect-change`, `detect-changes`, `detect_changes` 명령이 없다.
- 따라서 변경 범위 확인은 `rg`, `git diff`, Maven compile/test-compile, Next build로 보완했다.
- 인덱스 갱신과 관계 파악이 필요한 경우 사용자 로컬 환경에서 `gitnexus analyze`를 직접 실행한다.

## 7. 사용자 확인 안내

2026-05-20 사용자 확인 결과, 실제 Docker 실행 환경에서 Swagger 대표 오류 응답 확인을 완료했다.

서버 기동 후 Swagger에서 아래 오류 흐름을 1개 이상 확인한다.

대표 예시:

```text
POST /api/auth/login
Body: {"email":"wrong@test.com","password":"wrong"}
Expected: 401
```

기대 응답:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "AUTH_INVALID_CREDENTIALS",
    "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
    "traceId": "..."
  }
}
```

추가 확인 후보:

- 인증 없이 `GET /api/reservations/me` 호출: `AUTH_REQUIRED`
- 없는 예약 상세 조회: `RESERVATION_NOT_FOUND`
- 잘못된 대기 상태 변경: `QUEUE_INVALID_STATUS`

## 8. 남은 위험과 후속 작업

- 기존 서비스/정책의 `IllegalArgumentException` 메시지가 변경되면 `ErrorCode.fromMessage` 매핑도 함께 점검해야 한다.
- 다음 고도화 단계에서는 자주 발생하는 도메인 오류부터 `BusinessException(ErrorCode.X)`로 점진 교체하는 것이 좋다.
- Spring Security filter에서 직접 발생하는 인증/인가 실패 응답은 별도 브랜치에서 공통 응답으로 맞출 수 있다.

## 9. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] PR 문서 작성
- [x] 빌드와 테스트 컴파일 확인
- [x] 프론트 빌드 확인
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
