# Frontend Route Guard Access Denied Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/route-guard-access-denied` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | route guard, 권한 없음 화면, 문서 변경 있음 |
| 주요 커밋 | 아직 없음 |
| 타입 확인 | `npm.cmd exec -- tsc --noEmit` 통과 |
| 빌드 확인 | `npm.cmd run build` 통과 |
| 정적 공백 확인 | `git diff --check` 통과. LF/CRLF 경고만 표시 |
| GitNexus 확인 | stale index, analyze/impact/detect-changes CLI 실패. `rg`와 빌드로 보완 |
| 실행/API 확인 | 신규 API 없음. 브라우저 역할별 접근 확인은 사용자 직접 수행 필요 |

## PR 제목

```text
feat: 프론트엔드 역할별 라우트 가드 추가
```

## PR 본문

```markdown
## 개요

로그인 사용자가 자기 역할이 아닌 화면 URL로 직접 접근하는 경우를 막기 위해 프론트엔드 route guard와 권한 없음 화면을 추가합니다.

이번 PR에서는 시민, 직원, 관리자 App Router layout에 허용 역할을 명시하고, 권한 불일치 또는 API 403 응답 시 `/access-denied` 화면으로 안내합니다.

## 변경 내용

- `frontend/src/lib/route-access.ts` 추가
- 역할별 홈 경로와 역할 표시명 공통화
- `AppLayout`에 `allowedRoles` 기반 route guard 추가
- `/citizen/*`, `/staff/*`, `/admin/*` layout에 허용 역할 적용
- `/access-denied` 권한 없음 화면 추가
- API client에 403 응답 시 권한 없음 화면으로 이동하는 기본 처리 추가

## 검증

- [x] `npm.cmd exec -- tsc --noEmit`
- [x] `npm.cmd run build`
- [x] `git diff --check`
- [ ] 브라우저에서 시민 계정으로 `/staff/queues` 접근 시 권한 없음 화면 확인
- [ ] 브라우저에서 시민 계정으로 `/admin/dashboard` 접근 시 권한 없음 화면 확인
- [ ] 브라우저에서 직원 계정으로 `/citizen/reservations` 접근 시 권한 없음 화면 확인
- [ ] 브라우저에서 관리자 계정으로 `/staff/queues` 접근 시 권한 없음 화면 확인
- [ ] 권한 없음 화면의 "내 화면으로 이동" 버튼이 역할별 홈으로 이동하는지 확인
- [ ] API 403 응답 발생 시 `/access-denied` 이동 확인

## Swagger 대표 예시

신규 백엔드 API가 없어서 Swagger 대표 예시는 추가하지 않습니다.

## 추가 테스트 체크리스트

- [ ] Happy: 올바른 역할 사용자는 자기 역할 화면에 접근 가능
- [ ] Edge: 로그인 직후 인증 hydrate 중 화면 깜빡임 없이 대기
- [ ] Edge: 권한 없음 화면에 `from` query가 없을 때도 정상 표시
- [ ] Bad: 미로그인 사용자가 보호 화면 접근 시 `/login` 이동
- [ ] Bad: 역할이 다른 로그인 사용자가 직접 URL 입력 시 `/access-denied` 이동
- [ ] Bad: 백엔드 403 응답 시 `/access-denied` 이동

## 미검증 사유

- 브라우저 화면 확인과 API 런타임 확인은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- 신규 API가 없으므로 seed/mock 데이터 추가는 필요하지 않습니다.
- GitNexus CLI는 stale index 갱신 및 impact/detect-changes가 실패해 `rg`, TypeScript, Next build로 보완했습니다.

## 남은 위험

- 프론트 route guard는 사용자 경험 보강이며, 실제 보안은 백엔드 Spring Security 권한 규칙이 계속 담당해야 합니다.
- API 403 자동 이동은 기본 동작으로 추가했으므로, 특정 화면에서 inline 오류 표시가 필요하면 호출부에서 `redirectOnForbidden: false`를 지정해야 합니다.
- `package-lock.json`과 `pnpm-lock.yaml` 공존으로 Next build root 추론 경고가 남아 있습니다.

## 후속 작업

- 관리자 대시보드 API 연동
- ESLint 실행 기준 정리
- 패키지 매니저 기준 정리
- GitNexus analyze/impact 실패 원인 정리
```

## 커밋 메시지 초안

```text
feat: 프론트엔드 역할별 라우트 가드 추가

- 시민, 직원, 관리자 layout에 허용 역할 기준 적용
- 권한 없음 화면 추가
- API 403 응답 시 권한 없음 화면으로 이동
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 후속 관리자 대시보드 API 연동 브랜치 생성 여부 결정
