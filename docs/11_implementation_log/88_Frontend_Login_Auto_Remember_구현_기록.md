# Frontend Login Auto Remember 구현 기록

## 1. 작업 목표

- 테스트용 로그인 화면에서 시민/직원/관리자 역할별 빠른 로그인 버튼을 제거한다.
- 이미 로그인된 사용자가 `/login`에 진입하면 현재 사용자 역할에 맞는 첫 화면으로 자동 이동하게 한다.
- 로그인 화면에 `아이디 기억` 옵션을 추가해 이메일만 저장하고, 비밀번호는 저장하지 않는다.

## 2. 이번 브랜치 작업 범위 제안

- 포함:
  - [x] `docs/13_schedule/02_전체_작업_체크리스트.md`의 프론트엔드 로그인/API 연동 항목 확인
  - [x] 로그인 화면의 역할별 테스트 버튼 제거
  - [x] 기존 token 기반 사용자 복원 후 역할별 자동 이동 로직 추가
  - [x] 아이디 기억 체크박스와 이메일 localStorage 저장/삭제 로직 추가
  - [x] 프론트 README의 오래된 mock 로그인 설명 갱신
  - [x] TypeScript/Next build 확인
  - [x] 브랜치 기록과 PR 문서 초안 작성

- 제외:
  - [ ] 비밀번호 저장 기반 자동 로그인
  - [ ] refresh token 자동 재발급 정책 변경
  - [ ] Auth API, Spring Security, DB seed 변경
  - [ ] 서버 기동, Docker 실행, 브라우저 확인

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/05_frontend/01_화면_설계서_v0_프롬프트.md` 로그인 화면 기준 확인
- [x] `docs/05_frontend/02_UX_API_계약_우선순위.md` 로그인 API 우선순위 확인
- [x] `docs/05_frontend/03_v0_MVP_프론트엔드_제작_프롬프트.md` 인증/route guard 기준 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] GitNexus impact 확인

## 4. 영향 범위

GitNexus 인덱스 상태:

- `gitnexus status`: stale 경고
- `gitnexus analyze`: `Not inside a git repository` 오류로 실패
- 보완: `--repo health-center-smart-reservation` 옵션으로 impact 분석을 수행하고, `rg`/파일 직접 확인/Next build로 보완

Impact 결과:

| 대상 | risk | direct callers | affected processes |
|---|---|---:|---:|
| `LoginPage` | LOW | 0 | 0 |
| `AuthProvider` | LOW | 0 | 0 |

HIGH/CRITICAL 위험은 없었다.

## 5. 구현 내용

수정 파일:

- `frontend/app/login/page.tsx`
- `frontend/README.md`

변경 내용:

- `시민으로 로그인`, `직원으로 로그인`, `관리자로 로그인` 테스트 버튼과 테스트 계정 선택 구분선을 제거했다.
- 로그인 화면 문구를 이메일/비밀번호 입력 중심으로 정리했다.
- `useAuth()`의 `user`와 `isLoading`을 기준으로, 이미 인증된 사용자가 `/login`에 들어오면 역할별 시작 화면으로 `router.replace` 처리한다.
- `아이디 기억` 체크박스를 추가하고 `healthcenter.rememberedLoginEmail` 키에 이메일만 저장한다.
- 사용자가 `아이디 기억`을 해제한 상태로 로그인하면 저장된 이메일을 삭제한다.
- 프론트 README의 mock 로그인/API 미연동 설명을 현재 실제 API 연동 기준으로 갱신했다.

자동 이동 기준:

| 역할 | 이동 경로 |
|---|---|
| `CITIZEN` | `/citizen/reservations/new` |
| `STAFF` | `/staff/check-in` |
| `ADMIN` | `/admin/dashboard` |

## 6. 검증 결과

| 검증 | 결과 |
|---|---|
| `gitnexus impact LoginPage --direction upstream --repo health-center-smart-reservation` | LOW, direct callers 0 |
| `gitnexus impact AuthProvider --direction upstream --repo health-center-smart-reservation` | LOW, direct callers 0 |
| `.\node_modules\.bin\tsc.cmd --noEmit` | 통과 |
| `npm.cmd run build` | 통과 |
| 서버 기동 | 미수행. 사용자 직접 수행 범위 |
| 브라우저 확인 | 미수행. 사용자 직접 수행 범위 |

`npm.cmd run build`에서 Next.js workspace root 추정 경고가 표시되었으나, 다중 lockfile 구조에 대한 기존 경고이며 빌드는 성공했다.

## 7. 사용자 직접 확인 방법

브라우저 확인 기준:

1. 프론트엔드 실행 후 `/login` 접속
2. 시민/직원/관리자 빠른 로그인 버튼이 보이지 않는지 확인
3. `staff@test.com` / `password1234`로 로그인
4. `/staff/check-in`으로 이동하는지 확인
5. 로그아웃하지 않고 다시 `/login` 접속
6. 기존 토큰으로 사용자 정보가 복원된 뒤 `/staff/check-in`으로 자동 이동하는지 확인
7. `아이디 기억` 체크 후 로그인하면 다음 로그인 화면 진입 시 이메일이 채워지는지 확인

## 8. 사용자 코드 점검 결과

| 점검 시점 | 사용자 의견 | 반영 여부 |
|---|---|---|
| 작업 중 | 아직 사용자 별도 점검 없음 | 대기 |

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| README 갱신 중 | 프론트 README에 남아 있던 mock/API 미연동 설명 정리 | 현재 코드 상태와 문서가 달라 혼동 가능 | 이번 브랜치에서 갱신 |
| 검증 중 | Next.js workspace root 경고 정리 여부 검토 | 루트/프론트 lockfile이 함께 있어 build 경고 발생 | 후속 후보로 남김 |

## 10. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리

