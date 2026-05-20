# 보건소 도메인 비즈니스 예외 명시화 구현 기록

## 1. 작업 목표

- 보건소 주요 서비스/정책 계층의 `IllegalArgumentException`을 `BusinessException(ErrorCode.X)`로 교체한다.
- 메시지 기반 `ErrorCode.fromMessage()` 의존을 줄이고 오류 코드를 명시화한다.
- 기존 `success + data + error` 오류 응답 계약과 HTTP status는 유지한다.

## 2. 작업 범위

- [x] 현재 브랜치와 작업 트리 확인
- [x] 예약 도메인 `IllegalArgumentException` 사용처 확인 및 교체
- [x] 방문 도메인 `IllegalArgumentException` 사용처 확인 및 교체
- [x] 대기 도메인 `IllegalArgumentException` 사용처 확인 및 교체
- [x] 오피스 도메인 `IllegalArgumentException` 사용처 확인 및 교체
- [x] 대시보드, 인증, 소셜 로그인 잔여 사용처 확인 및 교체
- [x] GitNexus impact 확인
- [x] 필요한 `ErrorCode` 추가
- [x] 전체 체크리스트 갱신
- [x] 보류 목록 갱신
- [x] PR 문서 초안 작성

제외한다.

- [ ] `GlobalExceptionHandler` 구조 변경
- [ ] 응답 JSON 필드 변경
- [ ] `IllegalStateException` 기반 내부 시스템 오류 전환
- [ ] 자동화 테스트 추가

## 3. 영향 범위 확인

GitNexus 상태:

- `gitnexus status` 결과 인덱스가 stale 상태였다.
- `gitnexus impact --repo health-center-smart-reservation`로 수정 대상 영향을 확인했다.
- `SocialLoginService`는 GitNexus 인덱스에서 심볼을 찾지 못해 `rg`와 정적 diff 확인으로 보완했다.
- `gitnexus detect-changes`는 현재 설치 CLI에 없는 명령이라 `git status`, `git diff --stat`, `git diff --check`, `rg`, Maven compile/test-compile로 변경 범위를 확인했다.

Blast radius:

| 대상 | 직접 영향 | 위험도 |
|---|---|---|
| `ReservationCommandService` | `ReservationController` | LOW |
| `ReservationQueryService` | `ReservationController` | LOW |
| `ReservationSlotCommandService` | `ReservationSlotController` | LOW |
| `ReservationSlotQueryService` | `ReservationSlotController` | LOW |
| `ReservationCancelPolicy` | `ReservationCommandService`, 간접 `ReservationController` | LOW |
| `VisitCommandService` | `VisitController` | LOW |
| `VisitCheckInPolicy` | `VisitCommandService`, 간접 `VisitController` | LOW |
| `VisitWalkInPolicy` | `VisitCommandService`, 간접 `VisitController` | LOW |
| `QueueCommandService` | `QueueController` | LOW |
| `QueueTicketPolicy` | `QueueCommandService`, 간접 `QueueController` | LOW |
| `OfficeCommandService` | `ServiceTypeController`, `AdminOfficeController` | LOW |
| `DashboardQueryService` | `DashboardController`, `CongestionController` | LOW |
| `AuthCommandService` | `AuthController` | LOW |
| `ErrorCode` | upstream 0으로 확인 | LOW |

## 4. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `backend/src/main/java/egovframework/healthcenter/common/exception/ErrorCode.java` | 예약, 방문, 대기, 대시보드, 소셜 로그인 관련 명시적 오류 코드와 fallback 매핑 보강 |
| `backend/src/main/java/egovframework/healthcenter/reservation/application/ReservationCommandService.java` | 예약 생성/취소 검증 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/reservation/application/ReservationQueryService.java` | 예약 상세 조회의 인증/존재/권한 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/reservation/application/ReservationSlotCommandService.java` | 관리자 예약 슬롯 생성/수정/비활성화 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/reservation/application/ReservationSlotQueryService.java` | 예약 슬롯 조회 조건 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/reservation/policy/ReservationCancelPolicy.java` | 예약 취소 정책 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/visit/application/VisitCommandService.java` | 체크인/현장 접수 입력 검증 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/visit/policy/VisitCheckInPolicy.java` | 체크인 존재/권한/상태 정책 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/visit/policy/VisitWalkInPolicy.java` | 현장 접수 권한/입력 정책 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/queue/application/QueueCommandService.java` | 대기 상태 전이 실패 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/queue/policy/QueueTicketPolicy.java` | 대기표 인증/권한/상태 정책 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/office/application/OfficeCommandService.java` | 업무 유형, 직원, 창구 기준정보 검증 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/dashboard/application/DashboardQueryService.java` | 대시보드 인증/권한/보건소 ID 검증 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/member/application/AuthCommandService.java` | 로그인, 토큰 재발급, 로그아웃 예외를 `BusinessException`으로 교체 |
| `backend/src/main/java/egovframework/healthcenter/member/application/SocialLoginService.java` | 소셜 회원가입 토큰, 이메일 중복, provider 검증 예외를 `BusinessException`으로 교체 |

## 5. 검증 체크리스트

- [x] 보건소 도메인 `IllegalArgumentException` throw 잔여 사용처 없음 확인
- [x] `GlobalExceptionHandler`의 레거시 `IllegalArgumentException` 핸들러 유지 확인
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

- 방문 중복 체크인: `VISIT_ALREADY_CHECKED_IN`
- 대기표 잘못된 상태 전이: `QUEUE_INVALID_STATUS`
- 오피스 중복 업무 코드 생성: `SERVICE_TYPE_DUPLICATED`
- 관리자 아닌 사용자의 대시보드 조회: `DASHBOARD_FORBIDDEN`
- 소셜 회원가입 완료 토큰 오류: `SOCIAL_SIGNUP_TOKEN_INVALID`

## 7. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 예약 예외 전환 중 | Visit/Queue/Office 도메인 예외 전환 | 메시지 기반 `ErrorCode.fromMessage()` 의존 사용처가 남아 있었다. | 같은 브랜치에서 단계별로 교체 완료 |
| 잔여 검색 중 | Dashboard/Auth/Social 로그인 예외 전환 | 보건소 도메인 하위에 남은 `IllegalArgumentException` throw 사용처가 확인되었다. | 같은 브랜치에서 교체 완료 |
| 예외 전환 중 | 정책 오류 자동화 테스트 추가 | 오류 코드 회귀를 막으려면 대표 Bad case 테스트가 필요하다. | 후속 테스트 후보로 유지 |

## 8. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] 보류 목록 갱신
- [x] PR 문서 작성
- [x] 빌드와 테스트 컴파일 확인
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
