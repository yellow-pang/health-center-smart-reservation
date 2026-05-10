# Office Context 기준정보 및 ReservationSlot API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 작업 브랜치 | `feat/office-context-add` |
| 현재 후속 브랜치 | `feat/reservation-create-api` |
| base 브랜치 | `main` 추정 |
| PR 범위 | Office 기준정보 API와 예약 슬롯 조회/생성 API |
| 빌드 확인 | `mvn -q -DskipTests compile` 성공 |
| 테스트 확인 | `mvn -q test-compile` 성공 |
| 정적 확인 | `git diff --check` 성공 |
| 실행/API 확인 | 서버 기동과 API 런타임 호출은 사용자 직접 확인 필요 |

## PR 제목

```text
feat: Office 기준정보와 예약 슬롯 API 구현
```

## PR 본문

```markdown
## 개요

예약 신청, 방문 접수, 대기열 기능이 참조할 Office Context 기준정보와 예약 가능 시간 조회 기반을 구현합니다.

이번 PR은 `feat/office-context-add` 브랜치를 닫기 위한 통합 PR입니다. 업무 유형, 직원, 창구 기준정보를 먼저 고정하고, 그 기준정보를 소비하는 예약 슬롯 조회/생성 API까지 포함합니다.

## 변경 내용

- `GET /api/service-types` 공개 업무 유형 조회 API 추가
- 관리자 업무 유형 생성, 수정, 비활성화 API 추가
- 관리자 직원 목록 조회 API 추가
- 관리자 창구 업무 매핑 조회 API 추가
- `GET /api/reservation-slots` 예약 가능 시간 조회 API 추가
- `POST /api/admin/reservation-slots` 관리자 예약 슬롯 생성 API 추가
- Office, ReservationSlot Controller/Service/Mapper/DTO/VO 추가
- `service_types`, `service_windows`, `service_window_service_types`, `reservation_slots` schema 추가
- 업무 유형, 창구, 창구 업무 매핑, 14일치 예약 슬롯 seed 추가
- API 명세서, ERD, 전체 체크리스트, 브랜치 구현 기록 갱신

## 검증

- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [x] `git diff --check`
- [ ] 서버 기동 후 `GET /api/service-types` 확인
- [ ] 관리자 토큰으로 `/api/admin/service-types`, `/api/admin/staff`, `/api/admin/service-windows` 확인
- [ ] 로그인 토큰으로 `GET /api/reservation-slots` 확인
- [ ] 관리자 토큰으로 `POST /api/admin/reservation-slots` 확인
- [ ] Swagger UI에서 Office/Admin/ReservationSlot API 노출 확인

## 미검증 사유

- 프로젝트 운영 기준에 따라 서버 기동, API 런타임 호출, Swagger/브라우저 확인은 에이전트가 직접 실행하지 않고 사용자가 직접 확인합니다.
- GitNexus MCP/CLI를 사용할 수 없어 전용 impact/detect_changes는 수행하지 못했고, `rg`와 Maven 검증으로 대체했습니다.

## 후속 작업

- `feat/reservation-create-api` 브랜치에서 예약 신청 API 구현
- 예약 슬롯 정원 증가를 조건부 update로 처리해 정원 초과 방지
- 동일 사용자 동일 시간대 중복 예약 방지
- 예약번호 생성 정책 구현
- 내 예약 조회와 예약 취소 구현
- 창구/업무 매핑 변경 API와 직원 담당 업무 배정 API 검토
```

## 사용자 직접 확인 명령

### 서버 기동

```powershell
cd backend
mvn spring-boot:run
```

### 공개 업무 유형 조회

```powershell
Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/service-types'
```

기대 결과:

- `success`가 `true`
- `data`에 `VACCINATION`, `HEALTH_CHECK`, `HEALTH_CONSULT`가 포함

### 관리자 Office API 확인

```powershell
$admin = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"admin@test.com","password":"password1234"}'
$adminToken = $admin.data.accessToken
Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/admin/staff' -Headers @{Authorization="Bearer $adminToken"}
Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/admin/service-windows' -Headers @{Authorization="Bearer $adminToken"}
```

기대 결과:

- 직원 목록에 `staff@test.com`이 포함
- 창구 목록에 기본 창구와 담당 업무 유형이 포함

### 예약 슬롯 조회

```powershell
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"citizen@test.com","password":"password1234"}'
$token = $login.data.accessToken
$date = Get-Date -Format 'yyyy-MM-dd'
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/reservation-slots?serviceTypeId=1&date=$date" -Headers @{Authorization="Bearer $token"}
```

기대 결과:

- `success`가 `true`
- 09:00부터 16:30까지 30분 단위 슬롯이 포함
- `availableCount`와 `available`이 포함

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영
- [ ] `feat/reservation-create-api`에서 예약 신청 API 후속 작업 진행

## 커밋 메시지 초안

```text
feat: Office 기준정보와 예약 슬롯 API 구현

- 업무 유형, 직원, 창구 업무 매핑 조회 API 추가
- 관리자 업무 유형 생성, 수정, 비활성화 API 추가
- 예약 가능 시간 조회와 관리자 예약 슬롯 생성 API 추가
- Office와 ReservationSlot MyBatis Mapper 추가
- 기준정보와 예약 슬롯 seed 추가
- 구현 기록, PR 초안, 전체 체크리스트 갱신
```
