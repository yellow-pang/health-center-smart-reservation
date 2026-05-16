# dev to main 배포 전 확인 체크리스트

## 1. 문서 목적

이 문서는 `dev` 브랜치의 변경 사항을 `main`에 반영하기 전에 확인할 항목을 정리한다.

이 프로젝트는 `main` 브랜치 변경을 Jenkins가 감지하면 Docker Compose 배포가 실행되는 구조다.  
따라서 `dev -> main` PR 전에는 코드 빌드뿐 아니라 Docker Compose 설정, Jenkins 설정, `.env` 주입 기준까지 함께 확인해야 한다.

---

## 2. 전체 흐름

```text
dev 최신화
  -> 정적 빌드 확인
  -> Docker Compose 설정 확인
  -> Jenkins 실행 위치 확인
  -> Ubuntu VM Jenkins 설정 확인
  -> dev -> main PR 생성
  -> main merge
  -> Jenkins Pipeline 실행
  -> Ubuntu VM 컨테이너 상태 확인
  -> 브라우저/Swagger 대표 흐름 확인
```

중요:

```text
Jenkins가 배포하는 위치는 Jenkins 컨테이너가 마운트한 /var/run/docker.sock의 Docker host다.
```

Windows 로컬 Docker에서 Jenkins를 실행하면 애플리케이션 컨테이너도 Windows 로컬 Docker에 생성된다.
Ubuntu VM에 배포하려면 Jenkins 컨테이너도 Ubuntu VM 내부 Docker Engine 위에서 실행해야 한다.

상세 기준은 `docs/08_deploy/04_Jenkins_VM_배포_운영_가이드.md`를 함께 본다.

---

## 3. merge 전 로컬 정적 확인

아래 명령은 서버 기동이나 Docker 실행 없이 확인할 수 있다.

### 3.1 Git 상태 확인

```bash
git status --short --branch
git log --oneline --decorate -5
```

확인 기준:

- 현재 브랜치가 `dev`인지 확인한다.
- 커밋하지 않은 변경이 남아 있지 않은지 확인한다.
- `main`에 올릴 커밋 범위를 확인한다.

### 3.2 Backend 정적 빌드 확인

```bash
cd backend
mvn -q -DskipTests compile
mvn -q test-compile
```

확인 기준:

- compile 성공
- test-compile 성공
- Maven 구조 유지
- eGovFrame Simple Backend Template 구조 유지

### 3.3 Frontend 정적 빌드 확인

```bash
cd frontend
npm ci
npm run build
```

확인 기준:

- Next.js build 성공
- `NEXT_PUBLIC_API_BASE_URL` 기준 API client가 유지되는지 확인
- build 중 warning은 PR 문서에 기록한다.

### 3.4 Diff 공백 확인

```bash
git diff --check
```

확인 기준:

- trailing whitespace 오류가 없어야 한다.
- Windows CRLF 경고만 있는 경우, 실제 diff 오류는 아니지만 PR 문서에 필요 시 기록한다.

---

## 4. Docker Compose 설정 확인

Docker 실행 전 설정 치환만 확인한다.

```bash
docker compose --env-file .env config
```

확인 기준:

- `postgresql`, `backend`, `frontend` 서비스가 모두 보인다.
- PostgreSQL volume mount가 PostgreSQL 18 기준 `/var/lib/postgresql`로 되어 있다.
- backend `DB_HOST`가 `postgresql`이다.
- frontend `NEXT_PUBLIC_API_BASE_URL`이 브라우저 기준 backend URL이다.
- host port가 충돌하지 않는다.

예상 서비스:

```text
postgresql : 5432
backend    : 8080
frontend   : 3000
```

---

## 5. Jenkins 실행 위치와 설정 확인

Jenkins UI와 Ubuntu VM 터미널에서 확인한다.

### 5.1 Jenkins 실행 위치 확인

Ubuntu VM 터미널에서 확인한다.

```bash
docker ps --filter "name=health-center-jenkins"
docker exec -it health-center-jenkins sh -lc "hostname && docker info --format '{{.Name}} / {{.OperatingSystem}}'"
```

확인 기준:

- `health-center-jenkins` 컨테이너가 Ubuntu VM Docker Engine에서 실행 중이다.
- Jenkins UI 주소가 `http://<ubuntu-vm-ip>:8081`이다.
- Windows 로컬 Docker Desktop의 Jenkins UI가 아니라 VM Jenkins UI에서 Job을 설정한다.

### 5.2 Jenkins 컨테이너 상태

```bash
docker ps --filter "name=health-center-jenkins"
```

확인 기준:

- `health-center-jenkins` 컨테이너가 실행 중이다.
- Jenkins UI가 `http://<ubuntu-vm-ip>:8081`에서 열린다.

### 5.3 Jenkins 컨테이너 도구 확인

```bash
docker exec -it health-center-jenkins docker version
docker exec -it health-center-jenkins docker compose version
docker exec -it health-center-jenkins mvn -version
docker exec -it health-center-jenkins node --version
docker exec -it health-center-jenkins npm --version
```

확인 기준:

- Docker CLI 확인 성공
- Docker Compose v2 확인 성공
- Maven 확인 성공
- Node.js와 npm 확인 성공

### 5.4 Jenkins Credentials 확인

Jenkins UI에서 확인한다.

```text
Jenkins 관리
  -> Credentials
  -> System
  -> Global credentials
```

확인 기준:

- `health-center-env-file` credential이 있다.
- Kind는 `Secret file`이다.
- 업로드한 파일은 실제 배포용 `.env`이다.
- `.env.example`이 아니라 실제 값이 들어간 `.env`를 사용한다.
- Windows 로컬 Jenkins에 등록한 Credentials는 VM Jenkins로 자동 이전되지 않는다.

### 5.5 Pipeline Job 확인

Job 설정에서 확인한다.

```text
Definition: Pipeline script from SCM
SCM: Git
Repository URL: GitHub repo URL
Credentials: public repo면 none
Branch Specifier: */main
Script Path: Jenkinsfile
Lightweight checkout: checked
```

Trigger 기준:

```text
Poll SCM: H/5 * * * *
```

확인 기준:

- `main` 브랜치를 바라본다.
- `Jenkinsfile` 경로가 정확하다.
- GitHub repo가 public이면 Git credential은 없어도 된다.
- private repo로 바꾸면 별도 GitHub credential이 필요하다.
- Job은 Ubuntu VM Jenkins에 생성되어 있어야 한다.

---

## 6. dev -> main PR 전 확인

PR 본문에 아래 내용을 남긴다.

- [ ] backend compile/test-compile 통과
- [ ] frontend build 통과
- [ ] `git diff --check` 통과
- [ ] `docker compose --env-file .env config` 확인
- [ ] Jenkins가 Ubuntu VM 내부 Docker에서 실행 중인지 확인
- [ ] Jenkins credential `health-center-env-file` 확인
- [ ] Jenkins Job이 `*/main`과 `Jenkinsfile`을 바라보는지 확인
- [ ] Docker/Jenkins 런타임 확인은 main merge 후 수행한다고 명시

---

## 7. main merge 후 확인

main merge 후에는 Jenkins가 Poll SCM 주기에 따라 변경을 감지한다.

즉시 확인하고 싶으면 Jenkins UI에서 직접 실행한다.

```text
health-center-deploy
  -> Build Now
```

Jenkins Pipeline 단계:

```text
Checkout
Check Branch
Tool Versions
Backend Test Compile
Backend Package
Frontend Build
Prepare Env
Docker Compose Config
Docker Build
Deploy
Cleanup
```

확인 기준:

- `Check Branch`가 main으로 통과한다.
- `Prepare Env`에서 `health-center-env-file`을 찾는다.
- `docker compose --env-file .env config`가 성공한다.
- `docker compose --env-file .env build`가 성공한다.
- `docker compose --env-file .env up -d --remove-orphans`가 성공한다.

---

## 8. 배포 후 컨테이너 확인

Ubuntu VM에서 확인한다.

```bash
docker compose --env-file .env ps
docker ps
```

확인 기준:

- `health-center-postgres` running 또는 healthy
- `health-center-backend` running
- `health-center-frontend` running

로그 확인:

```bash
docker compose --env-file .env logs postgresql
docker compose --env-file .env logs backend
docker compose --env-file .env logs frontend
```

---

## 9. 배포 후 브라우저 확인

Windows 브라우저에서 확인한다.

```text
Frontend: http://<ubuntu-vm-ip>:3000
Backend:  http://<ubuntu-vm-ip>:8080
Swagger:  http://<ubuntu-vm-ip>:8080/swagger-ui/index.html
Jenkins:  http://<ubuntu-vm-ip>:8081
```

대표 확인 흐름:

1. Frontend 접속
2. `admin@test.com / password1234` 로그인
3. 관리자 대시보드 진입
4. Swagger 접속
5. `POST /api/auth/login` 대표 예시 확인

대표 Swagger 요청:

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

---

## 10. 실패 시 우선 확인 순서

### 10.1 Jenkinsfile을 찾지 못하는 경우

- Job의 `Branch Specifier`가 `*/main`인지 확인
- main 브랜치에 `Jenkinsfile`이 merge되었는지 확인
- `Script Path`가 `Jenkinsfile`인지 확인

### 10.1.1 Jenkins는 성공했는데 VM에 배포되지 않는 경우

- Jenkins UI 주소가 `http://<ubuntu-vm-ip>:8081`인지 확인
- Ubuntu VM에서 `docker ps --filter "name=health-center-jenkins"`로 Jenkins 실행 위치 확인
- Windows 로컬 Jenkins에서 실행한 Pipeline은 Windows 로컬 Docker에 배포된 것으로 판단
- VM 배포가 필요하면 Ubuntu VM Jenkins에서 Credentials와 Pipeline Job을 다시 설정

### 10.2 Credentials 오류

- Jenkins Credentials ID가 `health-center-env-file`인지 확인
- Kind가 `Secret file`인지 확인
- `.env.example`이 아니라 실제 `.env`를 업로드했는지 확인

### 10.3 Docker 권한 오류

```bash
docker exec -it health-center-jenkins docker version
```

실패하면:

- `/var/run/docker.sock` mount 확인
- Jenkins 컨테이너가 `user: root`로 실행 중인지 확인
- Ubuntu VM의 Docker daemon 상태 확인

### 10.4 PostgreSQL 18 volume 오류

PostgreSQL 18 계열 이미지는 volume을 아래 경로에 마운트해야 한다.

```text
/var/lib/postgresql
```

아래 경로에 직접 마운트하지 않는다.

```text
/var/lib/postgresql/data
```

초기 검증 중 데이터 보존이 필요 없다면 volume 삭제 후 재시도할 수 있다.

```bash
docker compose --env-file .env down -v
docker compose --env-file .env up -d --build
```

운영 데이터가 있으면 `down -v`를 실행하지 않는다.

### 10.5 Frontend API URL 오류

- `.env`의 `NEXT_PUBLIC_API_BASE_URL`이 브라우저 기준인지 확인한다.
- Windows 브라우저에서 Ubuntu VM에 접속한다면 보통 아래처럼 둔다.

```text
NEXT_PUBLIC_API_BASE_URL=http://<ubuntu-vm-ip>:8080
```

- backend `CORS_ALLOWED_ORIGINS`에 frontend Origin이 포함되어 있는지 확인한다.

```text
CORS_ALLOWED_ORIGINS=http://<ubuntu-vm-ip>:3000,http://localhost:3000
```

---

## 11. 완료 기준

dev에서 main으로 올리기 전:

- [ ] 정적 빌드 확인 완료
- [ ] Docker Compose config 확인 완료
- [ ] Jenkins Job/Credentials 설정 확인 완료
- [ ] PR 본문에 미검증 런타임 항목 명시

main merge 후:

- [ ] Jenkins Pipeline 성공
- [ ] Docker Compose 컨테이너 실행 확인
- [ ] Frontend 접속 확인
- [ ] Swagger 대표 예시 확인
- [ ] 문제 발생 시 로그와 해결 내용을 구현 기록 또는 트러블슈팅 문서에 기록
