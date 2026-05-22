# 미처리 대기표 마감 API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/close-pending-tickets` |
| base 브랜치 | `dev` |
| 작업 트리 | 미처리 대기표 마감 API 및 문서 수정 |
| 주요 커밋 | 커밋 전 |
| 정적 확인 | 진행 예정 |
| 테스트 확인 | 진행 예정 |
| 실행/API 확인 | 서버 기동, Docker, Swagger 런타임 호출은 사용자 직접 수행 |

## PR 제목

```text
feat: 미처리 대기표 마감 API 추가
```

## PR 본문

## 개요

영업일 마감 시 당일 미처리 대기표를 `NO_SHOW`로 일괄 처리할 수 있는 관리자 API를 추가합니다.

시민 혼잡도에는 `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS` 대기표가 포함되므로, 운영자가 마감 처리하지 않은 대기표가 다음 운영 데이터에 남지 않도록 정리 경로를 제공합니다.

## 변경 내용

- 관리자용 미처리 대기표 마감 API 추가
  - `POST /api/queues/admin/close-pending?date=YYYY-MM-DD`
  - `date` 생략 시 오늘 기준
  - `ADMIN`만 실행 가능
- 상태 동기화
  - 대상 QueueTicket: `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS` -> `NO_SHOW`
  - 연결 Visit -> `NO_SHOW`
  - 예약 방문이고 예약 상태가 `CHECKED_IN`이면 Reservation -> `NO_SHOW`
- 문서 갱신
  - API 명세 추가
  - 전체 체크리스트 갱신
  - 보류 목록 DC-026 상태 갱신

## 검증

- [x] `git diff --check`
- [ ] `mvn.cmd -q -DskipTests compile`
- [ ] `mvn.cmd -q test-compile`
- [ ] Swagger `Try it out`: `POST /api/queues/admin/close-pending?date=2026-05-22`
- [ ] Swagger `Try it out`: `GET /api/queues?status=NO_SHOW`로 결과 확인

## 추가 테스트 체크리스트

- [ ] Happy: 당일 `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS` 대기표가 `NO_SHOW` 처리된다.
- [ ] Happy: 예약 기반 방문의 `CHECKED_IN` 예약도 `NO_SHOW` 처리된다.
- [ ] Edge: `date`를 생략하면 오늘 기준으로 처리된다.
- [ ] Edge: 마감 대상이 없으면 `closedCount: 0`으로 성공 응답한다.
- [ ] Bad: `STAFF` 또는 `CITIZEN` 권한으로 호출하면 403 응답이 반환된다.
- [ ] Bad: 이미 `COMPLETED`, `NO_SHOW`, `CANCELED` 상태인 대기표는 변경되지 않는다.

## 미검증 사유

- 서버 기동, Docker 실행, Swagger 런타임 호출은 프로젝트 운영 기준상 사용자가 직접 수행한다.
- Maven은 현재 세션 PATH에서 `mvn.cmd`/`mvn` 실행 파일을 찾지 못해 미수행했다.

## 후속 작업

- 관리자 화면에서 마감 전 미처리 대기표 확인 UX 추가
- 관리자 화면에서 마감 API 실행 버튼 연동
- 운영 시간 확정 후 스케줄러 자동 마감 배치 검토

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 관리자 마감 UX 커밋 진행

## 커밋 메시지 초안

제목:

```text
feat: 미처리 대기표 마감 API 추가
```

본문:

```text
- 관리자용 미처리 대기표 NO_SHOW 일괄 처리 API 추가
- 대기표, 방문, 예약 상태 동기화 SQL 추가
- API 명세, 전체 체크리스트, 보류 목록 갱신
```
