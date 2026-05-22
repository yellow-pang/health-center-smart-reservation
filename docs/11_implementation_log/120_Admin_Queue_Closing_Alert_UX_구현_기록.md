# 관리자 대기 마감 알림 UX 구현 기록

## 1. 작업 목표

- 관리자가 로그인 중인 상태에서 마감 시간 이후 미처리 대기표가 남아 있으면 화면 상단에서 바로 인지할 수 있게 한다.
- 기존 관리자 대기 마감 관리 화면으로 빠르게 이동할 수 있는 배너와 1회 토스트 알림을 제공한다.
- 백엔드 API는 새로 만들지 않고 기존 `GET /api/queues?date=YYYY-MM-DD`를 재사용한다.

## 2. 작업 범위

- [x] 관리자 레이아웃에서만 동작하는 마감 알림 컴포넌트 추가
- [x] 17:30 이후 오늘 미처리 대기표 조회
- [x] `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS` 상태만 알림 대상으로 집계
- [x] 5분 주기 재조회
- [x] 같은 날짜 알림 세션 단위 숨김 처리
- [x] 대기 마감 관리 화면 이동 버튼 추가
- [x] 문서와 체크리스트 갱신

제외한다.

- [ ] 백엔드 운영시간 설정 API
- [ ] 보건소별 운영시간/휴무일 기준
- [ ] 알림 읽음 상태 서버 저장
- [ ] WebSocket/SSE 실시간 알림
- [ ] 자동 `NO_SHOW` 배치

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 확인
- [x] `docs/14_deferred_cleanup/01_보류_정리_목록.md`의 `DC-027` 확인
- [x] 현재 브랜치 `feat/admin-queue-closing-alert` 확인
- [x] 영향받는 파일 확인

## 4. 영향도 확인

GitNexus 인덱스가 stale 상태라 `gitnexus analyze`를 먼저 실행했지만, CLI가 현재 폴더를 Git 저장소로 인식하지 못해 실패했다.

```text
GitNexus Analyzer
Not inside a git repository.
```

보완으로 `git rev-parse --show-toplevel`, `rg`, 파일 직접 확인을 수행했고, 가능한 범위에서 GitNexus impact를 실행했다.

| 대상 | 결과 | 판단 |
|---|---|---|
| `AppLayout` | impactedCount 0, risk LOW | 관리자 전용 배너 삽입 지점으로 사용 |
| `AppSidebar` | impactedCount 0, risk LOW | 이번 브랜치에서는 수정하지 않음 |
| `getQueueEntries` | direct 2, processes 2, risk HIGH | 기존 직원 대기열/관리자 마감 화면 영향이 커서 함수 시그니처는 수정하지 않고 호출만 재사용 |

## 5. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `frontend/src/components/layout/admin-queue-closing-alert.tsx` | 관리자 마감 알림 배너, 1회 토스트, 5분 주기 조회, 세션 숨김 처리 추가 |
| `frontend/src/components/layout/app-layout.tsx` | `ADMIN` 사용자에게만 마감 알림 컴포넌트 표시 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 이번 브랜치 후보와 완료 상태 반영 |
| `docs/14_deferred_cleanup/01_보류_정리_목록.md` | `DC-027` 상태를 정리 완료로 갱신 |

## 6. 동작 기준

| 항목 | 기준 |
|---|---|
| 알림 시작 시간 | 매일 17:30 이후 |
| 조회 API | `GET /api/queues?date=오늘` |
| 대상 상태 | `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS` |
| 반복 주기 | 5분 |
| 숨김 기준 | `sessionStorage`에 날짜별 숨김 값 저장 |
| 이동 경로 | `/admin/queue-closing` |

## 7. 검증 체크리스트

- [x] `git diff --check`
- [x] `npm.cmd exec -- tsc --noEmit`
- [x] `npm.cmd run build`
- [ ] 브라우저에서 관리자 로그인 후 17:30 이후 배너 표시 확인
- [ ] 브라우저에서 `마감 관리` 버튼 클릭 시 `/admin/queue-closing` 이동 확인
- [ ] 브라우저에서 오늘 알림 숨김 버튼 클릭 후 같은 세션에서 배너 미표시 확인
- [ ] Swagger `GET /api/queues?date=YYYY-MM-DD` 런타임 확인

## 8. 사용자 확인 방법

관리자 계정으로 로그인한 뒤 17:30 이후 아래 흐름을 확인한다.

```text
1. 오늘 날짜에 WAITING/CALLED/HOLD/IN_PROGRESS 대기표를 준비한다.
2. 관리자 화면 아무 곳에 접속한다.
3. 상단 알림 배너와 토스트가 표시되는지 확인한다.
4. 마감 관리 버튼으로 /admin/queue-closing 이동을 확인한다.
5. 숨김 버튼을 누른 뒤 같은 세션에서 배너가 다시 뜨지 않는지 확인한다.
```

Swagger 대표 확인:

```text
GET /api/queues?date=2026-05-22
Authorization: Bearer {ADMIN_ACCESS_TOKEN}
```

기대 결과:

```json
{
  "success": true,
  "data": [
    {
      "queueTicketId": 1,
      "status": "WAITING",
      "ticketNumber": 1
    }
  ],
  "error": null
}
```

## 9. 추가 테스트 체크리스트

- [ ] Happy: 마감 시간 이후 미처리 대기표가 있으면 관리자 화면 상단에 알림이 표시된다.
- [ ] Happy: 알림의 마감 관리 버튼을 누르면 `/admin/queue-closing`으로 이동한다.
- [ ] Edge: 미처리 대기표가 없으면 알림이 표시되지 않는다.
- [ ] Edge: `COMPLETED`, `NO_SHOW`, `CANCELED`만 있으면 알림이 표시되지 않는다.
- [ ] Edge: 숨김 처리 후 같은 날짜, 같은 세션에서는 알림이 다시 표시되지 않는다.
- [ ] Bad: 대기열 조회 API 실패 시 화면 렌더링은 유지되고 알림만 표시되지 않는다.

## 10. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 구현 중 | 알림 기준 시간 서버 설정화 | 현재는 17:30 프론트 상수라 보건소별 운영시간 반영이 어렵다. | 후속 고도화 후보로 유지 |
| 구현 중 | 알림 읽음 상태 서버 저장 | 세션 숨김만으로는 기기/브라우저 간 공유되지 않는다. | 후속 고도화 후보로 유지 |

## 11. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] 보류 정리 목록 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
