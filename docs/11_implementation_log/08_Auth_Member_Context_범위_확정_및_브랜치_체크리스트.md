# Auth/Member Context 범위 확정 및 브랜치 체크리스트

## 1. 작업 목표

이번 브랜치의 초기 목표는 Auth/Member API를 바로 구현하기 전에 보건소 MVP 기준 인증, 회원, 권한 범위를 확정하는 것이었다.

이후 사용자 요청에 따라 같은 브랜치에서 범위 확정 결과를 기반으로 로그인, 현재 사용자 조회, 토큰 재발급, 로그아웃, URL 기반 역할별 접근 규칙, 레거시 인증 샘플 비노출까지 작은 단위로 이어서 구현했다.

기존 eGovFrame 로그인, SNS 로그인, 관리자 샘플 코드는 즉시 삭제하거나 그대로 채택하지 않고, 신규 `egovframework.healthcenter.member` 구현을 위한 참고/보류/전환 대상으로 구분한다.

## 2. 작업 범위

### 포함

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md`의 Auth/Member 관련 항목 확인
- [x] Auth/Member 관련 설계 문서 확인
- [x] 기존 eGovFrame 로그인/SNS/관리자 샘플 코드 유지 여부 검토
- [x] 보건소 MVP 사용자 유형과 인증 범위 제안
- [x] 이번 브랜치에서 구현할 것과 구현하지 않을 것 구분
- [x] Auth/Member Context 범위 확정
- [x] 브랜치별 체크리스트 초안 작성
- [x] 전체 체크리스트 갱신
- [x] PR 문서 초안 작성

### 제외

- [x] 실제 Auth/Member API 구현 제외
- [x] DB schema, seed SQL 변경 제외
- [x] Spring Security/JWT 코드 변경 제외
- [x] 기존 eGovFrame 로그인/SNS/관리자 샘플 삭제 제외
- [x] 실제 커밋, push, 배포 제외

## 3. 현재 브랜치 정보

| 항목 | 내용 |
|---|---|
| 브랜치 | `refactor/auth-member-domain` |
| 작업 방향 | Auth/Member Context 구현 범위 확정 |
| 이번 단위 | Auth/Member 기반 구현과 브랜치 통합 PR 정리 |
| 다음 단위 | 사용자 직접 런타임 확인 후 Reservation Context 구현 착수 |

## 4. 확인한 기준 문서

| 문서 | 확인 내용 |
|---|---|
| `docs/README.md` | eGovFrame Simple Backend Template, Maven, MyBatis, `egovframework.healthcenter` 기준 |
| `docs/09_agent/05_문서기반_자동진행_운영가이드.md` | 인증/권한 정책 변경은 신중히 다루고 실제 커밋은 사용자 승인 후 진행 |
| `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` | 브랜치별 체크리스트와 PR 작성안 작성 기준 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | `Auth/Member Context 구현`은 아직 미완료이며 완료 기준은 로그인, 토큰 재발급, 권한 분리 동작 |
| `docs/01_planning/01_프로젝트_기획서.md` | 사용자 유형, 권한 정책, 인증 비기능 요구사항 |
| `docs/01_planning/05_MVP_개발_로드맵_및_기능_고도화.md` | Member/Auth 구현 단계, API 우선순위, 기존 eGovFrame 로그인 샘플 전환 주의점 |
| `docs/02_domain/01_Bounded_Context_명세서.md` | Member Context 책임, 주요 엔티티, 패키지 구조 |
| `docs/02_domain/03_공통코드_정의서.md` | `USER_ROLE` 코드 값 |
| `docs/02_domain/04_공통코드_관리_설계서.md` | 권한 코드는 시스템 코드이며 삭제 금지 |
| `docs/03_database/01_ERD_및_테이블_명세서.md` | `members`, `refresh_tokens` 테이블 후보와 role 컬럼 |
| `docs/04_api/01_API_명세서.md` | `/api/auth/login`, `/api/auth/reissue`, `/api/auth/logout` 계약 |
| `docs/05_frontend/02_UX_API_계약_우선순위.md` | 예약 생성 전 인증/권한 연결 필요 |
| `docs/10_backend_transition/01_eGovFrame_백엔드_템플릿_전환_현황.md` | 현재 JWT 필터와 Swagger Bearer 형식 불일치 |
| `docs/10_backend_transition/03_샘플_코드_정리_범위_및_안전_절차.md` | 로그인/SNS/관리자 샘플은 Auth/Member 전환 전 영향 확인 필요 |

## 5. 현재 코드 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 작업 트리 | 깨끗한 상태에서 시작 |
| GitNexus MCP | 현재 세션에서 리소스가 노출되지 않음 |
| `gitnexus status` | stale 상태 확인 |
| `gitnexus analyze` | `Not inside a git repository`로 실패 |
| `gitnexus detect_changes` | `unknown command 'detect_changes'`로 실패 |
| 대체 탐색 | `rg`, 직접 파일 확인 사용 |
| 남은 Mapper XML | `egovframework/mapper/healthcenter/common/CommonCode_SQL_postgresql.xml`, `mapper-config.xml`만 확인 |

GitNexus 인덱스 갱신이 실패했으므로 이번 문서는 `rg`와 파일 직접 확인 결과를 기준으로 작성했다. 실제 Auth/Member API 구현 전에는 GitNexus 재시도 또는 동등한 참조 확인을 다시 수행한다.

## 6. 기존 eGovFrame 인증 샘플 검토

| 대상 | 현재 역할 | 판단 | 다음 처리 |
|---|---|---|---|
| `SecurityConfig` | SecurityFilterChain, 인증 예외, JWT 필터 등록 | 유지 | `/api/auth/**`, 역할별 API 규칙으로 전환 필요 |
| `JwtAuthenticationFilter` | `Authorization` 헤더 JWT 파싱, `LoginVO` 인증 객체 생성 | 전환 | Bearer 접두어 처리, `CITIZEN/GUARDIAN/STAFF/ADMIN` 권한 매핑 필요 |
| `EgovJwtTokenUtil` | `LoginVO` 기반 JWT 생성/검증 | 전환 | Healthcenter Member principal/claims 기반 유틸로 재정리 필요 |
| `EgovLoginApiController` | `/auth/login-jwt`, `/auth/logout` 샘플 API | 보류 후 대체 | 신규 `/api/auth/login`, `/api/auth/logout` 구현 후 삭제 또는 비노출 검토 |
| `EgovLoginService`, `LoginDAO` | eGovFrame 샘플 로그인 DAO/Service | 보류 후 대체 | `members` MyBatis Mapper 기반 AuthService로 대체 |
| `SnsLoginApiController`, `SnsUtils`, `SnsVO` | Kakao/Naver OAuth 샘플 | MVP 제외, 보류 | 이번 구현 범위에서 제외하고 2차 소셜/간편 인증 후보로 이동 |
| `EgovSiteManagerApiController` | `/jwtAuthAPI`, `/admin/password` 샘플 | 보류 후 대체 | 관리자 비밀번호 변경은 신규 Member/Admin API에서 재설계 |
| `EgovFileScrty` | 기존 비밀번호 암호화 유틸 | 참고 | 신규 비밀번호 해시 정책 확정 시 재사용 여부 판단 |
| SNS properties | Kakao/Naver 샘플 설정 | 보류 | MVP 구현 시 사용하지 않음, 추후 제거 또는 별도 프로필 분리 검토 |

결론:

- eGovFrame Security/JWT 골격은 유지한다.
- eGovFrame 로그인/SNS/관리자 샘플 API는 보건소 MVP API로 그대로 사용하지 않는다.
- 신규 구현은 `egovframework.healthcenter.member` 하위에 작성하고, 기존 샘플은 전환 완료 후 기능 묶음 단위로 정리한다.

## 7. 보건소 MVP 사용자 유형

| 유형 | 코드 | MVP 책임 | 인증 필요 여부 |
|---|---|---|---|
| 일반 시민 | `CITIZEN` | 본인 예약 신청, 내 예약 조회, 예약 취소, 혼잡도 확인 | 예약/내 정보는 필요, 혼잡도는 공개 가능 |
| 보호자 | `GUARDIAN` | 가족 또는 고령층 대리 예약, 대리 예약 내역 확인 | 필요 |
| 직원 | `STAFF` | 예약자 체크인, 현장 접수, 대기열 조회, 호출, 처리 시작/완료 | 필요 |
| 관리자 | `ADMIN` | 업무 유형, 창구, 직원, 예약 슬롯 관리, 대시보드 전체 조회 | 필요 |

MVP에서는 모든 예약/운영 기능을 로그인 기반으로 둔다. 단, `GET /api/service-types`, `GET /api/common-codes/**`, `GET /api/congestion/current`는 사용자 진입과 방문 판단을 위해 공개 가능 API로 유지한다.

## 8. 인증과 권한 범위 확정

### 이번 구현 목표 API

| API | 권한 | 구현 목적 |
|---|---|---|
| `POST /api/auth/login` | PUBLIC | 이메일/비밀번호 로그인, Access Token/Refresh Token 발급 |
| `POST /api/auth/reissue` | PUBLIC | Refresh Token 기반 Access Token 재발급 |
| `POST /api/auth/logout` | 로그인 사용자 | Refresh Token 폐기 또는 무효화 |
| `GET /api/members/me` | 로그인 사용자 | 현재 로그인 사용자 정보 확인 |

`GET /api/members/me`는 기존 API 명세의 Member API가 상세하지 않으므로 Auth/Member 구현 시 첫 Member 조회 API로 제안한다.

### 권한 적용 기준

| API 영역 | 권한 |
|---|---|
| 공통코드 조회 | PUBLIC |
| 업무 유형 조회 | PUBLIC |
| 현재 혼잡도 조회 | PUBLIC |
| 예약 가능 시간 조회 | 로그인 사용자 |
| 예약 신청 | `CITIZEN`, `GUARDIAN`, `STAFF`, `ADMIN` |
| 내 예약 조회/취소 | 예약자 본인, `ADMIN` |
| 체크인/현장 접수 | `STAFF`, `ADMIN` |
| 대기열 조회/호출/처리 | `STAFF`, `ADMIN` |
| 대시보드 전체 조회 | `ADMIN` |
| 기준정보 관리 | `ADMIN` |

### 토큰 정책

| 항목 | 확정 범위 |
|---|---|
| Access Token | JWT, `Authorization: Bearer {accessToken}` 형식 |
| Refresh Token | DB 저장, 재발급과 로그아웃 시 검증/폐기 |
| Principal | 기존 `LoginVO`가 아니라 보건소 Member 기준 principal 사용 |
| Claim | `memberId`, `email`, `name`, `role`, `healthCenterId` 중심 |
| 권한 매핑 | Spring Security 권한은 `ROLE_CITIZEN`, `ROLE_GUARDIAN`, `ROLE_STAFF`, `ROLE_ADMIN` |
| Secret | 운영 전 환경변수 또는 비공개 프로필로 분리 |

## 9. 이번 브랜치에서 구현할 것과 구현하지 않을 것

### 초기 범위에서 구현할 것

- Auth/Member 구현 범위 확정 문서 작성
- eGovFrame 로그인/SNS/관리자 샘플 유지 여부 판단
- MVP 사용자 유형과 권한 범위 제안
- 구현 대상 API와 보류 대상 API 구분
- 브랜치별 체크리스트 초안 작성
- 전체 체크리스트 갱신
- PR 문서 초안 작성

### 초기 범위에서 구현하지 않을 것

- `/api/auth/login`, `/api/auth/reissue`, `/api/auth/logout` 실제 Controller/Service/Mapper 구현
- `members`, `refresh_tokens` 실제 schema/seed SQL 작성
- Spring Security 설정 수정
- JWT 필터/토큰 유틸 변경
- 기존 `/auth/login-jwt`, `/jwtAuthAPI`, `/admin/password`, SNS API 삭제
- 프론트엔드 로그인 화면 구현
- Docker/Swagger 런타임 검증

### 후속 요청으로 같은 브랜치에서 구현한 것

- `POST /api/auth/login` 구현
- `GET /api/members/me` 구현
- `POST /api/auth/reissue` 구현
- `POST /api/auth/logout` 구현
- `members`, `refresh_tokens`, `health_centers` schema와 seed 작성
- Healthcenter Member 기준 JWT 발급과 `MemberPrincipal` 인증 연결
- API 명세의 권한 표 기준 URL 기반 역할별 접근 규칙 1차 반영
- eGovFrame 레거시 로그인/SNS/관리자 샘플 공개 경로와 Swagger 노출 정리

### 계속 구현하지 않을 것

- Reservation Context 객체 권한 정책
- Office/Reservation/Visit/Queue/Dashboard Controller 구현
- Refresh Token 해시 저장 또는 식별자 기반 저장 방식
- SNS/레거시 로그인 샘플 완전 삭제
- 프론트엔드 로그인 화면 구현
- Docker/Swagger/API 런타임 검증

## 10. 다음 구현 브랜치 초안

### 1차: Auth/Member 기반 코드 추가

- `egovframework.healthcenter.member` 패키지 생성
- Member VO/DTO/Mapper/Service/API 골격 추가
- `members`, `refresh_tokens` schema와 seed 작성
- `POST /api/auth/login`, `GET /api/members/me` 우선 구현
- Maven compile 확인

커밋 메시지 초안:

```text
feat: 보건소 회원 로그인 기반 추가

- Healthcenter Member 패키지와 DTO 골격 추가
- members, refresh_tokens 기반 MyBatis Mapper 추가
- 이메일 비밀번호 로그인과 현재 사용자 조회 API 구현
```

### 2차: 토큰 재발급과 로그아웃

- Refresh Token 저장/회전/만료 정책 구현
- `POST /api/auth/reissue` 구현
- `POST /api/auth/logout` 구현
- 재발급 실패/만료/폐기 테스트 후보 정리

커밋 메시지 초안:

```text
feat: 토큰 재발급과 로그아웃 흐름 구현

- Refresh Token 저장과 검증 로직 추가
- Access Token 재발급 API 구현
- 로그아웃 시 Refresh Token 무효화 처리
```

### 3차: Security 전환

- `Authorization: Bearer {accessToken}` 처리 통일
- `ROLE_CITIZEN`, `ROLE_GUARDIAN`, `ROLE_STAFF`, `ROLE_ADMIN` 권한 매핑
- 기존 eGovFrame 로그인/관리자 샘플 API 비노출 또는 삭제 판단
- 역할별 접근 수동 검증

커밋 메시지 초안:

```text
refactor: 보건소 역할 기준으로 JWT 권한 검증 전환

- Bearer 토큰 파싱 방식을 API 명세와 통일
- Member 역할을 Spring Security 권한으로 매핑
- 역할별 API 접근 규칙 정리
```

## 11. 이번 브랜치 체크리스트

### 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 확인
- [x] 관련 설계 문서 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] 영향받는 파일 확인

### 구현 체크리스트

- [x] Auth/Member 관련 전체 체크리스트 항목 확인
- [x] 기존 eGovFrame 로그인 샘플 검토
- [x] 기존 SNS 로그인 샘플 검토
- [x] 기존 관리자 샘플 검토
- [x] MVP 사용자 유형 확정
- [x] 인증 API 범위 확정
- [x] 역할별 권한 범위 정리
- [x] 이번 브랜치 포함/제외 범위 작성
- [x] 다음 구현 브랜치 단위 제안
- [x] PR 문서 초안 작성
- [x] 전체 체크리스트 갱신

### 검증 체크리스트

- [x] 문서 작성 완료
- [x] `rg` 기반 Auth/Member 관련 문서 참조 확인
- [x] `rg` 기반 기존 로그인/SNS/관리자 샘플 참조 확인
- [x] 남은 Mapper XML 목록 확인
- [ ] GitNexus impact 확인
- [ ] `gitnexus_detect_changes()` 확인
- [ ] Maven compile
- [ ] API 수동 호출
- [ ] Swagger 런타임 확인

이번 브랜치는 문서 범위 확정 작업이므로 Maven compile, API 호출, Swagger 런타임 확인은 수행하지 않았다.

## 12. 사용자 코드 점검 결과

| 점검 시점 | 사용자 의견 | 반영 여부 |
|---|---|---|
| 브랜치 시작 | 실제 Auth/Member API 구현은 하지 말고 범위 확정 문서부터 작성 | 반영 |
| 브랜치 시작 | 작업은 커밋 가능한 작은 단위로 나누고 커밋 메시지를 제안 | 반영 |
| 브랜치 시작 | 실제 커밋, push, 배포는 명시 전까지 금지 | 반영 |

## 13. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| Auth/Member 범위 검토 중 | 기존 로그인/관리자 DAO Mapper XML 부재 확인 | 샘플 제거 이후 기존 `/auth/login-jwt`, `/admin/password` 런타임 실패 가능성 확인 필요 | 다음 Auth 전환 브랜치에서 샘플 API 비노출/삭제 판단 |
| Auth/Member 범위 검토 중 | `JwtAuthenticationFilter` Bearer 접두어 미처리 | API 명세는 `Authorization: Bearer {accessToken}` 형식 | Security 전환 단위에서 수정 |
| Auth/Member 범위 검토 중 | GitNexus analyze/detect_changes 실패 | impact 분석과 변경 영향 확인을 GitNexus로 수행하지 못함 | 실패 사유 기록, `rg` 대체 확인 |

## 14. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
- [x] 후속 구현 단위 기록 작성
- [x] 통합 PR 작성안 작성

## 14.1 후속 구현 단위 기록

| 단위 | 기록 문서 | PR 작성안 |
|---|---|---|
| Auth/Member 범위 확정 | `docs/11_implementation_log/08_Auth_Member_Context_범위_확정_및_브랜치_체크리스트.md` | `docs/11_implementation_log/09_Auth_Member_Context_범위_확정_PR_작성안.md` |
| 로그인 기반 구현 | `docs/11_implementation_log/10_Auth_Member_로그인_기반_구현_기록.md` | `docs/11_implementation_log/11_Auth_Member_로그인_기반_PR_작성안.md` |
| 토큰 재발급/로그아웃 구현 | `docs/11_implementation_log/12_Auth_Member_토큰_재발급_로그아웃_구현_기록.md` | `docs/11_implementation_log/13_Auth_Member_토큰_재발급_로그아웃_PR_작성안.md` |
| 역할별 접근 규칙 구현 | `docs/11_implementation_log/14_Auth_Member_역할별_접근_규칙_구현_기록.md` | `docs/11_implementation_log/15_Auth_Member_역할별_접근_규칙_PR_작성안.md` |
| 레거시 인증 샘플 비노출 | `docs/11_implementation_log/16_eGovFrame_레거시_인증_샘플_비노출_기록.md` | `docs/11_implementation_log/17_eGovFrame_레거시_인증_샘플_비노출_PR_작성안.md` |
| 브랜치 통합 PR | - | `docs/11_implementation_log/18_Auth_Member_Context_통합_PR_작성안.md` |

## 15. 이번 브랜치 커밋 메시지 초안

```text
docs: Auth Member 구현 범위 확정

- MVP 사용자 유형과 인증 범위 정리
- 기존 eGovFrame 로그인/SNS/관리자 샘플 유지 여부 검토
- 이번 브랜치 포함/제외 작업과 후속 구현 단위 작성
- Auth/Member 브랜치 체크리스트와 PR 작성안 추가
- 전체 체크리스트에 범위 확정 진행 상태 반영
```
