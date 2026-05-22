# 시민 혼잡도 실제 API 연동 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `dev` |
| 권장 작업 브랜치 | `fix/citizen-congestion-real-api` |
| base 브랜치 | `dev` |
| 작업 트리 | 시민 혼잡도 화면 API 연동 및 문서 수정 |
| 주요 커밋 | 커밋 전 |
| 정적 확인 | `git diff --check` 통과, `npm.cmd run build` 통과 |
| 테스트 확인 | Maven 미수행: 현재 세션에서 `mvn.cmd`/`mvn` 실행 파일 미탐지 |
| 실행/API 확인 | 서버 기동, Docker, Swagger 런타임 호출, 브라우저 확인은 사용자 직접 수행 |

## PR 제목

```text
fix: 시민 혼잡도 화면 실제 API 연동
```

## PR 본문

## 개요

시민 혼잡도 화면이 고정 mock 데이터를 표시해 당일 접수가 없어도 대기 인원과 혼잡도가 보이는 문제를 수정합니다.

백엔드 현재 혼잡도 API는 당일 대기표 기준으로 집계하되, 시민이 체감하는 현장 점유 상태를 반영하기 위해 `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS`를 혼잡도에 포함합니다. 프론트 화면은 실제 `GET /api/congestion/current` 응답으로 교체합니다.

## 변경 내용

- 시민 혼잡도 API client 추가
  - `GET /api/congestion/current`
  - 공개 API이므로 토큰 없이 호출
  - 백엔드 `congestionLevel` 응답을 프론트 `level`로 정규화
- 시민 혼잡도 화면 수정
  - `mock-services`의 `getCongestionInfo()` 사용 제거
  - 실제 API 기준 대기 인원/예상 대기시간 표시
- 혼잡도 집계 기준 보정
  - `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS` 상태를 시민 혼잡도에 포함
  - 미처리 대기표는 현장 접수 후 미처리된 건으로 보고 마감 시 `NO_SHOW` 처리하는 정책 확정
- 문서 갱신
  - 구현 기록 작성
  - 전체 체크리스트 갱신

## 혼잡도 기준

현재 백엔드 기준:

- 대상 날짜: `queue_tickets.issued_at::date = CURRENT_DATE`
- 혼잡도 반영 인원: `status IN ('WAITING', 'CALLED', 'HOLD', 'IN_PROGRESS')`
- 예상 대기시간: `혼잡도 반영 인원 * averageProcessingMinutes`
- 평균 처리시간: 오늘 완료된 대기표 기준, 없으면 5분
- 미처리 대기표: 현장 접수 후 미처리된 건이므로 영업일 마감 시 `NO_SHOW` 처리

## 검증

- [x] `git diff --check`
- [x] `npm.cmd run build`
- [ ] `mvn.cmd -q -DskipTests compile`
- [ ] `mvn.cmd -q test-compile`
- [ ] Swagger `Try it out`: `GET /api/congestion/current?healthCenterId=1`
- [ ] 브라우저 시민 혼잡도 화면에서 실제 API 기준 대기 인원 확인

## 추가 테스트 체크리스트

- [ ] Happy: 당일 `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS` 대기표가 있으면 해당 업무의 혼잡도 반영 인원이 표시된다.
- [ ] Edge: 당일 대기표가 없으면 대기 인원과 예상 시간이 0으로 표시된다.
- [ ] Edge: 과거 날짜의 미처리 대기표가 시민 혼잡도에 포함되지 않는다.
- [ ] Edge: `COMPLETED`, `NO_SHOW`, `CANCELED` 대기표는 시민 혼잡도에 포함되지 않는다.
- [ ] Bad: 백엔드 API 실패 시 오류 상태와 재시도 버튼이 표시된다.

## 미검증 사유

- 서버 기동, Docker 실행, Swagger 런타임 호출, 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행한다.
- Maven은 현재 세션에서 `mvn.cmd`/`mvn` 실행 파일이 PATH에 없어 수행하지 못했다.

## 정책 결정

- 당일 `WAITING` 상태로 남은 접수는 시민 혼잡도에 계속 포함한다.
- `CALLED`, `HOLD`, `IN_PROGRESS` 상태도 시민이 체감하는 현장 점유/대기 흐름으로 보고 시민 혼잡도에 포함한다.
- 영업일 마감 시 미처리 대기표는 현장 접수 후 미처리된 건이므로 `NO_SHOW` 처리한다.

## 후속 작업

- 영업일 마감 시 미처리 대기표를 `NO_SHOW`로 일괄 처리하는 API 또는 배치 추가
- 관리자 화면에 마감 처리 전 미처리 대기표 확인 UX 추가

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 후속 운영 정책 브랜치 생성 여부 결정

## 커밋 메시지 초안

제목:

```text
fix: 시민 혼잡도 화면 실제 API 연동
```

본문:

```text
- 시민 혼잡도 화면의 고정 mock 데이터 사용 제거
- GET /api/congestion/current API client 추가
- 혼잡도 기준 확인 내용과 PR 체크리스트 정리
```
