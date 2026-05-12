# Dashboard Detail Metrics API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/dashboard-detail-metrics-api` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | Dashboard 세부 지표 API, DTO/VO, 집계 Mapper, API 명세, 전체 체크리스트 수정 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | `mvn.cmd -q -DskipTests compile` |
| 테스트 확인 | `mvn.cmd -q test-compile` |
| 정적 확인 | `Dashboard_SQL_postgresql.xml` XML 파싱 성공, `git diff --check` 성공 |
| 실행/API 확인 | Swagger 대표 순서 작성 |

## PR 제목

```text
feat: 대시보드 세부 지표 API 구현
```

## PR 본문

```markdown
## 개요

관리자 대시보드에서 사용할 시간대별 방문자 수, 업무별 평균 대기시간, 예약/현장 방문 비율, 노쇼율 세부 지표 API를 구현합니다.

## 변경 내용

- `GET /api/dashboard/hourly-visits` 시간대별 방문자 수 API 추가
- `GET /api/dashboard/service-wait-times` 업무별 평균 대기시간 API 추가
- `GET /api/dashboard/visit-type-ratio` 예약/현장 방문 비율 API 추가
- `GET /api/dashboard/no-show-rate` 노쇼율 API 추가
- Dashboard Controller, QueryService, Mapper, XML 확장
- 세부 지표 응답 DTO와 MyBatis VO 추가
- API 명세와 전체 체크리스트 갱신

## 검증

- [x] `Dashboard_SQL_postgresql.xml` XML 파싱 확인
- [x] `rg`로 신규 endpoint, Service, Mapper 참조 확인
- [x] git diff --check
- [ ] Maven compile
- [ ] Maven test-compile
- [ ] Swagger에서 `GET /api/dashboard/hourly-visits` 확인
- [ ] Swagger에서 `GET /api/dashboard/service-wait-times` 확인
- [ ] Swagger에서 `GET /api/dashboard/visit-type-ratio` 확인
- [ ] Swagger에서 `GET /api/dashboard/no-show-rate` 확인

## Swagger 대표 테스트 순서

1. `POST /api/auth/login`
   - `admin@test.com / password1234`
2. Swagger Authorize 창에 accessToken 값만 입력
3. `GET /api/dashboard/hourly-visits`

기대:

- `success = true`
- `data[0].hour` 반환
- `data[0].visitCount` 반환

## Swagger 추가 테스트 체크리스트

| 케이스 | 확인 방법 | 기대 결과 |
|---|---|---|
| 시간대별 방문자 수 | `GET /api/dashboard/hourly-visits` | 0~23시 데이터 반환 |
| 업무별 평균 대기시간 | `GET /api/dashboard/service-wait-times` | 업무 유형별 평균 대기시간과 호출 건수 반환 |
| 예약/현장 방문 비율 | `GET /api/dashboard/visit-type-ratio` | 전체/예약/현장 방문 수와 비율 반환 |
| 노쇼율 | `GET /api/dashboard/no-show-rate` | 계산 대상 예약 수, 노쇼 수, 노쇼율 반환 |
| 날짜 지정 | 각 API에 `date=2026-05-12` 지정 | 지정 날짜 기준 집계 |
| 권한 확인 | 직원 또는 시민 토큰으로 호출 | HTTP 403 또는 권한 실패 |

## 후속 작업

- 프론트엔드 대시보드/혼잡도 연동
- 배치 집계 테이블 도입 검토
- 동시 요청 자동화 테스트 후보 정리
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영
- [ ] 후속 브랜치 생성 또는 다음 작업 문서화

## 커밋 메시지 초안

```text
feat: 대시보드 세부 지표 API 구현

- 시간대별 방문자 수와 업무별 평균 대기시간 API 추가
- 예약/현장 방문 비율과 노쇼율 API 추가
- Dashboard 집계 Mapper와 API 문서 갱신
```
