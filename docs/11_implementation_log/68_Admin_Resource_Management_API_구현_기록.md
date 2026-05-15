# Admin Resource Management API 구현 기록

## 1. 작업 목표

- 관리자 기준정보 화면에서 예약 슬롯, 직원, 창구를 조회뿐 아니라 생성/수정/비활성화까지 처리할 수 있게 한다.
- 창구에는 담당 직원을 배정할 수 있게 하고, 개발 seed에 기본 담당자 배정 예시를 추가한다.
- 기존 eGovFrame Simple Backend Template, Maven, MyBatis, `egovframework.healthcenter` 패키지 기준을 유지한다.

## 2. 작업 범위

- [x] 현재 브랜치와 작업 트리 확인
- [x] 예약 슬롯 수정 API 추가
- [x] 예약 슬롯 비활성화 API 추가
- [x] 직원 생성 API 추가
- [x] 직원 수정 API 추가
- [x] 직원 비활성화 API 추가
- [x] 창구 생성 API 추가
- [x] 창구 수정 API 추가
- [x] 창구 비활성화 API 추가
- [x] 창구 담당자 배정 필드 추가
- [x] MyBatis Mapper XML 쿼리 추가
- [x] 관리자 기준정보 API client 갱신
- [x] 관리자 예약 슬롯 화면 수정/비활성화 UX 연결
- [x] 관리자 직원 화면 생성/수정/비활성화 UX 연결
- [x] 관리자 창구 화면 생성/수정/비활성화/담당자 배정 UX 연결
- [x] 담당자 배정 seed/schema 보강
- [x] TypeScript 정적 검증
- [x] Maven compile/test-compile 검증
- [ ] Swagger 대표 예시 확인
- [ ] 브라우저 관리자 기준정보 화면 확인

## 3. 구현 내용

### 3.1 예약 슬롯 관리

추가 API:

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| PUT | `/api/admin/reservation-slots/{id}` | 예약 슬롯 수정 | ADMIN |
| PATCH | `/api/admin/reservation-slots/{id}/deactivate` | 예약 슬롯 비활성화 | ADMIN |

주요 처리:

- 슬롯 날짜, 시간, 업무 유형, 정원, 활성 여부를 수정한다.
- 이미 예약된 수보다 작은 정원으로 수정하지 않도록 `reserved_count <= capacity` 조건을 둔다.
- 동일 업무/날짜/시간 중복 슬롯은 DB unique 제약과 서비스 예외로 방어한다.
- 관리자 예약 슬롯 화면에서 생성/수정 모드를 같은 다이얼로그로 처리하고, 비활성화 후 목록에서 제거한다.

### 3.2 직원 관리

추가 API:

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/admin/staff` | 직원 생성 | ADMIN |
| PUT | `/api/admin/staff/{id}` | 직원 수정 | ADMIN |
| PATCH | `/api/admin/staff/{id}/deactivate` | 직원 비활성화 | ADMIN |

주요 처리:

- 직원 생성 시 이름, 이메일, 비밀번호, 역할, 활성 여부를 입력받는다.
- 비밀번호는 기존 eGovFrame 보안 유틸 `EgovFileScrty.encryptPassword`를 사용해 암호화한다.
- 직원 수정 시 이메일은 식별자로 유지하고, 이름/역할/활성 여부를 수정한다.
- 직원 삭제는 실제 삭제가 아니라 `active = false` 비활성화로 처리한다.
- 관리자 직원 화면에서 생성/수정/비활성화 흐름을 실제 API로 연결했다.

### 3.3 창구 관리와 담당자 배정

추가 API:

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/admin/service-windows` | 창구 생성 | ADMIN |
| PUT | `/api/admin/service-windows/{id}` | 창구 수정 및 담당자/업무 매핑 변경 | ADMIN |
| PATCH | `/api/admin/service-windows/{id}/deactivate` | 창구 비활성화 | ADMIN |

주요 처리:

- `service_windows.staff_id`를 추가해 창구 담당 직원을 저장한다.
- 창구 응답에 `staffId`, `staffName`을 포함한다.
- 담당자로 배정할 수 있는 계정은 현재 보건소의 활성 직원/관리자 계정으로 제한한다.
- 창구 수정 시 담당자, 창구명, 창구 번호, 활성 여부, 업무 유형 매핑을 함께 갱신한다.
- 창구 업무 매핑은 기존 매핑을 비활성화한 뒤 요청된 업무 유형을 upsert한다.
- 관리자 창구 화면에서 담당자 선택, 업무 유형 체크박스, 생성/수정/비활성화 흐름을 실제 API로 연결했다.

### 3.4 개발 seed/schema

- `service_windows.staff_id` 컬럼을 추가했다.
- 기본 1번 창구에 `staff@test.com` 담당자 배정 seed를 추가했다.
- 창구 seed는 `ON CONFLICT` 갱신 시에도 담당자 배정이 유지되도록 보강했다.

## 4. 변경 파일 요약

| 영역 | 파일 |
|---|---|
| 예약 슬롯 API | `ReservationSlotController.java`, `ReservationSlotCommandService.java`, `ReservationSlotMapper.java`, `ReservationSlotUpdateRequest.java`, `ReservationSlot_SQL_postgresql.xml` |
| 직원/창구 API | `AdminOfficeController.java`, `OfficeCommandService.java`, `OfficeQueryService.java`, `OfficeMapper.java`, `Office_SQL_postgresql.xml` |
| 직원/창구 DTO/VO | `StaffCreateRequest.java`, `StaffUpdateRequest.java`, `ServiceWindowUpsertRequest.java`, `ServiceWindowResponse.java`, `ServiceWindowMappingVO.java` |
| DB | `schema.sql`, `data.sql` |
| Frontend | `admin-master-data-api.ts`, `mock-data.ts`, `reservation-slots/page.tsx`, `staff/page.tsx`, `service-windows/page.tsx` |

## 5. GitNexus 및 영향 범위

GitNexus impact/detect_changes 결과:

| 대상 | 결과 |
|---|---|
| `ReservationSlotController` | 실패. exit 1, 출력 없음 |
| `ReservationSlotCommandService` | 실패. exit 1, 출력 없음 |
| `ReservationSlotMapper` | 실패. exit 1, 출력 없음 |
| `AdminOfficeController` | 실패. exit 1, 출력 없음 |
| `OfficeCommandService` | 실패. exit 1, 출력 없음 |
| `OfficeMapper` | 실패. exit 1, 출력 없음 |
| `gitnexus detect_changes` | 실패. exit 1, 출력 없음 |

대체 확인:

- `rg`와 파일 구조 확인으로 Reservation/Office Context 변경 범위를 확인했다.
- 변경은 관리자 예약 슬롯, 직원, 창구 관리 기능과 공통 기준정보 API client에 제한했다.
- Maven compile/test-compile과 TypeScript 정적 검증으로 컴파일 정합성을 확인했다.

## 6. 검증 결과

| 검증 | 결과 |
|---|---|
| `git diff --check` | 통과. LF/CRLF 경고만 표시 |
| `npm.cmd exec -- tsc --noEmit` | 통과 |
| `npm.cmd run build` | 통과 |
| `mvn.cmd -q -DskipTests compile` | 통과 |
| `mvn.cmd -q test-compile` | 통과 |
| Swagger/API 런타임 확인 | 미수행. 사용자 직접 확인 필요 |
| 브라우저 화면 확인 | 미수행. 사용자 직접 확인 필요 |

## 7. Swagger 대표 예시

대표 예시는 변경 범위 중 상태 변경과 화면 확인 효과가 큰 창구 담당자 배정으로 둔다.

`PUT /api/admin/service-windows/1`

인증:

- `admin@test.com / password1234` 로그인 후 access token 사용

요청 예시:

```json
{
  "windowNumber": 1,
  "name": "1번 창구",
  "staffId": 2,
  "serviceTypeIds": [1, 2],
  "active": true
}
```

기대 결과:

- `success: true`
- `data.id`가 `1`
- `data.staffId`가 요청한 담당자 ID
- `data.serviceTypes`에 요청한 업무 유형 목록 포함

## 8. 사용자 직접 확인 방법

Swagger 확인:

1. Docker PostgreSQL과 백엔드를 사용자가 직접 실행한다.
2. Swagger에서 `admin@test.com / password1234`로 로그인한다.
3. access token을 Authorize에 설정한다.
4. 대표 예시 `PUT /api/admin/service-windows/1`을 `Try it out`으로 호출한다.
5. `GET /api/admin/service-windows`로 담당자와 업무 매핑이 반영됐는지 확인한다.

브라우저 확인:

1. `/admin/reservation-slots`에서 예약 슬롯 생성, 수정, 비활성화 버튼 흐름을 확인한다.
2. `/admin/staff`에서 직원 생성, 수정, 비활성화 흐름을 확인한다.
3. `/admin/service-windows`에서 창구 생성, 담당자 배정, 업무 유형 매핑 수정, 비활성화 흐름을 확인한다.

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 구현 중 | 창구 비활성화 시 진행 중 대기표 영향 검토 | 운영 중인 창구를 비활성화할 때 대기열 재배정 정책이 필요할 수 있음 | 후속 |
| 구현 중 | 직원 비활성화 시 창구 담당자 해제 정책 검토 | 비활성 직원이 기존 창구 담당자로 남는 경우 표시/운영 정책이 필요함 | 후속 |
| 구현 중 | 예약 슬롯 수정 시 이미 예약된 사용자 안내 정책 | 예약이 있는 슬롯의 시간/업무 변경은 알림 또는 제한 정책이 필요할 수 있음 | 후속 |

## 10. 커밋 기록

```text
e062ef8 feat: 예약 슬롯 수정과 비활성화 구현
97536d6 feat: 직원 생성 수정 삭제 API 구현
e885cee feat: 창구 생성 수정 담당자 배정 API 구현
```

