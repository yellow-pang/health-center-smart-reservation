# Frontend Admin Dashboard API Integration 구현 기록

## 1. 작업 목표

- 관리자 대시보드 화면의 mock 지표 조회를 실제 백엔드 Dashboard API 호출로 교체한다.
- 이번 브랜치에서는 `GET /api/dashboard/*` 조회 API 연동과 화면 표시 정규화만 진행한다.
- 신규 백엔드 API, DB, seed/mock 데이터 변경은 이번 범위에서 제외한다.

## 2. 작업 범위

- [x] 현재 브랜치와 작업 트리 확인
- [x] 전체 체크리스트의 관리자 대시보드/프론트 API 연동 항목 확인
- [x] Dashboard API 명세와 기존 구현 기록 확인
- [x] 관리자 대시보드 화면의 mock service 사용처 확인
- [x] Dashboard API client 추가
- [x] 대시보드 요약 API 연동
- [x] 시간대별 방문자 수 API 연동
- [x] 업무별 평균 대기시간 API 연동
- [x] 예약/현장 방문 비율 API 연동
- [x] 노쇼율 API 연동
- [x] 날짜 필터와 새로고침 흐름 연결
- [x] 빈 데이터 표시 보강
- [x] TypeScript 정적 검증
- [x] Next build 검증
- [ ] 브라우저에서 관리자 대시보드 API 연동 확인

## 3. 관련 전체 체크리스트 항목

| 영역 | 항목 | 이번 작업 반영 |
|---|---|---|
| 프론트엔드 | 관리자 대시보드 화면 구현 | 핵심 지표와 차트를 실제 Dashboard API로 연결 |
| 프론트엔드 | 프론트엔드 API 연동 | `GET /api/dashboard/*` mock service 교체 |
| 테스트 | 프론트엔드 화면 검증 | TypeScript/Next build 확인, 브라우저 확인은 사용자 직접 수행 필요 |

## 4. GitNexus 및 영향 범위

GitNexus 확인:

| 명령 | 결과 |
|---|---|
| `npm.cmd exec -- gitnexus status` | stale index 확인 |
| `npm.cmd exec -- gitnexus impact AdminDashboardPage --repo health-center-smart-reservation --direction upstream --depth 2` | 실패. exit 1, 출력 없음 |
| `npm.cmd exec -- gitnexus impact DashboardStats --repo health-center-smart-reservation --direction upstream --depth 2` | 실패. exit 1, 출력 없음 |
| `npm.cmd exec -- gitnexus detect_changes -r health-center-smart-reservation -s all` | 실패. exit 1, 출력 없음 |

대체 확인:

- `rg`로 `getDashboardStats`, `getHourlyVisitors`, `getServiceWaitTimes`, `getVisitTypeRatio`, `DashboardStats`, `HourlyVisitors`, `ServiceWaitTime`, `VisitTypeRatio` 사용처 확인
- 직접 영향은 관리자 대시보드 화면과 신규 dashboard API client에 한정

Blast radius:

| 대상 | 직접 사용처 | 영향 프로세스 | 위험도 |
|---|---|---|---|
| `AdminDashboardPage` | `/admin/dashboard` | 관리자 운영 지표 조회 화면 | MEDIUM |
| `dashboard-api.ts` | 관리자 대시보드 화면 | Dashboard API 응답 정규화 | LOW |
| mock dashboard service 사용처 | 관리자 대시보드 화면 | mock 지표 제거 | LOW |

HIGH/CRITICAL 경고는 GitNexus CLI 실패로 확인하지 못했다. 대신 사용처 검색, TypeScript, Next build로 정적 검증했다.

## 5. 구현 내용

### 5.1 Dashboard API client 추가

추가 파일:

| 파일 | 내용 |
|---|---|
| `frontend/src/lib/dashboard-api.ts` | Dashboard API 호출과 화면 타입 정규화 |

연동 API:

| 함수 | API | 화면 타입 매핑 |
|---|---|---|
| `getDashboardStats` | `GET /api/dashboard/summary` | `todayVisitCount -> todayVisitors`, `currentWaitingCount -> currentWaiting` |
| `getHourlyVisitors` | `GET /api/dashboard/hourly-visits` | `hour`, `visitCount -> count` |
| `getServiceWaitTimes` | `GET /api/dashboard/service-wait-times` | `serviceTypeName -> serviceType`, `averageWaitMinutes -> avgMinutes` |
| `getVisitTypeRatio` | `GET /api/dashboard/visit-type-ratio` | 예약/현장 2개 pie 데이터로 변환 |
| `getNoShowRate` | `GET /api/dashboard/no-show-rate` | 목표/현황 카드 보조 지표로 사용 |

### 5.2 관리자 대시보드 화면 연결

- `frontend/app/admin/dashboard/page.tsx`의 mock service import를 `dashboard-api.ts`로 교체했다.
- 날짜 입력 값을 `date` query parameter로 전달한다.
- 날짜 변경 시 해당 날짜 기준 지표를 다시 조회한다.
- 새로고침 버튼은 현재 선택된 날짜로 다시 조회한다.
- 노쇼율 카드의 고정 문구를 API 응답 기반 미방문 건수 표시로 변경했다.
- 시간대별 방문자, 업무별 평균 대기시간, 방문 유형 비율 데이터가 비어 있을 때 빈 상태를 표시한다.

## 6. 검증 결과

| 검증 | 결과 |
|---|---|
| `npm.cmd exec -- tsc --noEmit` | 통과 |
| `npm.cmd run build` | 통과 |
| `git diff --check` | 공백 오류 없음. LF/CRLF 경고만 표시 |
| GitNexus impact/detect-changes | CLI 오류로 완료하지 못함. `rg` 기반 대체 확인 |
| 브라우저 화면 확인 | 미수행. 사용자가 직접 확인 필요 |
| API 런타임 확인 | 미수행. 사용자가 Swagger 또는 브라우저로 직접 확인 필요 |

빌드 참고:

- Next build는 통과했다.
- `package-lock.json`과 `pnpm-lock.yaml`이 함께 있어 Turbopack root 추론 경고가 남아 있다.
- `next build`와 `tsc --noEmit` 후 `next-env.d.ts`, `tsconfig.tsbuildinfo` 산출물 변경이 생겨 원복했다.

## 7. 사용자 직접 확인 방법

프론트 실행 전 `.env.local`에 아래 값을 둔다.

```text
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

사용자 확인 순서:

1. 사용자가 PostgreSQL과 백엔드를 실행한다.
2. 프론트엔드를 실행한다.
3. `admin@test.com / password1234`로 로그인한다.
4. `/admin/dashboard`에서 KPI 카드와 차트가 표시되는지 확인한다.
5. 날짜 필터를 변경했을 때 Dashboard API가 해당 날짜로 다시 조회되는지 확인한다.
6. 새로고침 버튼이 현재 선택된 날짜 기준으로 다시 조회되는지 확인한다.

Swagger 대표 예시:

`GET /api/dashboard/summary?date=2026-05-14`

기대 결과:

- `success: true`
- `data.todayVisitCount` 존재
- `data.currentWaitingCount` 존재
- `data.averageWaitMinutes` 존재
- `data.noShowRate` 존재

## 8. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| Dashboard 연동 중 | 패키지 매니저 기준 정리 | root `package-lock.json`과 frontend `pnpm-lock.yaml` 공존으로 Next 경고 발생 | 후속 |
| Dashboard 연동 중 | 관리자 기준정보 화면 API 연동 | 대시보드 이후 관리자 영역의 남은 mock CRUD 화면 연결 필요 | 후속 |
| 검증 중 | GitNexus stale/impact 실패 원인 정리 | impact/detect-changes가 빈 출력으로 실패 | 후속 |

## 9. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성
- [x] PR 문서 초안 작성
- [x] 전체 체크리스트 갱신
- [x] TypeScript 정적 검증 완료
- [x] Next build 완료
- [ ] 브라우저 관리자 대시보드 화면 확인
- [ ] Swagger Dashboard 대표 예시 확인

## 10. 커밋 메시지 초안

```text
feat: 관리자 대시보드 API 연동

- Dashboard API client 추가
- 관리자 대시보드 지표와 차트를 실제 API로 연결
- 날짜 필터와 노쇼율 상세 표시 보강
```
