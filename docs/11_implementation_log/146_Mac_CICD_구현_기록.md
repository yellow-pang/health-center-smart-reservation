# Mac CI/CD 구현 기록

작성일: 2026-09-27. 브랜치: `feat/mac-cicd`, base: `dev` (`3766890`).

## 목표와 범위

dev/PR에서 실제 검증을 수행하고 main 반영 시 GHCR ARM64 이미지를 Mac에 배포하는 코드를 구현한다. 현재 운영 DB/관측성/Tunnel은 유지한다. 운영 가이드는 [14_GitHub_Actions_Mac_CICD.md](../08_deploy/14_GitHub_Actions_Mac_CICD.md)를 따른다.

## 구현

- [x] 새 브랜치 생성/체크아웃, 기존 dev 작업 트리 clean 확인
- [x] 루트 CI 및 main 배포 workflow, SHA로 고정한 액션, 필수 CI 집계
- [x] Java 실제 테스트, 격리 PostgreSQL migration 검증, prod startup smoke
- [x] Node 24, ESLint, TypeScript 검사 및 빌드 오류 차단
- [x] prod Flyway 관리, demo seed 분리, 최초 baseline 명시 허용
- [x] backend/frontend Docker healthcheck
- [x] 고정 운영 경로, 기존 DB/네트워크 확인, 앱만 교체
- [x] digest/OCI revision 확인, 배포 전 백업, 자동·수동 앱 복구
- [x] 최초 설정 스크립트와 중복 VM 배포 방지 Jenkins guard
- [x] 운영 가이드·README·전체 체크리스트 갱신

## 검증

- 단위 테스트 26건 및 PostgreSQL 18 integration 2건 성공. 새 DB seed 계정 0건, 참조 코드 27건, 기존 계정 비밀번호/예약/커스텀 코드 보존 확인.
- Node 24의 npm ci, lint(오류 0/기존 경고 20), typecheck, production build 성공.
- ARM64 backend/frontend 이미지 빌드 및 격리 컨테이너 healthcheck 성공. 실제 운영 컨테이너와 volume은 변경하지 않음.
- GitHub Actions actionlint 및 shell syntax 통과. 배포 실패·복구 28건, 설정 정책 13건, 최신 main 검증 3건 통과.
- 실제 `backend-smoke.sh` 성공과 자동 baseline 거부를 격리 DB/JDK 환경에서 확인.

## 완료되지 않은 외부 작업

- [ ] commit/push 및 실제 GitHub PR CI 실행
- [ ] GitHub production/main 보호 정책과 Mac 전용 runner 등록
- [ ] 운영 env 준비/최초 채택 허용, main 첫 배포 및 실제 운영 복구 확인

로컬 구현 완료와 운영 자동 배포 활성화를 구분한다. 현재 요청은 새 브랜치 구현이며 commit/push/운영 적용은 수행하지 않았다. 브랜치 종료 전 사용자 코드 점검은 아직 받지 않았다.

위 목록은 2026-09-27 구현 당시 상태다. 이후 PR #69 병합·실제 PR/dev CI 성공과 최초 설정 중 발생한 HTTP 422 수정/검증은 [148 후속 실행 기록](148_GitHub_Main_Protection_HTTP422_수정_기록.md)에서 확인한다.

## 운영 변화와 한계

- 루트 Compose 기본은 dev 시연 환경. prod 앱은 별도 배포 Compose를 사용한다.
- prod는 매 시작마다 demo 계정/예약/슬롯을 재생성하지 않는다. 운영 초기 데이터와 슬롯 운영은 명시적으로 관리한다.
- 이미지 rollback은 DB rollback이 아니다. 백업 archive 확인만으로 restore drill 완료라 하지 않는다.
- Mac 로그인/OrbStack 기동에 의존하며 자동 무인 재부팅 한계는 유지된다.
- 외부 PR workflow 승인 검토가 필요하다. public repo의 runner label은 보안 격리를 제공하지 않는다.
- 공개 URL만 실패하면 정상 origin은 유지하고 배포 job은 실패로 표시한다.
