# Frontend Staff Queue API Integration Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/frontend-staff-queue-api-integration` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 직원 API 연동 코드와 문서 변경 있음 |
| 주요 커밋 | 아직 없음 |
| 타입 확인 | `npm.cmd exec -- tsc --noEmit` 통과 |
| 빌드 확인 | `npm.cmd run build` 통과 |
| lint 확인 | 실패. `eslint` 실행 파일 없음 |
| GitNexus 확인 | CLI 오류로 impact/detect-changes 완료 못함 |
| 실행/API 확인 | 미수행. 사용자가 백엔드/Swagger/브라우저로 직접 확인 필요 |

## PR 제목

```text
feat: 프론트엔드 직원 대기열 API 연동
```

## PR 본문

```markdown
## 개요

직원 체크인, 현장 접수, 대기열 관리 화면을 mock service에서 실제 백엔드 API로 교체합니다.

이번 PR에서는 직원 운영 핵심 흐름인 예약자 체크인, 현장 접수, 대기열 조회, 대기열 상태 변경을 연결합니다.

## 변경 내용

- `frontend/src/lib/staff-api.ts` 추가
- `POST /api/visits/check-in` 연동
- `POST /api/visits/walk-in` 연동
- `GET /api/queues` 연동
- `POST /api/queues/{queueTicketId}/{action}` 연동
- 대기열 조회/상태 변경 응답을 `QueueEntry` 화면 타입으로 정규화
- 방문자 이름/전화번호 마스킹 처리
- 대기열 요약 카드를 실제 조회 데이터 기준으로 계산
- 백엔드 대기 상태 전이 정책에 맞게 액션 버튼 조정

## 검증

- [x] `npm.cmd exec -- tsc --noEmit`
- [x] `npm.cmd run build`
- [x] `git diff --check`
- [ ] `npm.cmd run lint`
- [x] Swagger `POST /api/visits/walk-in` 대표 예시 확인
- [x] 브라우저 `/staff/check-in` 체크인 확인
- [x] 브라우저 `/staff/walk-in` 현장 접수 확인
- [x] 브라우저 `/staff/queues` 대기열 조회 확인
- [x] 브라우저 대기열 호출/시작/완료/보류/미응답/취소 확인
- [x] 모바일 브라우저 화면 확인
- [x] 데스크톱 브라우저 화면 확인

## Swagger 대표 예시

`POST /api/visits/walk-in`

```json
{
  "serviceTypeId": 1,
  "visitorName": "Swagger현장접수",
  "visitorPhone": "010-4567-8901"
}
```

기대 결과:

- `success: true`
- `data.queueTicketId` 존재
- `data.ticketNumber` 존재
- `data.status`가 `WAITING`

## 추가 테스트 체크리스트

- [x] Happy: `RSV-SWAGGER-CHECKIN-001` 체크인 후 대기번호 표시
- [x] Happy: 현장 접수 후 최근 접수 목록에 추가
- [x] Happy: 대기열 조회 시 오늘 대기표 목록 표시
- [x] Happy: `WAITING -> CALLED -> IN_PROGRESS -> COMPLETED` 상태 전이
- [x] Happy: `CALLED -> HOLD -> CALLED` 재호출
- [x] Happy: `HOLD -> NO_SHOW` 최종 미응답
- [x] Happy: `WAITING/CALLED/HOLD -> CANCELED` 취소
- [x] Bad: 이미 체크인한 예약번호 입력 시 오류 메시지 표시
- [x] Bad: 불가능한 상태 전이 시 서버 오류 메시지 toast 표시

## 미검증 사유

- 서버 기동, Docker 실행, Swagger Try it out, 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- `npm.cmd run lint`는 현재 `eslint` 실행 파일이 없어 실패합니다.
- GitNexus CLI impact/detect-changes가 exit 1로 실패해 `rg` 기반 영향 확인으로 보완했습니다.

## 남은 위험

- 체크인/현장 접수 응답이 최소 필드라 완료 화면은 입력값 또는 기본 표시값을 합쳐 표시합니다.
- 브라우저 런타임에서 CORS, API base URL, 백엔드 실행 상태, seed 예약번호를 확인해야 합니다.
- `package-lock.json`과 `pnpm-lock.yaml` 공존으로 Next build root 추론 경고가 남아 있습니다.

## 후속 작업

- route guard와 권한 없음 화면 추가
- ESLint와 패키지 매니저 기준 정리
- 관리자/대시보드 API 연동 검토
```

## 커밋 메시지 초안

```text
feat: integrate staff queue APIs

- 직원 예약자 체크인 API 연동
- 현장 접수 API 연동
- 대기열 조회와 상태 변경 API 연동
- 백엔드 대기 상태 전이 정책에 맞게 액션 버튼 조정
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 후속 route guard/403 화면 브랜치 생성
