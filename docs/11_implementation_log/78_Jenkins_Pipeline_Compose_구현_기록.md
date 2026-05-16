# Jenkins Pipeline Compose 구현 기록

## 1. 작업 목표

- `infra/jenkins-pipeline` 브랜치에서 Jenkins 컨테이너를 Docker Compose 기반으로 실행할 수 있게 한다.
- Jenkins 컨테이너 안에서 Maven, Node.js, Docker CLI, Docker Compose v2를 사용할 수 있게 커스텀 이미지를 작성한다.
- main 브랜치 기준으로 backend/frontend 정적 빌드 후 Docker Compose 배포를 수행하는 `Jenkinsfile`을 작성한다.

## 2. 전체 체크리스트 관련 항목 확인

`docs/13_schedule/02_전체_작업_체크리스트.md` 기준 관련 항목은 아래와 같다.

- `9. 배포와 운영 작업`
  - Docker Compose 실행 정리
  - 환경변수 정리
  - 운영 체크리스트 작성
- `12.4 운영/배포 문서`
  - Docker Compose 실행 절차 문서화
  - Backend/Frontend/PostgreSQL 환경변수 문서화
  - 로컬 실행 README 보강

이번 브랜치는 README/Notion 문서까지 확장하지 않고 Jenkins 커스텀 이미지, Jenkins Compose, Jenkinsfile 작성까지 처리한다.

## 3. 작업 범위

포함한다.

- [x] 현재 브랜치 확인
- [x] Jenkins 구성 계획 문서 확인
- [x] `infra/jenkins/Dockerfile` 작성
- [x] `infra/jenkins/docker-compose.jenkins.yml` 작성
- [x] `Jenkinsfile` 작성
- [x] Jenkins Credentials 파일 주입 방식 기준 확정
- [x] main 브랜치 전용 배포 조건 작성
- [x] backend `mvn test-compile` / package 단계 작성
- [x] frontend `npm ci` / build 단계 작성
- [x] `docker compose --env-file .env config/build/up` 단계 작성
- [x] `git diff --check` 확인
- [x] 브랜치 구현 기록과 PR 문서 초안 작성
- [x] 전체 체크리스트와 배포 계획서 갱신

제외한다.

- [ ] Jenkins 컨테이너 실행
- [ ] Jenkins 초기 비밀번호 확인
- [ ] Jenkins 플러그인 설치
- [ ] Jenkins Credentials 등록
- [ ] Jenkins Job/Multibranch Pipeline 생성
- [ ] Docker socket 권한 런타임 확인
- [ ] Pipeline 실제 실행
- [ ] README 배포 섹션 작성
- [ ] 실제 커밋, push, 배포

## 4. 현재 상태 확인

| 구분 | 확인 결과 |
|---|---|
| 현재 브랜치 | `infra/jenkins-pipeline` |
| 애플리케이션 Compose | `postgresql`, `backend`, `frontend` 서비스 포함 |
| 환경변수 주입 기준 | Jenkins Credentials secret file, credentials ID `health-center-env-file` |
| Jenkins 실행 포트 | Host `8081` -> container `8080` |
| Docker 실행 방식 | Jenkins 컨테이너에 Docker socket mount |
| 테스트 명령 | backend `mvn -q test-compile`, frontend `npm ci && npm run build` |
| 배포 대상 기준 | Jenkins 컨테이너가 마운트한 `/var/run/docker.sock`의 Docker host |

## 5. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `infra/jenkins/Dockerfile` | Jenkins LTS JDK17 기반 이미지에 Maven, Node.js 20, Docker CLI, Docker Compose plugin 설치 |
| `infra/jenkins/docker-compose.jenkins.yml` | Jenkins 컨테이너, Jenkins home, Maven/NPM cache volume, Docker socket mount 구성 |
| `Jenkinsfile` | checkout, main 브랜치 확인, 도구 버전 확인, backend/frontend 빌드, `.env` credentials 주입, Docker Compose config/build/up, image prune 단계 작성 |
| `docs/08_deploy/02_Ubuntu_VM_Jenkins_Docker_Compose_배포_계획서.md` | Jenkins 구성과 Jenkinsfile 단계 진행 상태 갱신 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 운영/배포 진행 상태 갱신 |

## 6. 검증 체크리스트

- [x] `git status --short --branch`
- [x] 관련 배포 문서 확인
- [x] `git diff --check`
- [ ] Jenkins image build
- [ ] Jenkins Compose 실행
- [ ] Jenkins 컨테이너에서 `docker compose version` 확인
- [ ] Jenkins Credentials 등록
- [ ] Pipeline 실제 실행

비고:

- Jenkins 실행, Docker socket 확인, Pipeline 실행은 프로젝트 운영 기준상 사용자가 직접 수행한다.
- Jenkinsfile은 `health-center-env-file` ID의 secret file credential이 등록되어 있어야 동작한다.
- `Docker socket` mount는 로컬 VM 학습 환경용이며 운영 환경에서는 권한 위험을 별도 검토해야 한다.
- Windows 로컬 Jenkins에서 Pipeline을 실행하면 Windows 로컬 Docker에 배포된다. Ubuntu VM 배포는 Ubuntu VM 내부에서 실행한 Jenkins 컨테이너로 확인한다.

## 7. 사용자 확인 안내

Jenkins 컨테이너 실행:

```bash
docker compose -f infra/jenkins/docker-compose.jenkins.yml up -d --build
```

초기 비밀번호 확인:

```bash
docker logs health-center-jenkins
```

Jenkins 접속:

```text
http://<ubuntu-vm-ip>:8081
```

Jenkins 컨테이너 도구 확인:

```bash
docker exec -it health-center-jenkins docker version
docker exec -it health-center-jenkins docker compose version
docker exec -it health-center-jenkins mvn -version
docker exec -it health-center-jenkins node --version
```

Jenkins Credentials:

| 항목 | 값 |
|---|---|
| Kind | Secret file |
| ID | `health-center-env-file` |
| File | 실제 배포용 `.env` |

권장 Job:

- Multibranch Pipeline 또는 Pipeline from SCM
- Poll SCM: `H/5 * * * *`
- 배포는 `main` 브랜치에서만 수행

## 8. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| Jenkins 이미지 작성 중 | Maven과 Node.js 20을 Jenkins 이미지에 포함 | Jenkinsfile의 backend/frontend 빌드 단계를 같은 agent에서 실행하기 위함 | 이번 브랜치에서 반영 |
| Jenkinsfile 작성 중 | `.env`는 Jenkins secret file credentials로 주입 | Git checkout workspace에는 `.env`가 없고 민감 정보를 Git에 올리지 않기 위함 | 이번 브랜치에서 반영 |
| Jenkinsfile 작성 중 | `docker compose down` 대신 `up -d --remove-orphans` 사용 | 배포 중 불필요한 전체 중단을 줄이고 Compose가 필요한 컨테이너만 갱신하게 하기 위함 | 이번 브랜치에서 반영 |
| Jenkins 수동 설정 확인 중 | VM 배포 기준 문서 보강 필요 | Jenkins UI 설정은 Jenkins home volume별로 분리되고, 배포 대상은 Docker socket이 가리키는 host로 결정됨 | `docs/08_deploy/04_Jenkins_VM_배포_운영_가이드.md` 추가 |

## 9. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] PR 문서 작성
- [x] 전체 체크리스트 갱신
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
- [ ] 사용자 코드 점검 반영

## 10. 다음 작업 후보

1. Jenkins 수동 실행 결과와 트러블슈팅 기록
2. README 배포 섹션 작성
3. Notion 정리용 배포 문서 초안 작성
