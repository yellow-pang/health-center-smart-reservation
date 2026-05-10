# ReservationSlot API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/office-context-add` |
| 권장 브랜치명 | `feat/reservation-slot-api` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | ReservationSlot 신규 파일과 DB/API/체크리스트 문서 변경 있음 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | `mvn -q -DskipTests compile` 성공 |
| 테스트 확인 | `mvn -q test-compile` 성공 |
| 정적 확인 | `git diff --check` 성공 |
| 실행/API 확인 | 서버 기동과 API 런타임 호출은 사용자 직접 확인 필요 |

## PR 제목

```text
feat: 예약 슬롯 조회 및 생성 API 구현
```

## PR 본문

```markdown
## 개요

사용자 예약 신청 전에 필요한 날짜별 예약 가능 시간 조회 API와 관리자 예약 슬롯 생성 API를 추가합니다.

## 변경 내용

- `GET /api/reservation-slots` 예약 가능 시간 조회 API 추가
- `POST /api/admin/reservation-slots` 관리자 예약 슬롯 생성 API 추가
- ReservationSlot Controller, Service, Mapper, DTO/VO 추가
- `reservation_slots` schema와 14일치 seed 추가
- API 명세서, 전체 체크리스트, 브랜치 구현 기록 갱신

## 검증

- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [x] `git diff --check`
- [ ] 서버 기동 후 `GET /api/reservation-slots` 확인
- [ ] 관리자 토큰으로 `POST /api/admin/reservation-slots` 확인
- [ ] Swagger UI에서 ReservationSlot API 노출 확인

## 미검증 사유

- 프로젝트 운영 기준에 따라 서버 기동, API 런타임 호출, Swagger/브라우저 확인은 에이전트가 직접 실행하지 않고 사용자가 직접 확인합니다.
- GitNexus MCP/CLI를 사용할 수 없어 전용 impact와 detect_changes는 수행하지 못했고, `rg`와 Maven 검증으로 대체했습니다.

## 후속 작업

- 예약 신청 API 구현
- 예약 슬롯 정원 증가 조건부 update로 정원 초과 방지
- 동일 사용자 동일 시간대 중복 예약 방지
- 내 예약 조회와 예약 취소 구현
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
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/reservation-slots?serviceTypeId=1&date=$date" -Headers @{Authorization="Bearer $token"}
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영
- [ ] 후속 브랜치 생성 또는 다음 작업 문서화
