# Frontend Auth Aux Pages 구현 기록

## 1. 작업 목표

- v0가 만든 프론트엔드 로그인 골격을 해치지 않고 인증 보조 페이지를 추가한다.
- 빠져 있던 회원가입, 소셜 로그인, 비밀번호 찾기, 아이디 찾기 화면 진입 흐름을 만든다.
- 이번 브랜치에서는 실제 인증 API 연동 없이 mock 화면과 정적 라우트 생성까지만 확인한다.

## 2. 작업 범위 제안

브랜치명:

```text
feat/auth-ui
```

이번 작업에 포함:

- [x] 로그인 화면에 회원가입, 아이디 찾기, 비밀번호 찾기, 소셜 로그인 진입 링크 추가
- [x] `/register` 회원가입 페이지 추가
- [x] `/social-login` 소셜 로그인 페이지 추가
- [x] `/reset-password` 비밀번호 찾기 페이지 추가
- [x] `/find-id` 아이디 찾기 페이지 추가
- [x] v0 기존 역할별 테스트 로그인과 mock 인증 흐름 유지

이번 작업에서 제외:

- [ ] 실제 회원가입 API 연동
- [ ] 실제 소셜 인증 공급자 연동
- [ ] 실제 아이디/비밀번호 찾기 API 연동
- [ ] 백엔드 Auth/Member 도메인 변경
- [ ] 브라우저 수동 화면 확인

## 3. 브랜치별 체크리스트 초안

### 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 관련 항목 확인
- [x] `docs/05_frontend/01_화면_설계서_v0_프롬프트.md` 확인
- [x] `docs/05_frontend/02_UX_API_계약_우선순위.md` 확인
- [x] `docs/05_frontend/03_v0_MVP_프론트엔드_제작_프롬프트.md` 확인
- [x] 현재 코드 상태와 로그인 화면 구조 확인

### 구현 체크리스트

- [x] 로그인 화면의 기존 테스트 계정 흐름 유지
- [x] 인증 보조 링크 추가
- [x] 회원가입 mock 제출 완료 상태 추가
- [x] 소셜 로그인 provider 선택 mock 상태 추가
- [x] 비밀번호 찾기 mock 제출 완료 상태 추가
- [x] 아이디 찾기 mock 결과 상태 추가
- [x] 공통 shadcn/ui, lucide-react 스타일 유지

### 검증 체크리스트

- [x] `npm.cmd exec -- tsc --noEmit`
- [x] `git diff --check`
- [x] `npm.cmd run build`
- [ ] `npm.cmd run lint`
- [ ] 브라우저에서 `/login`, `/register`, `/social-login`, `/reset-password`, `/find-id` 확인
- [ ] 실제 인증 API 호출 확인

## 4. 관련 전체 체크리스트 항목

- 프론트엔드 작업: 로그인 화면과 v0 프론트엔드 골격 보강
- 프론트엔드 화면 검증: 브라우저 확인은 사용자가 직접 수행 필요
- 브랜치별 구현 기록 작성
- 브랜치별 PR 문서 작성

## 5. 변경 내용

| 파일 | 변경 내용 |
|---|---|
| `frontend/app/login/page.tsx` | 회원가입, 아이디 찾기, 비밀번호 찾기, 소셜 로그인 진입 링크 추가 |
| `frontend/app/register/page.tsx` | 시민 회원가입 mock 화면 추가 |
| `frontend/app/social-login/page.tsx` | 소셜 로그인 provider 선택 mock 화면 추가 |
| `frontend/app/reset-password/page.tsx` | 비밀번호 찾기 mock 화면 추가 |
| `frontend/app/find-id/page.tsx` | 아이디 찾기 mock 화면 추가 |

## 6. 검증 결과

| 검증 | 결과 |
|---|---|
| `npm.cmd exec -- tsc --noEmit` | 통과 |
| `git diff --check` | 통과. `LF will be replaced by CRLF` 경고만 표시 |
| `npm.cmd run build` | 통과 |
| `npm.cmd run lint` | 미수행. 이전 브랜치 기준 `eslint` 실행 파일 없음 이슈가 남아 있음 |
| 브라우저 확인 | 미수행. 사용자가 직접 확인 필요 |

빌드 산출물 참고:

- `next build` 이후 `next-env.d.ts`, `tsconfig.tsbuildinfo` 같은 산출물이 작업트리에 나타날 수 있다.
- 기능 변경과 무관한 산출물은 변경 대상에서 제외한다.

## 7. 사용자 직접 확인 방법

프론트엔드 서버 기동은 사용자가 직접 수행한다.

```text
cd frontend
npm run dev
```

확인 URL:

- `http://localhost:3000/login`
- `http://localhost:3000/register`
- `http://localhost:3000/social-login`
- `http://localhost:3000/reset-password`
- `http://localhost:3000/find-id`

기대 결과:

- 로그인 화면에서 인증 보조 페이지로 이동할 수 있다.
- 각 페이지에서 입력 후 mock 완료 상태 또는 안내 toast가 표시된다.
- 기존 시민/직원/관리자 테스트 로그인 버튼은 그대로 동작한다.

## 8. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 인증 보조 페이지 추가 중 | 실제 Auth API 계약 확장 검토 | 회원가입, 아이디 찾기, 비밀번호 찾기, 소셜 로그인은 현재 프론트 mock 화면만 있고 백엔드 API 계약은 MVP 로그인 중심이다 | 후속 |
| 검증 중 | Google font build 네트워크 의존성 검토 | sandbox 네트워크 제한에서 `next build`가 실패할 수 있다 | 기록 |
| 검증 중 | 빌드 산출물 관리 | `next-env.d.ts`, `tsconfig.tsbuildinfo`가 build/tsc 후 작업트리에 남을 수 있다 | 후속 |

## 9. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성
- [x] PR 문서 작성
- [x] 전체 체크리스트 갱신
- [x] TypeScript 정적 검증 완료
- [x] Next build 완료
- [ ] 브라우저 화면 확인
- [ ] 실제 API 연동 확인

## 10. 후속 작업

1. 로그인과 현재 사용자 API를 실제 API client로 연결
2. 회원가입, 아이디 찾기, 비밀번호 찾기 API 계약 확정
3. route guard와 401/403 화면 처리
4. 브라우저에서 모바일/데스크톱 화면 겹침 확인

## 11. 커밋 메시지 초안

```text
feat: 프론트엔드 인증 보조 페이지 추가

- 로그인 화면에 회원가입, 소셜 로그인, 아이디/비밀번호 찾기 링크 추가
- 회원가입, 소셜 로그인, 아이디 찾기, 비밀번호 찾기 mock 페이지 추가
- v0 기존 테스트 로그인 흐름을 유지하며 정적 라우트 빌드 확인
```
