# Frontend Admin Master Data API Integration 구현 기록

## 1. 작업 목표

- 관리자 기준정보 화면의 mock 조회를 실제 백엔드 Office/ReservationSlot API 호출로 교체한다.
- 이번 브랜치에서는 업무 유형, 예약 슬롯, 직원 목록, 창구 업무 매핑 화면을 실제 API 응답 기준으로 표시한다.
- 백엔드에 아직 없는 직원/창구 쓰기 API는 mock 상태 변경 대신 후속 작업 안내로 막는다.

## 2. 작업 범위

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] 전체 체크리스트의 프론트엔드 API 연동 항목 확인
- [x] Office/ReservationSlot API 명세와 기존 구현 기록 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] 관리자 기준정보 화면 mock 사용처 확인
- [x] 관리자 기준정보 API client 추가
- [x] 업무 유형 관리 화면 API 연동
- [x] 예약 슬롯 관리 화면 API 연동
- [x] 직원 관리 화면 API 연동
- [x] 창구 관리 화면 API 연동
- [x] TypeScript 정적 검증
- [x] Next build 검증
- [ ] 브라우저에서 관리자 기준정보 화면 확인
- [ ] Swagger에서 대표 예시 확인

## 3. 관련 전체 체크리스트 항목

| 영역 | 항목 | 이번 작업 반영 |
|---|---|---|
| 프론트엔드 | 프론트엔드 API 연동 | 관리자 기준정보 화면의 mock service 조회를 실제 API로 교체 |
| 프론트엔드 | 프론트엔드 화면 검증 | TypeScript/Next build 확인, 브라우저 확인은 사용자 직접 수행 필요 |
| Office Context | 업무 유형/직원/창구 API | 기존 백엔드 조회/업무 유형 쓰기 API를 프론트 화면에 연결 |
| Reservation Context | 관리자 예약 슬롯 생성 API | 예약 슬롯 화면에서 날짜별 조회와 슬롯 생성 연결 |

## 4. GitNexus 및 영향 범위

GitNexus 확인:

| 명령 | 결과 |
|---|---|
| `npm.cmd exec -- gitnexus impact ServiceTypesPage --repo health-center-smart-reservation --direction upstream --depth 2` | 실패. exit 1, 출력 없음 |
| `npm.cmd exec -- gitnexus impact ReservationSlotsPage --repo health-center-smart-reservation --direction upstream --depth 2` | 실패. exit 1, 출력 없음 |
| `npm.cmd exec -- gitnexus impact StaffManagementPage --repo health-center-smart-reservation --direction upstream --depth 2` | 실패. exit 1, 출력 없음 |
| `npm.cmd exec -- gitnexus impact ServiceWindowsPage --repo health-center-smart-reservation --direction upstream --depth 2` | 실패. exit 1, 출력 없음 |

대체 확인:

- `rg`로 관리자 기준정보 화면의 `mock-services` 사용처 확인
- 신규 API client와 관리자 화면 4개 파일에 변경 범위 제한
- `npm.cmd exec -- tsc --noEmit`, `npm.cmd run build`, `git diff --check`로 정적 검증

Blast radius:

| 대상 | 직접 사용처 | 영향 프로세스 | 위험도 |
|---|---|---|---|
| `admin-master-data-api.ts` | 관리자 기준정보 화면 4개 | API 응답 정규화 | MEDIUM |
| `ServiceTypesPage` | `/admin/service-types` | 업무 유형 조회/생성/수정/비활성화 | MEDIUM |
| `ReservationSlotsPage` | `/admin/reservation-slots` | 날짜별 예약 슬롯 조회/생성 | MEDIUM |
| `StaffManagementPage` | `/admin/staff` | 직원 목록 조회 | LOW |
| `ServiceWindowsPage` | `/admin/service-windows` | 창구 업무 매핑 조회 | LOW |

HIGH/CRITICAL 경고는 GitNexus CLI 실패로 확인하지 못했다. 변경은 관리자 화면과 프론트 API adapter에 한정했다.

## 5. 구현 내용

### 5.1 관리자 기준정보 API client 추가

추가 파일:

| 파일 | 내용 |
|---|---|
| `frontend/src/lib/admin-master-data-api.ts` | 관리자 기준정보 API 호출과 화면 타입 정규화 |

연동 API:

| 함수 | API | 용도 |
|---|---|---|
| `getAdminServiceTypes` | `GET /api/service-types` | 업무 유형 목록 조회 |
| `createAdminServiceType` | `POST /api/admin/service-types` | 업무 유형 생성 |
| `updateAdminServiceType` | `PUT /api/admin/service-types/{id}` | 업무 유형 수정 |
| `deactivateAdminServiceType` | `PATCH /api/admin/service-types/{id}/deactivate` | 업무 유형 비활성화 |
| `getAdminReservationSlots` | `GET /api/reservation-slots` | 날짜/업무별 예약 슬롯 조회 |
| `createAdminReservationSlot` | `POST /api/admin/reservation-slots` | 예약 슬롯 생성 |
| `getAdminStaff` | `GET /api/admin/staff` | 직원 목록 조회 |
| `getAdminServiceWindows` | `GET /api/admin/service-windows` | 창구 업무 매핑 조회 |

### 5.2 업무 유형 관리 화면

- `mock-services`의 `getAllServiceTypes`, `createServiceType`, `updateServiceType`, `deleteServiceType` 사용을 제거했다.
- 업무 유형 생성/수정/비활성화를 실제 관리자 API로 연결했다.
- 삭제 UX는 실제 백엔드 동작에 맞춰 비활성화 안내와 동작으로 변경했다.

### 5.3 예약 슬롯 관리 화면

- 기본 조회 날짜를 오늘 날짜로 설정했다.
- 업무 유형과 날짜 필터를 API query로 전달한다.
- 전체 업무 선택 시 현재 날짜 기준으로 활성 업무 유형별 슬롯을 병렬 조회해 합친다.
- 슬롯 추가 시 30분 단위 종료 시간을 계산해 `POST /api/admin/reservation-slots`로 생성한다.
- 업무 유형명은 API로 받은 업무 유형 목록 기준으로 표시한다.

### 5.4 직원/창구 관리 화면

- 직원 목록과 창구 업무 매핑 조회를 실제 API로 연결했다.
- 백엔드에 직원 생성/수정/삭제, 창구 생성/수정 API가 아직 없으므로 해당 쓰기 동작은 mock 변경을 하지 않고 후속 API 필요 안내를 표시한다.
- 창구 담당 업무명은 API 업무 유형 목록 기준으로 표시한다.

## 6. 검증 결과

| 검증 | 결과 |
|---|---|
| `npm.cmd exec -- tsc --noEmit` | 통과 |
| `npm.cmd run build` | 통과 |
| `git diff --check` | 공백 오류 없음. LF/CRLF 경고만 표시 |
| `rg` mock service 확인 | 관리자 기준정보 화면의 `mock-services` import 제거 확인 |
| GitNexus impact | CLI 오류로 완료하지 못함. `rg` 기반 대체 확인 |
| 브라우저 화면 확인 | 미수행. 사용자가 직접 확인 필요 |
| API 런타임 확인 | 미수행. 사용자가 Swagger 또는 브라우저로 직접 확인 필요 |

빌드 참고:

- Next build는 통과했다.
- `package-lock.json`과 `pnpm-lock.yaml`이 함께 있어 Turbopack root 추론 경고가 남아 있다.
- `next build`와 `tsc --noEmit` 후 `next-env.d.ts`, `tsconfig.tsbuildinfo` 산출물 변경은 원복했다.

## 7. 사용자 직접 확인 방법

프론트 실행 전 `.env.local`에 아래 값을 둔다.

```text
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

사용자 확인 순서:

1. 사용자가 PostgreSQL과 백엔드를 실행한다.
2. 프론트엔드를 실행한다.
3. `admin@test.com / password1234`로 로그인한다.
4. `/admin/service-types`에서 업무 유형 목록과 생성/수정/비활성화를 확인한다.
5. `/admin/reservation-slots`에서 오늘 날짜 슬롯 조회와 슬롯 생성을 확인한다.
6. `/admin/staff`에서 직원 목록 조회를 확인한다.
7. `/admin/service-windows`에서 창구별 담당 업무 표시를 확인한다.

Swagger 대표 예시:

`GET /api/admin/service-windows`

인증:

- `admin@test.com / password1234` 로그인 후 access token 사용

기대 결과:

- `success: true`
- `data`에 기본 창구 목록이 포함
- 각 창구의 `serviceTypes`에 담당 업무 유형 목록이 포함

## 8. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 기준정보 연동 중 | 관리자 전체 업무 유형 조회 API | 현재 `GET /api/service-types`는 활성 업무만 조회해 비활성 항목 관리에는 한계가 있음 | 후속 |
| 기준정보 연동 중 | 예약 슬롯 수정/비활성화 API | 운영 중 정원 변경, 휴무 처리, 슬롯 비활성화가 필요 | 후속 |
| 기준정보 연동 중 | 직원 생성/수정/삭제 API | 직원 관리 화면의 쓰기 동작을 실제 서버 상태로 반영해야 함 | 후속 |
| 기준정보 연동 중 | 창구 생성/수정/담당자 배정 API | 창구 관리 화면의 쓰기 동작과 담당자 표시를 실제 서버 상태로 반영해야 함 | 후속 |
| 검증 중 | GitNexus impact/detect_changes 실패 원인 정리 | impact와 detect_changes가 빈 출력으로 실패 | 후속 |

## 9. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성
- [x] PR 문서 초안 작성
- [x] 전체 체크리스트 갱신
- [x] TypeScript 정적 검증 완료
- [x] Next build 완료
- [x] 정적 공백 확인 완료
- [ ] 브라우저 관리자 기준정보 화면 확인
- [ ] Swagger 대표 예시 확인

## 10. 커밋 메시지 초안

```text
feat: 관리자 기준정보 화면 API 연동

- 관리자 기준정보 API client 추가
- 업무 유형 관리 화면을 실제 API로 연결
- 예약 슬롯 관리 화면을 실제 API로 연결
- 직원과 창구 기준정보 조회를 실제 API로 연결
- 미구현 쓰기 API 동작은 후속 안내로 정리
```
