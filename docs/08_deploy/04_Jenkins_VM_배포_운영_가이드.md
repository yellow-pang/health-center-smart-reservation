# Jenkins VM 배포 운영 가이드

## 1. 문서 목적

이 문서는 Jenkins를 어디에서 실행해야 Ubuntu VM에 배포되는지 정리한다.

핵심 기준은 아래 한 문장이다.

```text
Jenkins가 배포하는 위치는 Jenkins 컨테이너가 마운트한 /var/run/docker.sock의 Docker host다.
```

따라서 Windows 로컬 Docker Desktop에서 Jenkins를 띄우면 애플리케이션 컨테이너도 Windows 로컬 Docker에 생성된다.
Ubuntu VM에 배포하려면 Jenkins 컨테이너도 Ubuntu VM 내부 Docker Engine 위에서 실행해야 한다.

---

## 2. 올바른 배포 흐름

```text
Windows 작업 폴더
  -> GitHub push
  -> dev -> main PR merge
  -> Ubuntu VM 내부 Jenkins가 main 변경 감지
  -> Ubuntu VM 내부 Jenkins workspace에서 checkout
  -> Ubuntu VM Docker Engine에 docker compose build/up
  -> Windows 브라우저에서 Ubuntu VM IP로 접속
```

접속 URL 예시:

```text
Frontend: http://<ubuntu-vm-ip>:3000
Backend:  http://<ubuntu-vm-ip>:8080
Swagger:  http://<ubuntu-vm-ip>:8080/swagger-ui/index.html
Jenkins:  http://<ubuntu-vm-ip>:8081
```

---

## 3. 로컬 Jenkins와 VM Jenkins의 차이

| 구분 | Windows 로컬 Jenkins | Ubuntu VM Jenkins |
|---|---|---|
| Jenkins 실행 위치 | Windows Docker Desktop | Ubuntu VM Docker Engine |
| Docker socket 대상 | Windows 로컬 Docker | Ubuntu VM Docker |
| 배포되는 위치 | Windows 로컬 컨테이너 | Ubuntu VM 컨테이너 |
| Jenkins home volume | Windows Docker volume | Ubuntu VM Docker volume |
| UI 설정 공유 여부 | VM과 공유되지 않음 | 로컬과 공유되지 않음 |

Git에 저장되는 것은 `Jenkinsfile`, `infra/jenkins/*`, 애플리케이션 코드, 문서다.

아래 항목은 Jenkins 인스턴스의 `jenkins-home` volume에 저장되므로 VM에서 다시 설정해야 한다.

- 관리자 계정
- 설치 플러그인
- Credentials
- Pipeline Job
- Poll SCM 설정

---

## 4. Ubuntu VM에서 Jenkins 실행

Ubuntu VM에 접속한 뒤 repository를 준비한다.

```bash
ssh <ubuntu-user>@<ubuntu-vm-ip>
cd ~/apps
git clone <github-repo-url> health-center-smart-reservation
cd health-center-smart-reservation
git checkout main
git pull
```

Jenkins 컨테이너를 실행한다.

```bash
docker compose -f infra/jenkins/docker-compose.jenkins.yml up -d --build
```

초기 비밀번호를 확인한다.

```bash
docker logs health-center-jenkins
```

브라우저에서 접속한다.

```text
http://<ubuntu-vm-ip>:8081
```

### 4.1 VirtualBox NAT 포트 포워딩

VirtualBox 네트워크가 `NAT` 방식이면 Windows 브라우저에서 Ubuntu VM의 포트로 바로 접근할 수 없다.

이 경우 접속 흐름은 아래처럼 된다.

```text
Windows 브라우저
  -> VirtualBox NAT 포트 포워딩
  -> Ubuntu VM host port
  -> Docker Compose port mapping
  -> container port
```

예를 들어 Jenkins는 두 단계가 모두 필요하다.

```text
Windows localhost:8081
  -> VirtualBox Host 8081 / Guest 8081
  -> Docker Compose 8081:8080
  -> Jenkins container 8080
```

VirtualBox 설정 위치:

```text
VirtualBox
  -> 대상 VM 선택
  -> Settings
  -> Network
  -> Adapter 1
  -> Attached to: NAT
  -> Advanced
  -> Port Forwarding
```

권장 포트 포워딩 규칙:

| Name | Protocol | Host IP | Host Port | Guest IP | Guest Port | 용도 | 필수 여부 |
|---|---|---|---:|---|---:|---|---|
| SSH | TCP | `127.0.0.1` | 2222 | 비움 | 22 | Windows에서 VM SSH 접속 | 선택 |
| Frontend | TCP | `127.0.0.1` | 3000 | 비움 | 3000 | Next.js 화면 접속 | 권장 |
| Backend | TCP | `127.0.0.1` | 8080 | 비움 | 8080 | API/Swagger 접속 | 권장 |
| Jenkins | TCP | `127.0.0.1` | 8081 | 비움 | 8081 | Jenkins UI 접속 | 필수 |
| PostgreSQL | TCP | `127.0.0.1` | 5432 | 비움 | 5432 | Windows DB client 직접 접속 | 선택 |
| Jenkins Agent | TCP | `127.0.0.1` | 50000 | 비움 | 50000 | 외부 Jenkins agent 연결 | 보통 불필요 |

Jenkins UI만 먼저 확인하려면 아래 하나만 있어도 된다.

```text
Jenkins / TCP / Host IP 127.0.0.1 / Host Port 8081 / Guest Port 8081
```

NAT 방식에서 Windows 브라우저 접속 URL:

```text
Frontend: http://localhost:3000
Backend:  http://localhost:8080
Swagger:  http://localhost:8080/swagger-ui/index.html
Jenkins:  http://localhost:8081
```

SSH 접속 예시:

```bash
ssh -p 2222 <ubuntu-user>@127.0.0.1
```

주의:

- VirtualBox 포트 포워딩의 Guest Port는 Docker Compose의 host port와 맞춘다.
- Jenkins Compose는 `8081:8080`이므로 VirtualBox Guest Port는 `8081`이다.
- Backend Compose는 `8080:8080`이므로 VirtualBox Guest Port는 `8080`이다.
- Frontend Compose는 `3000:3000`이므로 VirtualBox Guest Port는 `3000`이다.
- PostgreSQL은 외부 DB client가 필요할 때만 열고, 평소에는 열지 않아도 된다.
- 외부 PC에서도 접속하게 하려면 Host IP를 비우거나 `0.0.0.0`으로 둘 수 있지만, 개인 학습 VM에서는 `127.0.0.1`을 우선 사용한다.

NAT 방식에서는 `.env`의 브라우저 기준 URL도 `localhost` 기준으로 맞춘다.

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

Bridged Adapter 방식에서는 VM IP 기준으로 맞춘다.

```env
NEXT_PUBLIC_API_BASE_URL=http://<ubuntu-vm-ip>:8080
CORS_ALLOWED_ORIGINS=http://<ubuntu-vm-ip>:3000,http://localhost:3000
```

둘 중 하나를 선택한다.

| 네트워크 방식 | Windows 접속 주소 | `.env` 브라우저 기준 URL |
|---|---|---|
| NAT + 포트 포워딩 | `http://localhost:<port>` | `localhost` 기준 |
| Bridged Adapter | `http://<ubuntu-vm-ip>:<port>` | VM IP 기준 |

---

## 5. VM Jenkins 최초 설정

Jenkins UI에서 아래 순서로 설정한다.

1. 초기 비밀번호 입력
2. 추천 플러그인 설치
3. 관리자 계정 생성
4. Credentials 등록
5. Pipeline Job 생성
6. Poll SCM 설정

Credentials:

| 항목 | 값 |
|---|---|
| Kind | Secret file |
| ID | `health-center-env-file` |
| File | 실제 배포용 `.env` |

Pipeline Job:

| 항목 | 값 |
|---|---|
| Definition | Pipeline script from SCM |
| SCM | Git |
| Repository URL | GitHub repository URL |
| Credentials | public repo면 none |
| Branch Specifier | `*/main` |
| Script Path | `Jenkinsfile` |
| Lightweight checkout | checked |
| Poll SCM | `H/5 * * * *` |

---

## 6. 배포 대상 확인

Jenkins가 어느 Docker host에 붙어 있는지 확인한다.

```bash
docker exec -it health-center-jenkins sh -lc "hostname && docker info --format '{{.Name}} / {{.OperatingSystem}}'"
```

확인 기준:

- 명령은 Ubuntu VM 터미널에서 실행한다.
- 출력되는 Docker host가 Ubuntu VM Docker Engine이어야 한다.
- 같은 명령을 Windows 로컬에서 실행하면 Windows Docker Desktop 대상이므로 VM 배포가 아니다.

Jenkins 컨테이너 상태:

```bash
docker ps --filter "name=health-center-jenkins"
```

Jenkins 컨테이너 도구:

```bash
docker exec -it health-center-jenkins docker version
docker exec -it health-center-jenkins docker compose version
docker exec -it health-center-jenkins mvn -version
docker exec -it health-center-jenkins node --version
docker exec -it health-center-jenkins npm --version
```

---

## 7. main merge 후 확인

Jenkins가 Poll SCM 주기에 따라 main 변경을 감지한다.

즉시 확인하려면 Jenkins UI에서 실행한다.

```text
health-center-deploy
  -> Build Now
```

Pipeline 성공 후 Ubuntu VM에서 확인한다.

```bash
docker compose --env-file .env ps
docker ps
```

실행 컨테이너 기준:

- `health-center-postgres`
- `health-center-backend`
- `health-center-frontend`

---

## 8. 문제 상황별 판단

### 8.1 Jenkins는 성공했는데 VM에 컨테이너가 없다

확인할 것:

- Jenkins를 Windows 로컬 Docker에서 실행하지 않았는지 확인한다.
- Ubuntu VM에서 `docker ps --filter "name=health-center-jenkins"`를 실행해 Jenkins 컨테이너가 있는지 확인한다.
- Jenkins Job URL이 `http://<ubuntu-vm-ip>:8081`인지 확인한다.

원인:

```text
Jenkins가 Windows 로컬 Docker socket을 사용하면 Windows 로컬 Docker에 배포된다.
```

해결:

```text
Ubuntu VM 내부에서 Jenkins Compose를 다시 실행하고, VM Jenkins UI에서 Credentials와 Pipeline Job을 다시 만든다.
```

### 8.1.1 VM Jenkins 컨테이너는 실행 중인데 브라우저 접속이 안 되는 경우

VirtualBox 네트워크 방식을 확인한다.

- `Bridged Adapter`면 `http://<ubuntu-vm-ip>:8081`로 접속한다.
- `NAT`면 VirtualBox Port Forwarding에 Jenkins 규칙을 추가하고 `http://localhost:8081`로 접속한다.

NAT Jenkins 포트 포워딩 규칙:

```text
Name: Jenkins
Protocol: TCP
Host IP: 127.0.0.1
Host Port: 8081
Guest IP: 비움
Guest Port: 8081
```

Jenkins만 접속되지 않는다면 먼저 위 규칙만 확인한다.
Frontend, Backend, Swagger까지 확인하려면 `4.1 VirtualBox NAT 포트 포워딩`의 전체 규칙을 적용한다.

### 8.2 VM Jenkins에서 Credentials가 없다

정상이다.

Jenkins Credentials는 Git에 저장되지 않고 Jenkins home volume에 저장된다.
Windows 로컬 Jenkins에 등록한 credentials는 Ubuntu VM Jenkins로 자동 이전되지 않는다.

### 8.3 Pipeline이 main 브랜치가 아니라고 중단된다

Job 설정을 확인한다.

```text
Branch Specifier: */main
Script Path: Jenkinsfile
```

dev 브랜치는 배포 대상이 아니다.
dev에서 검증한 뒤 main에 merge되면 Jenkins 배포가 실행된다.

---

## 9. 완료 기준

- [ ] Ubuntu VM 내부에서 Jenkins 컨테이너 실행
- [ ] VirtualBox NAT 사용 시 Jenkins 8081 포트 포워딩 설정
- [ ] VM Jenkins UI 접속
- [ ] VM Jenkins Credentials `health-center-env-file` 등록
- [ ] VM Jenkins Pipeline Job 생성
- [ ] Jenkins 컨테이너에서 `docker compose version` 확인
- [ ] main merge 또는 Build Now로 Pipeline 실행
- [ ] Ubuntu VM에서 `postgresql`, `backend`, `frontend` 컨테이너 확인
- [ ] Windows 브라우저에서 Frontend/Swagger 대표 흐름 확인
