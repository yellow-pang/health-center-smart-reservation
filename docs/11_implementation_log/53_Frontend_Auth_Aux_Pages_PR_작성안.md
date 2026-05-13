# Frontend Auth Aux Pages Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/auth-ui` |
| base 브랜치 | `main` |
| 작업 트리 | 프론트 인증 보조 페이지와 문서 변경 있음 |
| 주요 커밋 | 아직 없음 |
| 타입 확인 | `npm.cmd exec -- tsc --noEmit` 통과 |
| 빌드 확인 | `npm.cmd run build` 통과 |
| lint 확인 | 미수행. 기존 `eslint` 실행 파일 없음 이슈 |
| 실행/API 확인 | 미수행. 사용자가 브라우저로 직접 확인 필요 |

## PR 제목

```text
feat: 프론트엔드 인증 보조 페이지 추가
```

## PR 본문

```markdown
## 개요

v0로 생성된 프론트엔드 로그인 화면의 골격은 유지하면서, MVP 화면 목록에 있으나 빠져 있던 인증 보조 흐름을 추가합니다.

이번 PR에서는 실제 Auth API 연동 없이 화면 진입과 mock 완료 상태까지만 구현했습니다.

## 변경 내용

- `/login`에 회원가입, 아이디 찾기, 비밀번호 찾기, 소셜 로그인 진입 링크 추가
- `/register` 시민 회원가입 mock 화면 추가
- `/social-login` 소셜 로그인 provider 선택 mock 화면 추가
- `/reset-password` 비밀번호 찾기 mock 화면 추가
- `/find-id` 아이디 찾기 mock 화면 추가
- 기존 시민/직원/관리자 테스트 로그인 흐름 유지

## 검증

- [x] `npm.cmd exec -- tsc --noEmit`
- [x] `git diff --check`
- [x] `npm.cmd run build`
- [ ] `npm.cmd run lint`
- [ ] 브라우저에서 `/login` 확인
- [ ] 브라우저에서 `/register` 확인
- [ ] 브라우저에서 `/social-login` 확인
- [ ] 브라우저에서 `/reset-password` 확인
- [ ] 브라우저에서 `/find-id` 확인

## 미검증 사유

- 서버 기동과 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- 실제 회원가입/소셜 로그인/아이디 찾기/비밀번호 찾기 API가 아직 연결되지 않아 API 런타임 검증은 하지 않았습니다.
- `npm run lint`는 이전 작업 기준 `eslint` 실행 파일 없음 이슈가 남아 있어 이번 검증에서 제외했습니다.

## 사용자가 직접 확인할 항목

```text
cd frontend
npm run dev
```

- `http://localhost:3000/login`
- `http://localhost:3000/register`
- `http://localhost:3000/social-login`
- `http://localhost:3000/reset-password`
- `http://localhost:3000/find-id`

## 남은 위험

- 현재 인증 보조 페이지는 mock 화면이므로 실제 계정 생성, 인증번호 발송, 소셜 provider redirect는 동작하지 않습니다.
- `next build`는 기존 `next/font` Google font fetch 때문에 네트워크 제한 환경에서 실패할 수 있습니다.
- `next build`와 `tsc --noEmit` 후 `next-env.d.ts`, `tsconfig.tsbuildinfo` 산출물이 작업트리에 남을 수 있습니다.

## 후속 작업

- 로그인과 현재 사용자 API 실제 연동
- 회원가입, 아이디 찾기, 비밀번호 찾기 API 계약 확정
- 소셜 로그인은 MVP 포함 여부와 provider 정책 결정 후 백엔드 계약 수립
- route guard와 권한 없음 화면 추가
- 모바일/데스크톱 브라우저 화면 확인
```

## 커밋 메시지 초안

```text
feat: 프론트엔드 인증 보조 페이지 추가

- 로그인 화면에 인증 보조 페이지 링크 추가
- 회원가입, 소셜 로그인, 아이디 찾기, 비밀번호 찾기 mock 페이지 추가
- TypeScript와 Next build로 신규 정적 라우트 검증
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 후속 API 연동 브랜치 생성
