# 관리자 대기 마감 UX 구현 기록

## 1. 작업 목표

- 관리자가 영업일 마감 전에 미처리 대기표 목록을 확인할 수 있게 한다.
- 확인한 대기표를 `NO_SHOW`로 일괄 처리하는 API 실행 버튼을 관리자 화면에 연결한다.
- 마감 전 확인 UX와 실제 마감 API가 같은 날짜 기준을 사용하도록 대기열 조회 API에 날짜 필터를 보강한다.

## 2. 작업 범위

- [x] `GET /api/queues` 선택 날짜 조회 옵션 추가
- [x] 관리자 대기 마감 관리 화면 추가
- [x] 관리자 사이드바 메뉴 추가
- [x] 마감 대상 요약 카드와 대상 목록 표시
- [x] 확인 다이얼로그 후 `POST /api/queues/admin/close-pending` 실행
- [x] 문서와 전체 체크리스트 갱신

제외한다.

- [ ] 스케줄러 기반 자동 마감 배치
- [ ] 운영 시간별 자동 마감 정책
- [ ] 관리자 로그인 중 마감 시간 도달 알림
- [ ] 2일 경과 미처리 대기표 자동 `NO_SHOW` 처리

## 3. 구현 결정

| 항목 | 결정 |
|---|---|
| 화면 경로 | `/admin/queue-closing` |
| 미리보기 기준 | 선택 날짜의 `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS` 대기표 |
| 실행 API | `POST /api/queues/admin/close-pending?date=YYYY-MM-DD` |
| 실행 UX | 목록 확인 후 확인 다이얼로그에서 최종 실행 |
| 실행 후 처리 | 성공 토스트 표시 후 선택 날짜 목록 재조회 |

## 4. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `backend/src/main/java/egovframework/healthcenter/queue/api/QueueController.java` | `GET /api/queues`에 `date` query 추가 |
| `backend/src/main/java/egovframework/healthcenter/queue/application/QueueQueryService.java` | 날짜 기본값과 Mapper 전달 추가 |
| `backend/src/main/java/egovframework/healthcenter/queue/mapper/QueueTicketMapper.java` | 대기열 조회 파라미터에 `targetDate` 추가 |
| `backend/src/main/resources/egovframework/mapper/healthcenter/queue/QueueTicket_SQL_postgresql.xml` | `CURRENT_DATE` 고정 조회를 `targetDate` 기준으로 변경 |
| `frontend/src/lib/staff-api.ts` | 대기열 날짜 조회와 마감 API client 추가 |
| `frontend/app/admin/queue-closing/page.tsx` | 관리자 대기 마감 관리 화면 추가 |
| `frontend/src/components/layout/app-sidebar.tsx` | 관리자 사이드바에 대기 마감 관리 메뉴 추가 |
| `docs/04_api/01_API_명세서.md` | `GET /api/queues` 날짜 필터 명세 보강 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | Queue/Frontend 진행 상태 갱신 |
| `docs/14_deferred_cleanup/01_보류_정리_목록.md` | DC-026 상태 갱신 |

## 4.1 후속 고도화 메모

| 항목 | 권장 방향 | 분리 이유 |
|---|---|---|
| 관리자 마감 알림 | 관리자가 로그인 중이고 운영 마감 시간이 되었을 때 토스트 또는 상단 배너로 미처리 대기표 존재를 알린다. 1차는 프론트 주기 조회, 이후 알림 읽음 처리나 SSE/WebSocket을 검토한다. | 마감 시간 설정, 알림 반복/무시 정책, 읽음 상태 저장 기준이 필요하다. |
| 2일 경과 자동 `NO_SHOW` | `issued_at::date <= CURRENT_DATE - 2`인 `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS` 대기표를 자동 `NO_SHOW` 처리한다. 수동 마감 API와 같은 Visit/Reservation 동기화 규칙을 사용한다. | 자동 상태 변경은 감사 로그, 재실행 안전성, 휴무일/예외일 기준이 필요하다. |

## 5. 런타임 확인 안내

브라우저에서 관리자 계정으로 로그인한 뒤 아래 화면을 확인한다.

```text
/admin/queue-closing
```

확인 순서:

- 날짜 선택 후 마감 대상 목록이 표시되는지 확인한다.
- `마감 처리` 버튼을 눌러 확인 다이얼로그가 표시되는지 확인한다.
- 마감 실행 후 성공 토스트와 목록 재조회 결과를 확인한다.

## 6. 검증 체크리스트

- [x] `git diff --check`
- [x] `npm.cmd run build`
- [ ] `npm.cmd exec -- tsc --noEmit` - `.next/dev/types/validator.ts`가 제거된 과거 `/app/social-login/page.js`를 참조해 실패
- [ ] `mvn.cmd -q -DskipTests compile` - 현재 세션 PATH에서 `mvn.cmd` 실행 파일을 찾지 못해 미수행
- [ ] `mvn.cmd -q test-compile` - 현재 세션 PATH에서 `mvn.cmd` 실행 파일을 찾지 못해 미수행
- [ ] 브라우저 `/admin/queue-closing` 확인
- [ ] Swagger `GET /api/queues?date=2026-05-22`
- [ ] Swagger `POST /api/queues/admin/close-pending?date=2026-05-22`

## 7. 추가 테스트 체크리스트

- [ ] Happy: 마감 대상이 있으면 요약 카드와 목록에 같은 건수가 표시된다.
- [ ] Happy: 마감 실행 후 대상 대기표가 `NO_SHOW`로 변경되고 목록에서 사라진다.
- [ ] Edge: 마감 대상이 없으면 실행 버튼이 비활성화된다.
- [ ] Edge: 날짜를 바꾸면 해당 날짜 기준으로 목록이 재조회된다.
- [ ] Bad: API 실패 시 오류 토스트가 표시되고 목록이 임의 변경되지 않는다.

## 8. 브랜치 종료 전 체크리스트

- [x] 관리자 마감 UX 구현 완료
- [x] 문서 갱신
- [x] PR 문서 작성
- [x] 정적 검증과 빌드 확인
- [x] 브라우저/API 런타임 확인 안내
- [x] 커밋 메시지 정리
