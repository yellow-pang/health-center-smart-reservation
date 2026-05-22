# 시민 혼잡도 실제 API 연동 구현 기록

## 1. 작업 목표

- 시민 혼잡도 화면이 고정 mock 데이터를 표시해 당일 접수가 없어도 혼잡도가 보이는 문제를 수정한다.
- 백엔드의 현재 혼잡도 산정 기준을 확인하고, 프론트 화면을 `GET /api/congestion/current` 실제 API 기준으로 연결한다.
- 시민 혼잡도에 반영할 대기표 상태와 미처리 대기표 마감 정책을 확정한다.

## 2. 작업 범위

- [x] 현재 혼잡도 백엔드 SQL 기준 확인
- [x] 시민 혼잡도 화면의 mock 데이터 사용 여부 확인
- [x] 시민 혼잡도 API client 추가
- [x] 시민 혼잡도 화면을 실제 API로 교체
- [x] 시민 혼잡도 반영 상태를 `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS`로 확장
- [x] 미처리 대기표는 마감 시 `NO_SHOW` 처리하는 정책으로 문서화
- [x] 전체 체크리스트 갱신
- [x] PR 문서 초안 작성
- [x] 프론트 빌드 확인

제외한다.

- [ ] 미처리 대기표 자동 마감 배치

## 3. 혼잡도 기준 확인

현재 백엔드 `GET /api/congestion/current`는 아래 기준으로 업무별 혼잡도를 계산한다.

| 기준 | 현재 동작 |
|---|---|
| 대상 날짜 | `queue_tickets.issued_at::date = CURRENT_DATE` |
| 혼잡도 반영 인원 | `status IN ('WAITING', 'CALLED', 'HOLD', 'IN_PROGRESS')`인 대기표 포함 |
| 업무 구분 | 활성 업무 유형별 집계 |
| 예상 대기시간 | `혼잡도 반영 인원 * averageProcessingMinutes` |
| 평균 처리시간 | 오늘 완료된 대기표의 `completed_at - started_at`, 없으면 5분 |
| 미처리 대기표 마감 | 현장 접수 후 미처리된 건이므로 영업일 마감 시 `NO_SHOW` 처리 |

따라서 과거 날짜의 대기표가 현재 혼잡도에 그대로 포함되는 구조는 아니다.  
이번 문제는 시민 화면이 실제 API 대신 `mockCongestionInfo`를 반환하는 `getCongestionInfo()`를 사용해서 발생했다.

## 4. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `backend/src/main/resources/egovframework/mapper/healthcenter/dashboard/Dashboard_SQL_postgresql.xml` | 시민 혼잡도 반영 상태를 `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS`로 확장 |
| `backend/src/main/java/egovframework/healthcenter/dashboard/dto/CongestionResponse.java` | `waitingCount` 설명을 현재 혼잡도 반영 인원으로 보정 |
| `frontend/src/lib/dashboard-api.ts` | `getCurrentCongestion()` 추가, `congestionLevel` 응답을 프론트 `CongestionInfo.level`로 정규화 |
| `frontend/app/citizen/congestion/page.tsx` | `mock-services` 대신 실제 `GET /api/congestion/current` 호출 |
| `docs/04_api/01_API_명세서.md` | 혼잡도 반영 상태와 미처리 대기표 NO_SHOW 정책 반영 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 시민 혼잡도 mock 제거와 실제 API 연동 상태 반영 |

## 5. Swagger 대표 예시

서버 실행 후 Swagger `Try it out`에서 아래 요청을 확인한다.

```text
GET /api/congestion/current?healthCenterId=1
```

기대 결과:

- `success: true`
- 당일 `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS` 대기표가 없으면 각 업무의 `waitingCount`와 `estimatedWaitMinutes`가 0으로 조회된다.
- 당일 `WAITING`, `CALLED`, `HOLD`, `IN_PROGRESS` 대기표가 있으면 해당 업무의 혼잡도 반영 인원과 혼잡도가 증가한다.

## 6. 검증 체크리스트

- [x] `git diff --check`
- [x] `npm.cmd run build`
- [ ] `mvn.cmd -q -DskipTests compile`
- [ ] `mvn.cmd -q test-compile`
- [ ] Swagger `GET /api/congestion/current?healthCenterId=1` 확인
- [ ] 브라우저 시민 혼잡도 화면에서 실제 API 기준 대기 인원 확인

참고:

- 이번 변경은 프론트 API 연결 수정이므로 서버 기동, Docker 실행, Swagger 런타임 호출, 브라우저 확인은 사용자가 직접 수행한다.
- Maven은 현재 세션에서 `mvn.cmd`/`mvn` 실행 파일이 PATH에 없어 수행하지 못했다.

## 7. 정책 결정

| 항목 | 결정 |
|---|---|
| 당일 `WAITING` 상태로 남은 접수 | 시민 혼잡도에 계속 포함한다. |
| `CALLED`, `HOLD`, `IN_PROGRESS` 상태 | 시민이 체감하는 현장 점유/대기 흐름으로 보고 혼잡도에 포함한다. |
| 영업일 마감 시 미처리 대기표 | 현장 접수 후 미처리된 건이므로 `NO_SHOW` 처리한다. 자동 마감 배치/API 구현은 후속 운영 정책 브랜치에서 진행한다. |

## 8. 브랜치 종료 전 체크리스트

- [x] 원인 확인 완료
- [x] 코드 수정 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 가능한 정적 검증과 빌드 확인
- [x] 확인 질문 정리
- [ ] 커밋 메시지 정리
