# 예약 도메인 비즈니스 예외 명시화 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/reservation-business-exceptions` |
| base 브랜치 | `dev` |
| 작업 트리 | 예약 도메인 `BusinessException` 전환 진행 |
| GitNexus | stale 상태였으나 `impact --repo health-center-smart-reservation`로 수정 대상 영향 확인. 모두 LOW |
| 정적 확인 | `mvn.cmd -q -DskipTests compile`, `mvn.cmd -q test-compile`, `git diff --check` 통과 |
| 실행/API 확인 | 사용자가 Docker 실행 환경에서 Swagger 대표 오류 응답 확인 필요 |

## PR 제목

```text
refactor: 예약 도메인 예외 코드를 명시화
```

## PR 본문

```markdown
## 개요

예약 도메인의 주요 `IllegalArgumentException`을 `BusinessException(ErrorCode.X)`로 교체해 메시지 기반 오류 코드 매핑 의존을 줄입니다.

응답 형식은 기존 `success + data + error` 구조를 유지하며, 예약 생성/조회/취소/예약 슬롯 관리의 오류 코드가 더 명시적으로 내려가도록 정리합니다.

## 변경 내용

- `RESERVATION_FORBIDDEN`, `RESERVATION_SLOT_DUPLICATED` 오류 코드 추가
- 예약 생성/취소 검증 예외를 `BusinessException`으로 교체
- 예약 상세 조회의 인증/존재/권한 예외를 `BusinessException`으로 교체
- 예약 슬롯 조회/생성/수정/비활성화 예외를 `BusinessException`으로 교체
- 예약 취소 정책 예외를 `BusinessException`으로 교체

## 검증

- [x] 예약 도메인 `IllegalArgumentException` 잔여 사용처 없음 확인
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

## 미검증 사유

- 서버 기동, Docker 실행, Swagger 런타임 호출은 프로젝트 운영 기준상 사용자가 직접 확인합니다.

## 후속 작업

- Visit/Queue/Office 도메인의 `BusinessException(ErrorCode.X)` 점진 교체
- 예약 정책 오류 Bad case 자동화 테스트 추가
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 다음 보류 항목 브랜치 생성 여부 결정

## 커밋 메시지 초안

제목:

```text
refactor: 예약 도메인 예외 코드를 명시화
```

본문:

```text
- 예약 서비스와 정책의 IllegalArgumentException을 BusinessException으로 교체
- RESERVATION_FORBIDDEN, RESERVATION_SLOT_DUPLICATED 오류 코드 추가
- 예약 슬롯과 예약 취소 오류 응답 코드를 명시화
- 보류 목록과 전체 체크리스트에 예약 예외 전환 결과 반영
```

