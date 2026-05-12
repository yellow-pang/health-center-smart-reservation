# Frontend API Integration Base Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/frontend-api-integration` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 문서 작성 전 기준 clean |
| 주요 커밋 | `c9640ca`, `38c5afd` |
| 빌드 확인 | `npm.cmd run build` 통과 |
| 타입 확인 | `npm.cmd exec -- tsc --noEmit` 통과 |
| lint 확인 | 실패. `eslint` 실행 파일 없음 |
| 실행/API 확인 | 미수행. 사용자가 브라우저와 백엔드 런타임으로 확인 필요 |

## PR 제목

```text
feat: 프론트엔드 API 연동 기반 추가
```

## PR 본문

```markdown
## 개요

v0로 생성한 프론트엔드 화면을 실제 백엔드 API와 연결하기 전에 필요한 기반 작업을 추가합니다.

이번 PR에서는 두 가지를 진행했습니다.

- mock type과 mock data를 백엔드 API 계약에 맞게 정렬
- `NEXT_PUBLIC_API_BASE_URL` 기반 공통 API client 추가

화면별 실제 API 연동은 후속 PR에서 작은 단위로 진행합니다.

## 변경 내용

### mock 계약 정렬

- 업무 유형 mock을 `serviceTypeId`, `code`, `active`, `defaultCapacity` 중심으로 정리
- 예약 슬롯 mock을 `slotId`, `startTime`, `endTime`, `reservedCount`, `availableCount`, `available` 중심으로 정리
- 예약 mock을 `reservationId`, `reservationNo`, `RESERVED` 상태 기준으로 정리
- 대기열 mock을 `queueTicketId`, `ticketNumber`, `visitorNameMasked`, `visitorPhoneMasked` 중심으로 정리
- 혼잡도 상태를 `MEDIUM`에서 백엔드 기준 `NORMAL`로 정리
- 시민, 직원, 관리자 화면의 mock 필드 참조 수정

### API client 추가

- `frontend/src/lib/api-client.ts` 추가
- 공통 응답 타입 `{ success, data, error }` 정의
- `ApiClientError` 정의
- `NEXT_PUBLIC_API_BASE_URL` 기반 API URL 생성
- query/body 처리 추가
- access token localStorage helper 추가
- `Authorization: Bearer {token}` header 처리 추가
- 401 응답 시 token 제거 후 `/login` 이동 처리
- `frontend/.env.example`에 로컬 API base URL 예시 추가

## 검증

- [x] `npm.cmd exec -- tsc --noEmit`
- [x] `npm.cmd run build`
- [x] `git diff --check`
- [ ] `npm.cmd run lint`
- [ ] 브라우저 화면 확인
- [ ] 실제 API 호출 확인

## 미검증 사유

- `npm.cmd run lint`는 `eslint` 실행 파일이 없어 실패했습니다.
- 이번 PR은 API client 기반 추가까지만 포함하며, 화면별 실제 API 호출 교체는 후속 PR에서 진행합니다.
- 서버 기동, 브라우저 확인, 백엔드 API 런타임 호출은 프로젝트 운영 기준상 사용자가 직접 수행합니다.

## 사용자가 직접 확인할 항목

- `cd frontend`
- `npm run build`
- `npm run dev`
- `/login`, `/citizen/reservations/new`, `/staff/queues`, `/admin/dashboard` 화면이 기존 mock 기준으로 열리는지 확인
- 후속 API 연동 시 `.env.local`에 `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080` 설정

## 남은 위험

- `package-lock.json`과 `pnpm-lock.yaml`이 함께 있어 Next build에서 workspace root 추론 경고가 발생합니다.
- `eslint` 실행 파일이 없어 lint 검증을 완료하지 못했습니다.
- `next build`와 `tsc --noEmit` 후 `next-env.d.ts`, `tsconfig.tsbuildinfo` 같은 산출물이 작업트리에 남을 수 있어 ignore/설정 정리가 필요합니다.
- API client는 아직 화면에서 사용하지 않으므로 실제 401/403/토큰 만료 흐름은 후속 연동 시 검증해야 합니다.

## 후속 작업

- 로그인과 현재 사용자 API 연동
- 업무 유형과 예약 슬롯 조회 API 연동
- 시민 예약 신청/내 예약/취소 API 연동
- 직원 체크인/현장 접수/대기열 API 연동
- route guard와 권한 없음 화면 추가
- ESLint와 패키지 매니저 기준 정리
```

## 커밋 메시지 초안

이번 브랜치는 이미 아래 커밋 2개로 나누어 진행했다.

```text
refactor: 프론트 mock 계약을 백엔드 API 기준으로 정렬
feat: 프론트엔드 API 클라이언트 기반 추가
```

문서 커밋을 별도로 만든다면:

```text
docs: 프론트엔드 API 연동 기반 작업 기록 추가

- mock 계약 정렬과 API client 추가 작업 기록 작성
- PR 작성안과 검증/미검증 항목 정리
- 전체 체크리스트에 프론트 API 연동 기반 진행 상태 반영
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 후속 브랜치 생성
- [ ] 로그인/내 정보 API 연동 착수
