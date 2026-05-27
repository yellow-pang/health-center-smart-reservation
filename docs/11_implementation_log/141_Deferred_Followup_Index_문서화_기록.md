# 후속작업 통합 색인 문서화 기록

## 1. 작업 목표

- 최신 `dev` 커밋 상태에서 `docs/11_implementation_log` 문서들에 흩어진 후속작업 제안을 다시 확인한다.
- 새로 완료된 자동 NO_SHOW 배치, 대기열 조회 최적화, 관측성 스택, QR/바코드 작업을 반영해 미완 후속 후보를 재분류한다.
- 사용자가 배포 환경과 k6로 확인한 테스트 항목은 매우 높은 우선순위가 아니면 미완 후보에서 제외하거나 낮은 우선순위로 정리한다.
- 로그인 페이지에서 관리자/테스트 계정 비밀번호 노출 문구를 제거한다.
- `docs/14_deferred_cleanup` 아래 새 통합 색인 문서와 PR 참고 초안을 다시 작성한다.

## 2. 현재 git 커밋 상태

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `dev` |
| 원격 비교 | `dev...origin/dev` |
| 작업 트리 | 깨끗함 |
| HEAD | `bf1e058 Merge pull request #66 from yellow-pang/feat/auto-close-pending-queue-batch` |
| 최근 반영 | 자동 NO_SHOW 배치, 배포 후 상태 확인 가이드, 대기열 조회 성능 개선, 관측성 스택 보정 |

## 3. 이번 dev 문서 작업 범위

포함한다.

- [x] `docs/13_schedule/02_전체_작업_체크리스트.md`의 최신 남은 작업 확인
- [x] 구현 로그의 후속작업, 남은 위험, 미검증 사유 확인
- [x] `docs/14_deferred_cleanup/02_후속작업_통합_정리.md` 작성
- [x] `docs/14_deferred_cleanup/01_보류_정리_목록.md`에서 새 색인 문서 연결
- [x] `docs/README.md`에 새 색인 문서 연결
- [x] 배포/k6 확인済 항목의 문서상 우선순위 조정
- [x] 로그인 페이지 테스트 비밀번호 문구 제거
- [x] 전체 체크리스트에 이번 문서 정리 결과 반영
- [x] PR 작성안 형태의 참고 문서 작성

제외한다.

- [ ] 백엔드 API 구현 또는 DB schema 변경
- [ ] 신규 프론트 화면 구현
- [ ] 서버 기동, Docker 실행, Swagger 런타임 호출, 브라우저 확인
- [ ] Jenkins Pipeline 실행 또는 k6 VM 부하 테스트
- [ ] 실제 커밋, push, 배포

## 4. 브랜치별 체크리스트 초안

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 기준 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 기준 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 확인
- [x] `docs/14_deferred_cleanup/01_보류_정리_목록.md` 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] 보건소 백엔드 도메인 구조 확인
- [x] 후속작업 통합 문서 작성
- [x] 로그인 페이지 문구 수정
- [x] 전체 체크리스트 갱신
- [x] PR 참고 문서 초안 작성
- [x] 정적 확인 수행

## 5. 관련 문서와 현재 코드 상태

확인한 문서:

- `docs/README.md`
- `docs/09_agent/05_문서기반_자동진행_운영가이드.md`
- `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md`
- `docs/13_schedule/02_전체_작업_체크리스트.md`
- `docs/14_deferred_cleanup/01_보류_정리_목록.md`
- `docs/11_implementation_log` 하위 구현 기록과 PR 작성안의 후속 작업/미검증 항목

현재 코드 상태:

- 백엔드 보건소 도메인: `backend/src/main/java/egovframework/healthcenter` 하위에 `common`, `dashboard`, `member`, `office`, `queue`, `reservation`, `visit` 존재
- 프론트엔드: `frontend/app`, `frontend/src`, `frontend/components` 기반 Next.js 구조 존재
- 배포/운영 문서: `docs/08_deploy/07_배포후_상태확인_문제해결_가이드.md`까지 존재

## 6. 문서 수정 내용

| 파일 | 변경 내용 |
|---|---|
| `docs/14_deferred_cleanup/02_후속작업_통합_정리.md` | 최신 구현 로그와 보류 목록에 흩어진 후속작업 후보를 `FU-001`부터 `FU-024`까지 통합 색인으로 정리 |
| `docs/14_deferred_cleanup/01_보류_정리_목록.md` | 새 후속작업 통합 색인 문서 연결 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 후속작업 통합 색인 문서화 항목을 MVP 이후 고도화 후보와 진행 기록에 반영 |
| `docs/README.md` | `14_deferred_cleanup/02_후속작업_통합_정리.md`를 문서 읽는 순서와 코드 작성 전 확인 문서에 추가 |
| `frontend/app/login/page.tsx` | 테스트 계정 공통 비밀번호 안내 문구 제거 |
| `docs/11_implementation_log/141_Deferred_Followup_Index_문서화_기록.md` | 이번 문서 작업의 범위, 체크리스트, 검증 결과 기록 |
| `docs/11_implementation_log/142_Deferred_Followup_Index_PR_작성안.md` | PR 제목, 본문, 검증, 후속 작업 초안 작성 |

## 7. 검증 결과

- [x] `git status --short --branch`
- [x] `git log --oneline --decorate -8`
- [x] `rg`로 후속작업, 미검증, 보류, 고도화 후보 확인
- [x] 백엔드/프론트엔드 주요 디렉터리 구조 확인
- [x] `git diff --check`
- [ ] `npm.cmd run build`: 실패. 현재 로컬 `node_modules`에서 `jsbarcode`, `qrcode` 모듈을 찾지 못해 Turbopack build가 중단됐다. 이번 로그인 문구 수정과 직접 관련 없는 기존 QR/바코드 컴포넌트 의존성 문제로 보고 추가 재시도는 하지 않는다.
- [ ] Maven compile/test-compile: 문서-only 변경이라 미수행
- [ ] Swagger/브라우저 확인: 문서-only 변경이며 사용자 직접 수행 범위

## 8. 남은 위험과 후속 작업

- `02_후속작업_통합_정리.md`는 색인 문서이므로, 실제 완료 상태의 원장은 계속 `01_보류_정리_목록.md`와 전체 체크리스트에서 관리한다.
- 다음 구현 후보는 프론트 빌드 경고 정리, 자동 NO_SHOW 배치 dry-run/count API, 선택적 배포/k6 증적 보강, DB 통합/동시성 테스트 중 하나로 작게 고른다.

## 9. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
