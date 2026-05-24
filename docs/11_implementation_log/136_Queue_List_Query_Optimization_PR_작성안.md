# Queue List Query Optimization Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `perf/queue-list-query-optimization` |
| base 브랜치 | `dev` |
| 작업 트리 | 대기열 조회 API, Mapper SQL, schema index, k6 시나리오, API 문서, 구현 기록, PR 문서, 체크리스트 갱신 |
| 주요 커밋 | 커밋 전 |
| 빌드 확인 | `mvn.cmd -q -DskipTests compile` 통과 |
| 테스트 확인 | `mvn.cmd -q test-compile`, `npm.cmd run build`, `git diff --check` 통과 |
| 실행/API 확인 | 서버 기동, Docker, Swagger, 브라우저, k6 VM 재측정은 사용자가 직접 확인 |

## PR 제목

```text
perf: 대기열 조회 limit과 인덱스 최적화
```

## PR 본문

```markdown
## 개요

k6 VM VUS 20 실행에서 `GET /api/queues` p95가 2.31초로 조회 기준 1초를 초과해 대기열 조회를 1차 최적화합니다.

응답 DTO는 유지하고, 조회 범위를 제한할 수 있는 `limit` 파라미터와 SQL/인덱스 개선만 작은 범위로 반영했습니다.

## 변경 내용

- `GET /api/queues`에 `limit` 선택 파라미터 추가
  - 기본값 200
  - 최대값 500
  - 1 미만 입력 시 기본값 사용
- 대기열 조회 SQL 개선
  - `q.issued_at::date = #{targetDate}` 제거
  - `issued_at >= fromIssuedAt AND issued_at < toIssuedAt` 범위 조건 적용
  - SQL `LIMIT` 적용
- `queue_tickets` 조회용 복합 인덱스 추가
  - `idx_queue_tickets_list_lookup`
- k6 `queue_list` 요청에 `limit=100` 명시
- API 명세와 k6 README 갱신
- 브랜치 구현 기록과 전체 체크리스트 갱신

## 검증

- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `npm.cmd run build`
- [x] `git diff --check`
- [ ] Swagger `GET /api/queues?serviceTypeId=1&status=WAITING&date=2026-05-24&limit=100` 확인
- [ ] 직원 대기열 화면 확인
- [ ] 관리자 대기 마감 화면 확인
- [ ] Docker k6 VM 대상 재측정
- [ ] Grafana `HTTP Request Rate`, `Average HTTP Latency`, `Backend Error Logs` 확인

## Swagger 대표 예시

권한:

- `STAFF` 또는 `ADMIN`
- Swagger Authorize에는 accessToken 값만 입력

요청:

```text
GET /api/queues?serviceTypeId=1&status=WAITING&date=2026-05-24&limit=100
```

기대 응답:

- `success: true`
- `data`는 최대 100건
- 응답 필드는 기존 대기표 배열과 동일

## 미검증 사유

- 프로젝트 운영 기준상 서버 기동, Docker 실행, Swagger/API 런타임 호출, 브라우저 확인, k6 부하 테스트는 사용자가 직접 수행합니다.
- 이번 변경의 실제 성능 개선 폭은 VM PostgreSQL 실행 계획과 데이터 분포에 따라 달라져 VM 대상 k6 재측정이 필요합니다.

## 후속 작업

- VM 대상 k6 4단계 재실행으로 `queue_list` p95 재측정
- 필요 시 `EXPLAIN ANALYZE`로 실제 인덱스 사용 여부 확인
- 목록이 500건을 초과하는 운영 화면이 필요하면 cursor pagination 또는 count API 분리 검토
- 관리자 마감 화면은 전체 마감 처리와 목록 미리보기의 건수 차이를 UI 문구로 보강 검토
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] VM 대상 Docker k6 실행 결과 기록
- [ ] Grafana/Loki 확인 결과 기록
- [ ] 전체 체크리스트의 후속 성능 항목 갱신

## 커밋 메시지 초안

제목:

```text
perf: 대기열 조회 limit과 인덱스 최적화
```

본문:

```text
- GET /api/queues에 limit 파라미터와 서버 기본/최대값 적용
- issued_at 날짜 필터를 범위 조건으로 변경하고 SQL LIMIT 적용
- queue_tickets 조회용 복합 인덱스 추가
- k6 queue_list 요청과 API 문서에 limit 기준 반영
```
