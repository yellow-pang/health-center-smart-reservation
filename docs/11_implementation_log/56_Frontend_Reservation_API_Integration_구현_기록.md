# Frontend Reservation API Integration 구현 기록

## 1. 작업 목표

- 시민 예약 신청 화면과 내 예약 화면을 mock service에서 실제 백엔드 예약 API로 교체한다.
- 이번 브랜치 범위는 업무 유형 조회, 예약 슬롯 조회, 예약 신청, 내 예약 조회, 예약 취소 API 연동으로 제한한다.
- 직원 체크인/현장 접수/대기열, route guard/403 화면, ESLint/패키지 매니저 정리는 후속 브랜치에서 진행한다.

## 2. 작업 범위

- [x] 현재 브랜치와 작업 트리 확인
- [x] 예약 관련 API 명세 확인
- [x] 예약 화면의 mock service 사용처 확인
- [x] 업무 유형 조회 API 연동
- [x] 예약 슬롯 조회 API 연동
- [x] 예약 신청 API 연동
- [x] 내 예약 조회 API 연동
- [x] 예약 취소 API 연동
- [x] TypeScript 정적 검증
- [x] Next build 검증
- [ ] 브라우저에서 시민 예약 흐름 확인
- [ ] Swagger 대표 예시 확인

## 3. 작업 전 체크리스트

- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 확인
- [x] `docs/04_api/01_API_명세서.md` 확인
- [x] `docs/05_frontend/02_UX_API_계약_우선순위.md` 기준 확인
- [x] `frontend/app/citizen/reservations/new/page.tsx` 확인
- [x] `frontend/app/citizen/reservations/page.tsx` 확인
- [x] `frontend/src/lib/mock-services.ts` 확인

## 4. 관련 전체 체크리스트 항목

| 영역 | 항목 | 이번 작업 반영 |
|---|---|---|
| 프론트엔드 | 사용자 예약 화면 구현 | 실제 업무 유형/예약 슬롯/예약 신청 API로 연결 |
| 프론트엔드 | 내 예약 화면 구현 | 실제 내 예약 조회/취소 API로 연결 |
| 프론트엔드 | 프론트엔드 API 연동 | 예약 관련 mock service 교체 |
| 테스트 | 프론트엔드 화면 검증 | TypeScript/Next build 확인, 브라우저 확인은 사용자 직접 수행 |

## 5. GitNexus 및 영향 범위

GitNexus 확인:

| 명령 | 결과 |
|---|---|
| `npx gitnexus query -r health-center-smart-reservation "frontend login failure AuthContext login user"` | 실패. exit 1, 출력 없음 |
| `npx gitnexus impact -r health-center-smart-reservation AuthProvider -d upstream --depth 2` | 실패. exit 1, 출력 없음 |
| `npx gitnexus query -r health-center-smart-reservation "frontend reservation API integration"` | 실패. exit 1, 출력 없음 |
| `npx gitnexus detect-changes -r health-center-smart-reservation` | 실패. exit 1, 출력 없음 |

대체 확인:

- `rg`로 `getServiceTypes`, `getReservationSlots`, `createReservation`, `getUserReservations`, `cancelReservation` 사용처 확인
- 직접 영향은 시민 예약 신청 화면, 내 예약 화면, 예약 API client, 프론트 mock type 확장에 한정

Blast radius:

| 대상 | 직접 사용처 | 영향 프로세스 | 위험도 |
|---|---|---|---|
| `frontend/src/lib/reservation-api.ts` | 시민 예약 신청/내 예약 화면 | 예약 신청, 예약 조회, 예약 취소 | MEDIUM |
| `Reservation` type | 시민 예약/대기 관련 mock 및 화면 | 응답 필드 표시 | MEDIUM |
| `ServiceType` type | 예약/직원/관리자 화면 | 업무 유형 표시 | MEDIUM |

HIGH/CRITICAL 경고는 GitNexus CLI 실패로 확인하지 못했다. 대신 TypeScript와 Next build로 정적 검증했다.

## 6. 구현 내용

### 6.1 예약 API client 추가

추가 파일:

| 파일 | 내용 |
|---|---|
| `frontend/src/lib/reservation-api.ts` | 시민 예약 흐름에서 사용하는 실제 API 함수 |

연동 API:

| 함수 | API |
|---|---|
| `getServiceTypes` | `GET /api/service-types` |
| `getReservationSlots` | `GET /api/reservation-slots?serviceTypeId={id}&date={date}` |
| `createReservation` | `POST /api/reservations` |
| `getUserReservations` | `GET /api/reservations/me` |
| `cancelReservation` | `DELETE /api/reservations/{reservationId}` |

### 6.2 API 응답 정규화

- 백엔드 `ServiceTypeResponse`는 업무 유형 ID를 `id`로 반환하므로 프론트 화면 타입의 `serviceTypeId`로 정규화했다.
- 예약 생성 API는 `reservationId`, `reservationNo`, `status`만 반환하므로 선택한 슬롯/방문자 입력값을 합쳐 완료 화면에서 사용할 `Reservation` 형태로 구성했다.
- 내 예약 조회 응답의 `serviceTypeName`, `reservationSlotId`, `endTime`, `reservedAt`을 수용하도록 `Reservation` type에 optional 필드를 추가했다.

### 6.3 화면 연결

- `frontend/app/citizen/reservations/new/page.tsx`
  - mock service import를 `reservation-api.ts`로 교체
  - 예약 생성 요청에 `reservationSlotId` 전달
  - 예약 실패 시 백엔드 오류 메시지를 toast로 표시

- `frontend/app/citizen/reservations/page.tsx`
  - 내 예약 조회와 취소를 실제 API로 교체
  - 응답에 `serviceTypeName`이 있으면 우선 표시하고, 없으면 mock helper fallback 사용

## 7. 검증 결과

| 검증 | 결과 |
|---|---|
| `npm.cmd exec -- tsc --noEmit` | 통과 |
| `npm.cmd run build` | 통과 |
| `git diff --check` | 공백 오류 없음 |
| `npm.cmd run lint` | 실패. `eslint` 실행 파일 없음 |
| GitNexus impact/detect-changes | CLI 오류로 완료하지 못함. `rg` 기반 대체 확인 |
| 브라우저 화면 확인 | 미수행. 사용자가 직접 확인 필요 |
| API 런타임 확인 | 미수행. 사용자가 Swagger 또는 브라우저로 직접 확인 필요 |

빌드 참고:

- Next build는 통과했다.
- `package-lock.json`과 `pnpm-lock.yaml`이 함께 있어 Turbopack root 추론 경고가 남아 있다.
- `next build`와 `tsc --noEmit` 후 `next-env.d.ts`, `tsconfig.tsbuildinfo` 산출물 변경이 생겨 원복했다.

## 8. 사용자 직접 확인 방법

프론트 실행 전 `.env.local`에 아래 값을 둔다.

```text
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

사용자 확인 순서:

1. 사용자가 PostgreSQL과 백엔드를 실행한다.
2. 프론트엔드를 실행한다.
3. `citizen@test.com / password1234`로 로그인한다.
4. `/citizen/reservations/new`에서 업무 유형, 날짜, 시간, 방문자 정보를 선택해 예약을 신청한다.
5. `/citizen/reservations`에서 새 예약이 조회되는지 확인한다.
6. 취소 가능한 예약에서 취소 버튼을 눌러 상태가 `CANCELED`로 반영되는지 확인한다.

Swagger 대표 예시:

`POST /api/reservations`

```json
{
  "serviceTypeId": 1,
  "reservationSlotId": 10,
  "visitorName": "홍길동",
  "visitorPhone": "010-1234-5678"
}
```

기대 결과:

- `success: true`
- `data.reservationNo` 존재
- `data.status`가 `RESERVED`

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 예약 API 연동 중 | 예약 생성 응답 상세화 검토 | 생성 응답이 최소 필드라 완료 화면은 선택값을 합쳐 표시함 | 후속 검토 |
| 예약 API 연동 중 | 서비스 유형 이름 cache 또는 공통 query layer 검토 | 내 예약 응답은 `serviceTypeName`을 포함하지만 다른 화면은 mock helper fallback을 사용 중 | 후속 |
| 검증 중 | ESLint 실행 기준 정리 | `eslint` 실행 파일 없음 | 후속 |
| 검증 중 | 패키지 매니저 기준 정리 | root `package-lock.json`과 frontend `pnpm-lock.yaml` 공존으로 Next 경고 발생 | 후속 |

## 10. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성
- [x] PR 문서 초안 작성
- [x] 전체 체크리스트 갱신
- [x] TypeScript 정적 검증 완료
- [x] Next build 완료
- [ ] 브라우저 예약 신청 화면 확인
- [ ] 브라우저 내 예약 조회/취소 확인
- [ ] Swagger 대표 예시 확인

## 11. 커밋 메시지 초안

```text
feat: integrate reservation APIs

- 업무 유형과 예약 슬롯 조회를 실제 API로 연결
- 시민 예약 신청 API 연동
- 내 예약 조회와 예약 취소 API 연동
- 백엔드 예약 응답 필드를 프론트 예약 타입에 반영
```
