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
- [ ] VM Jenkins UI 접속
- [ ] VM Jenkins Credentials `health-center-env-file` 등록
- [ ] VM Jenkins Pipeline Job 생성
- [ ] Jenkins 컨테이너에서 `docker compose version` 확인
- [ ] main merge 또는 Build Now로 Pipeline 실행
- [ ] Ubuntu VM에서 `postgresql`, `backend`, `frontend` 컨테이너 확인
- [ ] Windows 브라우저에서 Frontend/Swagger 대표 흐름 확인
