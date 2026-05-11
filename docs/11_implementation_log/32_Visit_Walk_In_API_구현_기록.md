# Visit Walk-In API 구현 기록

## 1. 작업 목표

- 직원 또는 관리자가 예약 없이 방문자를 현장 접수할 수 있게 한다.
- 현장 접수 성공 시 Visit과 QueueTicket을 함께 생성한다.
- Swagger에서 대표 예시 1개로 빠르게 수동 검증할 수 있게 요청/응답 예시를 정리한다.

## 2. 작업 범위

- [x] 이번 브랜치에 포함: `POST /api/visits/walk-in`
- [x] 이번 브랜치에 포함: 현장 방문 `visits` 생성
- [x] 이번 브랜치에 포함: 대기번호 `queue_tickets` 생성
- [x] 이번 브랜치에 포함: 업무 유형과 직원/관리자 보건소 권한 확인
- [x] 이번 브랜치에서 제외: 대기열 조회, 호출, 처리 시작, 완료 API
- [x] 이번 브랜치에서 제외: 대기번호 발급 동시성 정책 보강
- [x] 이번 브랜치에서 제외: 방문/대기 취소 전용 API

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 관련 항목 확인
- [x] `docs/04_api/01_API_명세서.md` 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] 영향받는 파일 확인

## 4. 영향 분석

GitNexus 확인:

| 대상 | 결과 | 보완 확인 |
|---|---|---|
| `gitnexus status` | stale index 확인 | `rg`와 Maven 검증 병행 |
| `gitnexus impact VisitCommandService --repo health-center-smart-reservation` | 대상 심볼을 찾지 못함 | 신규 Visit 심볼이 stale index에 없음 |
| `gitnexus impact VisitController --repo health-center-smart-reservation` | 대상 심볼을 찾지 못함 | `rg` 직접 참조 확인 |
| `gitnexus impact QueueTicketMapper --repo health-center-smart-reservation` | 대상 심볼을 찾지 못함 | `rg` 직접 참조 확인 |
| `gitnexus impact VisitMapper --repo health-center-smart-reservation` | 대상 심볼을 찾지 못함 | `rg` 직접 참조 확인 |

`rg` 기준 blast radius:

| 대상 | 직접 영향 | 위험도 |
|---|---|---|
| `VisitController` | `/api/visits/check-in`, 신규 `/api/visits/walk-in` | MEDIUM |
| `VisitCommandService` | 체크인 흐름과 신규 현장 접수 흐름 | MEDIUM |
| `VisitMapper`, `Visit_SQL_postgresql.xml` | 예약 방문 insert와 신규 현장 방문 insert | LOW |
| `QueueTicketMapper` | 체크인과 현장 접수의 대기번호 발급 공유 | MEDIUM |

## 5. 구현 체크리스트

- [x] Controller 수정
- [x] CommandService 수정
- [x] Policy 추가
- [x] Mapper 수정
- [x] Mapper XML 수정
- [x] DTO 추가
- [x] 예외 처리 확인
- [x] 공통 응답 형식 확인
- [x] Swagger 대표 예시 작성
- [x] seed/mock 데이터 필요 여부 확인

## 6. 검증 체크리스트

- [x] Maven compile 확인
- [x] Maven test-compile 확인
- [x] `git diff --check` 확인
- [x] GitNexus detect-changes 시도 및 실패 사유 기록
- [x] Swagger URL과 인증 방법 작성
- [x] Swagger 대표 예시 1개와 기대 결과 작성
- [x] PR 문서에 추가 테스트 체크리스트 작성
- [ ] 사용자가 Docker PostgreSQL 실행 확인
- [ ] 사용자가 VS Code Spring Boot Dashboard로 서버 기동 확인
- [ ] 사용자가 Swagger에서 대표 예시로 API 런타임 호출 확인

## 7. 구현 내용

### 7.1 API

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/visits/walk-in` | 현장 접수 및 대기번호 발급 | 같은 보건소 `STAFF`, `ADMIN` |

### 7.2 정책

- `STAFF`, `ADMIN`만 현장 접수할 수 있다.
- 접수 담당자는 보건소 소속 정보가 있어야 한다.
- 요청한 업무 유형은 활성 상태이며 담당자와 같은 보건소에 속해야 한다.
- 현장 접수는 `visit_type = WALK_IN`, `status = WAITING`으로 Visit을 생성한다.
- 접수 직후 QueueTicket을 `WAITING` 상태로 발급한다.
- Swagger 대표 예시는 기존 업무 유형 seed를 사용하므로 별도 `data.sql` 추가는 하지 않았다.

## 8. 사용자 직접 런타임 확인 방법

이번 브랜치의 런타임 검증은 사용자가 Docker PostgreSQL, VS Code Spring Boot Dashboard, Swagger UI로 확인한다. API 테스트는 터미널 명령보다 Swagger `Try it out`을 우선 사용한다.

### 8.1 Swagger 인증

1. Swagger에서 `POST /api/auth/login`을 연다.
2. `Try it out`을 누른다.
3. 아래 대표 예시로 실행한다.

```json
{
  "email": "staff@test.com",
  "password": "password1234"
}
```

기대 결과:

- `success`가 `true`
- `data.accessToken`이 발급된다.
- Swagger Authorization에 `Bearer {accessToken}` 형식으로 입력한다.

### 8.2 Swagger 현장 접수

`POST /api/visits/walk-in`

```json
{
  "serviceTypeId": 1,
  "visitorName": "Swagger현장접수",
  "visitorPhone": "010-4567-8901"
}
```

기대 결과:

- HTTP 201
- `success`가 `true`
- `data.visitId`가 생성됨
- `data.queueTicketId`가 생성됨
- `data.ticketNumber`가 1 이상의 숫자
- `data.status`가 `WAITING`

참고:

- `serviceTypeId = 1`은 기존 업무 유형 seed를 전제로 한 대표 예시다.
- 실제 DB에서 ID가 다르면 `GET /api/service-types`에서 확인한 업무 유형 ID로 바꿔 실행한다.

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 범위 정리 | 대기열 조회/호출/처리 시작/완료 API | 현장 접수 이후 직원이 대기열을 처리할 수 있어야 전체 흐름이 닫힘 | 후속 작업 |
| 범위 정리 | 대기번호 발급 동시성 정책 보강 | 현재는 업무 유형별 당일 최대 번호 + 1 방식이라 동시 접수 경쟁 조건 검증 필요 | 후속 작업 |
| 범위 정리 | 방문/대기 취소 전용 정책 정리 | 예약 취소와 별개로 접수 이후 취소 정책이 필요함 | 후속 작업 |
| 검증 중 | GitNexus detect-changes 확인 | CLI에서 `unknown command 'detect-changes'`로 실패 | `git status`, `git diff --stat`, Maven 검증으로 보완 |

## 10. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
