# 관리자 대기 마감 UX Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/close-pending-tickets` |
| base 브랜치 | `dev` |
| 작업 트리 | 관리자 대기 마감 UX 및 날짜 조회 보강 |
| 주요 커밋 | 커밋 전 |
| 실행/API 확인 | 서버 기동, Docker, Swagger 런타임 호출, 브라우저 확인은 사용자 직접 수행 |

## PR 제목

```text
feat: 관리자 대기 마감 화면 추가
```

## PR 본문

## 개요

관리자가 영업일 마감 전에 미처리 대기표를 확인하고 `NO_SHOW` 일괄 처리를 실행할 수 있는 화면을 추가합니다.

기존 마감 API는 날짜를 받을 수 있으므로, 마감 전 확인 목록도 같은 날짜 기준으로 조회되도록 `GET /api/queues`에 `date` query를 보강했습니다.

## 변경 내용

- 대기열 조회 API 날짜 필터 보강
  - `GET /api/queues?date=YYYY-MM-DD`
  - `date` 생략 시 오늘 기준 유지
- 관리자 대기 마감 관리 화면 추가
  - `/admin/queue-closing`
  - 날짜 선택
  - 미처리 대기표 요약 카드
  - 마감 대상 목록
  - 확인 다이얼로그 후 마감 실행
- 관리자 사이드바 메뉴 추가
  - `대기 마감 관리`
- 문서 갱신
  - API 명세 보강
  - 전체 체크리스트 갱신
  - 보류 목록 DC-026 상태 갱신

## 검증

- [x] `git diff --check`
- [x] `npm.cmd run build`
- [ ] `npm.cmd exec -- tsc --noEmit`
- [ ] `mvn.cmd -q -DskipTests compile`
- [ ] `mvn.cmd -q test-compile`
- [ ] 브라우저 `/admin/queue-closing` 확인
- [ ] Swagger `Try it out`: `GET /api/queues?date=2026-05-22`
- [ ] Swagger `Try it out`: `POST /api/queues/admin/close-pending?date=2026-05-22`

## 추가 테스트 체크리스트

- [ ] Happy: 관리자 화면에서 마감 대상 대기표가 목록으로 표시된다.
- [ ] Happy: 마감 실행 후 `NO_SHOW` 처리 건수가 토스트로 표시되고 목록이 재조회된다.
- [ ] Edge: 마감 대상이 없으면 마감 처리 버튼이 비활성화된다.
- [ ] Edge: 날짜 변경 시 선택 날짜 기준으로 목록이 바뀐다.
- [ ] Bad: API 실패 시 오류 토스트가 표시되고 기존 목록을 유지한다.

## 미검증 사유

- 서버 기동, Docker 실행, Swagger 런타임 호출, 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행한다.
- `npm.cmd exec -- tsc --noEmit`은 `.next/dev/types/validator.ts`가 제거된 과거 `/app/social-login/page.js`를 참조해 실패했다.
- Maven은 현재 세션 PATH에서 `mvn.cmd`/`mvn` 실행 파일을 찾지 못해 미수행했다.

## 후속 작업

- 운영 시간/예외 기준 확정 후 스케줄러 자동 마감 배치 검토

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 자동 마감 배치 필요 여부 별도 결정

## 커밋 메시지 초안

제목:

```text
feat: 관리자 대기 마감 화면 추가
```

본문:

```text
- 관리자 대기 마감 관리 화면 추가
- 선택 날짜 기준 미처리 대기표 조회와 마감 API 실행 연결
- 대기열 조회 API 날짜 필터와 관련 문서 갱신
```
