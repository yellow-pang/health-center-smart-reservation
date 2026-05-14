# Frontend Route Guard Access Denied 구현 기록

## 1. 작업 목표

- 로그인 이후 역할별 화면 접근을 프론트 라우트 레벨에서 한 번 더 제한한다.
- 시민, 직원, 관리자 화면에 역할별 route guard를 적용하고 권한이 맞지 않으면 권한 없음 화면으로 안내한다.
- 이번 브랜치에서는 신규 백엔드 API, DB, seed/mock 데이터 변경 없이 프론트엔드 권한 UX만 처리한다.

## 2. 작업 범위

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 관련 항목 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] 프론트 인증 context, API client, 역할별 layout 확인
- [x] `AppLayout`에 허용 역할 기반 route guard 추가
- [x] `/citizen`, `/staff`, `/admin` layout에 허용 역할 명시
- [x] `/access-denied` 권한 없음 화면 추가
- [x] API 403 응답 시 권한 없음 화면으로 이동
- [x] TypeScript 정적 검증
- [x] Next build 검증
- [ ] 브라우저에서 역할별 직접 URL 접근 확인

## 3. 관련 전체 체크리스트 항목

| 영역 | 항목 | 이번 작업 반영 |
|---|---|---|
| 프론트엔드 | 프론트엔드 API 연동 | 권한 화면 연동과 API 403 이동 처리 |
| 프론트엔드 | 프론트엔드 화면 검증 | TypeScript/Next build 확인, 브라우저 확인은 사용자 직접 수행 필요 |
| 테스트 | API 수동 검증 | 신규 API 없음. 백엔드 seed/mock 데이터 추가 불필요 |

## 4. GitNexus 및 영향 범위

GitNexus 확인:

| 명령 | 결과 |
|---|---|
| `npm.cmd exec -- gitnexus status` | stale index 확인 |
| `npm.cmd exec -- gitnexus analyze` | 실패. `Not inside a git repository` |
| `npm.cmd exec -- gitnexus analyze .` | 실패. large file skip 메시지 후 exit 1, index 갱신 안 됨 |
| `npm.cmd exec -- gitnexus impact AppLayout --repo health-center-smart-reservation --direction upstream` | 실패. exit 1, 출력 없음 |
| `npm.cmd exec -- gitnexus impact AuthProvider --repo health-center-smart-reservation --direction upstream` | 실패. exit 1, 출력 없음 |
| `npm.cmd exec -- gitnexus impact apiResponse --repo health-center-smart-reservation --direction upstream` | 실패. exit 1, 출력 없음 |
| `npm.cmd exec -- gitnexus detect_changes -r health-center-smart-reservation -s all` | 실패. exit 1, 출력 없음 |

대체 확인:

- `rg`로 `AuthProvider`, `AppLayout`, 역할별 layout, 401/403 처리 사용처 확인
- 직접 영향은 프론트 공통 layout, 역할별 layout, 공통 API client, 권한 없음 화면에 한정

Blast radius:

| 대상 | 직접 사용처 | 영향 프로세스 | 위험도 |
|---|---|---|---|
| `AppLayout` | `/citizen/*`, `/staff/*`, `/admin/*` layout | 인증 이후 역할별 화면 진입 | MEDIUM |
| `apiResponse` | 모든 실제 API client | API 403 발생 시 화면 이동 | MEDIUM |
| `route-access.ts` | route guard, 권한 없음 화면 | 역할별 기본 이동 경로와 표시명 | LOW |

HIGH/CRITICAL 경고는 GitNexus CLI 실패로 확인하지 못했다. 대신 사용처 검색, TypeScript, Next build로 정적 검증했다.

## 5. 구현 내용

### 5.1 route guard 공통화

- `frontend/src/lib/route-access.ts` 추가
- 역할별 홈 경로, 역할 표시명, 접근 가능 여부 판단 함수를 분리
- `AppLayout`에 `allowedRoles` prop을 추가해 로그인 여부와 역할 일치를 함께 검사

역할별 접근 기준:

| 경로 | 허용 역할 |
|---|---|
| `/citizen/*` | `CITIZEN` |
| `/staff/*` | `STAFF` |
| `/admin/*` | `ADMIN` |

### 5.2 권한 없음 화면

- `frontend/app/access-denied/page.tsx` 추가
- 로그인 사용자는 현재 역할과 접근하려던 화면 역할을 안내하고 자기 역할 홈으로 이동할 수 있게 구성
- 미로그인 사용자는 로그인 화면으로 이동할 수 있게 구성
- Next 16의 `useSearchParams()` 정적 생성 제약에 맞춰 Suspense boundary 적용

### 5.3 API 403 처리

- `frontend/src/lib/api-client.ts`에 `redirectOnForbidden` 옵션 추가
- API 응답이 HTTP 403이면 `/access-denied?from={현재경로}`로 이동
- 기존 401 처리처럼 기본 동작은 자동 이동이며, 필요 시 호출부에서 비활성화할 수 있게 옵션화

## 6. 검증 결과

| 검증 | 결과 |
|---|---|
| `npm.cmd exec -- tsc --noEmit` | 통과 |
| `npm.cmd run build` | 통과 |
| `git diff --check` | 공백 오류 없음. LF/CRLF 경고만 표시 |
| GitNexus impact/detect-changes | CLI 오류로 완료하지 못함. `rg` 기반 대체 확인 |
| 브라우저 화면 확인 | 미수행. 사용자가 직접 확인 필요 |
| API 런타임 확인 | 신규 API 없음. Swagger 대표 예시와 seed/mock 데이터 추가 불필요 |

빌드 참고:

- Next build는 통과했다.
- `package-lock.json`과 `pnpm-lock.yaml`이 함께 있어 Turbopack root 추론 경고가 남아 있다.
- `next build`와 `tsc --noEmit` 후 생성 파일 변경이 생겨 작업 범위 밖 산출물은 원복했다.

## 7. 사용자 직접 확인 방법

프론트 실행 전 `.env.local`에 아래 값을 둔다.

```text
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

사용자 확인 순서:

1. 사용자가 백엔드와 프론트엔드를 실행한다.
2. `citizen@test.com / password1234`로 로그인한다.
3. 주소창에서 `/staff/queues` 또는 `/admin/dashboard`로 직접 이동한다.
4. `/access-denied` 화면으로 이동하고, "내 화면으로 이동" 버튼이 시민 화면으로 보내는지 확인한다.
5. `staff@test.com / password1234`로 로그인한 뒤 `/citizen/reservations` 접근이 차단되는지 확인한다.
6. 백엔드가 403을 반환하는 API 요청 상황에서 권한 없음 화면으로 이동하는지 확인한다.

## 8. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| GitNexus 확인 중 | GitNexus analyze/impact 실패 원인 확인 | stale index 상태에서 CLI가 impact/detect-changes를 완료하지 못함 | 후속 |
| Next build 중 | `useSearchParams()` Suspense boundary 필요 | `/access-denied` 정적 생성 실패 방지 | 이번 수정 반영 |
| 빌드 확인 중 | 패키지 매니저 기준 정리 | root `package-lock.json`과 frontend `pnpm-lock.yaml` 공존으로 Next 경고 발생 | 후속 |

## 9. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성
- [x] PR 문서 초안 작성
- [x] 전체 체크리스트 갱신
- [x] TypeScript 정적 검증 완료
- [x] Next build 완료
- [ ] 브라우저에서 시민 계정의 직원/관리자 화면 차단 확인
- [ ] 브라우저에서 직원 계정의 시민/관리자 화면 차단 확인
- [ ] 브라우저에서 관리자 계정의 시민/직원 화면 차단 확인
- [ ] API 403 발생 시 권한 없음 화면 이동 확인

## 10. 커밋 메시지 초안

```text
feat: 프론트엔드 역할별 라우트 가드 추가

- 시민, 직원, 관리자 layout에 허용 역할 기준 적용
- 권한 없음 화면 추가
- API 403 응답 시 권한 없음 화면으로 이동
```
