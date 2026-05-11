# Visit Walk-In API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/visits-walk-in` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 현장 접수 API, Visit/QueueTicket 생성, 관련 문서 수정 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | `mvn.cmd -q -DskipTests compile` 성공 |
| 테스트 확인 | `mvn.cmd -q test-compile` 성공 |
| 정적 확인 | `git diff --check` 성공, GitNexus detect-changes는 unknown command로 실패 |
| 실행/API 확인 | 사용자가 Docker PostgreSQL 실행 후 VS Code Spring Boot Dashboard로 서버를 기동하고 Swagger에서 대표 예시로 확인 필요 |

## PR 제목

```text
feat: 현장 접수 및 대기번호 발급 구현
```

## PR 본문

```markdown
## 개요

예약 없이 방문한 시민을 직원 또는 관리자가 현장에서 접수할 수 있도록 `POST /api/visits/walk-in` API를 구현합니다. 현장 접수 성공 시 Visit을 `WALK_IN` 유형으로 생성하고, QueueTicket을 `WAITING` 상태로 발급합니다.

## 변경 내용

- `POST /api/visits/walk-in` API 추가
- 현장 접수 요청/응답 DTO 추가
- 현장 접수 권한과 업무 유형 정책 추가
- Visit CommandService에 현장 접수 트랜잭션 추가
- Visit Mapper에 `WALK_IN` 방문 insert 추가
- 기존 QueueTicket 발급 흐름 재사용
- API 명세, 브랜치 구현 기록, 전체 체크리스트 갱신

## 검증

- [x] Maven compile
- [x] Maven test-compile
- [x] git diff --check
- [x] Swagger 대표 예시 작성
- [x] Swagger 대표 예시에 필요한 seed/mock 데이터 확인
- [ ] Docker PostgreSQL 실행 확인
- [ ] VS Code Spring Boot Dashboard 서버 기동 확인
- [ ] Swagger에서 `POST /api/auth/login` 직원 계정 로그인 확인
- [ ] Swagger에서 `POST /api/visits/walk-in` 대표 예시 정상 응답 확인
- [ ] Swagger에서 존재하지 않는 업무 유형 ID 실패 확인
- [ ] Swagger에서 권한 없는 사용자 실패 확인

## Swagger 대표 예시

`POST /api/visits/walk-in`

```json
{
  "serviceTypeId": 1,
  "visitorName": "Swagger현장접수",
  "visitorPhone": "010-4567-8901"
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

- `serviceTypeId = 1`은 기존 업무 유형 seed를 전제로 한 대표 예시입니다.
- 실제 DB에서 ID가 다르면 `GET /api/service-types`에서 확인한 업무 유형 ID로 바꿔 실행합니다.

## Swagger 추가 테스트 체크리스트

| 케이스 | 확인 방법 | 기대 결과 |
|---|---|---|
| 현장 접수 정상 | 직원 토큰으로 대표 예시 실행 | HTTP 201, `status = WAITING` |
| 업무 유형 미선택 | `serviceTypeId`를 비움 | HTTP 400, `error.code = VISIT_INVALID_REQUEST` |
| 없는 업무 유형 | 존재하지 않는 `serviceTypeId` 입력 | HTTP 404, `error.code = SERVICE_TYPE_NOT_FOUND` |
| 권한 없는 사용자 | 시민 토큰으로 호출 | HTTP 403 또는 인증/권한 실패 |
| 인증 없음 | Authorization 없이 호출 | HTTP 401 |

## 미검증 사유

- 서버 기동은 사용자가 VS Code Spring Boot Dashboard에서 직접 수행합니다.
- Docker PostgreSQL 실행과 API 런타임 검증은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- API 런타임 검증은 터미널 호출보다 Swagger UI를 우선 사용합니다.
- GitNexus index는 stale 상태이며 신규 Visit 심볼 impact는 대상 심볼을 찾지 못해 `rg`와 Maven 검증으로 보완했습니다.
- GitNexus detect-changes는 현재 CLI에서 `unknown command 'detect-changes'`로 실패했습니다.

## 후속 작업

- 대기열 조회/호출/처리 시작/완료 API 구현
- 대기번호 발급 동시성 정책 보강
- 방문/대기 취소 전용 정책 정리
- 후속 API 구현 시 Swagger 대표 예시와 seed/mock 데이터 추가 여부를 PR 체크리스트에서 계속 확인
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영
- [ ] 후속 브랜치 생성 또는 다음 작업 문서화

## 커밋 메시지 초안

```text
feat: 현장 접수 및 대기번호 발급 구현

- 예약 없는 현장 접수 API 추가
- 현장 접수 시 Visit과 QueueTicket 생성
- 업무 유형과 직원/관리자 보건소 권한 검증 추가
- Swagger 대표 예시와 PR 테스트 체크리스트 문서화
```
