# Admin Resource Management API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/admin-resource-management-api` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 문서 작성 전 코드 커밋 3개 완료 |
| 주요 커밋 | `e062ef8`, `97536d6`, `e885cee` |
| 타입 확인 | `npm.cmd exec -- tsc --noEmit` 통과 |
| 프론트 빌드 | `npm.cmd run build` 통과 |
| 백엔드 compile | `mvn.cmd -q -DskipTests compile` 통과 |
| 백엔드 test-compile | `mvn.cmd -q test-compile` 통과 |
| 정적 공백 확인 | `git diff --check` 통과. LF/CRLF 경고만 표시 |
| GitNexus 확인 | impact/detect_changes CLI 실패. `rg`와 빌드로 보완 |
| 실행/API 확인 | 미수행. 사용자가 Swagger/브라우저로 직접 확인 필요 |

## PR 제목

```text
feat: 관리자 예약 슬롯 직원 창구 관리 API 구현
```

## PR 본문

````markdown
## 개요

관리자 기준정보 화면에서 예약 슬롯, 직원, 창구를 실제 API로 관리할 수 있게 합니다.

예약 슬롯은 수정과 비활성화를 지원하고, 직원은 생성/수정/비활성화를 지원합니다. 창구는 생성/수정/비활성화와 함께 담당자 배정 및 업무 유형 매핑 수정을 처리합니다.

## 변경 내용

- `PUT /api/admin/reservation-slots/{id}` 추가
- `PATCH /api/admin/reservation-slots/{id}/deactivate` 추가
- `POST /api/admin/staff` 추가
- `PUT /api/admin/staff/{id}` 추가
- `PATCH /api/admin/staff/{id}/deactivate` 추가
- `POST /api/admin/service-windows` 추가
- `PUT /api/admin/service-windows/{id}` 추가
- `PATCH /api/admin/service-windows/{id}/deactivate` 추가
- `service_windows.staff_id` 컬럼 추가
- 창구 응답에 `staffId`, `staffName` 추가
- 관리자 기준정보 API client에 예약 슬롯/직원/창구 쓰기 API 추가
- 관리자 예약 슬롯 화면 수정/비활성화 연결
- 관리자 직원 화면 생성/수정/비활성화 연결
- 관리자 창구 화면 생성/수정/비활성화/담당자 배정 연결
- 기본 창구 담당자 seed 추가

## 검증

- [x] `git diff --check`
- [x] `npm.cmd exec -- tsc --noEmit`
- [x] `npm.cmd run build`
- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [ ] Swagger 대표 예시 확인
- [ ] 브라우저 `/admin/reservation-slots` 수정/비활성화 확인
- [ ] 브라우저 `/admin/staff` 생성/수정/비활성화 확인
- [ ] 브라우저 `/admin/service-windows` 생성/수정/담당자 배정/비활성화 확인

## Swagger 대표 예시

`PUT /api/admin/service-windows/1`

인증:

- `admin@test.com / password1234` 로그인 후 access token 사용

요청 예시:

```json
{
  "windowNumber": 1,
  "name": "1번 창구",
  "staffId": 2,
  "serviceTypeIds": [1, 2],
  "active": true
}
```

기대 결과:

- `success: true`
- `data.id`가 `1`
- `data.staffId`가 요청한 담당자 ID
- `data.serviceTypes`에 요청한 업무 유형 목록 포함

## 추가 테스트 체크리스트

- [ ] Happy: 예약 슬롯 수정 성공
- [ ] Happy: 예약 슬롯 비활성화 후 관리자 목록에서 제외
- [ ] Edge: 이미 예약된 수보다 작은 정원으로 예약 슬롯 수정 시 오류 응답
- [ ] Bad: 중복 업무/날짜/시간 예약 슬롯 수정 시 오류 응답
- [ ] Happy: 직원 생성 성공
- [ ] Happy: 직원 이름/역할/활성 여부 수정 성공
- [ ] Happy: 직원 비활성화 성공
- [ ] Bad: 중복 이메일 직원 생성 시 오류 응답
- [ ] Happy: 창구 생성 성공
- [ ] Happy: 창구 담당자와 업무 유형 매핑 수정 성공
- [ ] Happy: 창구 비활성화 성공
- [ ] Edge: 담당자 미배정 창구 저장 성공
- [ ] Bad: 비활성 또는 다른 보건소 계정을 담당자로 배정 시 오류 응답
- [ ] Bad: 직원/시민 계정으로 관리자 API 호출 시 403 확인

## 미검증 사유

- 서버 기동, Docker 실행, Swagger Try it out, 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- GitNexus CLI impact/detect_changes가 exit 1로 실패해 `rg` 기반 영향 확인과 빌드 검증으로 보완했습니다.

## 남은 위험

- 창구 비활성화 시 이미 배정된 대기표를 어떻게 처리할지 운영 정책이 필요합니다.
- 직원 비활성화 시 기존 창구 담당자 배정을 자동 해제할지 정책 결정이 필요합니다.
- 예약자가 존재하는 슬롯의 시간/업무 변경은 향후 알림 또는 제한 정책을 검토해야 합니다.

## 후속 작업

- 관리자 기준정보 화면 브라우저 수동 확인
- Swagger Happy/Edge/Bad 케이스 체크
- 관리자 기준정보 API 명세 문서 보강
- 창구/직원 비활성화 운영 정책 검토
````

## 커밋 메시지

```text
feat: 예약 슬롯 수정과 비활성화 구현
feat: 직원 생성 수정 삭제 API 구현
feat: 창구 생성 수정 담당자 배정 API 구현
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] Swagger/브라우저 수동 확인 결과 반영
- [ ] 후속 정책 검토 브랜치 생성 여부 결정
