# 공통 예외/오류 응답 일관화 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/common-error-response` |
| base 브랜치 | `dev` |
| 작업 트리 | 공통 예외/오류 응답 리팩터링 진행 |
| GitNexus | 로컬 설치 CLI는 `gitnexus status/analyze` 직접 실행 기준. 현재 설치 버전은 `detect-change` 계열 명령 미제공. MCP impact 도구 미노출로 `rg` 기반 영향 범위 확인 |
| 정적 확인 | `mvn.cmd -q -DskipTests compile`, `mvn.cmd -q test-compile`, `npm.cmd run build` 통과 |
| 실행/API 확인 | 사용자가 Docker 실행 환경에서 Swagger 대표 오류 응답 확인 완료 |

## PR 제목

```text
refactor: 공통 예외 응답 처리 일관화
```

## PR 본문

```markdown
## 개요

Controller별로 반복되던 `try/catch`, 오류 코드 결정, 인증 실패 응답 생성을 공통 예외 처리 흐름으로 정리합니다.

응답 형식은 기존 `success + data + error` 계약을 유지하고, 오류 추적을 위해 `error.traceId`를 추가합니다. 클라이언트에는 안전한 메시지를 반환하고, 서버 로그에는 `traceId`, `errorCode`, 예외 정보를 남깁니다.

## 변경 내용

- `ErrorCode` enum 추가
- `BusinessException` 추가
- `GlobalExceptionHandler` 추가
- 인증 principal 추출 helper 추가
- `ApiError`에 `traceId` 필드 추가
- `ApiResponse.failure(ErrorCode)` factory 추가
- Auth/Member/Office/Reservation/Visit/Queue/Dashboard Controller의 반복 `try/catch` 제거
- Frontend API error type에 `traceId` 선택 필드 추가

## 검증

- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `npm.cmd run build`
- [x] Controller 반복 `try/catch` 제거 확인
- [x] Next build 생성 파일 원복
- [x] GitNexus `detect-change` 계열 명령 미제공 확인 및 대체 검증 기준 정리
- [x] Swagger 대표 오류 응답 확인

## 미검증 사유

- 서버 기동, Docker 실행, Swagger 런타임 호출은 프로젝트 운영 기준상 사용자가 직접 확인하며, 2026-05-20 사용자 확인 결과 대표 오류 응답 확인을 완료했습니다.
- GitNexus MCP impact 도구가 현재 세션에 없습니다. 사용자 로컬 환경에서는 프로젝트에 설치된 GitNexus CLI를 `gitnexus status`, `gitnexus analyze`, `gitnexus list`처럼 직접 실행합니다.
- 현재 설치 버전에는 `detect-change`, `detect-changes`, `detect_changes` 명령이 없으므로 해당 검증은 요구하지 않습니다. 변경 범위 확인은 `rg`, `git diff`, Maven, Next build로 보완했습니다.

## Swagger 대표 확인

로그인 실패:

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "wrong@test.com",
  "password": "wrong"
}
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

## 후속 작업

- 서비스/정책 계층의 주요 `IllegalArgumentException`을 `BusinessException(ErrorCode.X)`로 점진 교체
- Spring Security filter 단계 인증/인가 실패 응답은 `fix/security-filter-error-response` 브랜치에서 공통 응답으로 정리
- 프론트 오류 화면이나 관리자 디버그 영역에서 `traceId` 표시 여부 검토
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 후속 고도화 브랜치 생성 여부 결정

## 커밋 메시지 초안

제목:

```text
refactor: 공통 예외 응답 처리 일관화
```

본문:

```text
- ErrorCode, BusinessException, GlobalExceptionHandler 추가
- ApiError에 traceId 필드 추가
- Controller별 반복 try/catch와 수동 오류 응답 제거
- 인증 principal 추출 helper 추가
- 프론트 API 오류 타입에 traceId 선택 필드 반영
- Maven compile/test-compile과 Next build 확인
```
