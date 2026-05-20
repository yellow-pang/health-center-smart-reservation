# 객체 권한 정책 보강 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `fix/object-authorization-policy` |
| base 브랜치 | `dev` |
| 작업 트리 | 예약 객체 권한 1차 보강 진행 |
| GitNexus | stale 상태였으나 `impact --repo health-center-smart-reservation`로 `ReservationCancelPolicy`, `ReservationCommandService`, `ReservationController`, `SecurityConfig` 영향 확인. 모두 LOW |
| 정적 확인 | `mvn.cmd -q -DskipTests compile`, `mvn.cmd -q test-compile`, `git diff --check` 통과 |
| 실행/API 확인 | 사용자가 Docker 실행 환경에서 Swagger 대표 권한 케이스 확인 필요 |

## PR 제목

```text
fix: 예약 객체 권한 정책 보강
```

## PR 본문

```markdown
## 개요

DC-007 객체 권한 정책 후보를 기준으로 예약/방문/대기/대시보드 접근 조건을 점검하고, 예약 생성/취소의 누락된 보건소 범위 검사를 보강합니다.

URL 역할 권한과 서비스/정책 계층 객체 권한을 맞춰 같은 보건소 `STAFF/ADMIN`이 필요한 예약 업무를 처리할 수 있게 하고, 다른 보건소 데이터에는 접근하지 못하게 합니다.

## 변경 내용

- `STAFF/ADMIN` 예약 생성 시 선택 슬롯의 보건소와 principal 보건소가 일치하는지 검사
- 예약 취소 정책을 예약자 본인 또는 같은 보건소 `STAFF/ADMIN`으로 정렬
- `DELETE /api/reservations/{id}` URL 권한에 `STAFF` 추가
- 예약 취소 Swagger 설명 갱신
- DC-007 보류 목록과 전체 체크리스트 갱신

## 검증

- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `git diff --check`
- [ ] Swagger 대표 권한 확인

## Swagger 대표 확인

```http
DELETE /api/reservations/{reservationId}
Authorization: Bearer {staffAccessToken}
```

기대 결과:

- 같은 보건소 STAFF/ADMIN: 취소 가능 조건이면 200
- 다른 보건소 STAFF/ADMIN: 403 또는 `FORBIDDEN` 오류 응답
- CITIZEN/GUARDIAN: 본인 예약이면 200, 타인 예약이면 403 또는 `FORBIDDEN`

## 미검증 사유

- 서버 기동, Docker 실행, Swagger 런타임 호출은 프로젝트 운영 기준상 사용자가 직접 확인합니다.

## 후속 작업

- 관리자 기준정보 API의 `DEFAULT_HEALTH_CENTER_ID` 제거 여부 검토
- 객체 권한 정책 자동화 테스트 추가
- 서비스/정책 계층의 주요 `IllegalArgumentException`을 `BusinessException(ErrorCode.X)`로 점진 교체
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 다음 보류 항목 브랜치 생성 여부 결정

## 커밋 메시지 초안

제목:

```text
fix: 예약 객체 권한 정책 보강
```

본문:

```text
- 예약 생성 시 STAFF/ADMIN의 보건소 슬롯 접근 범위 검사 추가
- 예약 취소를 예약자 본인 또는 같은 보건소 STAFF/ADMIN 기준으로 정렬
- 예약 취소 URL 권한에 STAFF 추가
- DC-007 보류 목록과 전체 체크리스트 갱신
```

