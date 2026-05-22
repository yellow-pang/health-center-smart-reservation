# 직원 예약 검색 접수 UX Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `dev` |
| 권장 작업 브랜치 | `feat/staff-reservation-check-in-search` |
| base 브랜치 | `dev` |
| 작업 트리 | 직원 예약 검색 접수 API/화면/문서 수정 |
| 주요 커밋 | 커밋 전 |
| 정적 확인 | `git diff --check` 통과, `npm.cmd run build` 통과 |
| 테스트 확인 | Maven 미수행: 현재 세션에서 `mvn.cmd`/`mvn` 실행 파일 미탐지 |
| 실행/API 확인 | 서버 기동, Docker, Swagger 런타임 호출, 브라우저 확인은 사용자 직접 수행 |

## PR 제목

```text
feat: 직원 예약 검색 접수 UX 개선
```

## PR 본문

## 개요

직원이 예약번호만으로 예약자를 접수해야 해서 현장 UX가 떨어지는 문제를 줄입니다.

이번 PR은 예약번호 직접 입력을 유지하면서, 직원이 예약자 이름/전화번호/예약번호 일부와 예약일로 접수 대상 예약을 검색한 뒤 결과에서 바로 접수할 수 있게 합니다.

## 변경 내용

- `GET /api/reservations/staff/search` 추가
  - `STAFF`, `ADMIN` 전용
  - 예약번호, 방문자 이름, 전화번호 일부 검색 지원
  - 예약일과 상태 필터 지원
- 직원 체크인 화면 개선
  - 예약자 검색 폼 추가
  - 검색 결과 카드에서 바로 접수
  - 예약번호 직접 입력은 보조 경로로 유지
- API/UX 문서와 전체 체크리스트 갱신

## 검증

- [x] `git diff --check`
- [ ] `mvn.cmd -q -DskipTests compile`
- [ ] `mvn.cmd -q test-compile`
- [x] `npm.cmd run build`
- [ ] Swagger `Try it out`: `GET /api/reservations/staff/search?date=2026-05-22&keyword=RSV-SWAGGER-CHECKIN-001&status=RESERVED`
- [ ] Swagger `Try it out`: 조회된 `reservationNo`로 `POST /api/visits/check-in`
- [ ] 브라우저 직원 체크인 화면에서 예약 검색 후 접수

## 추가 테스트 체크리스트

- [ ] Happy: 오늘 예약을 이름으로 검색하고 접수한다.
- [ ] Happy: 전화번호 일부로 예약을 검색하고 접수한다.
- [ ] Happy: 예약번호 직접 입력으로 기존 접수 흐름이 유지된다.
- [ ] Edge: 동명이인이 여러 명일 때 예약 시간/전화번호로 구분 가능하다.
- [ ] Edge: 검색 결과가 없을 때 빈 상태가 표시된다.
- [ ] Bad: 이미 체크인된 예약을 다시 접수하면 오류 메시지가 표시된다.
- [ ] Bad: 시민 권한으로 직원 검색 API를 호출하면 403 응답이 반환된다.

## 미검증 사유

- 서버 기동, Docker 실행, Swagger 런타임 호출, 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행한다.
- Maven은 현재 세션에서 `mvn.cmd`/`mvn` 실행 파일이 PATH에 없어 수행하지 못했다.
- 별도 TypeScript 검사인 `npm.cmd exec -- tsc --noEmit`은 기존 `.next/dev/types/validator.ts`가 삭제된 `/social-login` 페이지를 참조해 실패했다. Next production build는 통과했다.
- QR/바코드는 실제 장비 없이도 생성/표시까지는 구현 가능한 UX 고도화 후보로 분리했다.
- 키오스크 셀프 체크인은 실제 기기/운영 세팅이 필요해 이번 PR과 다음 UX 고도화 우선순위에서 제외했다.
- 휴대폰 인증번호 확인은 체크인 접수 방식보다 회원 등록/계정 복구/예약 변경의 본인확인 영역에 적합해 별도 후속 후보로 분리했다.

## 후속 작업

- QR/바코드 생성 및 표시 구현
- 예약 시간 기준 조기 도착/지각 안내 정책
- 검색 결과 개인정보 마스킹 정책 세분화
- 체크인 성공 응답에 방문자/업무 표시 정보를 포함할지 검토

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 후속 UX 고도화 브랜치 생성 여부 결정

## 커밋 메시지 초안

제목:

```text
feat: 직원 예약 검색 접수 UX 개선
```

본문:

```text
- 직원용 예약 검색 API 추가
- 직원 체크인 화면에 예약 검색 후 선택 접수 흐름 추가
- API/UX 문서와 전체 체크리스트 갱신
```
