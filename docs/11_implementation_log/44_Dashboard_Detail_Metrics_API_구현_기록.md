# Dashboard Detail Metrics API 구현 기록

## 1. 작업 목표

- 관리자 대시보드에서 사용할 세부 지표 API를 구현한다.
- 시간대별 방문자 수, 업무별 평균 대기시간, 예약/현장 방문 비율, 노쇼율을 각각 조회할 수 있게 한다.
- 기존 Dashboard/Congestion 1차 API 구조를 유지하고 MyBatis 집계 SQL만 확장한다.

## 2. 작업 범위

- [x] 이번 브랜치에 포함: `GET /api/dashboard/hourly-visits`
- [x] 이번 브랜치에 포함: `GET /api/dashboard/service-wait-times`
- [x] 이번 브랜치에 포함: `GET /api/dashboard/visit-type-ratio`
- [x] 이번 브랜치에 포함: `GET /api/dashboard/no-show-rate`
- [x] 이번 브랜치에 포함: Dashboard DTO/VO, Service, Mapper, XML 확장
- [x] 이번 브랜치에 포함: API 명세와 전체 체크리스트 갱신
- [x] 이번 브랜치에서 제외: 프론트엔드 대시보드 연동
- [x] 이번 브랜치에서 제외: 배치 집계 테이블 도입
- [x] 이번 브랜치에서 제외: 동시 요청 자동화 테스트

## 3. 작업 전 체크리스트

- [x] 현재 브랜치 확인: `feat/dashboard-detail-metrics-api`
- [x] 이전 PR 문서 `43_Dashboard_Congestion_Summary_API_PR_작성안.md` 후속 작업 확인
- [x] `docs/04_api/01_API_명세서.md` 세부 지표 API 목록 확인
- [x] `docs/06_dashboard/01_대시보드_지표_정의서.md` 지표 계산 기준 확인
- [x] 기존 Dashboard Controller/Service/Mapper 구조 확인
- [x] 영향받는 파일 확인

## 4. 영향 분석

| 대상 | 직접 영향 | 위험도 |
|---|---|---|
| `DashboardController` | 신규 세부 지표 endpoint 4개 추가 | LOW |
| `DashboardQueryService` | 관리자 검증 재사용, 날짜별 조회 메서드 추가 | LOW |
| `DashboardMapper`, XML | 방문/대기/예약 집계 SQL 추가 | MEDIUM |
| 신규 DTO/VO | API 응답과 MyBatis 매핑 추가 | LOW |
| API 명세/전체 체크리스트 | 구현 범위 반영 | LOW |

## 5. 구현 체크리스트

- [x] Controller endpoint 추가
- [x] QueryService 메서드 추가
- [x] DTO 추가
- [x] VO 추가
- [x] Mapper 메서드 추가
- [x] Mapper XML resultMap/select 추가
- [x] API 명세 갱신
- [x] 전체 체크리스트 갱신
- [x] PR 문서 초안 작성
- [x] 커밋 메시지 초안 작성

## 6. 검증 체크리스트

- [x] `Dashboard_SQL_postgresql.xml` XML 파싱 확인
- [x] `rg`로 신규 endpoint, Service, Mapper 참조 확인
- [x] `git diff --check`
- [ ] `mvn.cmd -q -DskipTests compile`
- [ ] `mvn.cmd -q test-compile`
- [ ] Swagger에서 `GET /api/dashboard/hourly-visits` 확인
- [ ] Swagger에서 `GET /api/dashboard/service-wait-times` 확인
- [ ] Swagger에서 `GET /api/dashboard/visit-type-ratio` 확인
- [ ] Swagger에서 `GET /api/dashboard/no-show-rate` 확인

## 7. 구현 내용

### 7.1 API

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/dashboard/hourly-visits?date=2026-05-12` | 시간대별 방문자 수 | `ADMIN` |
| GET | `/api/dashboard/service-wait-times?date=2026-05-12` | 업무별 평균 대기시간 | `ADMIN` |
| GET | `/api/dashboard/visit-type-ratio?date=2026-05-12` | 예약/현장 방문 비율 | `ADMIN` |
| GET | `/api/dashboard/no-show-rate?date=2026-05-12` | 노쇼율 | `ADMIN` |

### 7.2 집계 기준

- `date`를 생략하면 오늘 날짜를 사용한다.
- 모든 API는 로그인한 관리자의 `healthCenterId` 기준으로 집계한다.
- 시간대별 방문자 수는 `visits.checked_in_at` 기준이며 0시부터 23시까지 반환한다.
- 업무별 평균 대기시간은 `queue_tickets.called_at - issued_at` 기준이다.
- 예약/현장 비율은 `visits.visit_type`의 `RESERVED`, `WALK_IN` 기준이다.
- 노쇼율은 취소 예약을 제외한 예약 중 `NO_SHOW` 비율이다.

## 8. Swagger 대표 확인 방법

1. `POST /api/auth/login`에서 `admin@test.com / password1234`로 로그인한다.
2. Swagger Authorize 창에는 `Bearer `를 제외한 accessToken 값만 입력한다.
3. `GET /api/dashboard/hourly-visits`를 실행한다.

기대 결과:

- HTTP 200
- `success = true`
- `data[0].hour`, `data[0].visitCount` 반환

추가 세부 지표는 PR 문서의 Swagger 추가 테스트 체크리스트로 확인한다.

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 범위 정리 | 프론트엔드 대시보드 연동 | API 구현 이후 화면에서 지표를 표시해야 함 | 후속 작업 |
| 범위 정리 | 배치 집계 테이블 도입 검토 | 실시간 집계가 느려지는 시점에는 일별/시간대별 집계 테이블이 필요 | 후속 고도화 |
| 범위 정리 | 동시 요청 자동화 테스트 | Dashboard 구현과 별도 성격의 테스트 작업 | 보류 목록 DC-005 유지 |

## 10. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
