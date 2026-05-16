# Jenkins Pipeline Compose Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `infra/jenkins-pipeline` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | Jenkins 인프라/문서 변경 진행 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 정적 공백 확인 | `git diff --check` 통과 |
| 실행 확인 | Jenkins image build, Jenkins Compose 실행, Pipeline 실행은 사용자 직접 수행 범위 |

## PR 제목

```text
chore: Jenkins 배포 파이프라인 구성 추가
```

## PR 본문

````markdown
## 개요

Ubuntu VM에서 Jenkins 컨테이너가 main 브랜치 변경을 기준으로 backend/frontend 빌드와 Docker Compose 배포를 수행할 수 있도록 Jenkins 커스텀 이미지, Jenkins Compose, Jenkinsfile을 추가합니다.

이번 PR은 Jenkins 실행 파일과 Pipeline 정의를 추가하는 단계입니다. Jenkins 최초 설정, Credentials 등록, Job 생성, Docker socket 런타임 검증은 사용자가 직접 수행합니다.

## 변경 내용

- Jenkins LTS JDK17 기반 커스텀 Dockerfile 추가
- Jenkins 이미지에 Maven, Node.js 20, Docker CLI, Docker Compose plugin 설치
- Jenkins 전용 Compose 파일 추가
- Jenkins home, Maven cache, NPM cache volume 구성
- Docker socket mount 구성
- main 브랜치 전용 Jenkinsfile 추가
- backend `mvn test-compile` / package 단계 작성
- frontend `npm ci` / `npm run build` 단계 작성
- Jenkins Credentials secret file로 `.env` 주입
- `docker compose --env-file .env config/build/up` 배포 단계 작성
- 배포 계획서와 전체 체크리스트 갱신
- 브랜치 구현 기록과 PR 작성안 추가

## 검증

- [x] 현재 브랜치 확인: `infra/jenkins-pipeline`
- [x] Jenkins 배포 계획 문서 확인
- [x] `git diff --check`

## 미검증 사유

- Jenkins image build, Jenkins Compose 실행, Jenkins UI 초기 설정, Credentials 등록, Pipeline 실행은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- Docker socket mount는 로컬 VM 학습 환경용이며, 운영 환경에서는 별도 권한 검토가 필요합니다.

## 사용자 확인 체크리스트

- [ ] `docker compose -f infra/jenkins/docker-compose.jenkins.yml up -d --build`
- [ ] `docker logs health-center-jenkins`로 초기 비밀번호 확인
- [ ] `http://<ubuntu-vm-ip>:8081` 접속
- [ ] 추천 플러그인 설치
- [ ] Secret file credential 등록: `health-center-env-file`
- [ ] Jenkins 컨테이너에서 `docker compose version` 확인
- [ ] Pipeline from SCM 또는 Multibranch Pipeline 생성
- [ ] Poll SCM `H/5 * * * *` 설정
- [ ] main 브랜치에서 Pipeline 실행 확인

## 후속 작업

- Jenkins 수동 실행 결과와 트러블슈팅 기록
- dev to main 배포 전 확인 체크리스트 기준으로 main merge 전 점검
- README 배포 섹션 작성
- Notion 정리용 배포 문서 초안 작성
- Jenkinsfile 실패 로그 확인 절차 문서화
````

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 후속 브랜치 생성 여부 결정

## 커밋 메시지 초안

제목:

```text
chore: Jenkins 배포 파이프라인 구성 추가
```

본문:

```text
- Jenkins 커스텀 Dockerfile과 Compose 파일 추가
- Jenkins 이미지에 Maven, Node.js 20, Docker CLI, Docker Compose plugin 설치
- main 브랜치 전용 Jenkinsfile 작성
- Jenkins Credentials secret file로 .env 주입
- backend/frontend 빌드와 docker compose 배포 단계 구성
- 배포 계획서, 브랜치 기록, PR 작성안, 전체 체크리스트 갱신
```
