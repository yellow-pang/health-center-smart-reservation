# ReservationSlot API 구현 기록

## 1. 작업 목표

- 예약 신청 전에 필요한 날짜별 예약 가능 시간 API를 먼저 구현한다.
- 관리자 예약 슬롯 생성 API와 초기 seed를 추가해 프론트 예약 시간 선택 흐름을 고정한다.
- 예약 신청, 내 예약 조회, 예약 취소는 다음 작은 단위로 분리한다.

## 2. 작업 범위

- [x] 이번 브랜치에 포함: `GET /api/reservation-slots`
- [x] 이번 브랜치에 포함: `POST /api/admin/reservation-slots`
- [x] 이번 브랜치에 포함: `reservation_slots` schema/seed 추가
- [x] 이번 브랜치에 포함: ReservationSlot Mapper/Service/Controller/DTO 추가
- [x] 이번 브랜치에서 제외: 예약 신청
- [x] 이번 브랜치에서 제외: 내 예약 조회/상세 조회
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

- `reservation_slots` 관련 구현 코드는 기존에 없어 신규 심볼 직접 호출자 없음
- `/api/reservation-slots/**`는 기존 `SecurityConfig`에서 로그인 사용자 접근 규칙이 이미 있음
- `/api/admin/**`는 기존 `SecurityConfig`에서 ADMIN 권한으로 제한됨
- `schema.sql`, `data.sql`은 애플리케이션 SQL init에 영향을 주므로 런타임 확인 필요

위험도:

- 코드 호출 영향: 낮음
- DB 초기화 영향: 중간

## 5. 구현 체크리스트

- [x] Controller 추가
- [x] QueryService 추가
- [x] CommandService 추가
- [x] Mapper 추가
- [x] Mapper XML 추가
- [x] DTO/VO 추가
- [x] `reservation_slots` schema 추가
- [x] 14일치 예약 슬롯 seed 추가
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
| GET | `/api/reservation-slots?serviceTypeId=1&date=2026-05-10` | 예약 가능 시간 조회 | 로그인 사용자 |
| POST | `/api/admin/reservation-slots` | 예약 슬롯 생성 | ADMIN |

### 7.2 정책

- 조회 날짜는 오늘부터 14일 이내만 허용한다.
- 활성 업무 유형과 활성 슬롯만 조회한다.
- `availableCount = capacity - reservedCount`로 계산한다.
- `available`은 활성 상태이고 잔여 인원이 1명 이상일 때 true로 반환한다.
- 예약 슬롯 생성 시 종료 시간은 시작 시간보다 늦어야 한다.
- 예약 가능 인원은 1명 이상이어야 한다.

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
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/reservation-slots?serviceTypeId=1&date=$date" -Headers @{Authorization="Bearer $token"}
```

기대 결과:

- `success`가 `true`
- `data`에 09:00부터 16:30까지 30분 단위 슬롯이 포함
- `availableCount`와 `available`이 포함

### 8.3 관리자 예약 슬롯 생성

```powershell
$admin = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"admin@test.com","password":"password1234"}'
$adminToken = $admin.data.accessToken
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/admin/reservation-slots' -ContentType 'application/json' -Headers @{Authorization="Bearer $adminToken"} -Body '{"serviceTypeId":1,"date":"2026-05-25","startTime":"09:00","endTime":"09:30","capacity":5}'
```

기대 결과:

- `success`가 `true`
- 생성된 슬롯의 `reservedCount`는 0
- 중복 생성 시 `RESERVATION_SLOT_INVALID_REQUEST` 오류 응답

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 구현 중 | 예약 슬롯 수정/비활성화 API | 운영 중 정원 변경과 휴무 처리를 위해 필요 | 후속 작업 |
| 구현 중 | 예약 신청 동시성 정책 | `reserved_count` 증가를 조건부 update로 처리해야 함 | 다음 Reservation 작업 |

## 10. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
