# CICD Deploy Guide Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `docs/cicd-deploy-guide` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 문서 갱신 진행 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 백엔드 compile | 문서 전용 변경으로 미실행 |
| 백엔드 test-compile | 문서 전용 변경으로 미실행 |
| 프론트 빌드 | 문서 전용 변경으로 미실행 |
| 정적 공백 확인 | `git diff --check` 통과. 기존 문서 파일 CRLF 변환 경고만 표시 |
| GitNexus 확인 | status는 stale, analyze는 로컬 CLI 오류로 갱신 실패 |
| 실행/API 확인 | Docker, Jenkins, 서버 기동, Swagger/브라우저 확인은 사용자 직접 수행 범위 |

## PR 제목

```text
docs: Ubuntu VM Jenkins 배포 가이드와 PR 문서 정리
```

## PR 본문

````markdown
## 개요

Ubuntu VM에서 Jenkins와 Docker Compose로 보건소 스마트 예약·대기 시스템을 배포하기 위한 문서 기준을 정리합니다.

이번 PR은 실제 Dockerfile, Jenkinsfile, 전체 docker-compose 구성 추가 전 단계입니다. 현재 작성된 배포 계획서를 PR로 리뷰할 수 있도록 브랜치 구현 기록과 PR 작성안을 추가하고, 기존 배포 설계서의 오래된 Frontend/Vite 기준을 현재 Next.js 기준으로 정정했습니다.

## 변경 내용

- 기존 배포 설계서의 Frontend 실행 기준을 Next.js 기준으로 수정
- Frontend 환경변수 예시를 `VITE_API_BASE_URL`에서 `NEXT_PUBLIC_API_BASE_URL`로 정정
- Frontend Dockerfile 초안을 Next.js `npm run build`/`npm run start` 기준으로 수정
- Ubuntu VM Jenkins Docker Compose 계획서에 이번 브랜치 범위 확정 내용 추가
- CI/CD 배포 가이드 PR 진행용 브랜치 구현 기록 추가
- PR 제목, 본문, 검증, 미검증 사유, 후속 작업 초안 추가
- 전체 체크리스트의 운영/배포 문서 진행 상태 갱신

## 검증

- [x] 관련 문서 확인: `docs/README.md`, 에이전트 운영 가이드, 브랜치 기록 가이드
- [x] 전체 체크리스트 관련 항목 확인
- [x] 현재 브랜치 확인: `docs/cicd-deploy-guide`
- [x] 루트 `docker-compose.yml`이 현재 PostgreSQL 단일 서비스만 포함함을 확인
- [x] 프론트엔드가 Next.js와 `NEXT_PUBLIC_API_BASE_URL` 기준임을 확인
- [x] 배포 설계서와 CI/CD 계획서 간 기준 정렬
- [x] GitNexus status 확인 및 analyze 시도
- [x] `git diff --check`

## 미검증 사유

- 이번 PR은 문서 전용 변경이므로 Maven compile/test-compile과 Next build는 실행하지 않았습니다.
- Docker Compose 실행, Jenkins 실행, 서버 기동, Swagger/브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- GitNexus MCP 리소스가 현재 도구 목록에 노출되지 않아 `gitnexus_detect_changes()`는 수행하지 못했습니다. CLI status는 stale 상태였고 analyze는 로컬 CLI 오류로 갱신하지 못했습니다. 대신 문서 변경 범위는 `git status`, `git diff --stat`, `git diff --check`로 확인했습니다.

## 사용자 확인 체크리스트

- [ ] PR 본문에서 이번 브랜치가 문서 기준 확정 작업임을 확인
- [ ] 후속 브랜치 1순위를 `.env.example`과 환경변수 placeholder 정리로 진행할지 확인
- [ ] Jenkins `.env` 주입 방식을 Jenkins Credentials 파일 기준으로 유지할지 확인
- [ ] Ubuntu VM 접속 URL 기준을 `http://<ubuntu-vm-ip>:3000`, `http://<ubuntu-vm-ip>:8080`, `http://<ubuntu-vm-ip>:8081`로 사용할지 확인

## 후속 작업

- `.env.example`, backend 환경변수 placeholder, CORS 허용 Origin 기준 정리
- `backend/Dockerfile`, `frontend/Dockerfile`, `.dockerignore` 작성
- 루트 `docker-compose.yml`을 backend/frontend/postgresql 전체 구성으로 확장
- PostgreSQL init SQL과 Docker volume 초기화 절차 문서화
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
docs: Ubuntu VM Jenkins 배포 가이드 PR 문서 정리
```

본문:

```text
- 기존 배포 설계서의 Frontend 환경변수와 Dockerfile 초안을 Next.js 기준으로 정정
- Ubuntu VM Jenkins Docker Compose 계획서에 이번 브랜치 범위 확정 내용 추가
- CI/CD 배포 가이드 PR 진행용 구현 기록과 PR 작성안 추가
- 전체 체크리스트에 운영/배포 문서 진행 상태 반영
```
