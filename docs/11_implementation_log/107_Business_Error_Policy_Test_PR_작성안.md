# 비즈니스 예외 정책 테스트 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `test/business-error-policy-cases` |
| base 브랜치 | `dev` |
| 작업 트리 | 정책 계층 단위 테스트 추가 |
| 주요 커밋 | `54f0b58 test: 비즈니스 예외 정책 대표 케이스 추가` |
| GitNexus | `ReservationCancelPolicy`, `VisitCheckInPolicy`, `QueueTicketPolicy`, `ErrorCode` impact 확인. 모두 LOW |
| 정적 확인 | `git diff --check` 통과 |
| 테스트 확인 | 대상 테스트, 전체 `mvn.cmd -q test`, `test-compile`, `compile` 통과 |
| 실행/API 확인 | DB, Docker, 서버, Swagger 런타임 호출 없음. 순수 단위 테스트만 추가 |

## PR 제목

```text
test: 비즈니스 예외 정책 대표 케이스 추가
```

## PR 본문

## 개요

예약/방문/대기 정책 계층의 대표 Bad case를 단위 테스트로 추가해 `BusinessException(ErrorCode.X)` 전환 결과가 회귀하지 않도록 보호합니다.

이번 PR은 운영 코드 변경 없이 테스트만 추가하며, DB나 서버 기동 없이 Maven 테스트로 검증할 수 있는 정책 단위 안전망을 만듭니다.

## 변경 내용

- 예약 취소 정책 테스트 추가
  - 인증 없음
  - 타인 예약 취소
  - 취소 불가 상태
  - 취소 시간 만료
  - 같은 보건소 직원 취소 허용
- 방문 체크인 정책 테스트 추가
  - 예약 없음
  - 다른 보건소 예약
  - 시민 체크인 시도
  - 중복 체크인
  - 같은 보건소 직원 체크인 허용
- 대기표 정책 테스트 추가
  - 인증 없음
  - 시민 대기열 처리
  - 다른 보건소 대기표 접근
  - 잘못된 상태 전이
  - 정상 상태 전이 허용

## 검증

- [x] `mvn.cmd -q "-Dtest=ReservationCancelPolicyTest,VisitCheckInPolicyTest,QueueTicketPolicyTest" test`
- [x] `mvn.cmd -q test`
- [x] `mvn.cmd -q test-compile`
- [x] `mvn.cmd -q -DskipTests compile`
- [x] `git diff --check`

## 미검증 사유

- 이번 작업은 정책 계층 순수 단위 테스트라 Docker, 서버 기동, Swagger 런타임 호출은 수행하지 않았습니다.
- 예약 정원 초과, 중복 예약, 대기번호 동시 발급 같은 DB 제약/동시성 케이스는 별도 통합 테스트 브랜치에서 다루는 편이 안전합니다.

## 후속 작업

- 예약 생성 서비스의 정원 초과/중복 예약 통합 테스트 추가
- 현장 접수와 대기번호 발급 통합 테스트 추가
- 동시 요청 기반 예약/대기번호 경쟁 조건 테스트 추가
- Controller 계층 오류 응답 JSON 테스트 추가

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 후속 통합 테스트 브랜치 생성 여부 결정

## 커밋 메시지 초안

제목:

```text
docs: 비즈니스 예외 정책 테스트 기록 정리
```

본문:

```text
- 정책 계층 대표 Bad case 테스트 구현 기록 추가
- 테스트 PR 작성안과 검증 결과 정리
- 전체 체크리스트와 보류 목록에 테스트 진행 상태 반영
```
