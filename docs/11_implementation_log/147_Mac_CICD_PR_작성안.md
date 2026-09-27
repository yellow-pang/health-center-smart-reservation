# Mac CI/CD PR 작성안

base: `dev`, head: `feat/mac-cicd`. 실제 commit/push/PR 생성 전 초안.

## 제목

feat: main 반영 시 GitHub Actions로 Mac ARM64 배포

## 본문

Mac에서는 수동 local image만 실행되어 main 반영이 운영에 전달되지 않았습니다. dev/PR에서 실제 테스트·타입·빌드를 검사하고, main 커밋 검증 후 GHCR digest 이미지를 전용 Mac runner가 배포하도록 구현합니다.

운영 배포는 기존 PostgreSQL/관측성을 유지하면서 앱만 교체하고, DB 백업·release 식별·헬스 확인·앱 복구를 수행합니다. prod의 반복 demo seed 실행을 Flyway migration으로 분리하여 재배포가 기존 계정/예약 데이터를 초기화하지 않게 했습니다. 최초 전환은 명시적 DB 채택 허용이 필요합니다.

## 검증

- [x] backend 단위 26건, PostgreSQL 18 migration integration 2건
- [x] frontend npm ci/lint/typecheck/build (lint 기존 경고 20개)
- [x] backend/frontend ARM64 이미지 빌드, 격리 healthcheck
- [x] Actions 구문·shell syntax, 배포 28건/설정 13건/최신 main 3건 테스트
- [x] CI backend smoke helper 성공 및 잘못된 기존 DB 자동 채택 거부
- [x] git diff --check
- [ ] 실제 PR/main GitHub Actions 실행과 GHCR 권한 확인
- [ ] 전용 runner/production 환경 등록 후 운영 첫 배포
- [ ] 기존 DB/관측성 유지, 배포 SHA, 공개 UI/API 확인
- [ ] 실제 운영 앱 복구 검증

## 적용

[운영 가이드](../08_deploy/14_GitHub_Actions_Mac_CICD.md)의 순서로 dev 반영 → 최초 설정 → main PR을 진행합니다. 기존 Jenkinsfile은 VM 배포를 중단하므로 새 runner 준비 상태를 확인한 뒤 main으로 반영합니다. GitHub 기본 브랜치 dev는 유지합니다.

DB migration 이후 이미지 복구가 스키마/데이터를 되돌리지는 않습니다. 공개 저장소 runner는 외부 PR 승인 검토가 필요하며, Mac 로그인/OrbStack 의존성과 짧은 앱 재시작 중단이 있습니다.
