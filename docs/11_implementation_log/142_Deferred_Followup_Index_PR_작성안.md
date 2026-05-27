# 후속작업 통합 색인 문서화 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `dev` |
| base 브랜치 | 별도 브랜치 없이 `dev`에서 진행 |
| HEAD | `bf1e058 Merge pull request #66 from yellow-pang/feat/auto-close-pending-queue-batch` |
| 작업 트리 | 후속작업 통합 색인 문서, 관련 체크리스트, 로그인 페이지 문구 갱신 |
| 주요 커밋 | 커밋 전 |
| 실행/API 확인 | 문서-only 변경이라 서버 기동, Docker, Swagger, 브라우저 확인 없음 |

## PR 제목

```text
docs: 후속작업 통합 색인 문서화
```

## PR 본문

```markdown
## 개요

최신 `dev` 커밋 기준으로 구현 로그와 PR 작성안에 흩어진 후속작업 제안을 다시 확인하고, 아직 남은 기능/정책/고도화 후보를 `docs/14_deferred_cleanup` 아래 새 색인 문서로 모았습니다.

자동 NO_SHOW 배치, QR/바코드, 관측성 일부, 대기열 조회 최적화처럼 이미 완료된 항목은 완료 또는 일부 완료로 재분류했습니다. 배포 환경과 k6에서 이미 확인한 검증 항목은 매우 높은 우선순위가 아니면 미완 후보에서 제외하거나 낮은 우선순위로 낮췄습니다.

로그인 페이지 하단에 노출되던 테스트 계정 공통 비밀번호 안내도 제거했습니다.

## 변경 내용

- `docs/14_deferred_cleanup/02_후속작업_통합_정리.md` 추가
  - 후속작업 후보 `FU-001`부터 `FU-024`까지 정리
  - 완료/일부 완료/미완 정책/고도화 후보 구분
  - 기능 미완료와 런타임 검증 부채 분리
  - 다음 브랜치 후보 제안
  - 배포/k6 확인済 항목의 우선순위 조정
- `docs/14_deferred_cleanup/01_보류_정리_목록.md` 갱신
  - 새 통합 색인 문서 연결
- `docs/13_schedule/02_전체_작업_체크리스트.md` 갱신
  - MVP 이후 고도화 후보에 후속작업 통합 색인 문서화 반영
  - 진행 기록에 이번 문서 작업 추가
- `docs/README.md` 갱신
  - 새 후속작업 통합 정리 문서를 문서 읽는 순서와 코드 작성 전 확인 문서에 추가
- 구현 기록 문서 추가
  - `docs/11_implementation_log/141_Deferred_Followup_Index_문서화_기록.md`
- 로그인 페이지 문구 정리
  - 테스트 계정 공통 비밀번호 안내 제거

## 검증

- [x] `git status --short --branch`
- [x] `git log --oneline --decorate -8`
- [x] `rg`로 후속작업, 미검증, 보류, 고도화 후보 확인
- [x] 백엔드/프론트엔드 주요 디렉터리 구조 확인
- [x] `git diff --check`
- [ ] `npm.cmd run build` - 실패: 현재 로컬 `node_modules`에서 `jsbarcode`, `qrcode` 모듈을 찾지 못함
- [ ] Maven compile/test-compile
- [ ] Swagger `Try it out`
- [ ] 브라우저 확인

## 미검증 사유

- Maven compile/test-compile은 이번 변경의 핵심 확인 대상이 아니므로 수행하지 않습니다.
- 서버 기동, Docker 실행, Swagger 런타임 호출, 브라우저 확인이 필요한 API/화면 변경이 없습니다.
- `npm.cmd run build`는 1회만 시도했으며, 기존 QR/바코드 컴포넌트 의존성인 `jsbarcode`, `qrcode`를 현재 로컬 `node_modules`에서 찾지 못해 실패했습니다. 이번 로그인 문구 수정과 직접 관련 없는 환경/의존성 상태로 보고 추가 재시도는 하지 않았습니다.

## 후속 작업

- 프론트 빌드 경고와 lockfile/Turbopack root 정리 여부 결정
- 자동 NO_SHOW 배치 dry-run/count 운영 API 검토
- 필요 시 배포/k6/Grafana 확인 결과를 증적 문서로 보강
- 장기 회귀 방지가 필요할 때 DB 기반 통합/동시성 테스트 추가 여부 결정
```

## Merge 후 브랜치 정리 기준

- [ ] 전체 체크리스트 반영 확인
- [ ] 다음 후속작업 후보 1개 선택

## 커밋 메시지 초안

제목:

```text
docs: 후속작업 통합 색인 문서화
```

본문:

```text
- 최신 dev 기준 구현 로그의 미완 후속작업 후보를 통합 색인 문서로 정리
- 완료된 자동 노쇼 배치와 관측성/성능/배포 검증 작업을 색인 상태에 반영
- 로그인 페이지의 테스트 계정 비밀번호 안내 문구 제거
- 전체 체크리스트와 문서 안내에 새 후속작업 문서 반영
- dev 직접 작업 기준의 문서 기록과 PR 참고안 추가
```
