# Auth/Member 로그인 기반 구현 기록

## 1. 작업 목표

이번 단계의 목표는 범위 확정 문서를 바탕으로 보건소 Member 모델 기반 로그인 골격을 실제 코드로 추가하는 것이다.

완료 범위는 `members`, `refresh_tokens` 기반 DB 초기 구조, 이메일/비밀번호 로그인, JWT Access Token 발급, Refresh Token 저장, 현재 로그인 사용자 조회까지로 제한한다.

## 2. 작업 범위

### 포함

- [x] `egovframework.healthcenter.member` 하위 패키지 추가
- [x] `members`, `refresh_tokens`, `health_centers` PostgreSQL schema 추가
- [x] 관리자, 직원, 시민, 보호자 seed 계정 추가
- [x] Member Mapper/VO/XML 추가
- [x] 로그인 요청/응답 DTO 추가
- [x] Healthcenter Member 기준 JWT 발급 유틸 추가
- [x] `POST /api/auth/login` 구현
- [x] `GET /api/members/me` 구현
- [x] 기존 JWT 필터가 Bearer 토큰과 신규 Member principal을 인식하도록 연결
- [x] `SecurityConfig`에 `/api/auth/login`, `/api/auth/reissue` 공개 경로 추가

### 제외

- [x] `POST /api/auth/reissue` 실제 구현 제외
- [x] `POST /api/auth/logout` 실제 구현 제외
- [x] Refresh Token 회전/폐기 정책 구현 제외
- [x] 기존 `/auth/login-jwt`, `/jwtAuthAPI`, `/admin/password`, SNS API 삭제 제외
- [x] 역할별 전체 API 접근 검증 제외
- [x] 프론트엔드 로그인 화면 구현 제외
- [x] 실제 커밋, push, 배포 제외

## 3. GitNexus impact 결과

| 대상 | 결과 | blast radius |
|---|---|---|
| `SecurityConfig` | LOW | direct caller 0, affected process 0 |
| `JwtAuthenticationFilter` | LOW | direct 2, affected process 1 (`filterChain`), affected module 1 (`Security`) |
| `EgovConfigAppMapper` | LOW | direct caller 0, affected process 0 |
| `EgovFileScrty` | LOW | direct 2, 기존 로그인/관리자 샘플 import 확인. 수정하지 않고 재사용만 함 |

HIGH 또는 CRITICAL 위험은 없었다.

## 4. 구현 내용

### DB

| 파일 | 내용 |
|---|---|
| `backend/src/main/resources/db/postgresql/schema.sql` | `health_centers`, `members`, `refresh_tokens` 테이블과 인덱스 추가 |
| `backend/src/main/resources/db/postgresql/data.sql` | 기본 보건소와 `admin@test.com`, `staff@test.com`, `citizen@test.com`, `guardian@test.com` seed 추가 |

Seed 계정 비밀번호는 모두 `password1234`이며, 기존 eGovFrame `EgovFileScrty.encryptPassword(password, email)` 방식으로 해시했다.

### API

| API | 상태 | 설명 |
|---|---|---|
| `POST /api/auth/login` | 구현 | 이메일/비밀번호 검증 후 Access Token과 Refresh Token 발급 |
| `GET /api/members/me` | 구현 | Bearer Access Token 기반 현재 사용자 정보 반환 |
| `POST /api/auth/reissue` | 미구현 | 공개 경로만 선반영, 후속 구현 필요 |
| `POST /api/auth/logout` | 미구현 | 후속 구현 필요 |

### Security/JWT

- 기존 eGovFrame JWT 토큰 파싱은 유지했다.
- 신규 Healthcenter Access Token은 `memberId`, `healthCenterId`, `email`, `name`, `role`, `type` claim을 사용한다.
- `JwtAuthenticationFilter`는 `Authorization: Bearer {accessToken}` 형식을 처리한다.
- 신규 토큰이면 `MemberPrincipal`을 SecurityContext principal로 넣고, 기존 eGovFrame 토큰이면 기존 `LoginVO` 방식으로 fallback한다.

## 5. 추가된 파일

| 파일 | 내용 |
|---|---|
| `backend/src/main/java/egovframework/healthcenter/member/domain/MemberRole.java` | MVP 사용자 역할 Enum |
| `backend/src/main/java/egovframework/healthcenter/member/security/MemberPrincipal.java` | 보건소 회원 인증 principal |
| `backend/src/main/java/egovframework/healthcenter/member/security/HealthcenterJwtTokenProvider.java` | 보건소 Member 기준 JWT 발급/검증 |
| `backend/src/main/java/egovframework/healthcenter/member/mapper/MemberVO.java` | MyBatis 조회 결과 VO |
| `backend/src/main/java/egovframework/healthcenter/member/mapper/MemberMapper.java` | Member/RefreshToken MyBatis Mapper |
| `backend/src/main/java/egovframework/healthcenter/member/dto/LoginRequest.java` | 로그인 요청 DTO |
| `backend/src/main/java/egovframework/healthcenter/member/dto/LoginResponse.java` | 로그인 응답 DTO |
| `backend/src/main/java/egovframework/healthcenter/member/dto/MemberResponse.java` | 회원 응답 DTO |
| `backend/src/main/java/egovframework/healthcenter/member/application/AuthCommandService.java` | 로그인 처리 서비스 |
| `backend/src/main/java/egovframework/healthcenter/member/api/AuthController.java` | 인증 API Controller |
| `backend/src/main/java/egovframework/healthcenter/member/api/MemberController.java` | 회원 API Controller |
| `backend/src/main/resources/egovframework/mapper/healthcenter/member/Member_SQL_postgresql.xml` | Member/RefreshToken SQL Mapper |

## 6. 변경된 파일

| 파일 | 내용 |
|---|---|
| `backend/src/main/java/egovframework/com/security/SecurityConfig.java` | `/api/auth/login`, `/api/auth/reissue` 인증 예외 추가 |
| `backend/src/main/java/egovframework/com/jwt/JwtAuthenticationFilter.java` | Bearer 토큰 처리와 신규 Member principal 인증 연결 |
| `backend/src/main/java/egovframework/com/config/EgovConfigAppMapper.java` | 샘플 `let` Mapper 경로가 없어도 healthcenter Mapper 로딩이 계속되도록 보완 |
| `backend/src/main/resources/db/postgresql/schema.sql` | Auth/Member 테이블 추가 |
| `backend/src/main/resources/db/postgresql/data.sql` | Auth/Member seed 데이터 추가 |

## 7. 검증

- [x] GitNexus impact 확인
- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [x] `POST /api/auth/login` 런타임 호출
- [x] `GET /api/members/me` 런타임 호출
- [ ] Swagger UI 확인
- [ ] 역할별 API 접근 확인
- [ ] `gitnexus detect_changes`

런타임 확인 결과:

| API | 확인 결과 |
|---|---|
| `POST /api/auth/login` | `staff@test.com / password1234` 로그인 성공, role `STAFF` 확인 |
| `GET /api/members/me` | 발급된 Bearer 토큰으로 호출 성공, email `staff@test.com`, role `STAFF` 확인 |

## 8. 미검증 사유

- `gitnexus detect_changes`는 CLI에 없는 명령이라 수행하지 못했다.
- 토큰 재발급, 로그아웃, 역할별 접근 제한은 후속 구현 범위로 남겼다.
- Swagger UI는 브라우저로 확인하지 않았다.

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 로그인 구현 중 | Refresh Token 저장값 해시 또는 회전 정책 확정 | 현재는 재발급 구현 전 최소 저장만 추가됨 | 후속 토큰 재발급/로그아웃 단계에서 처리 |
| 로그인 구현 중 | `/api/auth/reissue` 공개 경로와 실제 API 구현 연결 | SecurityConfig에는 선반영했지만 API는 아직 없음 | 후속 구현 |
| 로그인 구현 중 | 기존 eGovFrame 로그인/관리자 샘플 비노출 또는 삭제 판단 | 신규 Auth 구현과 Swagger 노출 중복 가능 | Security 전환 단계에서 처리 |
| 로그인 구현 중 | API 명세에 `/api/members/me` 반영 | 구현 API가 기존 명세보다 구체화됨 | 이번 단계에서 반영 |
| 런타임 검증 중 | `EgovConfigAppMapper`의 삭제된 `let` Mapper 경로 처리 | 샘플 정리 후 `egovframework/mapper/let` 경로가 없어 애플리케이션 기동 실패 | 없는 Mapper 경로는 건너뛰도록 보완 |

## 10. 다음 작업

1. `POST /api/auth/reissue` 구현
2. `POST /api/auth/logout` 구현
3. Refresh Token 만료/폐기/회전 정책 정리
4. 역할별 API 접근 규칙을 SecurityConfig에 반영
5. 런타임에서 로그인과 `/api/members/me` 호출 확인

## 11. 커밋 메시지 초안

```text
feat: 보건소 회원 로그인 기반 추가

- Member 도메인 패키지와 로그인 DTO 추가
- members, refresh_tokens 테이블과 seed 계정 추가
- 이메일 비밀번호 로그인 API 구현
- Bearer JWT 기반 현재 사용자 조회 API 구현
- 기존 JWT 필터에 보건소 Member principal 인증 연결
```
