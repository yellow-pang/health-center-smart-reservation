# 직원 예약 검색 접수 UX 구현 기록

## 1. 작업 목표

- 직원이 예약번호를 직접 받아 입력해야 하는 접수 흐름의 UX 부담을 줄인다.
- 예약번호 접수는 보조 경로로 유지하고, 이름/전화번호/예약번호 일부 검색 후 선택 접수 흐름을 추가한다.
- DB 구조 변경 없이 현재 Reservation/Visit/Queue 흐름을 유지한다.

## 2. 작업 범위

- [x] 전체 체크리스트 관련 항목 확인
- [x] 현재 직원 체크인 화면과 Visit/Reservation API 구조 확인
- [x] 접수 방식 후보 조사 및 이번 브랜치 범위 선정
- [x] 직원용 예약 검색 API 추가
- [x] 직원 체크인 화면에 예약 검색 후 접수 UX 추가
- [x] API/UX 문서 갱신
- [x] 전체 체크리스트 갱신
- [x] PR 문서 초안 작성

제외한다.

- [ ] QR/바코드 생성 및 표시: 이번 브랜치에서는 제외하되, 장비 없이 구현 가능한 포트폴리오 UX 고도화 후보로 보류 목록에 이관한다.
- [ ] 현장 키오스크 셀프 체크인: 실제 기기/운영 세팅이 필요하므로 체크인 UX 고도화 범위에서 제외한다.
- [ ] 휴대폰 본인확인/인증번호 확인: 체크인 접수 방식이 아니라 회원 등록/비밀번호 재설정/예약 변경 같은 본인확인 영역의 후속 후보로 분리한다.
- [ ] 예약 시간 지각/조기 도착 정책 변경

## 3. 접수 방식 후보와 권장안

| 방식 | 장점 | 단점 | 이번 판단 |
|---|---|---|---|
| 예약번호 직접 입력 | 구현이 단순하고 오인 접수 위험이 낮음 | 사용자가 번호를 찾지 못하면 직원 업무가 느려짐 | 보조 경로로 유지 |
| 이름/전화번호/예약번호 검색 후 선택 | 직원이 가장 빨리 적용 가능하고 DB 변경이 작음 | 동명이인 확인 UI가 필요함 | 이번 브랜치 구현 |
| QR/바코드 생성 및 표시 | 장비 없이도 예약 완료/내 예약 화면에서 코드 이미지를 보여줄 수 있고, 직원 화면에서 문자열 입력으로 검증 가능 | 실제 스캔 장비/카메라 연동은 별도 범위 | 후속 고도화 권장 |
| 키오스크 셀프 체크인 | 직원 업무량 감소 | 별도 화면/기기/현장 운영 정책 필요 | 보류, 이번 UX 고도화에서 제외 |
| 휴대폰 인증번호 확인 | 오접수 방지에 강함 | 체크인보다 회원 등록/계정 복구/예약 변경의 본인확인에 더 적합 | 본인확인 후속 후보로 분리 |

이번 브랜치에서는 즉시 효과와 변경 범위를 고려해 `예약 검색 후 선택 접수`를 권장안으로 구현했다.

## 4. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `backend/src/main/java/egovframework/healthcenter/reservation/api/ReservationController.java` | `GET /api/reservations/staff/search` 추가 |
| `backend/src/main/java/egovframework/healthcenter/reservation/application/ReservationQueryService.java` | 직원/관리자 권한의 예약 검색 서비스 추가 |
| `backend/src/main/java/egovframework/healthcenter/reservation/mapper/ReservationMapper.java` | 직원 검색용 Mapper 메서드 추가 |
| `backend/src/main/resources/egovframework/mapper/healthcenter/reservation/Reservation_SQL_postgresql.xml` | 예약번호/이름/전화번호 일부 검색 SQL 추가 |
| `frontend/src/lib/staff-api.ts` | 직원용 예약 검색 API client 추가, 선택 예약 정보로 체크인 결과 표시 보강 |
| `frontend/app/staff/check-in/page.tsx` | 예약자 검색, 결과 카드, 선택 접수, 예약번호 직접 입력 보조 경로 추가 |
| `frontend/src/lib/dashboard-api.ts` | 시민 혼잡도 화면에서 사용할 현재 혼잡도 API client 추가 |
| `frontend/app/citizen/congestion/page.tsx` | 고정 mock 혼잡도 대신 `GET /api/congestion/current` 연동 |
| `docs/04_api/01_API_명세서.md` | 직원용 예약 검색 API 문서화 |
| `docs/05_frontend/02_UX_API_계약_우선순위.md` | 직원 예약 검색/체크인 UX 계약 보강 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 직원 접수 UX 개선 진행 상태 반영 |

## 5. Swagger 대표 예시

직원 계정 로그인 후 Swagger `Try it out`에서 아래 순서로 확인한다.

```text
GET /api/reservations/staff/search?date=2026-05-22&keyword=RSV-SWAGGER-CHECKIN-001&status=RESERVED
```

기대 결과:

- `success: true`
- `data[0].reservationNo`가 조회된다.
- 조회된 `reservationNo`로 `POST /api/visits/check-in`을 호출하면 대기번호가 발급된다.

## 6. 검증 체크리스트

- [x] `git diff --check`
- [ ] `mvn.cmd -q -DskipTests compile`
- [ ] `mvn.cmd -q test-compile`
- [x] `npm.cmd run build`
- [ ] Swagger `GET /api/reservations/staff/search` 대표 예시 확인
- [ ] 브라우저 직원 체크인 화면에서 검색 후 접수 확인

참고:

- 현재 세션에서 `mvn.cmd`와 `mvn` 실행 파일이 PATH에 없어 Maven compile/test-compile은 수행하지 못했다.
- 최초 `npm.cmd run build`는 Google Fonts 네트워크 차단으로 실패했고, 승인된 네트워크 실행으로 재시도해 통과했다.
- `npm.cmd exec -- tsc --noEmit`은 기존 `.next/dev/types/validator.ts`가 삭제된 `/social-login` 페이지를 참조해 실패했다. 이번 변경 파일의 Next build는 통과했다.

## 7. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 접수 UX 검토 | QR/바코드 생성 및 표시 | 스캔 장비가 없어도 예약번호 직접 입력을 넘어선 UX 고려를 포트폴리오에서 보여줄 수 있음 | `docs/14_deferred_cleanup/01_보류_정리_목록.md` DC-023으로 이관 |
| 접수 UX 검토 | 지각/조기 도착 안내 | 예약 시간과 실제 접수 시간이 다를 때 직원 판단을 줄일 수 있음 | 후속 정책 작업 |
| 접수 UX 검토 | 검색 결과 개인정보 마스킹 정책 | 동명이인 구분과 개인정보 보호 균형이 필요함 | 화면은 전화번호 마스킹, API는 기존 예약 응답 유지 |
| 혼잡도 확인 | 시민 혼잡도 화면이 고정 mock 데이터를 사용 | 실제 당일 접수가 없어도 mock 대기 인원이 표시될 수 있음 | 실제 `GET /api/congestion/current` API 연동으로 수정 |

## 8. 브랜치 종료 전 체크리스트

- [x] 구현 범위 선정
- [x] 코드 구현 완료
- [x] 관련 문서 갱신
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 가능한 정적 검증과 빌드 확인
- [x] 남은 위험 기록
- [ ] 커밋 메시지 정리
