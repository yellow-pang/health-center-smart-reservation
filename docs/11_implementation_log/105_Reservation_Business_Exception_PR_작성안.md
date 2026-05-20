# 보건소 도메인 비즈니스 예외 명시화 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/reservation-business-exceptions` |
| base 브랜치 | `dev` |
| 작업 트리 | 보건소 주요 도메인 `BusinessException` 전환 완료 |
| GitNexus | stale 상태였으나 `impact --repo health-center-smart-reservation`로 주요 수정 대상 영향 확인. 대부분 LOW, `SocialLoginService`는 인덱스 미탐지로 정적 확인 보완 |
| 정적 확인 | `mvn.cmd -q -DskipTests compile`, `mvn.cmd -q test-compile`, `git diff --check` 통과 |
| 실행/API 확인 | 서버 기동, Docker 실행, Swagger 런타임 호출은 사용자가 직접 확인 |

## PR 제목

```text
refactor: 보건소 도메인 예외 코드를 명시화
```

## PR 본문

```markdown
## 개요

보건소 주요 서비스/정책 계층의 `IllegalArgumentException`을 `BusinessException(ErrorCode.X)`로 교체해 메시지 기반 오류 코드 매핑 의존을 줄입니다.

응답 형식은 기존 `success + data + error` 구조를 유지하며, 예약/방문/대기/오피스/대시보드/인증/소셜 로그인 오류 코드가 더 명시적으로 내려가도록 정리합니다.

## 변경 내용

- 예약 생성/조회/취소/예약 슬롯 관리 예외를 `BusinessException`으로 교체
- 방문 체크인과 현장 접수 정책 예외를 `BusinessException`으로 교체
- 대기 상태 전이와 대기표 정책 예외를 `BusinessException`으로 교체
- 오피스 업무 유형, 직원, 창구 기준정보 예외를 `BusinessException`으로 교체
- 대시보드 인증/권한/요청 검증 예외를 `BusinessException`으로 교체
- 로그인, 토큰 재발급, 로그아웃, 소셜 회원가입 검증 예외를 `BusinessException`으로 교체
- 예약, 방문, 대기, 대시보드, 소셜 로그인 관련 `ErrorCode` 보강
- `GlobalExceptionHandler`의 `IllegalArgumentException` handler는 레거시 fallback으로 유지

## 검증

- [x] 보건소 도메인 `IllegalArgumentException` throw 잔여 사용처 없음 확인
- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `git diff --check`
- [ ] Swagger 대표 오류 응답 확인

## Swagger 대표 확인

```http
POST /api/reservations
Authorization: Bearer {citizenAccessToken}
Content-Type: application/json

{
  "serviceTypeId": 1,
  "reservationSlotId": 1,
  "visitorName": "홍길동",
  "visitorPhone": "010-1234-5678"
}
```

이미 같은 슬롯에 예약된 사용자가 다시 호출한다.

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

## 추가 테스트 체크리스트

- [ ] 방문 중복 체크인 시 `VISIT_ALREADY_CHECKED_IN` 응답 확인
- [ ] 대기표 잘못된 상태 전이 시 `QUEUE_INVALID_STATUS` 응답 확인
- [ ] 업무 유형 중복 생성 시 `SERVICE_TYPE_DUPLICATED` 응답 확인
- [ ] 관리자 아닌 사용자의 대시보드 조회 시 `DASHBOARD_FORBIDDEN` 응답 확인
- [ ] 소셜 회원가입 완료 토큰 오류 시 `SOCIAL_SIGNUP_TOKEN_INVALID` 응답 확인

## 미검증 사유

- 서버 기동, Docker 실행, Swagger 런타임 호출은 프로젝트 운영 기준상 사용자가 직접 확인합니다.

## 후속 작업

- 예약/방문/대기/권한 정책 Bad case 자동화 테스트 추가
- `GlobalExceptionHandler`의 `IllegalArgumentException` fallback 제거 가능 시점 재검토
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 다음 보류 항목 브랜치 생성 여부 결정

## 커밋 메시지 초안

제목:

```text
docs: 보건소 도메인 예외 전환 기록 정리
```

본문:

```text
- BusinessException 점진 교체 범위를 브랜치 구현 기록에 반영
- Reservation, Visit, Queue, Office, Dashboard, Auth, Social 예외 전환 결과 정리
- Swagger 대표 오류 응답과 추가 테스트 체크리스트 보강
- 전체 체크리스트와 보류 목록의 DC-006 상태 갱신
```
