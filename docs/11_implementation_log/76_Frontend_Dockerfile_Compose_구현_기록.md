# Frontend Dockerfile Compose 구현 기록

## 1. 작업 목표

- `infra/frontend-dockerization` 브랜치에서 Next.js 프론트엔드를 Docker image로 빌드할 수 있게 한다.
- 루트 `docker-compose.yml`에 frontend 서비스를 추가해 postgresql/backend/frontend 구성을 갖춘다.
- `NEXT_PUBLIC_API_BASE_URL`이 Next.js 빌드 시점과 컨테이너 실행 시점에 일관되게 주입되도록 정리한다.

## 2. 전체 체크리스트 관련 항목 확인

`docs/13_schedule/02_전체_작업_체크리스트.md` 기준 관련 항목은 아래와 같다.

- `9. 배포와 운영 작업`
  - Docker Compose 실행 정리
  - 환경변수 정리
- `12.4 운영/배포 문서`
  - Docker Compose 실행 절차 문서화
  - Backend/Frontend/PostgreSQL 환경변수 문서화

이번 브랜치는 Jenkinsfile과 README 배포 섹션까지 확장하지 않고, frontend Dockerfile과 Compose frontend 서비스 추가까지만 처리한다.

## 3. 작업 범위

포함한다.

- [x] 현재 브랜치와 작업 트리 확인
- [x] frontend Next.js 빌드/실행 명령 확인
- [x] `frontend/Dockerfile` 작성
- [x] `frontend/.dockerignore` 작성
- [x] `.env.example`과 `.env`에 `FRONTEND_PORT` 추가
- [x] `docker-compose.yml`에 frontend build 서비스 추가
- [x] `NEXT_PUBLIC_API_BASE_URL` build arg와 runtime env 정리
- [x] `npm.cmd run build` 확인
- [x] `git diff --check` 확인
- [x] 브랜치 구현 기록과 PR 문서 초안 작성
- [x] 전체 체크리스트와 배포 계획서 갱신

제외한다.

- [ ] Docker build 실행
- [ ] Docker Compose 실행
- [ ] 서버 기동
- [ ] Swagger/브라우저 런타임 확인
- [ ] Jenkinsfile 작성
- [ ] README 배포 섹션 작성
- [ ] 실제 커밋, push, 배포

## 4. 현재 코드 상태 확인

| 구분 | 확인 결과 |
|---|---|
| 현재 브랜치 | `infra/frontend-dockerization` |
| Frontend framework | Next.js 16, React 19 |
| 빌드 명령 | `npm run build` |
| 실행 명령 | `npm run start` |
| API URL | `frontend/src/lib/api-client.ts`에서 `NEXT_PUBLIC_API_BASE_URL` 사용 |
| lockfile | `package-lock.json`, `pnpm-lock.yaml`이 함께 존재하나 현재 Dockerfile은 `package-lock.json` 기준 `npm ci` 사용 |
| Compose 현재 상태 | `postgresql`, `backend`, `frontend` 서비스 포함 |

## 5. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `frontend/Dockerfile` | Node 20 Alpine 기반 deps/builder/runner 멀티 스테이지 Dockerfile 추가 |
| `frontend/.dockerignore` | `node_modules`, `.next`, env, 로그 등 build context 제외 |
| `.env.example` | `FRONTEND_PORT=3000` 예시 추가 |
| `.env` | 로컬 실행용 `FRONTEND_PORT=3000` 추가 |
| `docker-compose.yml` | `frontend` 서비스 추가, `NEXT_PUBLIC_API_BASE_URL` build arg/runtime env 주입, backend 의존성 설정 |
| `docs/08_deploy/02_Ubuntu_VM_Jenkins_Docker_Compose_배포_계획서.md` | frontend Docker/Compose 진행 상태 갱신 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 운영/배포 진행 상태 갱신 |

## 6. 검증 체크리스트

- [x] `git status --short --branch`
- [x] `rg`와 파일 확인으로 frontend API URL 기준 확인
- [x] `npm.cmd run build`
- [x] `git diff --check`
- [ ] Docker build
- [ ] Docker Compose 실행
- [ ] 브라우저 확인

비고:

- Docker build, Docker Compose 실행, 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행한다.
- Next.js의 `NEXT_PUBLIC_*` 값은 빌드 시점에 클라이언트 번들에 포함되므로 Compose `build.args.NEXT_PUBLIC_API_BASE_URL`에도 값을 넘긴다.
- `npm.cmd run build`는 통과했지만, 루트와 frontend lockfile이 함께 감지되어 Next.js Turbopack root 추론 경고가 표시되었다. 이번 브랜치에서는 Docker 구성에 집중하고, lockfile 정리는 후속 정리 후보로 둔다.

## 7. 사용자 확인 안내

사용자가 직접 확인할 대표 명령:

```bash
docker compose --env-file .env config
docker compose --env-file .env build frontend
docker compose --env-file .env up -d
```

확인 URL:

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`

대표 브라우저 확인:

- 로그인 화면 접속
- `admin@test.com / password1234` 로그인
- 관리자 대시보드 화면 진입

## 8. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| Dockerfile 작성 중 | `package-lock.json` 기준 `npm ci` 사용 | Docker build 재현성을 위해 npm lockfile 기준으로 설치하기 위함 | 이번 브랜치에서 반영 |
| Compose 작성 중 | `NEXT_PUBLIC_API_BASE_URL`을 build arg와 env에 함께 전달 | Next.js 클라이언트 환경변수는 빌드 시점 값이 중요하기 때문 | 이번 브랜치에서 반영 |
| Compose 작성 중 | `FRONTEND_PORT` 추가 | 로컬/VM에서 frontend host port를 `.env`로 관리하기 위함 | 이번 브랜치에서 반영 |
| Next build 확인 중 | Turbopack root 추론 경고 확인 | 루트와 frontend lockfile이 함께 감지됨. 빌드는 성공했지만 추후 lockfile 정리 또는 Next config root 설정 검토 필요 | 후속 작업 후보로 기록 |

## 9. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] PR 문서 작성
- [x] 전체 체크리스트 갱신
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
- [ ] 사용자 코드 점검 반영

## 10. 다음 작업 후보

1. Docker Compose 수동 실행 결과 문서화
2. PostgreSQL init SQL과 volume 초기화 절차 문서화
3. Jenkins 구성과 Jenkinsfile 작성
