# Dashboard Congestion Summary API 구현 기록

## 1. 작업 목표

- 관리자 대시보드 요약 API를 구현해 오늘 방문자 수, 현재 대기 인원, 평균 대기시간, 노쇼율을 조회한다.
- 사용자용 현재 혼잡도 API를 구현해 업무 유형별 대기 인원, 예상 대기시간, 혼잡도를 조회한다.
- 동시 요청 자동화 테스트 후보는 이번 구현 범위에서 분리해 보류 정리 목록에 기록한다.

## 2. 작업 범위

- [x] 이번 브랜치에 포함: `GET /api/dashboard/summary`
- [x] 이번 브랜치에 포함: `GET /api/congestion/current`
- [x] 이번 브랜치에 포함: Dashboard/Congestion Controller, QueryService, DTO, Mapper, XML 추가
- [x] 이번 브랜치에 포함: API 명세와 전체 체크리스트 갱신
- [x] 이번 브랜치에 포함: 동시 요청 자동화 테스트 후보 보류 목록 이관
- [x] 이번 브랜치에서 제외: 시간대별 방문자 수 API
- [x] 이번 브랜치에서 제외: 업무별 평균 대기시간 API
- [x] 이번 브랜치에서 제외: 예약/현장 방문 비율 API
- [x] 이번 브랜치에서 제외: 노쇼율 단독 API
- [x] 이번 브랜치에서 제외: 프론트엔드 연동

## 3. 작업 전 체크리스트

- [x] 현재 브랜치 확인: `feat/dashboard-congestion-summary-api`
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` Dashboard/Congestion 항목 확인
- [x] `docs/06_dashboard/01_대시보드_지표_정의서.md` 지표 계산 기준 확인
- [x] `docs/04_api/01_API_명세서.md` API 자리 확인
- [x] 보안 설정에서 `/api/dashboard/**`, `/api/congestion/current` 접근 규칙 확인
- [x] 기존 공통 응답 형식 확인
- [x] MyBatis mapper location 패턴 확인

## 4. 영향 분석

| 대상 | 직접 영향 | 위험도 |
|---|---|---|
| 신규 `dashboard` 패키지 | Controller, Service, DTO, Mapper 추가 | LOW |
| `Dashboard_SQL_postgresql.xml` | 방문/대기/예약 집계 SQL 추가 | MEDIUM |
| `docs/04_api/01_API_명세서.md` | Dashboard/Congestion 정책 상세화 | LOW |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | Dashboard 진행 상태 갱신 | LOW |
| `docs/14_deferred_cleanup/01_보류_정리_목록.md` | 동시 요청 테스트 후보 보류 항목 추가 | LOW |

## 5. 구현 체크리스트

- [x] Dashboard Controller 추가
- [x] Congestion Controller 추가
- [x] QueryService 추가
- [x] DTO 추가
- [x] Mapper/VO 추가
- [x] Mapper XML 추가
- [x] API 명세 갱신
- [x] 전체 체크리스트 갱신
- [x] 보류 정리 목록 갱신
- [x] PR 문서 초안 작성
- [x] 커밋 메시지 초안 작성

## 6. 검증 체크리스트

- [x] `Dashboard_SQL_postgresql.xml` XML 파싱 확인
- [x] `rg`로 신규 Controller, Service, Mapper 참조 확인
- [x] MyBatis mapper location이 `healthcenter/**/*_postgresql.xml`를 포함하는지 확인
- [x] `git diff --check`
- [ ] `mvn.cmd -q -DskipTests compile`
- [ ] `mvn.cmd -q test-compile`
- [ ] Swagger에서 `GET /api/dashboard/summary` 확인
- [ ] Swagger에서 `GET /api/congestion/current` 확인

## 7. 구현 내용

### 7.1 관리자 대시보드 요약

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/dashboard/summary?date=2026-05-12` | 날짜별 핵심 요약 지표 조회 | `ADMIN` |

집계 기준:

- `date`를 생략하면 오늘 날짜를 사용한다.
- 방문자 수는 `visits.checked_in_at` 기준으로 계산한다.
- 현재 대기 인원은 해당 날짜 발급 대기표 중 `WAITING` 상태 기준으로 계산한다.
- 평균 대기시간은 `queue_tickets.called_at - issued_at` 평균값이다.
- 노쇼율은 취소 예약을 제외한 예약 중 `NO_SHOW` 비율이다.

### 7.2 현재 혼잡도

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/congestion/current?healthCenterId=1` | 업무 유형별 현재 혼잡도 조회 | `PUBLIC` |

집계 기준:

- `healthCenterId`를 생략하면 기본 보건소 `1`을 사용한다.
- 활성 업무 유형별 `WAITING` 대기표 수를 계산한다.
- 평균 처리시간은 오늘 완료된 대기표의 `completed_at - started_at` 평균값이며, 데이터가 없으면 5분을 사용한다.
- 예상 대기시간은 `현재 대기 인원 * 평균 처리시간`이다.
- 혼잡도는 대기 인원 기준과 예상 대기시간 기준 중 더 높은 수준을 사용한다.

## 8. Swagger 대표 확인 방법

관리자 요약:

1. `POST /api/auth/login`에서 `admin@test.com / password1234`로 로그인한다.
2. Swagger Authorize 창에는 `Bearer `를 제외한 accessToken 값만 입력한다.
3. `GET /api/dashboard/summary`를 실행한다.

기대 결과:

- HTTP 200
- `success = true`
- `data.todayVisitCount`, `data.currentWaitingCount`, `data.averageWaitMinutes`, `data.noShowRate` 반환

현재 혼잡도:

1. `GET /api/congestion/current?healthCenterId=1`을 실행한다.
2. 인증 없이 호출 가능하다.

기대 결과:

- HTTP 200
- `success = true`
- 업무 유형별 `waitingCount`, `estimatedWaitMinutes`, `congestionLevel`, `congestionLabel` 반환

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 범위 정리 | 동시 요청 자동화 테스트 후보 정리 | 테스트 성격의 작업이라 Dashboard 구현 브랜치와 분리하는 편이 안전 | `docs/14_deferred_cleanup/01_보류_정리_목록.md` DC-005로 이관 |
| 범위 정리 | 세부 대시보드 API 구현 | summary/current 이후 시간대별/업무별/비율/노쇼율 API가 필요 | 후속 작업 |
| 범위 정리 | 프론트엔드 대시보드 연동 | 프론트엔드 작업은 별도 시점에 진행 | 후속 작업 |

## 10. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 보류 정리 목록 갱신
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
