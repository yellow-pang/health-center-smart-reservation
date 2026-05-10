# My Reservation Detail API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/my-reservation-detail-reservation` |
| base 브랜치 | `main` 추정 |
| PR 범위 | 내 예약 조회, 예약 상세 조회 API |
| 빌드 확인 | `mvn -q -DskipTests compile` 성공 |
| 테스트 확인 | `mvn -q test-compile` 성공 |
| 정적 확인 | `git diff --check` 성공 |
| 실행/API 확인 | 서버 기동과 API 런타임 호출은 사용자 직접 확인 필요 |

## PR 제목

```text
feat: 내 예약 및 예약 상세 조회 API 구현
```

## PR 본문

```markdown
## 개요

예약 신청 이후 사용자가 본인의 예약 목록을 확인하고, 예약자 본인 또는 같은 보건소 직원/관리자가 예약 상세를 확인할 수 있도록 조회 API를 추가합니다.

## 변경 내용

- `GET /api/reservations/me` 내 예약 조회 API 추가
- `GET /api/reservations/{id}` 예약 상세 조회 API 추가
- 예약 조회 전용 `ReservationQueryService` 추가
- 예약 목록/상세 응답 `ReservationResponse` 추가
- 예약 조회 Mapper와 MyBatis SQL 추가
- 예약 상세 조회 권한 검증 추가
- API 명세, 전체 체크리스트, 브랜치 구현 기록 갱신

## 검증

- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [x] `git diff --check`
- [ ] 서버 기동 후 `GET /api/reservations/me` 확인
- [ ] 서버 기동 후 `GET /api/reservations/{id}` 확인
- [ ] 다른 시민 계정으로 타인 예약 상세 조회 시 `FORBIDDEN` 확인
- [ ] Swagger UI에서 Reservation API 노출 확인

## 미검증 사유

- 프로젝트 운영 기준에 따라 서버 기동, API 런타임 호출, Swagger/브라우저 확인은 에이전트가 직접 실행하지 않고 사용자가 직접 확인합니다.
- GitNexus MCP 리소스는 세션에 노출되지 않았고, `gitnexus analyze`는 로컬 인덱스 오류로 실패했습니다. 기존 stale index 기반 impact와 `rg`, Maven 검증으로 대체했습니다.

## 후속 작업

- 예약 취소 API 구현
- 취소 시 예약 슬롯 `reserved_count` 복구
- 예약 취소 가능 시간 정책 검증
- 예약 상세 직원/관리자 권한 런타임 확인
- GitNexus analyze 실패 원인 점검
```

## 사용자 직접 확인 명령

```powershell
cd backend
mvn spring-boot:run
```

```powershell
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"citizen@test.com","password":"password1234"}'
$token = $login.data.accessToken
$myReservations = Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/reservations/me' -Headers @{Authorization="Bearer $token"}
$reservationId = $myReservations.data[0].reservationId
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/reservations/$reservationId" -Headers @{Authorization="Bearer $token"}
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영
- [ ] 후속 브랜치 생성 또는 다음 작업 문서화

## 커밋 메시지 초안

```text
feat: 내 예약 및 예약 상세 조회 API 구현

- 내 예약 조회와 예약 상세 조회 API 추가
- 예약 조회 QueryService와 응답 DTO 추가
- 예약 조회 Mapper와 MyBatis SQL 추가
- 예약 상세 조회 권한 검증 추가
- API 명세, 구현 기록, PR 초안, 전체 체크리스트 갱신
```
