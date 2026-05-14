# Admin Task Type List API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/admin-task-type-list-api` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 관리자 업무 유형 전체 조회/재활성화 코드와 문서 변경 있음 |
| 주요 커밋 | 아직 없음 |
| 타입 확인 | `npm.cmd exec -- tsc --noEmit` 통과 |
| 프론트 빌드 | `npm.cmd run build` 통과 |
| 백엔드 compile | `mvn.cmd -q -DskipTests compile` 통과 |
| 백엔드 test-compile | `mvn.cmd -q test-compile` 통과 |
| 정적 공백 확인 | `git diff --check` 통과. LF/CRLF 경고만 표시 |
| GitNexus 확인 | impact CLI 실패. `rg`와 빌드로 보완 |
| 실행/API 확인 | 미수행. 사용자가 Swagger/브라우저로 직접 확인 필요 |

## PR 제목

```text
feat: 관리자 업무 유형 전체 조회와 재활성화 구현
```

## PR 본문

```markdown
## 개요

관리자 업무 유형 화면에서 활성/비활성 업무 유형을 모두 조회하고, 비활성화한 업무 유형을 다시 활성화할 수 있게 합니다.

비활성화 후에는 비활성 탭으로 이동해 실수한 항목을 바로 확인하고 되돌릴 수 있도록 UX를 보강했습니다.

## 변경 내용

- `GET /api/admin/service-types` 추가
- `PATCH /api/admin/service-types/{id}/activate` 추가
- Office Query/Command Service와 MyBatis Mapper 쿼리 추가
- 관리자 기준정보 API client가 관리자 전체 업무 유형 조회 API를 사용하도록 변경
- 업무 유형 관리 화면에 활성/비활성 탭 추가
- 비활성화 성공 시 비활성 탭으로 이동
- 재활성화 성공 시 활성 탭으로 이동
- 비활성 탭 확인용 `DISABLED_TEST_SERVICE` 개발 seed 추가
- API 명세 갱신

## 검증

- [x] `npm.cmd exec -- tsc --noEmit`
- [x] `npm.cmd run build`
- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `git diff --check`
- [ ] Swagger `GET /api/admin/service-types` 확인
- [ ] Swagger `PATCH /api/admin/service-types/{id}/activate` 확인
- [ ] 브라우저 `/admin/service-types` 활성/비활성 탭 확인
- [ ] 브라우저 비활성화 후 비활성 탭 이동 확인
- [ ] 브라우저 재활성화 후 활성 탭 이동 확인

## Swagger 대표 예시

`GET /api/admin/service-types`

인증:

- `admin@test.com / password1234` 로그인 후 access token 사용

기대 결과:

- `success: true`
- `data`에 활성/비활성 업무 유형이 모두 포함
- 비활성 항목은 `active: false`로 표시
- 개발 seed 기준 `DISABLED_TEST_SERVICE`가 `active: false`로 포함

## 추가 테스트 체크리스트

- [ ] Happy: 관리자 계정으로 전체 업무 유형 조회 성공
- [ ] Happy: 활성 탭에 `active: true` 항목만 표시
- [ ] Happy: 비활성 탭에 `active: false` 항목만 표시
- [ ] Happy: 활성 업무 비활성화 후 비활성 탭으로 자동 이동
- [ ] Happy: 비활성 업무 재활성화 후 활성 탭으로 자동 이동
- [ ] Edge: 비활성 업무가 없는 경우 빈 상태 표시
- [ ] Bad: 직원/시민 계정으로 관리자 전체 조회 API 호출 시 403 확인
- [ ] Bad: 존재하지 않는 업무 유형 ID 재활성화 시 오류 응답 확인

## 미검증 사유

- 서버 기동, Docker 실행, Swagger Try it out, 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- GitNexus CLI impact가 exit 1로 실패해 `rg` 기반 영향 확인으로 보완했습니다.

## 남은 위험

- 현재 전체 조회 API는 서버 필터 없이 전체 목록을 반환합니다. 업무 유형이 많아지면 `active` query filter를 후속 검토할 수 있습니다.
- 업무 유형 code 수정은 기존 정책처럼 지원하지 않습니다.

## 후속 작업

- 관리자 업무 유형 목록 서버 필터 검토
- 예약 슬롯 수정/비활성화 API 구현
- 직원 생성/수정/삭제 API 구현
- 창구 생성/수정/담당자 배정 API 구현
```

## 커밋 메시지 초안

```text
feat: 관리자 업무 유형 전체 조회와 재활성화 구현

- 관리자 업무 유형 전체 조회 API 추가
- 업무 유형 재활성화 API 추가
- 관리자 업무 유형 화면에 활성/비활성 탭 추가
- 비활성화와 재활성화 후 탭 이동 UX 반영
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 후속 관리자 예약 슬롯 수정/비활성화 API 브랜치 생성 여부 결정
