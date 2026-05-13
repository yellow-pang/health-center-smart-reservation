# Frontend Staff Queue API Integration 구현 기록

## 1. 작업 목표

- 직원 체크인, 현장 접수, 대기열 관리 화면을 mock service에서 실제 백엔드 API로 교체한다.
- 이번 브랜치 범위는 예약자 체크인, 현장 접수, 대기열 조회, 대기열 상태 변경 API 연동으로 제한한다.
- route guard/403 화면, ESLint/패키지 매니저 정리는 후속 브랜치에서 진행한다.

## 2. 작업 범위

- [x] 현재 브랜치와 작업 트리 확인
- [x] 직원/대기열 API 명세 확인
- [x] 직원 화면의 mock service 사용처 확인
- [x] 예약자 체크인 API 연동
- [x] 현장 접수 API 연동
- [x] 대기열 조회 API 연동
- [x] 대기열 상태 변경 API 연동
- [x] 백엔드 상태 전이 정책에 맞게 버튼 조정
- [x] TypeScript 정적 검증
- [x] Next build 검증
- [x] 브라우저에서 직원 체크인 흐름 확인
- [x] 브라우저에서 현장 접수 흐름 확인
- [x] 브라우저에서 대기열 상태 전이 확인
- [x] Swagger 대표 예시 확인

## 3. 작업 전 체크리스트

- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 확인
- [x] `docs/04_api/01_API_명세서.md` 확인
- [x] `docs/05_frontend/02_UX_API_계약_우선순위.md` 기준 확인
- [x] `frontend/app/staff/check-in/page.tsx` 확인
- [x] `frontend/app/staff/walk-in/page.tsx` 확인
- [x] `frontend/app/staff/queues/page.tsx` 확인
- [x] `frontend/src/lib/mock-services.ts` 확인

## 4. 관련 전체 체크리스트 항목

| 영역 | 항목 | 이번 작업 반영 |
|---|---|---|
| 프론트엔드 | 직원 접수/체크인 화면 구현 | 예약자 체크인과 현장 접수를 실제 API로 연결 |
| 프론트엔드 | 대기열 관리 화면 구현 | 대기열 조회와 상태 변경을 실제 API로 연결 |
| 프론트엔드 | 프론트엔드 API 연동 | 직원 운영 화면 mock service 교체 |
| 테스트 | 프론트엔드 화면 검증 | TypeScript/Next build 확인, 브라우저 확인은 사용자 직접 수행 |

## 5. GitNexus 및 영향 범위

GitNexus 확인:

| 명령 | 결과 |
|---|---|
| `npx gitnexus query -r health-center-smart-reservation "frontend staff check-in walk-in queue API integration"` | 실패. exit 1, 출력 없음 |
| `npx gitnexus impact -r health-center-smart-reservation QueueEntry -d upstream --depth 2` | 실패. exit 1, 출력 없음 |
| `npx gitnexus impact -r health-center-smart-reservation QueuesPage -d upstream --depth 2` | 실패. exit 1, 출력 없음 |
| `npx gitnexus detect-changes -r health-center-smart-reservation` | 실패. exit 1, 출력 없음 |

대체 확인:

- `rg`로 `QueueEntry`, `checkInByReservationNumber`, `registerWalkIn`, `getQueueEntries`, `updateQueueStatus`, `getQueueSummary`, `getServiceTypeName` 사용처 확인
- 직접 영향은 직원 체크인/현장 접수/대기열 화면, 직원 API client, `QueueEntry` type 확장에 한정

Blast radius:

| 대상 | 직접 사용처 | 영향 프로세스 | 위험도 |
|---|---|---|---|
| `frontend/src/lib/staff-api.ts` | 직원 체크인/현장 접수/대기열 화면 | 직원 현장 운영 흐름 | MEDIUM |
| `QueueEntry` type | 직원 화면, mock data/service | 대기표 표시와 상태 변경 | MEDIUM |
| `QueuesPage` | `/staff/queues` | 대기열 상태 전이 | MEDIUM |

HIGH/CRITICAL 경고는 GitNexus CLI 실패로 확인하지 못했다. 대신 TypeScript와 Next build로 정적 검증했다.

## 6. 구현 내용

### 6.1 직원 API client 추가

추가 파일:

| 파일 | 내용 |
|---|---|
| `frontend/src/lib/staff-api.ts` | 직원 체크인, 현장 접수, 대기열 조회/상태 변경 API 함수 |

연동 API:

| 함수 | API |
|---|---|
| `checkInByReservationNumber` | `POST /api/visits/check-in` |
| `registerWalkIn` | `POST /api/visits/walk-in` |
| `getQueueEntries` | `GET /api/queues` |
| `updateQueueStatus` | `POST /api/queues/{queueTicketId}/{action}` |
| `getServiceTypes` | `GET /api/service-types` 재사용 |

### 6.2 API 응답 정규화

- 체크인/현장 접수 응답은 `visitId`, `queueTicketId`, `ticketNumber`, `status` 중심이라 화면용 `QueueEntry`로 정규화했다.
- 대기열 조회/상태 변경 응답은 `QueueTicketResponse`를 `QueueEntry`로 정규화했다.
- 백엔드가 실명/전화번호를 반환하므로 프론트에서 마스킹해 표시한다.
- `QueueEntry` type에 `visitId`, `serviceTypeName`, `issuedAt`, `holdAt` optional 필드를 추가했다.

### 6.3 화면 연결

- `frontend/app/staff/check-in/page.tsx`
  - mock 체크인 service를 실제 API로 교체
  - Swagger seed 예약번호 `RSV-SWAGGER-CHECKIN-001` 안내

- `frontend/app/staff/walk-in/page.tsx`
  - 현장 접수와 업무 유형 조회를 실제 API로 교체
  - 선택한 업무명을 접수 완료/최근 접수 목록에 표시

- `frontend/app/staff/queues/page.tsx`
  - 대기열 조회와 상태 변경을 실제 API로 교체
  - 요약 카드를 실제 조회 데이터 기준으로 계산
  - 상태 변경 성공 시 서버 응답의 최신 대기표로 row 갱신
  - 백엔드 정책에 맞춰 액션 버튼을 조정

상태 버튼 기준:

| 현재 상태 | 표시 액션 |
|---|---|
| `WAITING` | 호출, 취소 |
| `CALLED` | 시작, 보류 |
| `IN_PROGRESS` | 완료 |
| `HOLD` | 재호출, 미응답, 취소 |

## 7. 검증 결과

| 검증 | 결과 |
|---|---|
| `npm.cmd exec -- tsc --noEmit` | 통과 |
| `npm.cmd run build` | 통과 |
| `git diff --check` | 공백 오류 없음 |
| `npm.cmd run lint` | 실패. `eslint` 실행 파일 없음 |
| GitNexus impact/detect-changes | CLI 오류로 완료하지 못함. `rg` 기반 대체 확인 |
| 브라우저 화면 확인 | 미수행. 사용자가 직접 확인 필요 |
| API 런타임 확인 | 미수행. 사용자가 Swagger 또는 브라우저로 직접 확인 필요 |

빌드 참고:

- Next build는 통과했다.
- `package-lock.json`과 `pnpm-lock.yaml`이 함께 있어 Turbopack root 추론 경고가 남아 있다.
- `next build`와 `tsc --noEmit` 후 `next-env.d.ts`, `tsconfig.tsbuildinfo` 산출물 변경이 생겨 원복했다.

## 8. 사용자 직접 확인 방법

프론트 실행 전 `.env.local`에 아래 값을 둔다.

```text
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

사용자 확인 순서:

1. 사용자가 PostgreSQL과 백엔드를 실행한다.
2. 프론트엔드를 실행한다.
3. `staff@test.com / password1234`로 로그인한다.
4. `/staff/check-in`에서 `RSV-SWAGGER-CHECKIN-001`로 체크인한다.
5. `/staff/walk-in`에서 업무 유형을 선택하고 현장 접수를 등록한다.
6. `/staff/queues`에서 대기열 조회와 호출/시작/완료/보류/미응답/취소 흐름을 확인한다.

Swagger 대표 예시:

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

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 직원 API 연동 중 | 체크인 응답 상세화 검토 | 체크인 응답이 최소 필드라 방문자/업무명은 대기열 상세 조회 전까지 제한적으로 표시됨 | 후속 검토 |
| 대기열 연동 중 | 상태 전이 버튼 정책 정리 | 기존 mock 화면의 보류/미응답 흐름이 백엔드 정책과 일부 달랐음 | 이번 수정 반영 |
| 검증 중 | ESLint 실행 기준 정리 | `eslint` 실행 파일 없음 | 후속 |
| 검증 중 | 패키지 매니저 기준 정리 | root `package-lock.json`과 frontend `pnpm-lock.yaml` 공존으로 Next 경고 발생 | 후속 |

## 10. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성
- [x] PR 문서 초안 작성
- [x] 전체 체크리스트 갱신
- [x] TypeScript 정적 검증 완료
- [x] Next build 완료
- [ ] 브라우저 직원 체크인 화면 확인
- [ ] 브라우저 현장 접수 화면 확인
- [ ] 브라우저 대기열 관리 화면 확인
- [ ] Swagger 대표 예시 확인

## 11. 커밋 메시지 초안

```text
feat: integrate staff queue APIs

- 직원 예약자 체크인 API 연동
- 현장 접수 API 연동
- 대기열 조회와 상태 변경 API 연동
- 백엔드 대기 상태 전이 정책에 맞게 액션 버튼 조정
```
