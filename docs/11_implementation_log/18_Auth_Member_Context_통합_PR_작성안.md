# Auth/Member Context 통합 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/auth-member-domain` |
| base 브랜치 | `main` 예정 |
| 작업 성격 | Auth/Member 범위 확정에서 로그인, 토큰, 권한 1차 구현까지 확장 |
| 주요 커밋 | 아직 통합 PR 기준 커밋 여부 확인 필요 |
| 빌드 확인 | 각 구현 단위에서 `mvn -q -DskipTests compile` 성공 |
| 테스트 확인 | 각 구현 단위에서 `mvn -q test-compile` 성공 |
| API/Swagger 확인 | 사용자가 직접 확인할 명령과 기대 결과 문서화 |
| GitNexus 확인 | MCP 리소스 없음, CLI 모듈 없음으로 실패, `rg` 대체 확인 |

## PR 제목

```text
feat: Auth Member Context 기반 구현
```

## PR 본문

```markdown
## 개요

보건소 MVP에서 예약, 방문, 대기, 관리자 기능이 사용할 Auth/Member Context 기반을 구현했습니다.

이번 PR은 원래 Auth/Member 구현 범위 확정으로 시작했지만, 이후 작은 단위로 로그인, 현재 사용자 조회, 토큰 재발급, 로그아웃, URL 기반 역할별 접근 규칙, eGovFrame 레거시 인증 샘플 비노출까지 이어서 정리했습니다.

## 변경 내용

- Auth/Member 구현 범위와 MVP 사용자 유형 확정
- `health_centers`, `members`, `refresh_tokens` PostgreSQL schema 추가
- 관리자, 직원, 시민, 보호자 seed 계정 추가
- `egovframework.healthcenter.member` 하위 Member/Auth 패키지 추가
- `POST /api/auth/login` 구현
- `GET /api/members/me` 구현
- `POST /api/auth/reissue` 구현
- `POST /api/auth/logout` 구현
- Bearer JWT 기반 Member principal 인증 연결
- Refresh Token 회전과 폐기 처리 추가
- API 명세의 권한 표 기준으로 URL 기반 역할별 접근 규칙 1차 반영
- 레거시 eGovFrame 로그인/SNS/관리자 샘플을 공개 인증 예외와 Swagger 노출에서 제외
- API 명세, 전체 체크리스트, 구현 기록, PR 작성안 갱신

## 검증

- [x] `rg` 기반 Auth/Member 및 레거시 인증 샘플 참조 확인
- [x] `git diff --check`
- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [ ] 사용자 직접 `POST /api/auth/login` 확인
- [ ] 사용자 직접 `GET /api/members/me` 확인
- [ ] 사용자 직접 `POST /api/auth/reissue` 확인
- [ ] 사용자 직접 `POST /api/auth/logout` 확인
- [ ] 사용자 직접 역할별 접근 확인
- [ ] 사용자 직접 Swagger UI에서 레거시 Controller 비노출 확인

## 사용자 직접 확인 방법

서버와 Docker는 사용자가 직접 실행합니다.

```powershell
cd C:\Dev\health-center-smart-reservation\backend
mvn spring-boot:run
```

로그인과 현재 사용자 조회:

```powershell
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"staff@test.com","password":"password1234"}'
$token = $login.data.accessToken
Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/members/me' -Headers @{ Authorization = "Bearer $token" }
```

토큰 재발급과 로그아웃:

```powershell
$refreshToken = $login.data.refreshToken
$reissue = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/reissue' -ContentType 'application/json' -Body (@{ refreshToken = $refreshToken } | ConvertTo-Json)
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/logout' -Headers @{ Authorization = "Bearer $($reissue.data.accessToken)" } -ContentType 'application/json' -Body (@{ refreshToken = $reissue.data.refreshToken } | ConvertTo-Json)
```

Swagger 확인:

```text
http://localhost:8080/swagger-ui/index.html
```

확인 항목:

- `AuthController`, `MemberController`, `CommonCodeController` 노출
- `EgovLoginApiController`, `SnsLoginApiController`, `EgovSiteManagerApiController` 미노출

## 미검증 사유

- 서버 기동, Docker 실행, API 런타임 호출, Swagger 브라우저 확인은 사용자 직접 수행 기준에 따라 에이전트가 실행하지 않았습니다.
- GitNexus MCP 리소스가 현재 세션에 노출되지 않았고, CLI도 로컬 모듈을 찾지 못해 detect_changes는 수행하지 못했습니다.
- 예약자 본인 여부 같은 객체 권한 정책은 Reservation Context 구현 시 Service 정책으로 보강합니다.
- 아직 구현되지 않은 Reservation/Visit/Queue/Dashboard API는 URL 권한 규칙만 선반영했고, 실제 Controller 구현 후 역할별 접근을 재확인합니다.

## 후속 작업

- 사용자가 직접 Auth API와 Swagger UI 확인 후 결과 반영
- Reservation Context 구현 시작
- Reservation Context에서 예약자 본인, 직원, 관리자 객체 권한 정책 구현
- Refresh Token 해시 저장 또는 식별자 기반 저장 방식 검토
- SNS/레거시 로그인 샘플 완전 삭제 여부 별도 판단
```

## 변경 파일 요약

| 파일 | 내용 |
|---|---|
| `backend/src/main/java/egovframework/healthcenter/member/**` | Auth/Member API, DTO, Mapper, principal, JWT provider 추가 |
| `backend/src/main/resources/egovframework/mapper/healthcenter/member/Member_SQL_postgresql.xml` | Member/RefreshToken MyBatis SQL 추가 |
| `backend/src/main/java/egovframework/com/jwt/JwtAuthenticationFilter.java` | Bearer 토큰과 Member principal 인증 연결 |
| `backend/src/main/java/egovframework/com/security/SecurityConfig.java` | 신규 Auth 공개 경로, 역할별 접근 규칙, 레거시 공개 경로 정리 |
| `backend/src/main/java/egovframework/com/config/EgovConfigAppMapper.java` | 삭제된 샘플 Mapper 경로가 없어도 기동되도록 보완 |
| `backend/src/main/java/egovframework/let/uat/uia/web/EgovLoginApiController.java` | Swagger 비노출 처리 |
| `backend/src/main/java/egovframework/com/sns/SnsLoginApiController.java` | Swagger 비노출 처리 |
| `backend/src/main/java/egovframework/let/uat/esm/web/EgovSiteManagerApiController.java` | Swagger 비노출 처리 |
| `backend/src/main/resources/db/postgresql/schema.sql` | Auth/Member 테이블 추가 |
| `backend/src/main/resources/db/postgresql/data.sql` | 기본 보건소와 seed 계정 추가 |
| `docs/04_api/01_API_명세서.md` | Auth/Member API 예시와 오류 코드 반영 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | Auth/Member 진행 상태와 후속 작업 반영 |
| `docs/11_implementation_log/08~18_*` | 범위 확정, 구현 기록, PR 작성안 추가 |

## 커밋 메시지 초안

```text
feat: Auth Member Context 기반 구현

- 보건소 Member 기반 로그인과 현재 사용자 조회 API 추가
- Refresh Token 재발급과 로그아웃 흐름 구현
- JWT Member principal과 역할별 URL 접근 규칙 적용
- eGovFrame 레거시 인증 샘플 공개 경로와 Swagger 노출 정리
- Auth Member 구현 기록과 통합 PR 작성안 갱신
```
