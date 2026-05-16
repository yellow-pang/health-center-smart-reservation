# Backend Env Dockerfile 구현 기록

## 1. 작업 목표

- Ubuntu VM + Jenkins + Docker Compose 배포 준비를 위해 루트 `.env.example` 기준을 만든다.
- backend DB/JWT/CORS 설정을 환경변수 placeholder 기준으로 정리한다.
- Maven 기반 eGovFrame Simple Backend Template 구조를 유지하면서 `backend/Dockerfile`을 추가한다.

## 2. 전체 체크리스트 관련 항목 확인

`docs/13_schedule/02_전체_작업_체크리스트.md` 기준 이번 브랜치 관련 항목은 아래와 같다.

- `9. 배포와 운영 작업`
  - Docker Compose 실행 정리
  - 환경변수 정리
- `12.4 운영/배포 문서`
  - Docker Compose 실행 절차 문서화
  - Backend/Frontend/PostgreSQL 환경변수 문서화

이번 브랜치는 전체 Compose와 Jenkinsfile까지 확장하지 않고, backend 환경변수 분리와 backend Dockerfile 작성까지만 처리한다.

## 3. 이번 브랜치 작업 범위

포함한다.

- [x] 루트 `.env.example` 작성
- [x] `.gitignore`의 `.env`, `.env.local` 제외 기준 확인
- [x] backend DB 접속 설정 placeholder 적용
- [x] backend JWT secret/access/refresh 만료 설정 placeholder 적용
- [x] backend CORS 허용 Origin을 `CORS_ALLOWED_ORIGINS` 기준으로 정리
- [x] `backend/Dockerfile` 작성
- [x] `backend/.dockerignore` 작성
- [x] Maven compile/test-compile 확인
- [x] 브랜치 구현 기록과 PR 문서 초안 작성
- [x] 전체 체크리스트 갱신

제외한다.

- [ ] `frontend/Dockerfile` 작성
- [ ] 루트 `docker-compose.yml`을 backend/frontend/postgresql 전체 구성으로 확장
- [ ] Jenkinsfile 작성
- [ ] Docker build, Docker Compose 실행, 서버 기동, Swagger/브라우저 런타임 확인
- [ ] 실제 커밋, push, 배포

## 4. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 확인
- [x] `docs/08_deploy/02_Ubuntu_VM_Jenkins_Docker_Compose_배포_계획서.md` 확인
- [x] 현재 브랜치 확인: `infra/backend-dockerization`
- [x] 작업 트리 확인
- [x] 영향받는 설정/심볼 확인

## 5. 영향 분석

GitNexus 기준:

- `npm.cmd exec -- gitnexus status` 결과 index stale 상태를 확인했다.
- 지침에 따라 `npm.cmd exec -- gitnexus analyze`를 실행했지만 CLI가 `Not inside a git repository`를 반환해 갱신하지 못했다.
- `gitnexus impact ... --repo health-center-smart-reservation`도 빈 실패 응답을 반환해 도구 기반 impact 결과를 확보하지 못했다.

보완 확인:

| 대상 | `rg` 기준 직접 영향 | 판단 |
|---|---|---|
| `SecurityConfig` / `corsConfigurationSource` | Spring Security CORS 설정에서 사용 | CORS 허용 Origin 기준 변경. API 권한 정책은 유지 |
| `HealthcenterJwtTokenProvider` | `AuthCommandService`, `JwtAuthenticationFilter`에서 사용 | JWT secret과 access token 만료 시간을 Spring 환경변수로 주입 |
| `AuthCommandService` | `AuthController`에서 사용 | refresh token 만료 시간을 기존 14일과 같은 1209600초 기본값으로 주입 |
| `EgovConfigAppDatasource` | `Globals.postgresql.*`를 Spring `Environment`로 읽음 | DB placeholder 적용 대상. 클래스 코드는 수정하지 않음 |

blast radius는 Auth 로그인/토큰 검증/CORS 진입부에 걸리므로 `MEDIUM`으로 본다. 다만 API 계약, 권한 매핑, DB schema, MyBatis Mapper는 변경하지 않았다.

## 6. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `.env.example` | PostgreSQL, Spring profile, DB 접속, JWT, Frontend API URL, CORS Origin 예시 추가 |
| `backend/src/main/resources/application.properties` | DB/JWT/CORS 값을 환경변수 placeholder로 변경 |
| `backend/src/main/java/egovframework/com/security/SecurityConfig.java` | CORS 허용 Origin을 `Globals.Allow.Origin`에서 읽고 쉼표 기준으로 파싱 |
| `backend/src/main/java/egovframework/healthcenter/member/security/HealthcenterJwtTokenProvider.java` | `EgovProperties` raw 읽기 대신 Spring `@Value` 주입으로 JWT secret/access 만료 적용 |
| `backend/src/main/java/egovframework/healthcenter/member/application/AuthCommandService.java` | refresh token 만료 시간을 `Globals.jwt.refresh-token-validity-seconds`로 주입 |
| `backend/Dockerfile` | Maven builder + Java 17 JRE runtime 멀티 스테이지 Dockerfile 추가 |
| `backend/.dockerignore` | Docker build context에서 target/log/git 등 제외 |

## 7. 검증 체크리스트

- [x] `git status --short --branch`로 브랜치 확인
- [x] `rg`로 관련 설정과 참조 확인
- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `git diff --check`
- [ ] GitNexus `detect_changes`
- [ ] Docker build
- [ ] Docker Compose 실행
- [ ] 서버 기동
- [ ] Swagger/브라우저 런타임 확인

비고:

- Docker build, Docker Compose 실행, 서버 기동, Swagger/브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행한다.
- GitNexus detect_changes는 CLI/MCP 상태 문제로 수행하지 못했고, `git diff --stat`, `git diff --check`, Maven compile/test-compile로 보완했다.

## 8. 사용자 런타임 확인 안내

Docker/서버/API 런타임은 사용자가 직접 확인한다.

대표 확인 흐름:

```bash
cp .env.example .env
# .env에서 <ubuntu-vm-ip>, DB/JWT 값을 실제 값으로 수정
docker build -t health-center-backend:local ./backend
```

Spring Boot Dashboard로 backend를 실행한 뒤 Swagger에서 확인할 대표 예시는 기존 Auth 흐름을 사용한다.

- URL: `http://localhost:8080/swagger-ui/index.html`
- Controller: `AuthController`
- 대표 API: `POST /api/auth/login`
- 대표 요청:

```json
{
  "email": "admin@test.com",
  "password": "password1234"
}
```

기대 결과:

- `success: true`
- `data.accessToken` 존재
- `data.refreshToken` 존재
- `data.member.role`이 `ADMIN`

## 9. 사용자 코드 점검 결과

| 점검 시점 | 사용자 의견 | 반영 여부 |
|---|---|---|
| 브랜치 시작 | `.env.example`, backend 환경변수 placeholder, CORS 허용 Origin 기준 정리와 `backend/Dockerfile` 생성 요청 | 반영 |

## 10. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| JWT placeholder 적용 중 | `HealthcenterJwtTokenProvider`의 `EgovProperties` raw 읽기 제거 | `${JWT_SECRET:...}` placeholder가 그대로 secret으로 쓰이지 않게 하기 위함 | 이번 브랜치에서 Spring `@Value` 주입으로 변경 |
| Dockerfile 작성 중 | `backend/.dockerignore` 추가 | `target`, 로그, Git 메타데이터가 Docker build context에 들어가지 않게 하기 위함 | 이번 브랜치에서 추가 |
| GitNexus 확인 중 | CLI analyze/impact 실패 | 로컬 CLI가 repo 인식 또는 repo 지정 후 impact 응답을 정상 반환하지 못함 | Maven/rg/git diff 검증으로 보완, 후속 GitNexus 상태 재점검 필요 |

## 11. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
- [ ] 사용자 코드 점검 반영

## 12. 다음 작업 후보

1. `frontend/Dockerfile`과 frontend `.dockerignore` 작성
2. 루트 `docker-compose.yml`을 backend/frontend/postgresql 전체 구성으로 확장
3. README 배포 섹션과 Docker Compose 실행 절차 보강
