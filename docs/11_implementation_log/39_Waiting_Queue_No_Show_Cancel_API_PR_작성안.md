# Waiting Queue No Show Cancel API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/hold-to-no-show-api` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 대기표 최종 미응답 처리 API, 방문/대기 취소 API, Swagger seed와 문서 수정 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | `mvn.cmd -q -DskipTests compile` |
| 테스트 확인 | `mvn.cmd -q test-compile` |
| 정적 확인 | `git diff --check` 성공 |
| 실행/API 확인 | Swagger 대표 순서 작성 |

## PR 제목

```text
feat: 대기표 최종 미응답 및 취소 API 구현
```

## PR 본문

```markdown
## 개요

호출 후 보류된 대기자를 최종 미응답(`NO_SHOW`)으로 종료하고, 접수 이후 대기 또는 방문 취소를 전용 API로 처리할 수 있도록 Queue API를 확장합니다.

## 변경 내용

- `POST /api/queues/{id}/no-show` 최종 미응답 처리 API 추가
- `POST /api/queues/{id}/cancel` 방문/대기 취소 API 추가
- `HOLD -> NO_SHOW`, `WAITING/CALLED/HOLD -> CANCELED` 상태 전이 정책 추가
- QueueTicket 상태 변경 시 Visit 상태를 함께 갱신
- 예약 기반 방문의 `CHECKED_IN` 예약 상태를 `NO_SHOW` 또는 `CANCELED`로 동기화
- Swagger 취소 확인용 `Swagger대기취소` seed 추가
- API 명세와 브랜치 구현 기록 갱신

## 검증

- [ ] Maven compile
- [ ] Maven test-compile
- [x] git diff --check
- [x] Swagger 대표 예시에 필요한 seed/mock 데이터 확인 및 추가
- [x] Swagger 대표 확인 순서 작성
- [ ] Swagger에서 `GET /api/queues?status=WAITING` 조회 확인
- [ ] Swagger에서 `POST /api/queues/{id}/call` 호출 확인
- [ ] Swagger에서 `POST /api/queues/{id}/hold` 보류 확인
- [ ] Swagger에서 `POST /api/queues/{id}/no-show` 최종 미응답 확인
- [ ] Swagger에서 `POST /api/queues/{id}/cancel` 방문/대기 취소 확인

## Swagger 대표 테스트 순서

1. `POST /api/auth/login`
   - `staff@test.com / password1234`
2. Swagger Authorize 창에 accessToken 값만 입력
3. `GET /api/queues?status=WAITING`
   - `visitorName = Swagger대기열`의 `queueTicketId` 확인
4. `POST /api/queues/{queueTicketId}/call`
   - 기대: `status = CALLED`
5. `POST /api/queues/{queueTicketId}/hold`
   - 기대: `status = HOLD`
6. `POST /api/queues/{queueTicketId}/no-show`
   - 기대: `status = NO_SHOW`

## Swagger 추가 테스트 체크리스트

| 케이스 | 확인 방법 | 기대 결과 |
|---|---|---|
| 최종 미응답 정상 | `HOLD` 대기표로 no-show | `status = NO_SHOW` |
| 최종 미응답 잘못된 순서 | `WAITING` 대기표로 no-show | HTTP 409, `error.code = QUEUE_INVALID_STATUS` |
| 취소 정상 | `Swagger대기취소` 대기표로 cancel | `status = CANCELED` |
| 호출 후 취소 | `CALLED` 대기표로 cancel | `status = CANCELED` |
| 보류 후 취소 | `HOLD` 대기표로 cancel | `status = CANCELED` |
| 처리 시작 후 취소 불가 | `IN_PROGRESS` 대기표로 cancel | HTTP 409, `error.code = QUEUE_INVALID_STATUS` |
| 없는 대기표 | 존재하지 않는 ID 사용 | HTTP 404, `error.code = QUEUE_TICKET_NOT_FOUND` |
| 권한 없는 사용자 | 시민 토큰으로 호출 | HTTP 403 또는 인증/권한 실패 |

## 후속 작업

- 대기번호 발급 동시성 정책 보강
- 대기열 화면과 Queue API 연결
- Dashboard/Congestion Context 구현 시 `NO_SHOW`, `CANCELED` 집계 정책 반영
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영
- [ ] 후속 브랜치 생성 또는 다음 작업 문서화

## 커밋 메시지 초안

```text
feat: 대기표 최종 미응답 및 취소 API 구현

- HOLD 대기표를 NO_SHOW로 종료하는 API 추가
- 방문/대기 취소 API와 상태 전이 정책 추가
- Swagger 취소 확인용 seed와 API 문서 갱신
```
