# Reservation Check-In API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/reservation-check-in-api` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 예약 체크인 API, Visit/QueueTicket 생성, 관련 문서 수정 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | `mvn.cmd -q -DskipTests compile` 성공 |
| 테스트 확인 | `mvn.cmd -q test-compile` 성공 |
| 정적 확인 | `git diff --check` 성공, GitNexus detect-changes는 상세 출력 없이 실패 |
| 실행/API 확인 | 사용자가 Docker PostgreSQL 실행 후 VS Code Spring Boot Dashboard로 서버를 기동하고 Swagger에서 대표 예시로 확인 필요 |

## PR 제목

```text
feat: 예약자 체크인 및 대기번호 발급 구현
```

## PR 본문

```markdown
## 개요

예약 취소 이후 후속 흐름으로 예약자 체크인 API를 구현합니다. 체크인 성공 시 예약 상태를 CHECKED_IN으로 변경하고, 방문 이력과 대기번호를 하나의 트랜잭션에서 생성합니다.

## 변경 내용

- `POST /api/visits/check-in` API 추가
- Visit Context의 Controller, Service, Policy, Mapper 추가
- QueueTicket 발급 Mapper 추가
- PostgreSQL schema에 `visits`, `queue_tickets` 테이블 추가
- Swagger 체크인/취소/상세 스모크 테스트용 seed 예약 추가
- 예약번호 조회 SQL 보강 및 예약 상태 `CHECKED_IN` 변경 Mapper 추가
- 체크인 이후 예약 취소 불가 정책 문서화

## 검증

- [x] Maven compile
- [x] Maven test-compile
- [x] git diff --check
- [ ] Docker PostgreSQL 실행 확인
- [ ] VS Code Spring Boot Dashboard 서버 기동 확인
- [ ] Swagger 접속 확인
- [ ] Swagger에서 `POST /api/auth/login` 직원 계정 로그인 확인
- [ ] Swagger에서 `POST /api/visits/check-in` 대표 예시 정상 응답 확인
- [ ] Swagger에서 체크인 이후 예약 상태 `CHECKED_IN` 확인
- [ ] Swagger에서 체크인 이후 예약 취소 실패 확인
- [ ] Swagger에서 중복 체크인 실패 확인

## Swagger 대표 예시

`POST /api/visits/check-in`

```json
{
  "reservationNo": "RSV-SWAGGER-CHECKIN-001"
}
```

기대 응답:

- HTTP 201
- `success = true`
- `data.visitId` 존재
- `data.queueTicketId` 존재
- `data.ticketNumber`가 1 이상의 숫자
- `data.status = WAITING`

참고:

- `RSV-SWAGGER-CHECKIN-001`은 Swagger 체크인 스모크 테스트용 seed 예약이다.
- `RSV-SWAGGER-CANCEL-001`은 예약 취소 테스트용, `RSV-SWAGGER-DETAIL-001`은 예약 상세 테스트용 seed 예약이다.
- 체크인 성공 후 같은 예약번호로 다시 호출하면 중복 체크인 실패가 정상이다.
- 정상 체크인을 다시 확인하려면 백엔드를 재시작해 seed 예약을 `RESERVED` 상태로 초기화한다.

## Swagger 추가 테스트 체크리스트

| 케이스 | 확인 방법 | 기대 결과 |
|---|---|---|
| 예약 상세 상태 확인 | `RSV-SWAGGER-DETAIL-001`의 예약 ID로 `GET /api/reservations/{id}` | `success = true` |
| 체크인 이후 예약 취소 | 체크인된 `RSV-SWAGGER-CHECKIN-001`의 예약 ID로 `DELETE /api/reservations/{id}` | HTTP 409, `error.code = RESERVATION_CANCEL_INVALID_STATUS` |
| 일반 예약 취소 | `RSV-SWAGGER-CANCEL-001`의 예약 ID로 `DELETE /api/reservations/{id}` | `success = true`, `status = CANCELED` |
| 중복 체크인 | 같은 `reservationNo`로 `POST /api/visits/check-in` 재호출 | HTTP 409, `error.code = ALREADY_CHECKED_IN` |
| 없는 예약번호 체크인 | 존재하지 않는 `reservationNo`로 체크인 | HTTP 404, `error.code = RESERVATION_NOT_FOUND` |
| 다른 보건소 또는 권한 없는 사용자 | 권한 없는 토큰으로 체크인 | HTTP 403, `error.code = FORBIDDEN` |

## 미검증 사유

- 서버 기동은 `mvn spring-boot:run`이 아니라 사용자가 VS Code Spring Boot Dashboard에서 직접 수행합니다.
- Docker PostgreSQL 실행과 API 런타임 검증은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- API 런타임 검증은 터미널 호출보다 Swagger UI를 우선 사용합니다.
- GitNexus analyze는 `.gitnexus/lbug.shadow` 접근 거부로 실패했고, impact/context CLI도 상세 출력 없이 실패해 `rg`와 Maven 검증으로 보완했습니다.

## 후속 작업

- 현장 접수 `POST /api/visits/walk-in` 구현
- 대기열 조회/호출/처리 시작/완료 API 구현
- 대기번호 발급 동시성 정책 보강
- 방문/대기 취소 전용 정책 정리
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영
- [ ] 후속 브랜치 생성 또는 다음 작업 문서화

## 커밋 메시지 초안

```text
feat: 예약자 체크인 및 대기번호 발급 구현

- 예약번호 기반 체크인 API 추가
- 체크인 시 예약 상태를 CHECKED_IN으로 변경
- Visit과 QueueTicket을 하나의 트랜잭션에서 생성
- 체크인 이후 예약 취소 불가 정책 문서화
```
