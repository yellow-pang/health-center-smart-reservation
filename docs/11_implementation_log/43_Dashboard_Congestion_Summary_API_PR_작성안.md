# Dashboard Congestion Summary API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/dashboard-congestion-summary-api` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | Dashboard/Congestion 요약 API, 집계 Mapper, API 명세, 전체 체크리스트, 보류 목록 수정 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | `mvn.cmd -q -DskipTests compile` |
| 테스트 확인 | `mvn.cmd -q test-compile` |
| 정적 확인 | `Dashboard_SQL_postgresql.xml` XML 파싱 성공, `git diff --check` 성공 |
| 실행/API 확인 | Swagger 대표 순서 작성 |

## PR 제목

```text
feat: 대시보드 요약 및 현재 혼잡도 API 구현
```

## PR 본문

```markdown
## 개요

예약, 방문, 대기열 데이터를 집계해 관리자 대시보드 요약 지표와 사용자용 현재 혼잡도 지표를 조회할 수 있도록 Dashboard/Congestion API를 추가합니다.

## 변경 내용

- `GET /api/dashboard/summary` 관리자 요약 API 추가
- `GET /api/congestion/current` 현재 혼잡도 API 추가
- Dashboard/Congestion Controller, QueryService, DTO, Mapper, XML 추가
- 방문자 수, 현재 대기 인원, 평균 대기시간, 노쇼율 집계 SQL 추가
- 업무 유형별 대기 인원, 예상 대기시간, 혼잡도 계산 추가
- 동시 요청 자동화 테스트 후보를 보류 정리 목록 DC-005로 이관
- API 명세와 전체 체크리스트 갱신

## 검증

- [x] `Dashboard_SQL_postgresql.xml` XML 파싱 확인
- [x] `rg`로 신규 Controller, Service, Mapper 참조 확인
- [x] MyBatis mapper location 패턴 확인
- [x] git diff --check
- [ ] Maven compile
- [ ] Maven test-compile
- [ ] Swagger에서 `GET /api/dashboard/summary` 확인
- [ ] Swagger에서 `GET /api/congestion/current` 확인

## Swagger 대표 테스트 순서

### 관리자 대시보드 요약

1. `POST /api/auth/login`
   - `admin@test.com / password1234`
2. Swagger Authorize 창에 accessToken 값만 입력
3. `GET /api/dashboard/summary`

기대:

- `success = true`
- `data.todayVisitCount` 반환
- `data.currentWaitingCount` 반환
- `data.averageWaitMinutes` 반환
- `data.noShowRate` 반환

### 현재 혼잡도

1. `GET /api/congestion/current?healthCenterId=1`

기대:

- `success = true`
- 업무 유형별 `waitingCount`, `estimatedWaitMinutes`, `congestionLevel`, `congestionLabel` 반환

## Swagger 추가 테스트 체크리스트

| 케이스 | 확인 방법 | 기대 결과 |
|---|---|---|
| 요약 날짜 생략 | `GET /api/dashboard/summary` | 오늘 기준 요약 반환 |
| 요약 날짜 지정 | `GET /api/dashboard/summary?date=2026-05-12` | 지정 날짜 기준 요약 반환 |
| 관리자 권한 | 관리자 토큰으로 요약 조회 | HTTP 200 |
| 비관리자 권한 | 직원 또는 시민 토큰으로 요약 조회 | HTTP 403 또는 권한 실패 |
| 혼잡도 기본 보건소 | `GET /api/congestion/current` | 기본 보건소 기준 조회 |
| 혼잡도 보건소 지정 | `GET /api/congestion/current?healthCenterId=1` | 지정 보건소 기준 조회 |
| 완료 데이터 없음 | 완료 대기표가 없는 업무 유형 조회 | 평균 처리시간 fallback 5분 기준 예상 대기시간 계산 |

## 후속 작업

- 시간대별 방문자 수 API 구현
- 업무별 평균 대기시간 API 구현
- 예약/현장 방문 비율 API 구현
- 노쇼율 단독 API 구현
- 동시 요청 자동화 테스트 후보 정리
- 프론트엔드 대시보드/혼잡도 연동
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영
- [ ] 후속 브랜치 생성 또는 다음 작업 문서화

## 커밋 메시지 초안

```text
feat: 대시보드 요약 및 현재 혼잡도 API 구현

- 관리자 대시보드 요약 지표 집계 API 추가
- 업무 유형별 현재 혼잡도 조회 API 추가
- Dashboard 집계 Mapper와 보류/일정 문서 갱신
```
