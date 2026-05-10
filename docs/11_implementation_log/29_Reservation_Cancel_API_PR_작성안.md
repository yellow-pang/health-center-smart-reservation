# Reservation Cancel API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/cancle-reservation-slot-reserved_count` |
| base 브랜치 | `main` 추정 |
| PR 범위 | 예약 취소 API, 슬롯 예약 수 복구 |
| 빌드 확인 | `mvn -q -DskipTests compile` 성공 |
| 테스트 확인 | `mvn -q test-compile` 성공 |
| 정적 확인 | `git diff --check` 성공 |
| 실행/API 확인 | 서버 기동과 API 런타임 호출은 사용자 직접 확인 필요 |

## PR 제목

```text
feat: 예약 취소 및 슬롯 예약 수 복구 구현
```

## PR 본문

```markdown
## 개요

예약자 본인 또는 같은 보건소 관리자가 예약 시간 1시간 전까지 예약을 취소할 수 있도록 `DELETE /api/reservations/{id}` API를 추가합니다.

## 변경 내용

- 예약 취소 API 추가
- 예약 취소 정책 `ReservationCancelPolicy` 추가
- 예약 상태를 `CANCELED`로 변경하는 Mapper SQL 추가
- 예약 슬롯 `reserved_count`를 1 감소시키는 Mapper SQL 추가
- 예약 취소와 슬롯 복구를 하나의 트랜잭션으로 처리
- API 명세, 전체 체크리스트, 브랜치 구현 기록 갱신

## 검증

- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [x] `git diff --check`
- [ ] 서버 기동 후 `DELETE /api/reservations/{id}` 확인
- [ ] 취소 후 예약 상세 상태가 `CANCELED`인지 확인
- [ ] 취소 후 슬롯 `reservedCount`가 1 감소하는지 확인
- [ ] 예약 시간 1시간 이내 취소 시 `RESERVATION_CANCEL_TIME_EXPIRED` 확인
- [ ] 타인 예약 취소 시 `FORBIDDEN` 확인
- [ ] Swagger UI에서 Reservation API 노출 확인

## 미검증 사유

- 프로젝트 운영 기준에 따라 서버 기동, API 런타임 호출, Swagger/브라우저 확인은 에이전트가 직접 실행하지 않고 사용자가 직접 확인합니다.
- GitNexus index는 stale 상태이며, `gitnexus analyze`는 로컬 인덱스 오류로 실패했습니다.
- `gitnexus detect_changes --repo health-center-smart-reservation`는 현재 CLI에서 unknown command로 실패했습니다.
- 기존 stale index 기반 impact와 `rg`, Maven 검증으로 대체했습니다.

## 후속 작업

- 프론트 내 예약 화면 취소 버튼 연동
- 체크인 이후 예약/방문/대기 취소 정책 정리
- 예약 취소 정책 테스트 추가
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
Invoke-RestMethod -Method Delete -Uri "http://localhost:8080/api/reservations/$reservationId" -Headers @{Authorization="Bearer $token"}
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/reservations/$reservationId" -Headers @{Authorization="Bearer $token"}
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영
- [ ] 후속 브랜치 생성 또는 다음 작업 문서화

## 커밋 메시지 초안

```text
feat: 예약 취소 및 슬롯 예약 수 복구 구현

- 예약 취소 API 추가
- 예약 취소 정책 클래스 추가
- 예약 상태 변경과 슬롯 예약 수 감소를 트랜잭션으로 처리
- API 명세, 구현 기록, PR 초안, 전체 체크리스트 갱신
```
