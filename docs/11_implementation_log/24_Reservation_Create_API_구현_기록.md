# Reservation Create API 구현 기록

## 1. 작업 목표

- 로그인 사용자가 예약 슬롯을 선택해 예약을 신청할 수 있게 한다.
- 예약 신청 시 정원 초과와 동일 사용자 중복 예약을 서버에서 방지한다.
- 내 예약 조회, 상세 조회, 예약 취소는 다음 작은 단위로 분리한다.

## 2. 작업 범위

- [x] 이번 브랜치에 포함: `POST /api/reservations`
- [x] 이번 브랜치에 포함: `reservations` schema 추가
- [x] 이번 브랜치에 포함: 예약번호 생성
- [x] 이번 브랜치에 포함: 예약 슬롯 정원 조건부 증가
- [x] 이번 브랜치에 포함: 동일 사용자 동일 슬롯 중복 예약 방지
- [x] 이번 브랜치에서 제외: 내 예약 조회
- [x] 이번 브랜치에서 제외: 예약 상세 조회
- [x] 이번 브랜치에서 제외: 예약 취소

## 3. 작업 전 체크리스트

- [x] `docs/04_api/01_API_명세서.md` 확인
- [x] `docs/05_frontend/02_UX_API_계약_우선순위.md` 확인
- [x] `docs/03_database/01_ERD_및_테이블_명세서.md` 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] 영향받는 파일 확인

## 4. 영향 분석

GitNexus MCP 도구는 현재 세션에 노출되어 있지 않고, `npm.cmd exec -- gitnexus status`도 로컬 `gitnexus` 모듈 누락으로 실패했다.

대체 확인:

- 신규 `ReservationController`, `ReservationCommandService`, `ReservationMapper`는 기존 호출자가 없음
- `ReservationSlotMapper`에는 `increaseReservedCountIfAvailable` 메서드를 추가했으며 기존 `selectActiveSlots`, `selectSlotById`, `insertSlot` 호출부는 유지
- `ReservationSlot_SQL_postgresql.xml`의 `selectSlotById`는 비활성 업무 유형 슬롯을 제외하도록 조건을 보강
- `SecurityConfig`에는 기존에 `POST /api/reservations` 역할 규칙이 있음

위험도:

- 신규 API 호출 영향: 낮음
- 예약 슬롯 정원 변경 SQL 영향: 중간
- DB 초기화 영향: 중간

## 5. 구현 체크리스트

- [x] Controller 추가
- [x] CommandService 추가
- [x] Mapper 추가
- [x] Mapper XML 추가
- [x] DTO/VO 추가
- [x] `reservations` schema 추가
- [x] `reservation_slots.reserved_count` 조건부 증가 SQL 추가
- [x] 중복 예약 방지 unique index 추가
- [x] 공통 응답 형식 확인

## 6. 검증 체크리스트

- [x] Maven compile 확인
- [x] Maven test-compile 확인
- [x] `git diff --check` 확인
- [x] API 수동 호출 방법과 기대 결과 작성
- [x] Swagger 확인 URL과 확인 항목 작성

## 7. 구현 내용

### 7.1 API

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/reservations` | 예약 신청 | CITIZEN, GUARDIAN, STAFF, ADMIN |

### 7.2 정책

- 로그인 사용자의 `memberId`를 예약자로 사용한다.
- 요청의 `serviceTypeId`와 예약 슬롯의 업무 유형이 일치해야 한다.
- 예약 슬롯은 활성 상태이고, 업무 유형도 활성 상태여야 한다.
- 예약 가능 날짜는 오늘부터 14일 이내다.
- `reserved_count < capacity` 조건부 update로 정원 초과를 방지한다.
- 동일 사용자가 같은 예약 슬롯에 `RESERVED`, `CHECKED_IN` 상태 예약을 중복 생성할 수 없다.
- 예약번호는 `RSV-yyyyMMdd-랜덤8자리` 형식으로 생성한다.

## 8. 사용자 직접 확인 방법

### 8.1 서버 기동

```powershell
cd backend
mvn spring-boot:run
```

### 8.2 로그인 후 예약 슬롯 조회

```powershell
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"citizen@test.com","password":"password1234"}'
$token = $login.data.accessToken
$date = Get-Date -Format 'yyyy-MM-dd'
$slots = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/reservation-slots?serviceTypeId=1&date=$date" -Headers @{Authorization="Bearer $token"}
$slotId = $slots.data[0].slotId
```

### 8.3 예약 신청

```powershell
$body = @{
  serviceTypeId = 1
  reservationSlotId = $slotId
  visitorName = '홍길동'
  visitorPhone = '010-1234-5678'
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/reservations' -ContentType 'application/json' -Headers @{Authorization="Bearer $token"} -Body $body
```

기대 결과:

- `success`가 `true`
- `data.reservationNo`가 `RSV-`로 시작
- `data.status`가 `RESERVED`

### 8.4 중복 예약 확인

같은 요청을 다시 보내면 기대 결과:

- `success`가 `false`
- `error.code`가 `RESERVATION_DUPLICATED`

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 구현 중 | 예약 신청 실패 후 정원 복구 검증 | 같은 트랜잭션 안에서 예외 발생 시 rollback되어야 함 | Maven 검증 완료, 런타임 확인 필요 |
| 구현 중 | 내 예약 조회/취소 구현 | 예약 생성 후 사용자 확인과 취소 흐름 필요 | 후속 작업 |
| 구현 중 | 예약번호 순번 정책 검토 | 현재는 충돌 가능성이 낮은 랜덤 8자리 방식 | 포트폴리오 완성 단계에서 필요 시 순번 정책 검토 |

## 10. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
