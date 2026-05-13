# Frontend Real API Integration 구현 기록

## 1. 작업 목표

- 프론트엔드 mock service를 실제 백엔드 API 호출로 작은 단위씩 교체한다.
- 이번 브랜치의 첫 단위는 로그인과 현재 사용자 조회 API 실제 연동으로 제한한다.
- 예약, 직원 대기열, route guard, 패키지 매니저 정리는 같은 브랜치 후보로 관리하되 각 커밋 단위로 나누어 진행한다.

## 2. 작업 범위

- [x] 이번 브랜치 전체 방향과 관련 전체 체크리스트 확인
- [x] 커밋 단위 분리
- [x] 로그인 요청을 실제 `POST /api/auth/login` API와 연결
- [x] 현재 사용자 조회 `GET /api/members/me` API client 추가
- [x] access token과 refresh token 저장/삭제 흐름 정리
- [x] 앱 시작 시 저장된 access token으로 인증 상태 복원
- [x] 로그아웃 시 `POST /api/auth/logout` 호출 후 토큰 삭제
- [x] 인증 로딩 중 보호 레이아웃이 즉시 `/login`으로 이동하지 않도록 조정
- [ ] 업무 유형과 예약 슬롯 조회 API 연동
- [ ] 시민 예약 신청/내 예약/취소 API 연동
- [ ] 직원 체크인/현장 접수/대기열 API 연동
- [ ] route guard와 권한 없음 화면 추가
- [ ] 모바일/데스크톱 브라우저 화면 확인
- [ ] ESLint와 패키지 매니저 기준 정리

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 확인
- [x] `docs/04_api/01_API_명세서.md` 인증 API 계약 확인
- [x] `docs/05_frontend/02_UX_API_계약_우선순위.md` 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] 영향받는 파일 확인

## 4. 관련 전체 체크리스트 항목

| 영역 | 항목 | 이번 작업 반영 |
|---|---|---|
| 프론트엔드 | 프론트엔드 API 연동 | 로그인과 현재 사용자 API 실제 연동 1차 완료 |
| 프론트엔드 | 사용자 예약 화면 구현 | 후속 커밋 |
| 프론트엔드 | 내 예약 화면 구현 | 후속 커밋 |
| 프론트엔드 | 직원 접수/체크인 화면 구현 | 후속 커밋 |
| 프론트엔드 | 대기열 관리 화면 구현 | 후속 커밋 |
| 테스트 | 프론트엔드 화면 검증 | 사용자가 브라우저에서 직접 확인 필요 |

## 5. 커밋 단위 설계

권장 진행 순서는 아래와 같다.

```text
feat: integrate login and current user API
feat: integrate reservation APIs
feat: integrate staff queue APIs
feat: add route guard and forbidden page
chore: align eslint and package manager setup
```

이번 작업에서 완료한 단위:

```text
feat: integrate login and current user API
```

## 6. GitNexus 및 영향 범위

GitNexus 확인:

| 명령 | 결과 |
|---|---|
| `npx gitnexus status` | stale. indexed commit과 현재 commit 불일치 |
| `npx gitnexus analyze` | 실패. `Not inside a git repository` 출력 |
| `npx gitnexus analyze .` | 실패. large file skip 메시지 후 exit 1 |
| `npx gitnexus impact -r health-center-smart-reservation AuthProvider -d upstream --depth 2` | 실패. exit 1, 출력 없음 |
| `npx gitnexus impact -r health-center-smart-reservation apiRequest -d upstream --depth 2` | 실패. exit 1, 출력 없음 |
| `npx gitnexus detect-changes -r health-center-smart-reservation` | 실패. exit 1, 출력 없음 |

대체 영향 확인:

- `rg`로 `AuthProvider`, `useAuth`, `apiRequest`, `setAuthTokens`, `clearAuthTokens`, `getAccessToken` 사용처 확인
- 직접 영향은 인증 컨텍스트, 로그인 화면, 보호 레이아웃, 공통 API client에 한정

Blast radius:

| 대상 | 직접 사용처 | 영향 프로세스 | 위험도 |
|---|---|---|---|
| `AuthProvider` | `frontend/app/layout.tsx` | 전역 인증 상태 제공 | MEDIUM |
| `useAuth` | 로그인 화면, 앱 레이아웃, 사이드바 | 로그인, 로그아웃, 보호 화면 진입 | MEDIUM |
| `apiRequest` | 신규 `auth-api.ts`와 후속 API 연동 후보 | 공통 API 호출 | MEDIUM |
| `AppLayout` | 시민/직원/관리자 layout | 인증 필요 화면 진입 | MEDIUM |

HIGH/CRITICAL 경고는 GitNexus에서 확인하지 못했다. 다만 인증 흐름은 중요 경로이므로 TypeScript와 Next build로 정적 검증했다.

## 7. 구현 내용

### 7.1 인증 API 계층 추가

추가 파일:

| 파일 | 내용 |
|---|---|
| `frontend/src/lib/auth-api.ts` | 로그인, 테스트 역할 로그인, 현재 사용자 조회, 로그아웃 API 함수 |

연동 API:

| 함수 | API |
|---|---|
| `login` | `POST /api/auth/login` |
| `getCurrentUser` | `GET /api/members/me` |
| `logout` | `POST /api/auth/logout` |

테스트 계정 버튼은 백엔드 seed 계정을 사용한다.

| 역할 | 이메일 | 비밀번호 |
|---|---|---|
| 시민 | `citizen@test.com` | `password1234` |
| 직원 | `staff@test.com` | `password1234` |
| 관리자 | `admin@test.com` | `password1234` |

### 7.2 인증 상태 저장 흐름 정리

- 로그인 성공 시 `accessToken`, `refreshToken`을 localStorage에 저장
- 저장된 access token이 있으면 앱 시작 시 `/api/members/me`로 사용자 정보 복원
- 로그아웃 시 refresh token이 있으면 백엔드 로그아웃 API를 호출한 뒤 localStorage token 삭제
- 401 또는 현재 사용자 조회 실패 시 token 삭제

### 7.3 로그인 화면 조정

- 로그인 성공 후 이메일 문자열 추정 대신 API 응답의 `member.role` 기준으로 이동
- 테스트 계정 버튼도 실제 로그인 API를 호출하도록 변경
- 안내 문구를 실제 seed 계정 비밀번호 기준으로 변경

### 7.4 보호 레이아웃 조정

- 인증 상태 복원 중에는 `/login`으로 즉시 redirect하지 않도록 `isLoading`을 확인
- 인증 복원 완료 후 사용자 정보가 없을 때만 `/login`으로 이동

### 7.5 사용자 런타임 확인 중 로그인 실패 수정

- 백엔드 `POST /api/auth/login`은 200 OK와 `member.role`을 정상 반환했지만 프론트에서 실패 toast가 표시되는 문제가 있었다.
- 원인은 `AuthContext.login()`과 `AuthContext.loginWithRole()`이 `auth-api.ts`의 성공 결과에서 `user`를 제거한 채 `{ success: true }`만 반환한 것이었다.
- 로그인 페이지는 `result.success && result.user`를 기준으로 성공 분기와 role redirect를 수행하므로, 컨텍스트에서 원래 `AuthResult`를 그대로 반환하도록 수정했다.

## 8. 검증 결과

| 검증 | 결과 |
|---|---|
| `npm.cmd exec -- tsc --noEmit` | 통과 |
| `npm.cmd run build` | 통과 |
| `git diff --check` | 공백 오류 없음 |
| `npm.cmd run lint` | 실패. `eslint` 실행 파일 없음 |
| GitNexus impact | CLI 오류로 완료하지 못함. `rg` 기반 대체 확인 |
| 브라우저 화면 확인 | 미수행. 사용자가 직접 확인 필요 |
| API 런타임 확인 | 미수행. 사용자가 Swagger 또는 브라우저로 직접 확인 필요 |
| 사용자 런타임 제보 기반 재검증 | 로그인 200 OK 후 프론트 실패 toast 원인 확인 및 수정, TypeScript/Next build 재통과 |

빌드 참고:

- Next build는 통과했다.
- `package-lock.json`과 `pnpm-lock.yaml`이 함께 있어 Turbopack root 추론 경고가 남아 있다.
- `next build`와 `tsc --noEmit` 후 `next-env.d.ts`, `tsconfig.tsbuildinfo` 산출물 변경이 생길 수 있다.

## 9. 사용자 직접 확인 방법

프론트 실행 전 `.env.local`에 아래 값을 둔다.

```text
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

사용자 확인 순서:

1. 백엔드와 PostgreSQL을 사용자가 직접 실행한다.
2. Swagger에서 `POST /api/auth/login`이 seed 계정으로 동작하는지 확인한다.
3. 프론트엔드를 사용자가 직접 실행한다.
4. `http://localhost:3000/login`에서 `staff@test.com / password1234`로 로그인한다.
5. 직원 화면으로 이동하고 새로고침 후에도 현재 사용자 정보가 유지되는지 확인한다.

Swagger 대표 예시:

```json
{
  "email": "staff@test.com",
  "password": "password1234"
}
```

기대 결과:

- `success: true`
- `data.accessToken`, `data.refreshToken` 존재
- `data.member.role`이 `STAFF`

## 10. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 인증 API 연동 중 | refresh token 재발급 자동화 | 현재는 401 시 로그인 이동만 처리하며 자동 재발급은 없음 | 후속 |
| 인증 API 연동 중 | role 기반 route guard 분리 | 현재 보호 레이아웃은 로그인 여부 중심이며 권한 부족 화면은 없음 | 후속 |
| 검증 중 | ESLint 실행 기준 정리 | `eslint` 실행 파일 없음 | 후속 |
| 검증 중 | 패키지 매니저 기준 정리 | root `package-lock.json`과 frontend `pnpm-lock.yaml` 공존으로 Next 경고 발생 | 후속 |
| 검증 중 | GitNexus CLI 재점검 | impact/detect-changes가 exit 1로 실패 | 후속 |
| 사용자 런타임 확인 중 | 로그인 성공 결과의 `user` 반환 누락 수정 | 백엔드 200 OK 응답에도 로그인 페이지가 실패로 분기함 | 이번 수정 반영 |

## 11. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성
- [x] PR 문서 초안 작성
- [x] 전체 체크리스트 갱신
- [x] 타입 검증 완료
- [x] Next build 완료
- [ ] 브라우저 로그인 화면 확인
- [ ] Swagger 로그인 대표 예시 확인
- [ ] 예약 API 연동 커밋 진행
- [ ] 직원 대기열 API 연동 커밋 진행
- [ ] route guard/403 화면 커밋 진행
- [ ] ESLint/패키지 매니저 정리 커밋 진행

## 12. 커밋 메시지 초안

```text
feat: integrate login and current user API

- 로그인 화면을 실제 Auth API와 연결
- 현재 사용자 조회 API client로 인증 상태 복원
- access token과 refresh token 저장/삭제 흐름 정리
- 테스트 계정 버튼을 seed 계정 기반 실제 로그인으로 변경
```
