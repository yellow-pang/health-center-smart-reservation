# 비즈니스 예외 정책 테스트 구현 기록

## 1. 작업 목표

- 보건소 주요 정책 계층의 대표 Bad case를 단위 테스트로 고정한다.
- `BusinessException(ErrorCode.X)` 전환 이후 오류 코드가 회귀하지 않도록 보호한다.
- DB, Docker, 서버 기동 없이 Maven 테스트로 확인 가능한 최소 안전망을 추가한다.

## 2. 작업 범위

- [x] 현재 브랜치와 작업 트리 확인
- [x] 기존 테스트 구조 확인
- [x] GitNexus impact 확인
- [x] 예약 취소 정책 대표 케이스 테스트 추가
- [x] 방문 체크인 정책 대표 케이스 테스트 추가
- [x] 대기표 상태 전이 정책 대표 케이스 테스트 추가
- [x] Maven 테스트 실행
- [x] 전체 체크리스트 갱신
- [x] PR 문서 초안 작성

제외한다.

- [ ] Docker/PostgreSQL 기반 통합 테스트
- [ ] Swagger 런타임 호출 테스트
- [ ] 동시 요청 테스트
- [ ] Controller API 테스트

## 3. 영향 범위 확인

GitNexus impact:

| 대상 | 직접 영향 | 위험도 |
|---|---|---|
| `ReservationCancelPolicy` | `ReservationCommandService`, 간접 `ReservationController` | LOW |
| `VisitCheckInPolicy` | `VisitCommandService`, 간접 `VisitController` | LOW |
| `QueueTicketPolicy` | `QueueQueryService`, `QueueCommandService`, 간접 `QueueController` | LOW |
| `ErrorCode` | upstream 0으로 확인 | LOW |

이번 브랜치는 운영 코드 변경 없이 테스트만 추가했다.

## 4. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `backend/src/test/java/egovframework/healthcenter/reservation/policy/ReservationCancelPolicyTest.java` | 인증 없음, 타인 예약, 취소 불가 상태, 취소 시간 만료, 같은 보건소 직원 취소 허용 테스트 추가 |
| `backend/src/test/java/egovframework/healthcenter/visit/policy/VisitCheckInPolicyTest.java` | 예약 없음, 다른 보건소 예약, 시민 체크인 시도, 중복 체크인, 같은 보건소 직원 체크인 허용 테스트 추가 |
| `backend/src/test/java/egovframework/healthcenter/queue/policy/QueueTicketPolicyTest.java` | 인증 없음, 시민 대기열 처리, 다른 보건소 대기표 접근, 잘못된 상태 전이, 정상 상태 전이 테스트 추가 |

## 5. 검증 체크리스트

- [x] `mvn.cmd -q "-Dtest=ReservationCancelPolicyTest,VisitCheckInPolicyTest,QueueTicketPolicyTest" test`
- [x] `mvn.cmd -q test`
- [x] `mvn.cmd -q test-compile`
- [x] `mvn.cmd -q -DskipTests compile`
- [x] `git diff --check`

참고:

- 첫 대상 테스트 실행은 sandbox에서 로컬 `.m2` 접근 권한 문제로 실패했다.
- 승인된 Maven 실행으로 재시도해 대상 테스트와 전체 테스트가 통과했다.
- 전체 테스트 실행 중 기존 Logback/SLF4J/Mockito 경고가 출력되었으나 테스트 실패는 없었다.

## 6. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 테스트 범위 선정 중 | 서비스/Controller/API 테스트 추가 | Mapper mock 또는 Spring MVC 테스트 설정이 필요해 범위가 커진다. | 후속 테스트 후보 |
| 테스트 범위 선정 중 | 예약 정원 초과, 중복 예약 통합 테스트 | DB update 결과와 unique constraint를 함께 검증해야 한다. | 후속 통합 테스트 후보 |
| 테스트 범위 선정 중 | 동시 요청 테스트 | 실제 동시성은 트랜잭션과 DB 제약이 함께 필요하다. | 후속 테스트 후보 |

## 7. 브랜치 종료 전 체크리스트

- [x] 테스트 코드 추가 완료
- [x] Maven 테스트 확인
- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
