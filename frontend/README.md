# Health Center Smart Reservation Frontend

보건소 스마트 예약·대기 및 혼잡도 분석 시스템의 프론트엔드 프로젝트입니다.

현재 프론트엔드는 v0로 생성한 MVP 화면 골격을 기반으로 실제 백엔드 API와 주요 화면을 연동한 상태입니다. 시민, 직원, 관리자 화면은 역할별 route guard와 API client를 통해 접근합니다.

## 기술 스택

- Next.js App Router
- React
- TypeScript
- Tailwind CSS
- shadcn/ui
- lucide-react
- Recharts
- sonner toast

## 현재 구현 범위

| 구분 | 화면 | 경로 |
|---|---|---|
| 공통 | 로그인 | `/login` |
| 시민 | 예약 신청 | `/citizen/reservations/new` |
| 시민 | 내 예약 | `/citizen/reservations` |
| 시민 | 현재 혼잡도 | `/citizen/congestion` |
| 직원 | 예약자 체크인 | `/staff/check-in` |
| 직원 | 현장 접수 | `/staff/walk-in` |
| 직원 | 대기열 관리 | `/staff/queues` |
| 관리자 | 대시보드 | `/admin/dashboard` |
| 관리자 | 업무 유형 관리 | `/admin/service-types` |
| 관리자 | 예약 슬롯 관리 | `/admin/reservation-slots` |
| 관리자 | 직원 관리 | `/admin/staff` |
| 관리자 | 창구 관리 | `/admin/service-windows` |

`/` 경로는 `/login`으로 이동합니다.

## 주요 구조

```text
frontend
├─ app
│  ├─ login
│  ├─ citizen
│  ├─ staff
│  └─ admin
├─ src
│  ├─ components
│  │  ├─ common
│  │  └─ layout
│  ├─ contexts
│  └─ lib
├─ components/ui
└─ public
```

| 위치 | 역할 |
|---|---|
| `app/*` | Next.js App Router 화면 |
| `src/components/layout` | 로그인 후 공통 레이아웃과 역할별 사이드바 |
| `src/components/common` | 화면 제목, KPI 카드, 상태 배지, 데이터 테이블, 로딩/빈/오류 상태 |
| `src/contexts/auth-context.tsx` | 실제 Auth API 기반 로그인 사용자 상태 |
| `src/lib/api-client.ts` | 공통 API client와 토큰 저장/401/403 처리 |
| `src/lib/mock-data.ts` | 화면 타입과 일부 보조 mock 데이터 |
| `src/lib/mock-services.ts` | 잔여 mock 보조 서비스 |
| `components/ui` | shadcn/ui 기반 UI 컴포넌트 |

## 실행

```bash
npm install
npm run dev
```

기본 접속 경로:

```text
http://localhost:3000/login
```

## 스크립트

| 명령 | 설명 |
|---|---|
| `npm run dev` | 로컬 개발 서버 실행 |
| `npm run build` | 프로덕션 빌드 |
| `npm run start` | 빌드 결과 실행 |
| `npm run lint` | ESLint 실행 |

현재 `package.json`에는 `lint` 스크립트가 있지만, 생성 결과 기준으로 `eslint` 실행 파일이 없어 lint 실행은 실패합니다. 후속 작업에서 ESLint 의존성 또는 lint 스크립트 기준을 정리해야 합니다.

## 로그인 테스트

로그인 화면은 이메일/비밀번호 방식의 실제 Auth API를 사용합니다.

| 역할 | 이동 화면 |
|---|---|
| 시민 | 예약 신청 |
| 직원 | 체크인 |
| 관리자 | 대시보드 |

기본 테스트 계정 비밀번호는 `password1234`입니다. `자동 로그인`을 선택하면 토큰을 브라우저 localStorage에 저장해 다음 브라우저 실행 후에도 로그인 상태를 복원합니다. 선택하지 않으면 토큰은 sessionStorage에만 저장되어 현재 브라우저 세션 동안만 유지됩니다. `아이디 기억`을 선택하면 이메일만 브라우저 localStorage에 저장합니다.

## API 연동 상태

`NEXT_PUBLIC_API_BASE_URL` 기반 API client, 공통 응답 `{ success, data, error }` 타입, access token 저장, `Authorization: Bearer {token}` header, 401/403 처리와 route guard가 구현되어 있습니다.

## 알려진 차이점

- `package-lock.json`과 `pnpm-lock.yaml`이 함께 있어 패키지 매니저 기준 결정이 필요합니다.
- 일부 mock 보조 데이터와 mock service 파일은 잔여 화면 보조용으로 남아 있어 후속 정리 대상입니다.

상세 점검 내용은 `docs/11_implementation_log/48_v0_MVP_프론트엔드_생성_결과_점검_기록.md`를 참고합니다.
