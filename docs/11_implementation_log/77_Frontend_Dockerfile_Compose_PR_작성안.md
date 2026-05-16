# Frontend Dockerfile Compose Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `infra/frontend-dockerization` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 프론트엔드 Docker/Compose/문서 변경 진행 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 프론트 빌드 | `npm.cmd run build` 통과 |
| 정적 공백 확인 | `git diff --check` 통과 |
| 실행/API 확인 | Docker build, Docker Compose, 브라우저 확인은 사용자 직접 수행 범위 |

## PR 제목

```text
chore: 프론트엔드 Docker Compose 구성 추가
```

## PR 본문

````markdown
## 개요

Next.js 프론트엔드를 Docker image로 빌드하고, 루트 Docker Compose에서 frontend/backend/postgresql을 함께 구성할 수 있도록 frontend 서비스를 추가합니다.

이번 PR은 Jenkinsfile 작성 전 단계입니다. `NEXT_PUBLIC_API_BASE_URL`은 브라우저 기준 backend URL이므로 `.env`와 Compose build arg를 통해 빌드 시점에 주입되도록 정리했습니다.

## 변경 내용

- `frontend/Dockerfile` 추가
- `frontend/.dockerignore` 추가
- `.env.example`과 로컬 `.env`에 `FRONTEND_PORT=3000` 추가
- `docker-compose.yml`에 frontend build 서비스 추가
- frontend build arg/runtime env에 `NEXT_PUBLIC_API_BASE_URL` 주입
- frontend 서비스가 backend 이후 시작되도록 Compose 의존성 추가
- 배포 계획서와 전체 체크리스트 갱신
- 브랜치 구현 기록과 PR 작성안 추가

## 검증

- [x] 현재 브랜치 확인: `infra/frontend-dockerization`
- [x] frontend package script 확인
- [x] `NEXT_PUBLIC_API_BASE_URL` 사용 위치 확인
- [x] `npm.cmd run build`
- [x] `git diff --check`

## 미검증 사유

- Docker build, Docker Compose 실행, 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- 실제 브라우저에서 CORS, API base URL, 로그인/대시보드 흐름 확인은 Docker Compose 실행 후 확인해야 합니다.
- `npm.cmd run build`는 통과했지만 루트와 frontend lockfile이 함께 감지되어 Next.js Turbopack root 추론 경고가 표시되었습니다. 이번 PR에서는 Docker 구성에 집중하고 lockfile 정리는 후속 작업으로 둡니다.

## 사용자 확인 체크리스트

- [ ] `docker compose --env-file .env config`로 변수 치환 확인
- [ ] `docker compose --env-file .env build frontend` 실행
- [ ] `docker compose --env-file .env up -d` 실행
- [ ] `http://localhost:3000` 접속
- [ ] `admin@test.com / password1234` 로그인
- [ ] 관리자 대시보드 화면 진입
- [ ] 브라우저 콘솔에서 CORS/API base URL 오류 없음 확인

## 후속 작업

- Docker Compose 수동 실행 결과 문서화
- frontend lockfile 정리 또는 Next.js `turbopack.root` 설정 검토
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
chore: 프론트엔드 Docker Compose 구성 추가
```

본문:

```text
- Next.js frontend Dockerfile과 .dockerignore 추가
- docker-compose.yml에 frontend 이미지 build 서비스 추가
- NEXT_PUBLIC_API_BASE_URL을 Compose build arg와 runtime env로 주입
- .env.example에 FRONTEND_PORT 예시 추가
- 배포 계획서, 브랜치 기록, PR 작성안, 전체 체크리스트 갱신
```
