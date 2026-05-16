# CICD Deploy Guide PR 문서 기록

## 1. 작업 목표

- `docs/cicd-deploy-guide` 브랜치에서 CI/CD 배포환경 가이드를 PR로 제출할 수 있도록 구현 기록과 PR 작성안을 정리한다.
- 배포 파일 구현 전 단계로, 현재 문서와 코드 상태를 대조해 다음 브랜치에서 진행할 범위를 명확히 한다.
- 이번 브랜치에서는 서버 기동, Docker 실행, Jenkins 실행, API 런타임 호출은 수행하지 않는다.

## 2. 전체 체크리스트 관련 항목 확인

`docs/13_schedule/02_전체_작업_체크리스트.md` 기준 관련 항목은 아래와 같다.

- `9. 배포와 운영 작업`
  - Docker Compose 실행 정리
  - 환경변수 정리
  - 운영 체크리스트 작성
- `10. 포트폴리오와 PR 작업`
  - 브랜치별 구현 기록 작성
  - 브랜치별 PR 문서 작성
- `12.4 운영/배포 문서`
  - Docker Compose 실행 절차 문서화
  - Backend/Frontend/PostgreSQL 환경변수 문서화
  - Swagger 검증 순서와 기본 계정 문서화
  - 로컬 실행 README 보강
  - 운영 로그/장애 대응/데이터 초기화 기준 문서화

이번 브랜치는 위 항목 중 “배포 문서 기준 확정”과 “브랜치별 PR 문서 작성”까지만 처리한다. Dockerfile, `docker-compose.yml`, Jenkinsfile, `.env.example` 작성은 후속 브랜치에서 진행한다.

## 3. 이번 브랜치 작업 범위 제안

포함한다.

- [x] `docs/README.md`와 에이전트 운영 기준 문서 확인
- [x] 전체 체크리스트의 운영/배포 문서 관련 항목 확인
- [x] 기존 배포 설계서와 Ubuntu VM Jenkins Docker Compose 계획서 확인
- [x] 현재 코드 상태 확인
- [x] 현재 프론트엔드가 Next.js와 `NEXT_PUBLIC_API_BASE_URL` 기준임을 문서에 반영
- [x] 브랜치 구현 기록 작성
- [x] PR 문서 초안 작성
- [x] 전체 체크리스트 진행 상태 갱신

제외한다.

- [ ] `backend/Dockerfile`, `frontend/Dockerfile` 실제 생성
- [ ] `docker-compose.yml`을 backend/frontend/postgresql 전체 구성으로 확장
- [ ] `.env.example`, Jenkinsfile, Jenkins Compose 파일 추가
- [ ] Docker/Jenkins/서버/API/Swagger 런타임 검증
- [ ] 실제 커밋, push, 배포

## 4. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 확인
- [x] `docs/08_deploy/01_배포_설계서.md` 확인
- [x] `docs/08_deploy/02_Ubuntu_VM_Jenkins_Docker_Compose_배포_계획서.md` 확인
- [x] 현재 브랜치 확인: `docs/cicd-deploy-guide`
- [x] 작업 트리 확인

## 5. 관련 문서와 현재 코드 상태 확인

| 구분 | 확인 결과 |
|---|---|
| 현재 브랜치 | `docs/cicd-deploy-guide` |
| 작업 트리 | 작업 시작 시 변경 파일 없음 |
| 루트 `docker-compose.yml` | 현재 PostgreSQL 단일 서비스만 정의 |
| Frontend | `frontend/package.json` 기준 Next.js 16, `npm run build`, `npm run start` 사용 |
| Frontend API URL | `frontend/src/lib/api-client.ts`에서 `NEXT_PUBLIC_API_BASE_URL` 사용 |
| Backend | eGovFrame Simple Backend Template 기반 Maven 구조 유지 |
| CORS | `backend/src/main/java/egovframework/com/security/SecurityConfig.java`에 CORS 설정 존재, 환경변수화 여부는 후속 확인 필요 |
| 배포 설계서 | 기존 `VITE_API_BASE_URL`, Vite `dist`/`serve` 기준이 남아 있어 현재 코드와 불일치 |
| CI/CD 계획서 | Ubuntu VM + Jenkins + Docker Compose 단계별 계획은 작성되어 있고, 실제 파일 생성은 후속 브랜치 범위로 남아 있음 |

## 6. 문서 수정 내용

| 파일 | 변경 내용 |
|---|---|
| `docs/08_deploy/01_배포_설계서.md` | Frontend 기준을 React/Vite 실행 예시에서 현재 Next.js 실행 기준으로 정정하고, `NEXT_PUBLIC_API_BASE_URL`과 Next.js Dockerfile 초안으로 변경 |
| `docs/08_deploy/02_Ubuntu_VM_Jenkins_Docker_Compose_배포_계획서.md` | 1단계의 “다음 작업 브랜치 범위 확정”을 완료 처리하고 이번 브랜치 범위를 명시 |
| `docs/11_implementation_log/72_CICD_Deploy_Guide_PR_문서_기록.md` | 이번 브랜치 구현 기록과 체크리스트 작성 |
| `docs/11_implementation_log/73_CICD_Deploy_Guide_PR_작성안.md` | PR 제목, 본문, 검증, 미검증 사유, 후속 작업 초안 작성 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 운영/배포 문서와 진행 중 발견된 추가 작업에 이번 문서 정리 상태 반영 |

## 7. 검증 체크리스트

- [x] `git status --short --branch`로 브랜치와 작업 트리 확인
- [x] GitNexus 상태 확인
- [x] `rg`로 배포 환경변수와 관련 파일 상태 확인
- [x] 문서 변경 범위 확인
- [x] `git diff --check` 확인
- [ ] GitNexus `detect_changes` 확인
- [ ] Maven compile 확인
- [ ] Maven test-compile 확인
- [ ] Docker Compose 실행 확인
- [ ] Jenkins 실행 확인
- [ ] Swagger/브라우저 런타임 확인

비고:

- 이번 브랜치는 문서 전용 변경이므로 Maven compile/test-compile은 필수 완료 조건으로 두지 않는다.
- Docker Compose, Jenkins, Swagger/브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행한다.
- GitNexus MCP 리소스가 현재 도구 목록에 노출되지 않아 `detect_changes`는 직접 수행하지 못했다. 대신 `git diff --check`와 `git diff --stat`으로 문서 변경 범위를 확인한다.
- `git diff --check`는 통과했다. 다만 기존 문서 파일에 대해 LF가 CRLF로 바뀔 수 있다는 Git 경고가 표시되었다.
- `npm.cmd exec -- gitnexus status`는 stale 상태를 보고했다. 지침에 따라 `npm.cmd exec -- gitnexus analyze`, `npx.cmd gitnexus analyze`, `npm.cmd exec -- gitnexus analyze .`를 시도했지만 로컬 CLI가 Git repository 인식 실패 또는 비정상 종료를 반환해 갱신하지 못했다.

## 8. 사용자 코드 점검 결과

| 점검 시점 | 사용자 의견 | 반영 여부 |
|---|---|---|
| 브랜치 시작 | CI/CD 배포환경 가이드를 PR로 진행하기 위한 문서 필요 | 반영 |

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 배포 설계서 확인 중 | `docs/08_deploy/01_배포_설계서.md`의 Vite 기준을 Next.js 기준으로 정정 | 현재 프론트엔드는 Next.js와 `NEXT_PUBLIC_API_BASE_URL`을 사용하므로 PR 문서 전 기준 불일치를 줄여야 함 | 이번 브랜치에서 문서 수정 |
| 현재 코드 상태 확인 중 | CORS 허용 Origin 환경변수화 여부 확인 | VM 배포 시 `http://<ubuntu-vm-ip>:3000` 허용이 필요할 수 있음 | 후속 환경변수 분리 브랜치로 이관 |
| 현재 코드 상태 확인 중 | 루트 README 현재 상태 갱신 필요 | README가 아직 설계 단계/JPA 기준으로 되어 있어 현재 구현 상태와 차이가 있음 | 후속 README 배포 섹션 보강 브랜치로 이관 |

## 10. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] PR 문서 작성
- [x] 전체 체크리스트 갱신
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] `git diff --check` 확인
- [ ] 사용자 코드 점검 반영
- [ ] 커밋 메시지 정리

## 11. 다음 작업 후보

1. `.env.example`, backend 환경변수 placeholder, CORS 허용 Origin 기준 정리
2. `backend/Dockerfile`, `frontend/Dockerfile`, `.dockerignore` 작성
3. 루트 `docker-compose.yml`을 backend/frontend/postgresql 전체 실행 구조로 확장
