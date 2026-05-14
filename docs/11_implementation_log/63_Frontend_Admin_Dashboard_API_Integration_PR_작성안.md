# Frontend Admin Dashboard API Integration Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/admin-dashboard-api` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 관리자 대시보드 API 연동 코드와 문서 변경 있음 |
| 주요 커밋 | 아직 없음 |
| 타입 확인 | `npm.cmd exec -- tsc --noEmit` 통과 |
| 빌드 확인 | `npm.cmd run build` 통과 |
| 정적 공백 확인 | `git diff --check` 통과. LF/CRLF 경고만 표시 |
| GitNexus 확인 | stale index, impact/detect-changes CLI 실패. `rg`와 빌드로 보완 |
| 실행/API 확인 | 미수행. 사용자가 백엔드/Swagger/브라우저로 직접 확인 필요 |

## PR 제목

```text
feat: 관리자 대시보드 API 연동
```

## PR 본문

```markdown
## 개요

관리자 대시보드 화면의 mock 지표 조회를 실제 백엔드 Dashboard API 호출로 교체합니다.

이번 PR에서는 요약 KPI, 시간대별 방문자 수, 업무별 평균 대기시간, 예약/현장 방문 비율, 노쇼율을 실제 API 응답으로 표시합니다.

## 변경 내용

- `frontend/src/lib/dashboard-api.ts` 추가
- `GET /api/dashboard/summary` 연동
- `GET /api/dashboard/hourly-visits` 연동
- `GET /api/dashboard/service-wait-times` 연동
- `GET /api/dashboard/visit-type-ratio` 연동
- `GET /api/dashboard/no-show-rate` 연동
- 백엔드 Dashboard 응답 필드를 프론트 차트 타입으로 정규화
- 관리자 대시보드 날짜 필터 추가
- 새로고침 시 현재 선택 날짜 기준으로 재조회
- 데이터가 비어 있을 때 차트 영역에 빈 상태 표시
- 노쇼율 카드에 계산 대상 예약 수와 미방문 건수 표시

## 검증

- [x] `npm.cmd exec -- tsc --noEmit`
- [x] `npm.cmd run build`
- [x] `git diff --check`
- [ ] Swagger `GET /api/dashboard/summary?date=2026-05-14` 대표 예시 확인
- [ ] 브라우저 `/admin/dashboard` KPI 카드 표시 확인
- [ ] 브라우저 시간대별 방문자 차트 확인
- [ ] 브라우저 업무별 평균 대기시간 차트 확인
- [ ] 브라우저 방문 유형 비율 차트 확인
- [ ] 브라우저 노쇼율 카드 확인
- [ ] 날짜 필터 변경 시 재조회 확인
- [ ] 새로고침 버튼 클릭 시 재조회 확인

## Swagger 대표 예시

`GET /api/dashboard/summary?date=2026-05-14`

기대 결과:

- `success: true`
- `data.todayVisitCount` 존재
- `data.currentWaitingCount` 존재
- `data.averageWaitMinutes` 존재
- `data.noShowRate` 존재

## 추가 테스트 체크리스트

- [ ] Happy: 관리자 계정으로 대시보드 진입 시 KPI/차트 조회 성공
- [ ] Happy: 날짜 필터 변경 시 모든 Dashboard API가 같은 날짜로 조회
- [ ] Happy: 방문자 수가 0인 시간대도 차트에서 자연스럽게 표시
- [ ] Edge: 업무별 호출 데이터가 없으면 평균 대기시간 0으로 표시
- [ ] Edge: 예약/현장 방문 수가 모두 0이면 빈 상태 표시
- [ ] Edge: 노쇼 계산 대상 예약 수가 0이면 노쇼율 0으로 표시
- [ ] Bad: 시민/직원 계정으로 접근 시 route guard 또는 403 처리 확인
- [ ] Bad: Dashboard API 실패 시 오류 상태와 재시도 버튼 표시

## 미검증 사유

- 서버 기동, Docker 실행, Swagger Try it out, 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- 신규 백엔드 API가 없으므로 seed/mock 데이터 추가는 필요하지 않습니다.
- GitNexus CLI impact/detect-changes가 exit 1로 실패해 `rg` 기반 영향 확인으로 보완했습니다.

## 남은 위험

- 브라우저 런타임에서 CORS, API base URL, 관리자 accessToken, 백엔드 실행 상태를 확인해야 합니다.
- Dashboard API가 날짜별 데이터가 거의 없는 경우 차트가 대부분 0 또는 빈 상태로 보일 수 있습니다.
- `package-lock.json`과 `pnpm-lock.yaml` 공존으로 Next build root 추론 경고가 남아 있습니다.

## 후속 작업

- 관리자 기준정보 화면 API 연동
- 시민 현재 혼잡도 API 연동
- ESLint와 패키지 매니저 기준 정리
- GitNexus analyze/impact 실패 원인 정리
```

## 커밋 메시지 초안

```text
feat: 관리자 대시보드 API 연동

- Dashboard API client 추가
- 관리자 대시보드 지표와 차트를 실제 API로 연결
- 날짜 필터와 노쇼율 상세 표시 보강
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 후속 관리자 기준정보 API 연동 브랜치 생성 여부 결정
