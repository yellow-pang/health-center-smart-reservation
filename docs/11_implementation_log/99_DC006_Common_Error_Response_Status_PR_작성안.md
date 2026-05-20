# DC-006 공통 예외/오류 응답 보류 상태 정리 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `docs/deferred-common-error-status` |
| base 브랜치 | `dev` |
| 작업 트리 | DC-006 문서 상태 정합화 진행 |
| GitNexus | `gitnexus status` 결과 stale. `npx gitnexus analyze`, `gitnexus analyze`는 현재 세션에서 `Not inside a git repository`로 실패. 코드 symbol 수정 없음 |
| 정적 확인 | `git diff --check` 통과 |
| 빌드 확인 | 문서-only 변경이라 미수행 |
| 실행/API 확인 | 사용자가 Docker 실행 환경에서 Swagger 대표 오류 응답 확인 완료 |

## PR 제목

```text
docs: 공통 예외 응답 보류 항목 상태 정리
```

## PR 본문

```markdown
## 개요

보류 목록의 `DC-006 공통 예외/오류 응답 안정화` 상태가 이미 완료된 공통 예외 응답 구현 기록과 맞지 않아 문서 상태를 정리합니다.

기존 구현 기록과 PR 작성안의 Swagger 대표 오류 응답 확인 상태도 사용자의 Docker 실행 환경 확인 결과에 맞게 완료로 반영합니다.

## 변경 내용

- `DC-006` 상태를 `고도화 후보`에서 `정리 완료`로 변경
- 공통 예외 응답 구현 기록과 PR 작성안 링크 보강
- 기존 공통 예외 응답 구현 기록의 Swagger 확인 체크 반영
- 전체 체크리스트의 공통 예외 응답 항목에 Swagger 확인 완료 상태 반영
- 이번 문서 정리 브랜치 구현 기록과 PR 작성안 추가

## 검증

- [x] `git status --short --branch`
- [x] `rg`로 관련 문서 상태 확인
- [x] `git diff --check`
- [x] Swagger 대표 오류 응답 확인 완료 반영
- [ ] Maven compile/test-compile

## 미검증 사유

- 이번 PR은 문서-only 변경이라 Maven compile/test-compile은 수행하지 않았습니다.
- 서버 기동, Docker 실행, Swagger 런타임 확인은 사용자가 직접 수행했고 완료 결과를 문서에 반영했습니다.

## 후속 작업

- Spring Security filter 단계 401/403 응답을 공통 오류 응답과 맞출지 별도 브랜치에서 검토
- 서비스/정책 계층의 주요 `IllegalArgumentException`을 `BusinessException(ErrorCode.X)`로 점진 교체
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 다음 보류 항목 브랜치 생성 여부 결정

## 커밋 메시지 초안

제목:

```text
docs: 공통 예외 응답 보류 항목 상태 정리
```

본문:

```text
- DC-006 보류 목록 상태를 정리 완료로 갱신
- 공통 예외 응답 구현 기록과 PR 작성안의 Swagger 확인 상태 반영
- 전체 체크리스트에 사용자 Docker/Swagger 확인 완료 결과 반영
- 이번 문서 정리 브랜치 기록과 PR 작성안 추가
```

