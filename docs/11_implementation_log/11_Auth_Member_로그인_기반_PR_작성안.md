# Auth/Member 로그인 기반 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/auth-member-domain` |
| base 브랜치 | `main` 예정 |
| 작업 트리 | Auth/Member 코드와 문서 변경 있음 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | `mvn -q -DskipTests compile` 성공 |
| 테스트 확인 | `mvn -q test-compile` 성공 |
| API 확인 | 로그인과 현재 사용자 조회 런타임 호출 성공 |
| GitNexus impact | `SecurityConfig`, `JwtAuthenticationFilter`, `EgovConfigAppMapper`, `EgovFileScrty` LOW |

## PR 제목

```text
feat: 보건소 회원 로그인 기반 추가
```

## PR 본문

```markdown
## 개요

보건소 MVP의 Auth/Member Context 구현을 시작하기 위해 Member 테이블 기반 로그인 골격을 추가했습니다.

이번 PR에서는 이메일/비밀번호 로그인, Access Token 발급, Refresh Token 저장, 현재 로그인 사용자 조회까지 구현했습니다. 토큰 재발급과 로그아웃, 역할별 전체 API 접근 제한은 후속 작업으로 남겼습니다.

## 변경 내용

- `egovframework.healthcenter.member` 하위 Member/Auth 패키지 추가
- `health_centers`, `members`, `refresh_tokens` PostgreSQL schema 추가
- 관리자, 직원, 시민, 보호자 seed 계정 추가
- `POST /api/auth/login` 구현
- `GET /api/members/me` 구현
- 보건소 Member 기준 JWT 발급/검증 유틸 추가
- 기존 JWT 필터에 Bearer 토큰 처리와 Member principal 인증 연결
- 샘플 `let` Mapper 경로가 없는 경우에도 healthcenter Mapper 로딩이 계속되도록 보완
- `/api/auth/login`, `/api/auth/reissue` 인증 예외 경로 추가
- API 명세와 전체 체크리스트 갱신

## 검증

- [x] GitNexus impact 확인
- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [x] `POST /api/auth/login` 런타임 호출
- [x] `GET /api/members/me` 런타임 호출
- [ ] Swagger UI 확인
- [ ] 역할별 API 접근 확인

## 미검증 사유

- `gitnexus detect_changes`는 CLI에서 제공되지 않아 수행하지 못했습니다.
- Refresh Token 재발급/로그아웃과 역할별 접근 제한은 후속 구현 범위입니다.
- Swagger UI는 브라우저로 확인하지 않았습니다.

## 후속 작업

- `POST /api/auth/reissue` 구현
- `POST /api/auth/logout` 구현
- Refresh Token 해시 저장, 회전, 폐기 정책 확정
- `ROLE_CITIZEN`, `ROLE_GUARDIAN`, `ROLE_STAFF`, `ROLE_ADMIN` 기준 API 접근 제한 적용
- 기존 eGovFrame 로그인/관리자 샘플 API 비노출 또는 삭제 판단
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 토큰 재발급/로그아웃 후속 브랜치 생성 또는 다음 작업 문서화

## 변경 파일 요약

| 파일 | 내용 |
|---|---|
| `backend/src/main/java/egovframework/healthcenter/member/**` | Member/Auth API, DTO, Mapper, Security principal/token provider 추가 |
| `backend/src/main/resources/egovframework/mapper/healthcenter/member/Member_SQL_postgresql.xml` | Member/RefreshToken MyBatis SQL 추가 |
| `backend/src/main/java/egovframework/com/security/SecurityConfig.java` | 신규 Auth 공개 경로 추가 |
| `backend/src/main/java/egovframework/com/jwt/JwtAuthenticationFilter.java` | Bearer 토큰과 Member principal 인증 연결 |
| `backend/src/main/java/egovframework/com/config/EgovConfigAppMapper.java` | 삭제된 샘플 Mapper 경로가 없어도 기동되도록 보완 |
| `backend/src/main/resources/db/postgresql/schema.sql` | Auth/Member 테이블 추가 |
| `backend/src/main/resources/db/postgresql/data.sql` | 기본 보건소와 seed 계정 추가 |
| `docs/04_api/01_API_명세서.md` | `/api/members/me` 반영 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | Auth/Member 부분 구현 상태 반영 |
| `docs/11_implementation_log/10_Auth_Member_로그인_기반_구현_기록.md` | 구현 기록 추가 |
| `docs/11_implementation_log/11_Auth_Member_로그인_기반_PR_작성안.md` | PR 작성안 추가 |

## 커밋 메시지 초안

```text
feat: 보건소 회원 로그인 기반 추가

- Member 도메인 패키지와 로그인 DTO 추가
- members, refresh_tokens 테이블과 seed 계정 추가
- 이메일 비밀번호 로그인 API 구현
- Bearer JWT 기반 현재 사용자 조회 API 구현
- 기존 JWT 필터에 보건소 Member principal 인증 연결
```
