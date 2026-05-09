# Auth/Member 토큰 재발급 및 로그아웃 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/auth-member-domain` |
| base 브랜치 | `main` 예정 |
| 작업 트리 | Auth/Member 토큰 재발급/로그아웃 코드와 문서 변경 있음 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | `mvn -q -DskipTests compile` 성공 |
| 테스트 확인 | `mvn -q test-compile` 성공 |
| API 확인 | 사용자가 직접 확인할 명령 작성 |
| GitNexus 확인 | MCP 리소스 없음, CLI 모듈 없음으로 실패, `rg` 대체 확인 |

## PR 제목

```text
feat: Auth Member 토큰 재발급과 로그아웃 구현
```

## PR 본문

```markdown
## 개요

Auth/Member 로그인 기반 위에 Refresh Token을 이용한 토큰 재발급과 로그아웃 흐름을 추가했습니다.

이번 PR에서는 Refresh Token 회전 방식으로 `/api/auth/reissue`를 구현하고, 로그인 사용자가 본인의 Refresh Token을 폐기할 수 있도록 `/api/auth/logout`을 구현했습니다.

## 변경 내용

- `POST /api/auth/reissue` 구현
- `POST /api/auth/logout` 구현
- Refresh Token 조회, 회전, 폐기 MyBatis SQL 추가
- 토큰 재발급/로그아웃 요청 DTO 추가
- API 명세에 재발급/로그아웃 요청과 응답 예시 추가
- 구현 기록과 전체 체크리스트 갱신

## 검증

- [x] `rg` 기반 Auth/Member 참조 확인
- [x] `git diff --check`
- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [ ] 사용자 직접 `POST /api/auth/reissue` 확인
- [ ] 사용자 직접 `POST /api/auth/logout` 확인
- [ ] 사용자 직접 Swagger UI 확인

## 사용자 직접 확인 방법

```powershell
cd C:\Dev\health-center-smart-reservation\backend
mvn spring-boot:run
```

```powershell
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"staff@test.com","password":"password1234"}'
$refreshToken = $login.data.refreshToken
$reissue = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/reissue' -ContentType 'application/json' -Body (@{ refreshToken = $refreshToken } | ConvertTo-Json)
$accessToken = $reissue.data.accessToken
$newRefreshToken = $reissue.data.refreshToken
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/logout' -Headers @{ Authorization = "Bearer $accessToken" } -ContentType 'application/json' -Body (@{ refreshToken = $newRefreshToken } | ConvertTo-Json)
```

## 미검증 사유

- 서버 기동, Docker 실행, API 런타임 호출, Swagger 브라우저 확인은 사용자 직접 수행 기준에 따라 에이전트가 실행하지 않았습니다.
- GitNexus MCP 리소스가 현재 세션에 노출되지 않았고, CLI도 로컬 모듈을 찾지 못해 detect_changes는 수행하지 못했습니다.

## 후속 작업

- 역할별 API 접근 규칙을 `SecurityConfig`에 반영
- Refresh Token 해시 저장 또는 식별자 기반 저장 방식 검토
- 기존 eGovFrame 로그인/관리자/SNS 샘플 비노출 또는 삭제 판단
```

## 변경 파일 요약

| 파일 | 내용 |
|---|---|
| `backend/src/main/java/egovframework/healthcenter/member/dto/ReissueTokenRequest.java` | 토큰 재발급 요청 DTO 추가 |
| `backend/src/main/java/egovframework/healthcenter/member/dto/LogoutRequest.java` | 로그아웃 요청 DTO 추가 |
| `backend/src/main/java/egovframework/healthcenter/member/application/AuthCommandService.java` | 토큰 재발급과 로그아웃 서비스 로직 추가 |
| `backend/src/main/java/egovframework/healthcenter/member/api/AuthController.java` | `/api/auth/reissue`, `/api/auth/logout` API 추가 |
| `backend/src/main/java/egovframework/healthcenter/member/mapper/MemberMapper.java` | Refresh Token 조회/폐기 Mapper 메서드 추가 |
| `backend/src/main/resources/egovframework/mapper/healthcenter/member/Member_SQL_postgresql.xml` | Refresh Token 조회/폐기 SQL 추가 |
| `docs/04_api/01_API_명세서.md` | 재발급/로그아웃 API 예시 추가 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | Auth/Member 구현 진행 상태 갱신 |
| `docs/11_implementation_log/12_Auth_Member_토큰_재발급_로그아웃_구현_기록.md` | 구현 기록 추가 |
| `docs/11_implementation_log/13_Auth_Member_토큰_재발급_로그아웃_PR_작성안.md` | PR 작성안 추가 |

## 커밋 메시지 초안

```text
feat: Auth Member 토큰 재발급과 로그아웃 구현

- Refresh Token 기반 Access Token 재발급 API 추가
- 재발급 성공 시 Refresh Token 회전과 기존 토큰 폐기 처리
- 로그인 사용자 Refresh Token 로그아웃 폐기 처리
- API 명세와 구현 기록에 확인 방법 반영
```
