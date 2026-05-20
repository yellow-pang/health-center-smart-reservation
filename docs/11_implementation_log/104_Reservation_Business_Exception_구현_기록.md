# 예약 도메인 비즈니스 예외 명시화 구현 기록

## 1. 작업 목표

- 예약 도메인의 주요 `IllegalArgumentException`을 `BusinessException(ErrorCode.X)`로 교체한다.
- 메시지 기반 `ErrorCode.fromMessage()` 의존을 줄인다.
- 기존 `success + data + error` 오류 응답 계약과 HTTP status는 유지한다.

## 2. 작업 범위

- [x] 현재 브랜치와 작업 트리 확인
- [x] 예약 도메인 `IllegalArgumentException` 사용처 확인
- [x] GitNexus impact 확인
- [x] `ReservationCommandService` 예외 교체
- [x] `ReservationQueryService` 예외 교체
- [x] `ReservationSlotCommandService` 예외 교체
- [x] `ReservationSlotQueryService` 예외 교체
- [x] `ReservationCancelPolicy` 예외 교체
- [x] 필요한 예약 도메인 `ErrorCode` 추가
- [x] 보류 목록과 전체 체크리스트 갱신
- [x] PR 문서 초안 작성

제외한다.

- [ ] Visit/Queue/Office 도메인 예외 전환
- [ ] `GlobalExceptionHandler` 구조 변경
- [ ] 응답 JSON 필드 변경
- [ ] 자동화 테스트 추가

## 3. 영향 범위 확인

GitNexus 상태:

- `gitnexus status` 결과 인덱스가 stale 상태였다.
- `gitnexus impact --repo health-center-smart-reservation`로 수정 대상 영향을 확인했다.

Blast radius:

| 대상 | 직접 영향 | 위험도 |
|---|---|---|
| `ReservationCommandService` | `ReservationController` | LOW |
| `ReservationQueryService` | `ReservationController` | LOW |
| `ReservationSlotCommandService` | `ReservationSlotController` | LOW |
| `ReservationSlotQueryService` | `ReservationSlotController` | LOW |
| `ReservationCancelPolicy` | `ReservationCommandService`, 간접 `ReservationController` | LOW |
| `ErrorCode` | upstream 0으로 확인 | LOW |

## 4. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `backend/src/main/java/egovframework/healthcenter/common/exception/ErrorCode.java` | `RESERVATION_FORBIDDEN`, `RESERVATION_SLOT_DUPLICATED` 추가 및 기존 메시지 fallback 매핑 보강 |
| `backend/src/main/java/egovframework/healthcenter/reservation/application/ReservationCommandService.java` | 예약 생성/취소 검증 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/reservation/application/ReservationQueryService.java` | 예약 상세 조회의 인증/존재/권한 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/reservation/application/ReservationSlotCommandService.java` | 관리자 예약 슬롯 생성/수정/비활성화 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/reservation/application/ReservationSlotQueryService.java` | 예약 슬롯 조회 조건 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/reservation/policy/ReservationCancelPolicy.java` | 예약 취소 정책 예외를 `BusinessException`으로 교체 |

## 5. 검증 체크리스트

- [x] 예약 도메인 `IllegalArgumentException` 잔여 사용처 없음 확인
- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `git diff --check`
- [ ] Swagger 대표 오류 응답 확인

## 6. Swagger 확인 안내

서버 기동과 Swagger 확인은 사용자가 직접 수행한다.

대표 예시:

```text
POST /api/reservations
Authorization: Bearer {citizenAccessToken}
Body: 이미 예약된 reservationSlotId로 재요청
Expected: 409
```

기대 응답:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "RESERVATION_DUPLICATED",
    "message": "동일 시간대에 이미 예약이 존재합니다.",
    "traceId": "..."
  }
}
```

추가 확인 후보:

- 없는 예약 상세 조회: `RESERVATION_NOT_FOUND`
- 다른 보건소 또는 타인 예약 상세 조회: `RESERVATION_FORBIDDEN`
- 예약 마감 슬롯 신청: `RESERVATION_SLOT_FULL`
- 중복 예약 슬롯 생성: `RESERVATION_SLOT_DUPLICATED`

## 7. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 예약 예외 전환 중 | Visit/Queue/Office 도메인 예외 전환 | 아직 메시지 기반 `ErrorCode.fromMessage()`에 의존하는 도메인이 남아 있다. | 후속 브랜치 후보로 유지 |
| 예약 예외 전환 중 | 예약 정책 오류 자동화 테스트 추가 | 오류 코드 회귀를 막으려면 대표 Bad case 테스트가 필요하다. | 후속 테스트 후보로 유지 |

## 8. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] 보류 목록 갱신
- [x] PR 문서 작성
- [x] 빌드와 테스트 컴파일 확인
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리

