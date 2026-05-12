# Frontend API Integration Base 구현 기록

## 1. 작업 목표

- v0로 생성된 프론트엔드 mock 구조를 백엔드 API 계약에 맞게 정렬한다.
- 실제 API 연동으로 넘어가기 전에 `NEXT_PUBLIC_API_BASE_URL` 기반 공통 API client를 추가한다.
- 이번 브랜치에서는 화면별 실제 API 교체까지 진행하지 않고, 후속 연동이 가능한 기반까지만 만든다.

## 2. 작업 범위

- [x] mock type과 mock data를 백엔드 API 계약에 맞게 정렬
- [x] 화면에서 변경된 mock 필드명을 참조하도록 조정
- [x] 예약/대기/혼잡도 상태 코드를 백엔드 기준으로 정렬
- [x] `NEXT_PUBLIC_API_BASE_URL` 기반 API client 추가
- [x] 공통 응답 `{ success, data, error }` 타입 추가
- [x] access token localStorage 저장/조회/삭제 helper 추가
- [x] Authorization Bearer header 처리 추가
- [x] `.env.example`에 API base URL 예시 추가
- [ ] 실제 로그인 API 연동
- [ ] mock service를 실제 API 함수로 교체
- [ ] 브라우저에서 API 호출 확인

## 3. 작업 전 체크리스트

- [x] 현재 브랜치 확인: `feat/frontend-api-integration`
- [x] 작업 트리 확인
- [x] `docs/05_frontend/02_UX_API_계약_우선순위.md` 기준 확인
- [x] `docs/05_frontend/03_v0_MVP_프론트엔드_제작_프롬프트.md` 후속 구현 기준 확인
- [x] mock data/service 사용처 `rg` 확인
- [x] GitNexus 상태 확인

## 4. GitNexus 및 영향 범위

GitNexus 상태:

| 항목 | 결과 |
|---|---|
| `npm.cmd exec -- gitnexus status` | stale |
| `npm.cmd exec -- gitnexus analyze` | 실패. CLI가 현재 경로를 git repository로 인식하지 못함 |
| `npm.cmd exec -- gitnexus detect-changes --scope all --repo health-center-smart-reservation` | 실패. exit 1, 출력 없음 |

대체 확인:

- `rg`로 `ReservationStatus`, `CongestionInfo`, `ServiceType`, `ReservationSlot`, `QueueEntry`, `mockServiceTypes`, `mockReservations`, `mockQueueEntries` 사용처 확인
- 변경 대상은 프론트엔드 mock 계약과 그 직접 사용 화면에 한정

직접 영향 파일:

| 구분 | 파일 |
|---|---|
| mock 계약 | `frontend/src/lib/mock-data.ts`, `frontend/src/lib/mock-services.ts` |
| 공통 표시 | `frontend/src/components/common/status-badge.tsx` |
| 시민 화면 | `frontend/app/citizen/reservations/new/page.tsx`, `frontend/app/citizen/reservations/page.tsx`, `frontend/app/citizen/congestion/page.tsx` |
| 직원 화면 | `frontend/app/staff/check-in/page.tsx`, `frontend/app/staff/walk-in/page.tsx`, `frontend/app/staff/queues/page.tsx` |
| 관리자 화면 | `frontend/app/admin/reservation-slots/page.tsx`, `frontend/app/admin/service-types/page.tsx`, `frontend/app/admin/service-windows/page.tsx`, `frontend/app/admin/staff/page.tsx` |
| API client | `frontend/src/lib/api-client.ts`, `frontend/.env.example` |

위험도:

- 낮음: 화면 mock 계약 정렬과 신규 API client 추가 중심이다.
- 주의: 필드명 변경이 여러 화면에 닿으므로 TypeScript 검증과 Next build로 확인했다.

## 5. 커밋 단위 작업

### 5.1 mock 계약 정렬

커밋:

```text
c9640ca refactor: 프론트 mock 계약을 백엔드 API 기준으로 정렬
```

변경 내용:

| 영역 | 기존 | 변경 |
|---|---|---|
| 업무 유형 | `id`, `isActive`, `estimatedMinutes` | `serviceTypeId`, `code`, `active`, `defaultCapacity` |
| 예약 슬롯 | `id`, `time`, `reserved` | `slotId`, `startTime`, `endTime`, `reservedCount`, `availableCount`, `available` |
| 예약 | `id`, `reservationNumber`, `time`, `CONFIRMED/PENDING` | `reservationId`, `reservationNo`, `startTime`, `RESERVED` |
| 대기열 | `id`, `queueNumber`, `visitorName`, `visitorPhone` | `queueTicketId`, `ticketNumber`, `visitorNameMasked`, `visitorPhoneMasked` |
| 혼잡도 | `MEDIUM` | `NORMAL` |
| 업무 유형 mock | 5개 항목 | MVP 기준 3개 항목: 예방접종, 건강검진/검사, 건강상담 |

화면 반영:

- 시민 예약 신청/내 예약 화면의 예약 번호, 시간, 상태 참조 수정
- 시민 혼잡도 화면의 `NORMAL` 상태 처리 수정
- 직원 체크인/현장 접수/대기열 화면의 대기번호와 마스킹 방문자 필드 참조 수정
- 관리자 업무 유형/예약 슬롯/창구 화면의 식별자와 active 필드 참조 수정

### 5.2 API client 기반 추가

커밋:

```text
38c5afd feat: 프론트엔드 API 클라이언트 기반 추가
```

추가 파일:

| 파일 | 내용 |
|---|---|
| `frontend/src/lib/api-client.ts` | 공통 API request/response 함수, 응답 타입, 오류 타입, token helper |
| `frontend/.env.example` | `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080` 예시 |

API client 기준:

- `NEXT_PUBLIC_API_BASE_URL`이 없으면 `http://localhost:8080` 사용
- query object를 URL query string으로 변환
- object/array body는 JSON으로 직렬화
- access token이 있으면 `Authorization: Bearer {token}` header 추가
- 401 응답 시 token 제거 후 `/login` 이동
- HTTP 오류 또는 `success: false` 응답을 `ApiClientError`로 표현

## 6. 검증 결과

| 검증 | 결과 |
|---|---|
| `git status --short --branch` | 각 커밋 전후 작업트리 확인 |
| `npm.cmd exec -- tsc --noEmit` | 통과 |
| `npm.cmd run build` | 통과 |
| `git diff --check` | 공백 오류 없음 |
| `npm.cmd run lint` | 실패. `eslint` 실행 파일 없음 |
| 브라우저 화면 확인 | 미수행. 사용자가 직접 확인 필요 |
| 실제 API 호출 | 미수행. 이번 브랜치는 API client 기반까지만 추가 |

빌드 참고:

- Next.js build는 통과했다.
- `package-lock.json`과 `pnpm-lock.yaml`이 함께 있어 Turbopack root 추론 경고가 남아 있다.
- `next build`와 `tsc --noEmit`은 `next-env.d.ts`, `tsconfig.tsbuildinfo` 같은 산출물을 건드릴 수 있어 커밋 전 정리했다.

## 7. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 검증 중 | ESLint 실행 기준 정리 | `npm run lint`가 `eslint` 실행 파일 없음으로 실패 | 후속 |
| 검증 중 | 패키지 매니저 기준 정리 | `package-lock.json`과 `pnpm-lock.yaml` 공존으로 Next build 경고 발생 | 후속 |
| 검증 중 | `next-env.d.ts`, `tsconfig.tsbuildinfo` 산출물 관리 | build/tsc 후 작업트리에 산출물 변경이 남음 | 커밋 전 정리, 후속으로 ignore/설정 검토 |
| API client 추가 후 | 실제 API 함수 계층 분리 | 공통 client만 있고 화면별 API 함수는 아직 없음 | 후속 |

## 8. 브랜치 종료 전 체크리스트

- [x] mock 계약 정렬 커밋 완료
- [x] API client 기반 추가 커밋 완료
- [x] TypeScript 정적 검증 완료
- [x] Next build 완료
- [x] 구현 기록 작성
- [x] PR 문서 작성
- [x] 전체 체크리스트 갱신
- [ ] 브라우저 화면 확인
- [ ] 실제 API 연동 확인

## 9. 후속 작업

1. 로그인과 현재 사용자 API 연동
2. 업무 유형/예약 슬롯 조회 API 연동
3. 시민 예약 신청/내 예약/취소 API 연동
4. route guard와 401/403 화면 처리
5. ESLint와 패키지 매니저 기준 정리

## 10. 커밋 메시지

이미 완료한 커밋:

```text
refactor: 프론트 mock 계약을 백엔드 API 기준으로 정렬

- 업무 유형, 예약 슬롯, 예약, 대기열 mock 타입 필드명 정리
- 예약 상태와 혼잡도 상태 코드를 백엔드 기준으로 정렬
- 시민, 직원, 관리자 화면의 mock 필드 참조 수정
```

```text
feat: 프론트엔드 API 클라이언트 기반 추가

- NEXT_PUBLIC_API_BASE_URL 기반 공통 API 요청 함수 추가
- success, data, error 공통 응답 타입과 API 오류 타입 정의
- access token Authorization 헤더와 401 처리 흐름 추가
- 로컬 API base URL 예시 환경변수 추가
```
