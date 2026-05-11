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
| 실행/API 확인 | 사용자가 직접 서버 기동 후 `POST /api/visits/check-in` 호출 필요 |

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
- 예약번호 조회 SQL 보강 및 예약 상태 `CHECKED_IN` 변경 Mapper 추가
- 체크인 이후 예약 취소 불가 정책 문서화

## 검증

- [x] Maven compile
- [x] Maven test-compile
- [x] git diff --check
- [ ] API 런타임 확인
- [ ] Swagger 확인

## 미검증 사유

- 서버 기동, Docker 실행, API 런타임 호출, Swagger 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
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
