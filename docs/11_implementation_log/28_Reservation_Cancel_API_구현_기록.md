# Reservation Cancel API 구현 기록

## 1. 작업 목표

- 로그인 사용자가 본인 예약을 취소할 수 있게 한다.
- 관리자가 같은 보건소 예약을 취소할 수 있게 한다.
- 예약 취소 시 예약 슬롯의 `reserved_count`를 함께 복구한다.

## 2. 작업 범위

- [x] 이번 브랜치에 포함: `DELETE /api/reservations/{id}`
- [x] 이번 브랜치에 포함: 예약 취소 상태 변경
- [x] 이번 브랜치에 포함: 예약 슬롯 `reserved_count` 감소
- [x] 이번 브랜치에 포함: 예약 취소 정책 클래스 분리
- [x] 이번 브랜치에 포함: 예약 시간 1시간 전 취소 제한
- [x] 이번 브랜치에서 제외: 체크인 후 취소/방문/대기 연계 취소
- [x] 이번 브랜치에서 제외: 프론트엔드 취소 버튼 연동

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 관련 항목 확인
- [x] `docs/04_api/01_API_명세서.md` 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] 영향받는 파일 확인

## 4. 영향 분석

GitNexus impact 분석:

| 대상 | 직접 영향 | 영향 프로세스 | 위험도 |
|---|---:|---:|---|
| `ReservationController` | 0 | 0 | LOW |
| `ReservationCommandService` | 1 (`ReservationController.java`) | 0 | LOW |
| `ReservationMapper` | 1 (`ReservationCommandService.java`) | 0 | LOW |
| `ReservationSlotMapper` | 3 (`ReservationSlotQueryService.java`, `ReservationSlotCommandService.java`, `ReservationCommandService.java`) | 0 | LOW |
| `ReservationVO` | 2 (`ReservationCommandService.java`, `ReservationCreateResponse.java`) | 0 | LOW |

주의:

- `gitnexus status`는 stale 상태다.
- 이전 작업에서 `gitnexus analyze`는 로컬 인덱스 오류로 실패했다.
- `gitnexus detect_changes --repo health-center-smart-reservation`는 현재 CLI에서 unknown command로 실패했다.
- 이번 작업은 기존 stale index impact와 `rg`, Maven compile/test-compile로 보완 검증한다.

## 5. 구현 체크리스트

- [x] Controller 수정
- [x] CommandService 수정
- [x] Policy 추가
- [x] Mapper 수정
- [x] Mapper XML 수정
- [x] 예외 처리 확인
- [x] 공통 응답 형식 확인

## 6. 검증 체크리스트

- [x] Maven compile 확인
- [x] Maven test-compile 확인
- [x] `git diff --check` 확인
- [x] API 수동 호출 방법과 기대 결과 작성
- [x] Swagger 확인 URL과 확인 항목 작성
- [ ] 서버 기동 확인
- [ ] API 런타임 호출 확인
- [ ] Swagger 브라우저 확인

## 7. 구현 내용

### 7.1 API

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| DELETE | `/api/reservations/{id}` | 예약 취소 | 예약자 본인, 같은 보건소 ADMIN |

### 7.2 정책

- `RESERVED` 상태 예약만 취소할 수 있다.
- 예약자 본인은 본인 예약만 취소할 수 있다.
- `ADMIN`은 같은 보건소 예약만 취소할 수 있다.
- 예약 시간 1시간 전까지만 취소할 수 있다.
- 예약 취소와 슬롯 `reserved_count` 감소는 하나의 트랜잭션에서 처리한다.
- 슬롯 감소가 실패하면 트랜잭션 rollback으로 예약 취소도 함께 취소된다.

## 8. 사용자 직접 확인 방법

### 8.1 서버 기동

```powershell
cd backend
mvn spring-boot:run
```

### 8.2 로그인과 예약 준비

```powershell
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"citizen@test.com","password":"password1234"}'
$token = $login.data.accessToken
$myReservations = Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/reservations/me' -Headers @{Authorization="Bearer $token"}
$reservationId = $myReservations.data[0].reservationId
```

### 8.3 예약 취소

```powershell
Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/reservations/$reservationId" -Headers @{Authorization="Bearer $token"}
```

기대 결과:

- `success`가 `true`
- `data`가 `null`
- 같은 예약 상세 조회 시 `status`가 `CANCELED`
- 같은 슬롯 조회 시 `reservedCount`가 1 감소

### 8.4 취소 가능 시간 초과 확인

예약 시간이 1시간 이내인 예약을 취소하면 기대 결과:

- HTTP 409
- `success`가 `false`
- `error.code`가 `RESERVATION_CANCEL_TIME_EXPIRED`

### 8.5 권한 실패 확인

다른 시민 계정으로 타인 예약을 취소하면 기대 결과:

- HTTP 403
- `success`가 `false`
- `error.code`가 `FORBIDDEN`

## 9. Swagger 확인 항목

URL:

```text
http://localhost:8080/swagger-ui/index.html
```

확인:

- `ReservationController`에 `DELETE /api/reservations/{reservationId}` 노출
- 성공 응답이 `success + data + error` 구조인지 확인
- 실패 응답 코드 `FORBIDDEN`, `RESERVATION_CANCEL_TIME_EXPIRED`, `RESERVATION_CANCEL_INVALID_STATUS` 확인

## 10. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 구현 중 | 프론트 내 예약 화면 취소 버튼 연동 | API 구현 후 사용자 예약 관리 UX 완성 필요 | 후속 작업 |
| 구현 중 | 체크인 후 취소 정책 확장 | Visit/Queue 생성 이후에는 예약 취소와 방문/대기 취소 정책이 함께 필요 | Visit/Queue 구현 이후 재검토 |
| 구현 중 | 브랜치명 오타 정리 검토 | 현재 브랜치명 `cancle` 오타와 `reserved_count` 언더스코어가 규칙과 다름 | 사용자가 필요 시 별도 rename |

## 11. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
