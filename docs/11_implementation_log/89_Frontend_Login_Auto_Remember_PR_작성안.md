# Frontend Login Auto Remember Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/auth-login-enhancement` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 로그인 화면, 프론트 README, 브랜치/전체 체크리스트 문서 변경 있음 |
| 주요 커밋 | 아직 없음 |
| TypeScript 확인 | `.\node_modules\.bin\tsc.cmd --noEmit` 통과 |
| Next build | `npm.cmd run build` 통과 |
| 실행/브라우저 확인 | 미수행. 사용자가 직접 확인 필요 |

## PR 제목

```text
feat: 로그인 자동 이동과 아이디 기억 기능 추가
```

## PR 본문

```markdown
## 개요

테스트 편의를 위해 남아 있던 시민/직원/관리자 빠른 로그인 버튼을 로그인 화면에서 제거하고, 실제 이메일/비밀번호 로그인 흐름 중심으로 정리합니다.

기존 access token으로 현재 사용자 정보가 복원된 경우 `/login` 화면에 머물지 않고 역할별 시작 화면으로 자동 이동합니다. 또한 `아이디 기억` 옵션을 추가해 이메일만 저장하고 비밀번호는 저장하지 않습니다.

## 변경 내용

- 로그인 화면에서 시민/직원/관리자 테스트 로그인 버튼 제거
- 로그인 화면 안내 문구를 실제 이메일/비밀번호 로그인 기준으로 정리
- 이미 로그인된 사용자의 `/login` 접근 시 역할별 화면 자동 이동 처리
- `아이디 기억` 체크박스 추가 및 이메일 localStorage 저장/삭제 처리
- 프론트 README의 mock 로그인/API 미연동 설명을 현재 실제 API 연동 기준으로 갱신
- 구현 기록과 PR 작성안 문서 추가

## 검증

- [x] `.\node_modules\.bin\tsc.cmd --noEmit`
- [x] `npm.cmd run build`
- [x] GitNexus impact 확인: `LoginPage`, `AuthProvider` LOW
- [ ] 브라우저 `/login`에서 테스트 역할 버튼 제거 확인
- [ ] 이메일/비밀번호 로그인 후 역할별 첫 화면 이동 확인
- [ ] 로그인 상태에서 `/login` 재진입 시 자동 이동 확인
- [ ] `아이디 기억` 체크 후 이메일 복원 확인
- [ ] `아이디 기억` 해제 후 저장 이메일 삭제 확인

## 브라우저 대표 확인

`/login`에서 `staff@test.com` / `password1234`로 로그인한다.

기대 결과:

- 로그인 성공 toast 표시
- `/staff/check-in`으로 이동
- 로그아웃하지 않은 상태에서 `/login`에 다시 접속하면 `/staff/check-in`으로 자동 이동
- `아이디 기억` 체크 상태로 로그인했다면 다음 로그인 화면에서 이메일이 자동 입력됨

## 추가 테스트 체크리스트

- [ ] Happy: 시민 계정 로그인 후 `/citizen/reservations/new` 이동
- [ ] Happy: 직원 계정 로그인 후 `/staff/check-in` 이동
- [ ] Happy: 관리자 계정 로그인 후 `/admin/dashboard` 이동
- [ ] Edge: 저장된 토큰이 만료된 상태에서 `/login` 진입 시 로그인 화면 유지
- [ ] Edge: `아이디 기억` 해제 후 재로그인 시 저장 이메일 삭제
- [ ] Bad: 잘못된 비밀번호 입력 시 오류 toast 표시 및 이메일 저장 여부 정책 확인

## 미검증 사유

- 서버 기동과 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- 이번 변경은 프론트 화면/클라이언트 상태 처리만 다루며 Auth API, 토큰 발급 정책, DB seed는 변경하지 않았습니다.

## 남은 위험

- 현재 token 저장은 기존 구현대로 localStorage를 사용하므로, 공용 PC 보안 정책이 필요하면 자동 로그인 유지 방식 자체를 별도 브랜치에서 재검토해야 합니다.
- Next.js build 시 workspace root 추정 경고가 계속 표시되며, package-lock/pnpm-lock 병존 정리는 후속 작업 후보입니다.

## 후속 작업

- refresh token 자동 재발급 UX가 필요하면 API client/AuthProvider 책임 범위 재검토
- 잔여 mock service와 frontend README 정리 범위 추가 점검
- 패키지 매니저 lockfile 기준 결정
```

## 커밋 메시지 초안

```text
feat: 로그인 자동 이동과 아이디 기억 기능 추가

- 로그인 화면에서 역할별 테스트 로그인 버튼 제거
- 로그인된 사용자의 로그인 화면 재진입 시 역할별 화면으로 이동
- 이메일 아이디 기억 기능과 프론트 README 설명 갱신
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 브라우저 확인 결과 필요 시 구현 기록에 추가 반영

