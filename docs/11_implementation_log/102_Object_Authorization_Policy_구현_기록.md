# 객체 권한 정책 보강 구현 기록

## 1. 작업 목표

- DC-007 객체 권한 정책 후보를 기준으로 예약/방문/대기/대시보드의 데이터 접근 조건을 점검한다.
- 누락된 예약 객체 권한을 보강한다.
- URL 역할 권한과 서비스/정책 계층 객체 권한이 서로 어긋나지 않게 맞춘다.

## 2. 작업 범위

- [x] `docs/14_deferred_cleanup/01_보류_정리_목록.md`의 DC-007 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md`의 객체 권한 잔여 항목 확인
- [x] 예약 상세 조회 객체 권한 확인
- [x] 예약 취소 객체 권한 보강
- [x] 예약 생성 시 STAFF/ADMIN 보건소 슬롯 권한 보강
- [x] 방문 체크인/현장 접수 객체 권한 확인
- [x] 대기열 조회/상태 변경 객체 권한 확인
- [x] 대시보드 관리자 보건소 범위 확인
- [x] 전체 체크리스트와 보류 목록 갱신
- [x] PR 문서 초안 작성

제외한다.

- [ ] 다중 보건소 관리자 기준정보 API 전면 개편
- [ ] `DEFAULT_HEALTH_CENTER_ID` 제거
- [ ] 서비스/정책 계층 `BusinessException` 전면 교체
- [ ] 자동화 테스트 추가

## 3. 영향 범위 확인

GitNexus 상태:

- `gitnexus status` 결과 인덱스가 stale 상태였다.
- `gitnexus impact --repo health-center-smart-reservation ReservationCancelPolicy` 실행 결과 risk `LOW`, 직접 영향 1개(`ReservationCommandService.java`), 간접 영향 1개(`ReservationController.java`)로 확인했다.
- `gitnexus impact --repo health-center-smart-reservation ReservationCommandService` 실행 결과 risk `LOW`, 직접 영향 1개(`ReservationController.java`)로 확인했다.
- `gitnexus impact --repo health-center-smart-reservation SecurityConfig` 실행 결과 risk `LOW`, upstream 영향 0개로 확인했다.
- `gitnexus impact --repo health-center-smart-reservation ReservationController` 실행 결과 risk `LOW`, upstream 영향 0개로 확인했다.

Blast radius:

| 대상 | 직접 영향 | 위험도 |
|---|---|---|
| `ReservationCommandService` | 예약 생성 API | LOW |
| `ReservationCancelPolicy` | 예약 취소 API | LOW |
| `ReservationController` | Swagger 설명 문구 | LOW |
| `SecurityConfig` | 예약 취소 URL 역할 권한 | LOW |

## 4. 기존 권한 확인 결과

| 영역 | 확인 결과 |
|---|---|
| 예약 상세 조회 | 예약자 본인 또는 같은 보건소 `STAFF/ADMIN`만 조회 가능 |
| 예약 취소 | 기존에는 예약자 본인 또는 같은 보건소 `ADMIN`만 가능했고 URL 권한도 `STAFF`가 빠져 있었다. 이번 브랜치에서 같은 보건소 `STAFF/ADMIN`으로 정렬 |
| 예약 생성 | `STAFF/ADMIN`이 다른 보건소 슬롯으로 예약할 수 있는 여지가 있어 같은 보건소 슬롯만 허용하도록 보강 |
| 방문 체크인 | 같은 보건소 `STAFF/ADMIN`만 예약 체크인 가능 |
| 현장 접수 | 같은 보건소 업무 유형에 대해서만 `STAFF/ADMIN` 접수 가능 |
| 대기열 조회/상태 변경 | principal의 보건소 기준으로 조회하고, 상태 변경 전 대기표 보건소 일치 검사 수행 |
| 대시보드 | 관리자 principal의 보건소 ID로 관리자 지표 조회 |
| 현재 혼잡도 | 사용자 공개 API라 `healthCenterId` 파라미터 또는 기본 보건소 기준 조회 유지 |

## 5. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `backend/src/main/java/egovframework/healthcenter/reservation/application/ReservationCommandService.java` | `STAFF/ADMIN`이 다른 보건소 예약 슬롯으로 예약을 생성하지 못하도록 검사 추가 |
| `backend/src/main/java/egovframework/healthcenter/reservation/policy/ReservationCancelPolicy.java` | 예약 취소 권한을 예약자 본인 또는 같은 보건소 `STAFF/ADMIN`으로 정렬 |
| `backend/src/main/java/egovframework/healthcenter/reservation/api/ReservationController.java` | 예약 취소 Swagger 설명을 직원/관리자 정책으로 갱신 |
| `backend/src/main/java/egovframework/com/security/SecurityConfig.java` | `DELETE /api/reservations/{id}` URL 권한에 `STAFF` 추가 |
| `docs/14_deferred_cleanup/01_보류_정리_목록.md` | DC-007 상태와 처리 결과 갱신 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 객체 권한 정책 보강 항목 완료 반영 |

## 6. 검증 체크리스트

- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `git diff --check`
- [ ] Swagger 런타임 대표 권한 확인

## 7. Swagger 확인 안내

서버 기동과 Swagger 확인은 사용자가 직접 수행한다.

대표 예시:

```text
DELETE /api/reservations/{reservationId}
Authorization: Bearer {staffAccessToken}
Expected: 200 또는 정책 조건에 따른 403
```

확인 기준:

- 같은 보건소 STAFF/ADMIN은 예약 취소 가능
- 다른 보건소 STAFF/ADMIN은 예약 생성/취소 불가
- CITIZEN/GUARDIAN은 본인 예약만 취소 가능

추가 Happy/Edge/Bad 후보:

- Happy: CITIZEN 본인 예약 취소 성공
- Happy: 같은 보건소 STAFF 예약 취소 성공
- Bad: 다른 보건소 STAFF 예약 취소 실패
- Bad: STAFF가 다른 보건소 예약 슬롯으로 예약 생성 실패

## 8. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 객체 권한 점검 중 | 관리자 기준정보 API의 `DEFAULT_HEALTH_CENTER_ID` 제거 검토 | 현재 MVP는 단일 보건소 기준이라 동작하지만 다중 보건소 확장 시 admin principal의 보건소 ID를 주입해야 한다. | 후속 고도화 후보로 유지 |
| 객체 권한 점검 중 | 객체 권한 자동화 테스트 추가 | Swagger 수동 확인 외에 권한 회귀를 막으려면 역할별 테스트가 필요하다. | 후속 테스트 후보로 유지 |

## 9. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] 보류 목록 갱신
- [x] PR 문서 작성
- [x] 빌드와 테스트 컴파일 확인
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리

