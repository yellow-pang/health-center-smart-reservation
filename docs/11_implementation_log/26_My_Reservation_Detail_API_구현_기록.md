# My Reservation Detail API 구현 기록

## 1. 작업 목표

- 로그인 사용자가 본인의 예약 목록을 조회할 수 있게 한다.
- 예약자 본인 또는 같은 보건소의 직원/관리자가 예약 상세를 조회할 수 있게 한다.
- 예약 취소는 다음 작은 단위로 분리한다.

## 2. 작업 범위

- [x] 이번 브랜치에 포함: `GET /api/reservations/me`
- [x] 이번 브랜치에 포함: `GET /api/reservations/{id}`
- [x] 이번 브랜치에 포함: 예약 목록/상세 응답 DTO
- [x] 이번 브랜치에 포함: 예약 조회 QueryService
- [x] 이번 브랜치에 포함: 예약자 본인 또는 같은 보건소 직원/관리자 상세 조회 권한 검증
- [x] 이번 브랜치에서 제외: 예약 취소
- [x] 이번 브랜치에서 제외: 취소 시 슬롯 예약 수 복구
- [x] 이번 브랜치에서 제외: Visit/Queue 연계

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 관련 항목 확인
- [x] `docs/04_api/01_API_명세서.md` 확인
- [x] `docs/05_frontend/02_UX_API_계약_우선순위.md` 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] 영향받는 파일 확인

## 4. 영향 분석

GitNexus MCP 리소스는 현재 세션에 노출되어 있지 않았다.

GitNexus CLI 상태:

- `gitnexus status`: stale
- `gitnexus analyze`: 실패
- 실패 사유: `COPY failed for File: Binder exception: Trying to insert into an index on table File but its extension is not loaded.`

수행한 impact 분석:

| 대상 | 직접 영향 | 영향 프로세스 | 위험도 |
|---|---:|---:|---|
| `ReservationController` | 0 | 0 | LOW |
| `ReservationMapper` | 1 (`ReservationCommandService`) | 0 | LOW |
| `ReservationVO` | 2 (`ReservationCommandService`, `ReservationCreateResponse`) | 0 | LOW |
| `selectReservationByNo` | 1 (`ReservationCommandService`) | 1 | LOW |
| `createReservation` | 1 (`ReservationController.createReservation`) | 1 | LOW |

대체 확인:

- `rg`로 `ReservationController`, `ReservationMapper`, `ReservationVO`, `/api/reservations` 참조 확인
- 기존 예약 신청 API 호출 경로를 유지하면서 조회 메서드만 추가
- `SecurityConfig`에 `GET /api/reservations/me`, `GET /api/reservations/*` 인증 규칙이 이미 존재함을 확인

## 5. 구현 체크리스트

- [x] Controller 수정
- [x] QueryService 추가
- [x] Mapper 수정
- [x] Mapper XML 수정
- [x] DTO/VO 추가 또는 수정
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
| GET | `/api/reservations/me` | 내 예약 조회 | 로그인 사용자 |
| GET | `/api/reservations/{id}` | 예약 상세 조회 | 예약자 본인, 같은 보건소 STAFF/ADMIN |

### 7.2 정책

- 내 예약 목록은 로그인 사용자의 `memberId` 기준으로 조회한다.
- 예약 상세는 예약자 본인에게 허용한다.
- `STAFF`, `ADMIN`은 같은 보건소 예약만 상세 조회할 수 있다.
- 권한이 없으면 `FORBIDDEN` 오류 응답을 반환한다.
- 예약이 없으면 `RESERVATION_NOT_FOUND` 오류 응답을 반환한다.

## 8. 사용자 직접 확인 방법

### 8.1 서버 기동

```powershell
cd backend
mvn spring-boot:run
```

### 8.2 로그인

```powershell
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"citizen@test.com","password":"password1234"}'
$token = $login.data.accessToken
```

### 8.3 내 예약 조회

```powershell
$myReservations = Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/reservations/me' -Headers @{Authorization="Bearer $token"}
$myReservations
```

기대 결과:

- `success`가 `true`
- `data`가 배열
- 예약이 있으면 `reservationId`, `reservationNo`, `serviceTypeName`, `date`, `startTime`, `status` 포함

### 8.4 예약 상세 조회

```powershell
$reservationId = $myReservations.data[0].reservationId
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/reservations/$reservationId" -Headers @{Authorization="Bearer $token"}
```

기대 결과:

- `success`가 `true`
- `data.reservationId`가 요청한 ID와 일치
- `data.visitorName`, `data.visitorPhone`, `data.status` 포함

### 8.5 권한 실패 확인

다른 사용자의 예약 ID를 시민 계정으로 조회하면 기대 결과:

- HTTP 403
- `success`가 `false`
- `error.code`가 `FORBIDDEN`

## 9. Swagger 확인 항목

URL:

```text
http://localhost:8080/swagger-ui/index.html
```

확인:

- `ReservationController`에 `GET /api/reservations/me` 노출
- `ReservationController`에 `GET /api/reservations/{reservationId}` 노출
- 응답 스키마가 `success + data + error` 구조인지 확인

## 10. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 구현 중 | 예약 취소 API 구현 | 내 예약 조회 후 사용자가 예약을 취소하는 후속 흐름 필요 | 후속 작업 |
| 구현 중 | 예약 상세의 직원/관리자 객체 권한 런타임 확인 | 같은 보건소 조건이 실제 토큰/seed와 맞는지 확인 필요 | 사용자 직접 확인 |
| 구현 중 | GitNexus analyze 실패 원인 점검 | stale index 갱신이 실패해 detect_changes 전용 검증을 수행하지 못함 | 후속 점검 |

## 11. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
