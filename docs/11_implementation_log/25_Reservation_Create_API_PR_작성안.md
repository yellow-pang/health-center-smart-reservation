# Reservation Create API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/reservation-create-api` |
| base 브랜치 | `main` 추정 |
| PR 범위 | 예약 신청 API |
| 빌드 확인 | `mvn -q -DskipTests compile` 성공 |
| 테스트 확인 | `mvn -q test-compile` 성공 |
| 정적 확인 | `git diff --check` 성공 |
| 실행/API 확인 | 서버 기동과 API 런타임 호출은 사용자 직접 확인 필요 |

## PR 제목

```text
feat: 예약 신청 API 구현
```

## PR 본문

```markdown
## 개요

로그인 사용자가 예약 슬롯을 선택해 예약을 신청할 수 있도록 `POST /api/reservations` API를 추가합니다.

## 변경 내용

- 예약 신청 Controller, CommandService, Mapper, DTO/VO 추가
- `reservations` 테이블과 조회/삽입 Mapper XML 추가
- 예약 슬롯 `reserved_count < capacity` 조건부 증가 SQL 추가
- 동일 사용자 동일 슬롯 중복 예약 방지 unique index 추가
- 예약번호 생성 및 예약 완료 응답 추가
- ERD, 전체 체크리스트, 브랜치 구현 기록 갱신

## 검증

- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [x] `git diff --check`
- [ ] 서버 기동 후 `POST /api/reservations` 확인
- [ ] 같은 사용자/같은 슬롯 중복 예약 실패 확인
- [ ] 정원 초과 시 `RESERVATION_SLOT_FULL` 확인
- [ ] Swagger UI에서 Reservation API 노출 확인

## 미검증 사유

- 프로젝트 운영 기준에 따라 서버 기동, API 런타임 호출, Swagger/브라우저 확인은 에이전트가 직접 실행하지 않고 사용자가 직접 확인합니다.
- GitNexus MCP/CLI를 사용할 수 없어 전용 impact/detect_changes는 수행하지 못했고, `rg`와 Maven 검증으로 대체했습니다.

## 후속 작업

- 내 예약 조회 API 구현
- 예약 상세 조회 API 구현
- 예약 취소 API 구현
- 취소 시 예약 슬롯 `reserved_count` 복구
- 예약 취소 가능 시간 정책 검증
```

## 사용자 직접 확인 명령

```powershell
cd backend
mvn spring-boot:run
```

```powershell
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"citizen@test.com","password":"password1234"}'
$token = $login.data.accessToken
$date = Get-Date -Format 'yyyy-MM-dd'
$slots = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/reservation-slots?serviceTypeId=1&date=$date" -Headers @{Authorization="Bearer $token"}
$slotId = $slots.data[0].slotId
$body = @{serviceTypeId=1; reservationSlotId=$slotId; visitorName='홍길동'; visitorPhone='010-1234-5678'} | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/reservations' -ContentType 'application/json' -Headers @{Authorization="Bearer $token"} -Body $body
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영
- [ ] 후속 브랜치 생성 또는 다음 작업 문서화

## 커밋 메시지 초안

```text
feat: 예약 신청 API 구현

- 예약 신청 Controller, Service, Mapper 추가
- reservations 테이블과 MyBatis XML 추가
- 예약 슬롯 정원 조건부 증가 로직 추가
- 동일 사용자 중복 예약 방지 인덱스 추가
- 구현 기록, PR 초안, 전체 체크리스트 갱신
```
