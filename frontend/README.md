# Health Center Smart Reservation Frontend

보건소 스마트 예약·대기 및 혼잡도 분석 시스템의 프론트엔드 프로젝트입니다.

현재 프론트엔드는 v0로 생성한 MVP 화면 골격입니다. 백엔드 API와 직접 연동하기 전 단계이며, mock data와 mock service로 시민, 직원, 관리자 주요 화면 흐름을 먼저 확인할 수 있게 구성되어 있습니다.

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
| `src/contexts/auth-context.tsx` | mock 로그인 사용자 상태 |
| `src/lib/mock-data.ts` | 화면 확인용 mock 데이터와 타입 |
| `src/lib/mock-services.ts` | API 교체 전 mock 비동기 서비스 |
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

로그인 화면에는 mock 역할 선택 버튼이 있습니다.

| 역할 | 이동 화면 |
|---|---|
| 시민 | 예약 신청 |
| 직원 | 체크인 |
| 관리자 | 대시보드 |

이 로그인은 실제 토큰 인증이 아닙니다. 현재는 화면 흐름 확인용 mock 인증입니다.

## API 연동 상태

아직 실제 백엔드 API 호출은 구현하지 않았습니다.

후속 작업 기준:

1. `NEXT_PUBLIC_API_BASE_URL` 기반 API client 추가
2. 공통 응답 `{ success, data, error }` 타입 정의
3. access token 저장과 `Authorization: Bearer {token}` header 처리
4. mock service를 실제 API 함수로 화면별 교체
5. 401/403 처리와 route guard 추가

## 알려진 차이점

- 예약 상태 코드가 백엔드 기준 `RESERVED`가 아니라 `PENDING`, `CONFIRMED` 중심입니다.
- 혼잡도 상태 코드가 백엔드 기준 `NORMAL`이 아니라 `MEDIUM`입니다.
- 업무 유형 mock이 MVP 기준 3종 외 항목을 포함합니다.
- 일부 필드명이 백엔드 응답 계약과 다릅니다.
- unauthorized/forbidden 전용 화면과 route guard는 아직 없습니다.
- `package-lock.json`과 `pnpm-lock.yaml`이 함께 있어 패키지 매니저 기준 결정이 필요합니다.

상세 점검 내용은 `docs/11_implementation_log/48_v0_MVP_프론트엔드_생성_결과_점검_기록.md`를 참고합니다.
