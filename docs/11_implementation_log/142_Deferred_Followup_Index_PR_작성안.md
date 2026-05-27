# 후속작업 통합 색인 문서화 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `dev` |
| base 브랜치 | `dev` 기준 문서-only 작업으로 제안 |
| HEAD | `bf1e058 Merge pull request #66 from yellow-pang/feat/auto-close-pending-queue-batch` |
| 작업 트리 | 후속작업 통합 색인 문서와 관련 체크리스트 갱신 |
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

자동 NO_SHOW 배치, QR/바코드, 관측성 일부, 대기열 조회 최적화처럼 이미 완료된 항목은 완료 또는 일부 완료로 재분류했습니다.

## 변경 내용

- `docs/14_deferred_cleanup/02_후속작업_통합_정리.md` 추가
  - 후속작업 후보 `FU-001`부터 `FU-024`까지 정리
  - 완료/일부 완료/미완 정책/고도화 후보 구분
  - 기능 미완료와 런타임 검증 부채 분리
  - 다음 브랜치 후보 제안
- `docs/14_deferred_cleanup/01_보류_정리_목록.md` 갱신
  - 새 통합 색인 문서 연결
- `docs/13_schedule/02_전체_작업_체크리스트.md` 갱신
  - MVP 이후 고도화 후보에 후속작업 통합 색인 문서화 반영
  - 진행 기록에 이번 문서 작업 추가
- `docs/README.md` 갱신
  - 새 후속작업 통합 정리 문서를 문서 읽는 순서와 코드 작성 전 확인 문서에 추가
- 구현 기록 문서 추가
  - `docs/11_implementation_log/141_Deferred_Followup_Index_문서화_기록.md`

## 검증

- [x] `git status --short --branch`
- [x] `git log --oneline --decorate -8`
- [x] `rg`로 후속작업, 미검증, 보류, 고도화 후보 확인
- [x] 백엔드/프론트엔드 주요 디렉터리 구조 확인
- [x] `git diff --check`
- [ ] Maven compile/test-compile
- [ ] Swagger `Try it out`
- [ ] 브라우저 확인

## 미검증 사유

- 이번 PR은 문서-only 변경이라 Maven compile/test-compile은 수행하지 않습니다.
- 서버 기동, Docker 실행, Swagger 런타임 호출, 브라우저 확인이 필요한 API/화면 변경이 없습니다.

## 후속 작업

- VM 대상 k6 재측정과 `EXPLAIN ANALYZE` 결과 기록
- 프론트 빌드 경고와 lockfile/Turbopack root 정리 여부 결정
- 자동 NO_SHOW 배치 dry-run/count 운영 API 검토
- DB 기반 통합/동시성 테스트 브랜치 생성 여부 결정
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
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
- 완료된 자동 노쇼 배치와 관측성/성능 작업을 색인 상태에 반영
- 전체 체크리스트와 문서 안내에 새 후속작업 문서 반영
- 문서-only 브랜치 기록과 PR 작성안 추가
```
