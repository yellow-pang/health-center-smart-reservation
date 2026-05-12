# v0 MVP 프론트엔드 생성 결과 점검 기록

## 1. 작업 목표

- v0로 생성한 `frontend` 프로젝트를 이번 브랜치의 실제 변경 범위로 기록한다.
- `docs/05_frontend`의 화면 설계, UX/API 계약, v0 제작 프롬프트와 현재 코드의 일치 여부를 정적으로 점검한다.
- 백엔드 API 연동 전 필요한 계약 차이와 후속 정리 대상을 남긴다.

이번 브랜치는 프론트엔드 MVP의 첫 골격을 추가하는 작업이다.  
문서 추가만이 아니라 `frontend` 아래 Next.js 기반 화면 코드가 함께 PR 대상이다.

## 2. 작업 범위

- [x] v0 생성 프론트엔드 파일을 `frontend` 아래로 반영
- [x] Next.js App Router 기반 라우트 구조 확인
- [x] 시민, 직원, 관리자 역할별 화면 구조 확인
- [x] mock data와 mock service 분리 구조 확인
- [x] 공통 레이아웃, 사이드바, 상태/표시 컴포넌트 확인
- [x] `docs/05_frontend` 계획 대비 충족/차이점 정리
- [x] 브랜치 점검 기록 문서 작성
- [x] PR 작성안 문서 작성
- [ ] 실제 백엔드 API 연동
- [ ] 브라우저 화면 검증
- [ ] 모바일 화면 스크린샷 검증

## 3. 브랜치 기준

| 항목 | 내용 |
|---|---|
| 현재 브랜치 | `feat/create-front-page-backbone` |
| 작업 성격 | v0 기반 프론트엔드 MVP 골격 추가 |
| 주요 변경 | `frontend` Next.js 앱 코드 추가, 생성 결과 점검 문서 추가 |
| 백엔드 변경 | 없음 |
| 직접 실행하지 않은 작업 | dev server, 브라우저 확인, API 런타임 호출 |
| GitNexus | 코드 심볼 수정이 아닌 신규 프론트엔드 생성/문서 점검 중심이라 로컬 문서와 `rg` 확인으로 대체 |

## 4. 추가된 프론트엔드 구성

### 4.1 프로젝트 기반

| 구분 | 추가 내용 |
|---|---|
| 앱 프레임워크 | Next.js App Router |
| 언어 | TypeScript |
| 스타일 | Tailwind CSS |
| UI 컴포넌트 | shadcn/ui 계열 컴포넌트 |
| 아이콘 | lucide-react |
| 차트 | Recharts |
| 알림 | sonner toast |
| 인증 상태 | mock `AuthProvider` |
| 데이터 | mock data + mock service 함수 |

### 4.2 주요 설정 파일

| 파일 | 역할 |
|---|---|
| `frontend/package.json` | Next.js 실행 스크립트와 의존성 정의 |
| `frontend/tsconfig.json` | TypeScript 경로 alias와 컴파일 기준 |
| `frontend/next.config.mjs` | Next.js 설정 |
| `frontend/postcss.config.mjs` | Tailwind/PostCSS 설정 |
| `frontend/components.json` | shadcn/ui 컴포넌트 설정 |
| `frontend/app/globals.css` | 앱 전역 스타일 |
| `frontend/styles/globals.css` | v0 생성 전역 스타일 보조 파일 |

### 4.3 역할별 라우트

| 역할 | 라우트 | 화면 목적 |
|---|---|---|
| 공통 | `/` | `/login`으로 redirect |
| 공통 | `/login` | 이메일/비밀번호 입력, 역할별 테스트 로그인 |
| 시민 | `/citizen/reservations/new` | 업무 유형, 날짜, 시간, 방문자 정보를 단계적으로 선택해 예약 신청 |
| 시민 | `/citizen/reservations` | 내 예약 목록, 상태 배지, 취소 버튼 |
| 시민 | `/citizen/congestion` | 업무 유형별 대기 인원과 예상 대기시간 표시 |
| 직원 | `/staff/check-in` | 예약번호 기반 체크인, 대기번호 표시 |
| 직원 | `/staff/walk-in` | 현장 방문자 접수와 최근 접수 목록 |
| 직원 | `/staff/queues` | 대기열 조회, 필터, 상태별 액션 버튼 |
| 관리자 | `/admin/dashboard` | KPI, 시간대별 방문자, 업무별 대기시간, 방문 유형 비율 차트 |
| 관리자 | `/admin/service-types` | 업무 유형 mock CRUD 화면 |
| 관리자 | `/admin/reservation-slots` | 예약 슬롯 mock 관리 화면 |
| 관리자 | `/admin/staff` | 직원 목록 mock 관리 화면 |
| 관리자 | `/admin/service-windows` | 창구 업무 매핑 mock 관리 화면 |

### 4.4 공통 컴포넌트

| 파일 | 역할 |
|---|---|
| `frontend/src/components/layout/app-layout.tsx` | 로그인 후 화면 공통 레이아웃 |
| `frontend/src/components/layout/app-sidebar.tsx` | 역할별 메뉴와 로그아웃 버튼 |
| `frontend/src/components/common/page-header.tsx` | 화면 제목/설명/액션 영역 |
| `frontend/src/components/common/metric-card.tsx` | KPI 카드 |
| `frontend/src/components/common/status-badge.tsx` | 예약/대기/혼잡도 상태 배지 |
| `frontend/src/components/common/data-table.tsx` | 대기열/관리 화면 표 |
| `frontend/src/components/common/loading-state.tsx` | 로딩 상태 |
| `frontend/src/components/common/error-state.tsx` | 오류 상태와 재시도 |
| `frontend/src/components/common/empty-state.tsx` | 빈 목록 상태 |

### 4.5 mock 데이터와 서비스

| 파일 | 내용 |
|---|---|
| `frontend/src/lib/mock-data.ts` | 사용자, 업무 유형, 예약 슬롯, 예약 내역, 대기열, 창구, 대시보드 지표 mock 데이터와 타입 |
| `frontend/src/lib/mock-services.ts` | 로그인, 예약, 체크인, 현장 접수, 대기 상태 변경, 대시보드 조회를 흉내 내는 async 함수 |
| `frontend/src/contexts/auth-context.tsx` | mock 로그인 사용자 상태와 role login 처리 |

mock service 함수가 화면과 데이터 사이의 경계 역할을 하므로, 후속 브랜치에서 실제 API client로 교체할 위치는 비교적 명확하다.

## 5. 확인한 문서

| 문서 | 확인 내용 |
|---|---|
| `docs/README.md` | 프론트엔드 문서 읽기 순서와 프로젝트 고정 기준 |
| `docs/09_agent/05_문서기반_자동진행_운영가이드.md` | 작은 단위 진행, 정적 확인 범위, 체크리스트 갱신 기준 |
| `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` | 브랜치별 기록/PR 문서 구조 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 프론트엔드 코드 구조 확인, 화면 구현, API 연동, 화면 검증 항목 |
| `docs/05_frontend/01_화면_설계서_v0_프롬프트.md` | 화면 목록과 개별 v0 프롬프트 |
| `docs/05_frontend/02_UX_API_계약_우선순위.md` | 화면별 API 필드 계약과 백엔드 검증 기준 |
| `docs/05_frontend/03_v0_MVP_프론트엔드_제작_프롬프트.md` | v0 첫 프롬프트, 결과물 점검 체크리스트, Codex 후속 구현 기준 |

## 6. 문서 계획 대비 충족된 점

| 계획 항목 | 현재 결과 |
|---|---|
| 랜딩페이지보다 실제 앱 화면 우선 | `/`에서 `/login`으로 이동 |
| 역할 분리 | 시민, 직원, 관리자 하위 라우트 분리 |
| mock UI 우선 | 실제 fetch 없이 mock service 기반 구현 |
| API 교체 가능성 | 화면이 `mock-services.ts` 함수에 의존 |
| 시민 예약 신청 | 업무 유형, 날짜, 시간, 방문자 정보, 완료 단계 구성 |
| 내 예약 조회 | 예약 목록, 상태 배지, 취소 버튼 구성 |
| 현재 혼잡도 | 업무 유형별 대기 인원, 예상 대기시간, 혼잡도 배지 구성 |
| 직원 체크인 | 예약번호 입력과 성공 시 대기번호 표시 |
| 직원 현장 접수 | 방문자 정보 입력, 업무 선택, 접수 완료 표시 |
| 대기열 관리 | 요약 카드, 업무/상태 필터, 상태별 액션 버튼 구성 |
| 관리자 대시보드 | KPI 4개와 Recharts 기반 차트 구성 |
| 관리자 기준정보 | 업무 유형, 예약 슬롯, 직원, 창구 관리 화면 생성 |
| 공통 컴포넌트 | `StatusBadge`, `PageHeader`, `MetricCard`, `DataTable` 등 생성 |
| 상태 처리 | 다수 화면에 loading, empty, error, disabled, toast 처리 포함 |

## 7. v0 결과물 점검 체크리스트

| 항목 | 결과 | 근거 |
|---|---|---|
| 첫 화면이 랜딩페이지가 아니라 실제 앱 화면이다 | 충족 | `frontend/app/page.tsx`가 `/login`으로 redirect |
| 시민, 직원, 관리자 화면이 분리되어 있다 | 충족 | `app/citizen`, `app/staff`, `app/admin` 라우트 존재 |
| mock data와 화면 컴포넌트가 분리되어 있다 | 충족 | `mock-data.ts`, `mock-services.ts`, 공통 컴포넌트 분리 |
| 실제 fetch 호출이 과하게 섞이지 않았다 | 충족 | 정적 검색 기준 실제 fetch/API client 없음 |
| Codex가 API 클라이언트로 교체할 위치가 명확하다 | 부분 충족 | mock service 함수가 있으나 백엔드 DTO 이름과 불일치가 있음 |
| loading, empty, error, unauthorized, forbidden 상태가 있다 | 부분 충족 | loading/empty/error는 다수 화면에 있음. unauthorized/forbidden 전용 화면 또는 guard는 없음 |
| 직원 대기열 버튼이 상태별로 다르게 보인다 | 부분 충족 | 상태별 버튼은 있으나 백엔드 정책과 일부 전이가 다름 |
| 관리자 대시보드는 KPI와 차트가 한 화면에서 읽힌다 | 충족 | KPI 4개, 라인/바/파이 차트 존재 |
| 모바일에서 시민 예약 흐름의 버튼과 텍스트가 겹치지 않는다 | 미확인 | 브라우저/스크린샷 검증 미수행 |
| 과한 장식보다 보건소 업무 앱에 맞는 단정한 UI이다 | 정적 기준 충족 | 업무 앱 라우트와 카드/테이블 중심 구성 |

## 8. 기존 계획과 현재 코드의 차이

| 구분 | 현재 상태 | 후속 작업 제안 |
|---|---|---|
| 예약 상태 코드 | `PENDING`, `CONFIRMED` 사용 | 백엔드 기준 `RESERVED`, `CANCELED`, `CHECKED_IN`, `NO_SHOW`, `COMPLETED`로 정렬 |
| 혼잡도 상태 코드 | `MEDIUM` 사용 | 백엔드 기준 `NORMAL`로 정렬 |
| 업무 유형 mock | 증명서 발급, 모자보건, 정신건강 상담 포함 | MVP 기준 `VACCINATION`, `HEALTH_CHECK`, `HEALTH_CONSULT` 중심으로 정렬 |
| 업무 유형 필드 | `id`, `isActive` 중심 | `serviceTypeId`, `code`, `active` 등 백엔드 응답 계약과 매핑 |
| 예약 번호 필드 | `reservationNumber` 사용 | 백엔드/문서 기준 `reservationNo` 또는 실제 응답명 확정 필요 |
| 대기 번호 필드 | `queueNumber` 사용 | 백엔드 응답 기준 `ticketNumber`로 정렬 |
| 권한 처리 | mock user가 없으면 sidebar만 숨김 | unauthorized/forbidden 화면과 라우트 보호 추가 |
| 대기 상태 전이 | `CALLED -> NO_SHOW`, `IN_PROGRESS -> HOLD`, `HOLD -> IN_PROGRESS` 포함 | 문서 기준 `CALLED -> HOLD`, `HOLD -> CALLED`, `HOLD -> NO_SHOW`로 조정 |
| 현재 호출 표시 | 테이블 안에서 호출 중 번호 강조 | 현재 호출 중 대기번호를 별도 크게 표시 |
| 관리자 대시보드 | 날짜 필터 없음 | 후속 API 연동 시 날짜 필터 추가 |
| 패키지 락파일 | `package-lock.json`, `pnpm-lock.yaml` 공존 | npm 또는 pnpm 중 하나로 기준 결정 |
| `frontend/README.md` | 생성 전 문구가 남아 있었음 | 이번 문서 정리에서 현재 상태로 갱신 |
| 회원가입/공지사항 | 화면 미생성 | `01_화면_설계서` 전체 화면 목록 기준 MVP 포함 여부 재결정 |

## 9. 이번 브랜치에서 제외한 작업

- 백엔드 API client 추가
- mock data를 실제 API 호출로 교체
- 인증 토큰 저장과 Authorization header 처리
- route guard, unauthorized, forbidden 화면 구현
- dev server 실행과 브라우저 검증
- Docker, 백엔드 서버, Swagger 실행
- 커밋, push, 배포

## 10. 검증 기록

| 검증 | 결과 |
|---|---|
| `git status --short --branch` | `feat/create-front-page-backbone` 브랜치와 `frontend` 미추적 파일 확인 |
| `rg --files docs/05_frontend` | 프론트엔드 기준 문서 3개 확인 |
| `rg --files frontend` | 생성된 라우트, 컴포넌트, mock 파일 구조 확인 |
| `rg` 정적 검색 | 실제 fetch/API client 부재, mock service 사용, 상태 컴포넌트 사용 확인 |
| `git diff --check` | 공백 오류 없음. 단, 기존 파일 줄 끝 형식 관련 CRLF 경고가 표시됨 |
| `npm.cmd run lint` | 실패. `eslint` 실행 파일이 없어 lint 스크립트를 실행할 수 없음 |
| Maven compile/test-compile | 백엔드 코드 변경이 없어 수행하지 않음 |
| 프론트엔드 빌드 | 현재 요청 범위에서는 미수행 |
| 브라우저 화면 확인 | 사용자가 직접 수행 필요 |

## 11. 다음 작업 후보

1. `fix/frontend-mock-contract-alignment`: mock type과 백엔드 API 계약 정렬
2. `feat/frontend-api-client`: `NEXT_PUBLIC_API_BASE_URL` 기반 API client, 공통 응답 타입, token header 처리 추가
3. `feat/frontend-citizen-reservation-api`: 시민 예약 화면부터 실제 API 연동

## 12. 커밋 메시지 초안

```text
feat: v0 기반 프론트엔드 MVP 골격 추가

- Next.js App Router 기반 프론트엔드 프로젝트 추가
- 시민, 직원, 관리자 역할별 화면과 라우트 구성
- mock data, mock service, 공통 레이아웃과 UI 컴포넌트 추가
- v0 생성 결과와 기존 프론트엔드 계획의 차이점 문서화
```
