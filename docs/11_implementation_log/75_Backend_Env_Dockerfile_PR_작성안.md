# Backend Env Dockerfile Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `infra/backend-dockerization` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 코드/설정/문서 변경 진행 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 백엔드 compile | `mvn.cmd -q -DskipTests compile` 통과 |
| 백엔드 test-compile | `mvn.cmd -q test-compile` 통과 |
| 정적 공백 확인 | `git diff --check` 통과 |
| GitNexus 확인 | status는 stale, analyze/impact는 CLI 오류로 완료하지 못함 |
| 실행/API 확인 | Docker build, Docker Compose, 서버 기동, Swagger/브라우저 확인은 사용자 직접 수행 범위 |

## PR 제목

```text
chore: 백엔드 환경변수와 Dockerfile 정리
```

## PR 본문

````markdown
## 개요

Ubuntu VM + Jenkins + Docker Compose 배포 준비를 위해 백엔드 실행 설정을 환경변수 기반으로 정리하고, Maven 기반 backend Dockerfile을 추가합니다.

이번 PR은 전체 docker-compose 확장이나 Jenkinsfile 작성 전 단계입니다. 루트 `.env.example`을 기준으로 DB/JWT/CORS 값을 주입할 수 있게 하고, 기존 eGovFrame Simple Backend Template의 Maven 구조와 MyBatis 기준은 유지했습니다.

## 변경 내용

- 루트 `.env.example` 추가
- DB 접속 정보, JWT secret/access/refresh 만료, CORS 허용 Origin을 Spring placeholder로 변경
- CORS 허용 Origin을 `CORS_ALLOWED_ORIGINS`의 쉼표 구분 값으로 파싱
- 보건소 JWT 토큰 발급기의 secret/access 만료 시간을 Spring 환경변수 주입으로 전환
- refresh token 만료 시간을 환경변수 주입으로 전환하되 기본값은 기존 14일과 동일하게 유지
- `backend/Dockerfile` 추가
- `backend/.dockerignore` 추가
- 브랜치 구현 기록과 PR 작성안 추가
- 전체 체크리스트와 배포 계획서 진행 상태 갱신

## 검증

- [x] `docs/README.md`, 에이전트 운영 가이드, 브랜치 기록 가이드 확인
- [x] 전체 체크리스트 관련 항목 확인
- [x] 현재 브랜치 확인: `infra/backend-dockerization`
- [x] `.gitignore`에 `.env`, `.env.local` 제외 기준 확인
- [x] `rg`로 CORS/JWT/DB 설정 참조 확인
- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `git diff --check`

## 미검증 사유

- Docker build, Docker Compose 실행, 서버 기동, Swagger/브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- GitNexus index가 stale 상태였고, `gitnexus analyze`는 로컬 CLI가 git repository를 인식하지 못해 실패했습니다.
- `gitnexus impact`는 repo 지정 후에도 빈 실패 응답을 반환해 완료하지 못했습니다. 대신 `rg` 참조 확인과 Maven compile/test-compile로 보완했습니다.

## 사용자 확인 체크리스트

- [ ] `.env.example`을 `.env`로 복사하고 실제 DB/JWT/VM IP 값으로 수정
- [ ] `docker build -t health-center-backend:local ./backend` 실행
- [ ] Spring Boot Dashboard로 backend 실행
- [ ] Swagger `POST /api/auth/login` 대표 예시 확인
- [ ] 브라우저에서 frontend Origin과 backend CORS 오류가 없는지 확인

## Swagger 대표 예시

- URL: `http://localhost:8080/swagger-ui/index.html`
- Controller: `AuthController`
- API: `POST /api/auth/login`

```json
{
  "email": "admin@test.com",
  "password": "password1234"
}
```

기대 결과:

- `success: true`
- `data.accessToken`, `data.refreshToken` 존재
- `data.member.role`이 `ADMIN`

## 추가 테스트 체크리스트

- [ ] Happy: `admin@test.com` 로그인 성공
- [ ] Happy: `staff@test.com` 로그인 성공 후 STAFF API 접근 가능
- [ ] Edge: `CORS_ALLOWED_ORIGINS`에 VM IP와 localhost를 함께 넣었을 때 브라우저 호출 성공
- [ ] Bad: 잘못된 비밀번호 로그인 실패
- [ ] Bad: 허용되지 않은 Origin에서 브라우저 CORS 차단 확인

## 후속 작업

- `frontend/Dockerfile`과 frontend `.dockerignore` 작성
- 루트 `docker-compose.yml`을 backend/frontend/postgresql 전체 구성으로 확장
- PostgreSQL init SQL과 volume 초기화 절차 문서화
- Jenkins 커스텀 이미지, Jenkins Compose, Jenkinsfile 작성
- README 배포 섹션과 Notion 정리용 문서 작성
````

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 후속 브랜치 생성 여부 결정

## 커밋 메시지 초안

제목:

```text
chore: 백엔드 환경변수와 Dockerfile 정리
```

본문:

```text
- 루트 .env.example에 DB/JWT/CORS/Frontend API URL 예시 추가
- backend application.properties의 DB/JWT/CORS 값을 환경변수 placeholder로 변경
- CORS 허용 Origin과 JWT 만료 설정을 Spring 환경변수 주입으로 정리
- Maven 기반 backend Dockerfile과 .dockerignore 추가
- 브랜치 구현 기록, PR 작성안, 전체 체크리스트 갱신
```
