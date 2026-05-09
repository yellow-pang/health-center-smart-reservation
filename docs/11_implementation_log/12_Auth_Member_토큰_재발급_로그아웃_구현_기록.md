# Auth/Member 토큰 재발급 및 로그아웃 구현 기록

## 1. 작업 목표

이번 단계의 목표는 로그인 기반 구현 위에 Refresh Token 기반 토큰 재발급과 로그아웃 흐름을 추가하는 것이다.

서버 기동, Docker 실행, API 런타임 호출, Swagger 브라우저 확인은 사용자가 직접 수행하는 기준에 맞춰 이번 기록에는 에이전트가 확인한 정적 검증과 사용자가 실행할 확인 명령을 분리해 둔다.

## 2. 작업 범위

### 포함

- [x] `POST /api/auth/reissue` 구현
- [x] `POST /api/auth/logout` 구현
- [x] Refresh Token 조회, 회전, 폐기 SQL 추가
- [x] 토큰 재발급/로그아웃 요청 DTO 추가
- [x] API 명세서에 재발급/로그아웃 요청과 응답 예시 추가
- [x] 전체 체크리스트 진행 상태 갱신
- [x] PR 문서 초안 작성

### 제외

- [x] Refresh Token 해시 저장 제외
- [x] 전체 세션 일괄 로그아웃 제외
- [x] 역할별 전체 API 접근 제한 적용 제외
- [x] 기존 eGovFrame 로그인/관리자/SNS 샘플 삭제 제외
- [x] 서버 기동, Docker 실행, API 런타임 호출, Swagger 브라우저 확인 제외
- [x] 실제 커밋, push, 배포 제외

## 3. 영향 확인

GitNexus MCP 리소스가 현재 세션에 노출되지 않았고, `npm.cmd exec -- gitnexus status`는 로컬 `node_modules`의 gitnexus CLI 모듈을 찾지 못해 실패했다.

대체 확인은 `rg`와 파일 직접 확인으로 수행했다.

| 대상 | 영향 범위 | 판단 |
|---|---|---|
| `AuthCommandService` | `AuthController`에서 호출 | LOW, Auth API 내부 변경 |
| `AuthController` | `/api/auth/*` 엔드포인트 노출 | LOW, 신규 endpoint 추가 |
| `MemberMapper` | Auth 서비스와 JWT 필터의 회원 조회 기반 | LOW, 기존 조회 메서드는 유지하고 Refresh Token 메서드만 추가 |
| `Member_SQL_postgresql.xml` | Member/RefreshToken MyBatis statement | LOW, 기존 statement 유지 후 신규 statement 추가 |
| `docs/04_api/01_API_명세서.md` | API 계약 문서 | LOW, 구현된 API 예시 반영 |

HIGH 또는 CRITICAL 위험은 확인되지 않았다.

## 4. 구현 내용

### API

| API | 상태 | 설명 |
|---|---|---|
| `POST /api/auth/reissue` | 구현 | 유효한 Refresh Token을 검증하고 기존 Refresh Token을 폐기한 뒤 Access Token과 새 Refresh Token을 발급 |
| `POST /api/auth/logout` | 구현 | 로그인한 사용자의 Refresh Token을 폐기 |

### Refresh Token 정책

- 재발급은 Refresh Token 회전 방식으로 처리한다.
- 재발급 성공 시 기존 Refresh Token은 `revoked = true`로 변경한다.
- 로그아웃은 현재 로그인한 사용자와 요청 Refresh Token이 일치할 때만 폐기한다.
- 만료되었거나 이미 폐기된 Refresh Token은 유효하지 않은 토큰으로 처리한다.
- Refresh Token 해시 저장은 아직 적용하지 않고 후속 보안 강화 작업으로 남긴다.

## 5. 변경 파일

| 파일 | 내용 |
|---|---|
| `backend/src/main/java/egovframework/healthcenter/member/dto/ReissueTokenRequest.java` | 토큰 재발급 요청 DTO 추가 |
| `backend/src/main/java/egovframework/healthcenter/member/dto/LogoutRequest.java` | 로그아웃 요청 DTO 추가 |
| `backend/src/main/java/egovframework/healthcenter/member/application/AuthCommandService.java` | 재발급, 로그아웃 서비스 로직 추가 |
| `backend/src/main/java/egovframework/healthcenter/member/api/AuthController.java` | `/api/auth/reissue`, `/api/auth/logout` API 추가 |
| `backend/src/main/java/egovframework/healthcenter/member/mapper/MemberMapper.java` | Refresh Token 조회/폐기 Mapper 메서드 추가 |
| `backend/src/main/resources/egovframework/mapper/healthcenter/member/Member_SQL_postgresql.xml` | Refresh Token 조회/폐기 SQL 추가 |
| `docs/04_api/01_API_명세서.md` | 토큰 재발급/로그아웃 예시 추가 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | Auth/Member 구현 진행 상태 갱신 |
| `docs/11_implementation_log/12_Auth_Member_토큰_재발급_로그아웃_구현_기록.md` | 이번 구현 기록 추가 |
| `docs/11_implementation_log/13_Auth_Member_토큰_재발급_로그아웃_PR_작성안.md` | PR 작성안 추가 |

## 6. 검증

- [x] `rg` 기반 Auth/Member 참조 확인
- [x] `git diff --check`
- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [ ] 사용자 직접 `POST /api/auth/reissue` 확인
- [ ] 사용자 직접 `POST /api/auth/logout` 확인
- [ ] 사용자 직접 Swagger UI 확인
- [ ] `gitnexus detect_changes`

Maven compile과 test-compile은 서버 기동 없이 수행했고 모두 성공했다.

## 7. 사용자 직접 확인 명령

서버와 Docker는 사용자가 직접 실행한다.

```powershell
cd C:\Dev\health-center-smart-reservation\backend
mvn spring-boot:run
```

로그인 후 Refresh Token을 받아 재발급을 확인한다.

```powershell
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"staff@test.com","password":"password1234"}'
$refreshToken = $login.data.refreshToken
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/reissue' -ContentType 'application/json' -Body (@{ refreshToken = $refreshToken } | ConvertTo-Json)
```

새 Access Token과 Refresh Token이 응답되면 성공이다.

로그아웃은 로그인 또는 재발급으로 받은 Access Token과 Refresh Token으로 확인한다.

```powershell
$accessToken = $login.data.accessToken
$refreshToken = $login.data.refreshToken
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/logout' -Headers @{ Authorization = "Bearer $accessToken" } -ContentType 'application/json' -Body (@{ refreshToken = $refreshToken } | ConvertTo-Json)
```

`success: true`, `data: null`, `error: null`이면 성공이다.

## 8. 미검증 사유

- 서버 기동, Docker 실행, API 런타임 호출, Swagger 브라우저 확인은 사용자가 직접 수행하는 운영 기준에 따라 에이전트가 실행하지 않는다.
- GitNexus MCP 리소스가 노출되지 않았고 CLI 실행도 실패해 `gitnexus detect_changes`는 수행하지 못했다.

## 9. 후속 작업

1. 역할별 API 접근 규칙을 `SecurityConfig`에 반영
2. Refresh Token 해시 저장 또는 식별자 기반 저장 방식 검토
3. 기존 eGovFrame 로그인/관리자/SNS 샘플 비노출 또는 삭제 판단
4. 사용자 직접 런타임 확인 결과를 PR 문서에 반영

## 10. 커밋 메시지 초안

```text
feat: Auth Member 토큰 재발급과 로그아웃 구현

- Refresh Token 기반 Access Token 재발급 API 추가
- 재발급 성공 시 Refresh Token 회전과 기존 토큰 폐기 처리
- 로그인 사용자 Refresh Token 로그아웃 폐기 처리
- API 명세와 구현 기록에 확인 방법 반영
```
