# Queue List Query Optimization 구현 기록

## 1. 작업 목표

- k6 VM VUS 20 실행에서 `GET /api/queues` p95가 2.31초로 조회 기준 1초를 초과한 병목 후보를 1차 개선한다.
- 대기열 조회 응답 구조는 유지하면서 SQL 필터, 인덱스, 정렬, 응답 payload 제한을 작은 범위로 보강한다.
- 서버 기동, Docker 실행, Swagger/API 런타임, k6 재측정은 사용자가 직접 확인할 수 있도록 기준을 남긴다.

## 2. 작업 범위

- [x] 대기열 조회 API에 `limit` 선택 파라미터 추가
- [x] `limit` 기본값 200, 최대값 500으로 서버 방어 로직 추가
- [x] `q.issued_at::date = #{targetDate}` 조건을 하루 범위 조건으로 변경
- [x] 대기열 조회용 복합 인덱스 추가
- [x] k6 대기열 조회 요청에 `limit=100` 명시
- [x] API 명세와 k6 README 갱신
- [x] 전체 체크리스트와 보류 목록 갱신
- [x] PR 작성안 작성

제외한다.

- [ ] 응답을 페이지 객체로 바꾸는 breaking change
- [ ] `offset` 기반 페이지네이션
- [ ] 대기열 조회 전용 summary/count API
- [ ] VM 대상 k6 재실행
- [ ] Swagger/브라우저 런타임 확인

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 확인
- [x] `docs/14_deferred_cleanup/01_보류_정리_목록.md` 확인
- [x] `docs/04_api/01_API_명세서.md` 확인
- [x] `performance/k6/README.md` 확인
- [x] 현재 브랜치 확인: `perf/queue-list-query-optimization`
- [x] 영향받는 조회 경로 확인

## 4. 영향 범위 확인

GitNexus 상태:

- `npx gitnexus status`: stale
- `npx gitnexus analyze`: 현재 CLI가 "Not inside a git repository"로 실패
- `npx gitnexus analyze C:\Dev\health-center-smart-reservation`: large file skip 메시지 이후 비정상 종료
- `npx gitnexus impact -r health-center-smart-reservation findQueueTickets`
- `npx gitnexus impact -r health-center-smart-reservation selectQueueTickets`
- 위 impact/context 명령은 빈 출력과 exit code 1로 종료되어 신뢰 가능한 그래프 결과를 얻지 못했다.

후속 문서 정리:

- 이 프로젝트의 `.git`은 repository 루트(`C:\Dev\health-center-smart-reservation`)에만 있고 `backend`, `frontend` 하위 폴더에는 없다.
- GitNexus CLI는 하위 폴더가 아니라 루트에서 실행해야 한다.
- 관련 기준은 `START_HERE.md`, `README.md`, `AGENTS.md`, `CLAUDE.md`, `docs/09_agent/01_코드_에이전트_작업_가이드.md`, `docs/09_agent/03_Codex_GitNexus_UTF8_작업_주의사항.md`, `docs/13_schedule/02_전체_작업_체크리스트.md`에 반영했다.

`rg` 기준 직접 확인한 blast radius:

| 대상 | 직접 영향 |
|---|---|
| `QueueController.findQueueTickets` | `GET /api/queues` 요청 파라미터 처리 |
| `QueueQueryService.findQueueTickets` | 직원 권한 검증, 날짜/상태/limit 정규화, mapper 호출 |
| `QueueTicketMapper.selectQueueTickets` | MyBatis parameter map 구성 |
| `QueueTicket_SQL_postgresql.xml.selectQueueTickets` | 대기열 조회 SQL |
| `frontend/src/lib/staff-api.ts getQueueEntries` | 직원 대기열, 관리자 마감 화면, 관리자 마감 알림 호출 |
| `performance/k6/vaccination-queue-flow.js` | k6 `queue_list` 측정 요청 |

위험도 판단:

- 위험도: MEDIUM
- 이유: 직원/관리자 대기열 화면과 k6 측정 경로에 직접 영향이 있다.
- 완화: 응답 DTO를 바꾸지 않고 선택 파라미터만 추가했다. 기본 limit는 서버에서 보정하고, 기존 호출은 계속 동작한다.

## 5. 구현 내용

| 파일 | 내용 |
|---|---|
| `backend/src/main/java/egovframework/healthcenter/queue/api/QueueController.java` | `GET /api/queues`에 `limit` 요청 파라미터 추가 |
| `backend/src/main/java/egovframework/healthcenter/queue/application/QueueQueryService.java` | 기본 200건, 최대 500건 limit 정규화와 날짜 범위 파라미터 생성 |
| `backend/src/main/java/egovframework/healthcenter/queue/mapper/QueueTicketMapper.java` | `fromIssuedAt`, `toIssuedAt`, `limit` mapper 파라미터 전달 |
| `backend/src/main/resources/egovframework/mapper/healthcenter/queue/QueueTicket_SQL_postgresql.xml` | `issued_at::date` 조건 제거, 날짜 범위 조건과 `LIMIT` 적용 |
| `backend/src/main/resources/db/postgresql/schema.sql` | `idx_queue_tickets_list_lookup` 복합 인덱스 추가 |
| `frontend/src/lib/staff-api.ts` | `getQueueEntries` filter에 `limit` 선택값 추가 |
| `performance/k6/vaccination-queue-flow.js` | 대기열 조회 요청에 `limit=100` 명시 |
| `docs/04_api/01_API_명세서.md` | 대기열 조회 `limit` 정책과 예시 갱신 |
| `performance/k6/README.md` | k6 대기열 조회 예시 갱신 |

## 6. 검증 체크리스트

- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `npm.cmd run build`
- [x] `git diff --check`
- [ ] 백엔드 서버 기동
- [ ] Swagger `GET /api/queues` 대표 예시 확인
- [ ] 직원 대기열 화면 확인
- [ ] 관리자 대기 마감 화면 확인
- [ ] Docker k6 VM 대상 재측정
- [ ] Grafana/Loki 확인

`npm.cmd run build` 참고:

- Next.js build는 성공했다.
- 기존과 같은 workspace root 추론 경고가 표시됐다.

## 7. Swagger 대표 예시

Swagger URL:

```text
http://localhost:8080/swagger-ui/index.html
```

권한:

- `STAFF` 또는 `ADMIN` 계정으로 로그인
- Swagger Authorize에는 accessToken 값만 입력

대표 요청:

```text
GET /api/queues?serviceTypeId=1&status=WAITING&date=2026-05-24&limit=100
```

기대 결과:

- `success: true`
- `data`는 최대 100건
- 각 항목의 응답 필드는 기존 `QueueTicketResponse`와 동일
- `date` 기준 당일 발급 대기표만 반환

## 8. 사용자 코드 점검 결과

| 점검 시점 | 사용자 의견 | 반영 여부 |
|---|---|---|
|  |  |  |

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 구현 중 | VM 대상 k6 재측정 | 이번 변경의 실제 p95 개선 여부는 런타임 부하 테스트가 필요 | PR 체크리스트 후속 확인으로 기록 |
| 구현 중 | 페이지 객체 또는 cursor pagination 검토 | 현재 응답 배열 계약을 유지하기 위해 breaking change는 피함 | 후속 고도화 후보로 유지 |

## 10. 남은 위험

- `limit` 기본값이 생겨 기존에 200건을 초과해 보던 화면은 첫 200건만 볼 수 있다.
- 관리자 마감 처리 API 자체는 전체 대상을 처리하지만, 관리자 화면의 목록 미리보기는 제한된 건수만 보여줄 수 있다.
- 실제 성능 개선 폭은 PostgreSQL 실행 계획과 데이터 분포에 따라 달라지므로 VM 대상 k6 재측정이 필요하다.

## 11. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] 보류 목록 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리

## 12. 커밋 메시지 초안

```text
perf: 대기열 조회 limit과 인덱스 최적화

- 대기열 조회 API에 limit 파라미터와 서버 기본/최대값 적용
- issued_at 날짜 필터를 범위 조건으로 변경하고 LIMIT을 SQL에 반영
- queue_tickets 조회용 복합 인덱스 추가
- k6 queue_list 요청과 API 문서에 limit 기준 반영
```
