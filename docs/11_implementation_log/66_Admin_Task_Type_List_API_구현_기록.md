# Admin Task Type List API 구현 기록

## 1. 작업 목표

- 관리자 업무 유형 화면에서 활성/비활성 업무 유형을 모두 조회할 수 있게 한다.
- 비활성화 후 비활성 탭으로 이동해 실수한 항목을 바로 확인하고 되돌릴 수 있게 한다.
- 비활성 업무 유형을 재활성화하는 API와 프론트 버튼을 추가한다.

## 2. 작업 범위

- [x] 현재 브랜치와 작업 트리 확인
- [x] 기존 업무 유형 API/Service/Mapper 구조 확인
- [x] 관리자 전체 업무 유형 조회 API 추가
- [x] 업무 유형 재활성화 API 추가
- [x] MyBatis Mapper XML 쿼리 추가
- [x] 비활성 탭 확인용 개발 seed 추가
- [x] 관리자 기준정보 API client 갱신
- [x] 관리자 업무 유형 화면 활성/비활성 탭 추가
- [x] 비활성화 성공 시 비활성 탭으로 이동
- [x] 재활성화 성공 시 활성 탭으로 이동
- [x] API 명세 갱신
- [x] TypeScript 정적 검증
- [x] Next build 검증
- [x] Maven compile/test-compile 검증
- [ ] Swagger 대표 예시 확인
- [ ] 브라우저 업무 유형 화면 확인

## 3. 구현 내용

### 3.1 백엔드 API

추가 API:

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/admin/service-types` | 활성/비활성 업무 유형 전체 조회 | ADMIN |
| PATCH | `/api/admin/service-types/{id}/activate` | 비활성 업무 유형 재활성화 | ADMIN |

수정 파일:

| 파일 | 내용 |
|---|---|
| `ServiceTypeController.java` | 전체 조회, 재활성화 endpoint 추가 |
| `OfficeQueryService.java` | `findAllServiceTypes` 추가 |
| `OfficeCommandService.java` | `activateServiceType` 추가, 생성 후 조회를 전체 목록 기준으로 보정 |
| `OfficeMapper.java` | 전체 조회와 재활성화 mapper 메서드 추가 |
| `Office_SQL_postgresql.xml` | `selectAllServiceTypes`, `activateServiceType` SQL 추가 |

### 3.2 프론트엔드

- `getAdminServiceTypes`가 `GET /api/admin/service-types`를 사용하도록 변경했다.
- `activateAdminServiceType`를 추가했다.
- `/admin/service-types` 화면에 활성/비활성 필터 버튼을 추가했다.
- 업무 유형 비활성화 성공 시 목록에서 제거하지 않고 상태를 갱신한 뒤 비활성 탭으로 이동한다.
- 비활성 탭에서는 재활성화 버튼을 제공하고, 성공 시 활성 탭으로 이동한다.

### 3.3 개발 seed

- `backend/src/main/resources/db/postgresql/data.sql`에 `DISABLED_TEST_SERVICE` 업무 유형을 추가했다.
- `active = false` 상태로 생성되어 관리자 업무 유형 화면의 비활성 탭과 재활성화 흐름을 바로 확인할 수 있다.
- 예약 슬롯 seed는 활성 업무 유형만 대상으로 하므로 비활성 테스트 업무에는 예약 슬롯이 생성되지 않는다.

## 4. GitNexus 및 영향 범위

GitNexus impact 결과:

| 대상 | 결과 |
|---|---|
| `ServiceTypeController` | 실패. exit 1, 출력 없음 |
| `OfficeQueryService` | 실패. exit 1, 출력 없음 |
| `OfficeCommandService` | 실패. exit 1, 출력 없음 |
| `OfficeMapper` | 실패. exit 1, 출력 없음 |

대체 확인:

- `rg`로 `/api/service-types`, `/api/admin/service-types`, `deactivateServiceType` 사용처 확인
- 변경 범위를 Office Context와 관리자 업무 유형 화면으로 제한
- Maven compile/test-compile, TypeScript, Next build로 정적 검증

## 5. 검증 결과

| 검증 | 결과 |
|---|---|
| `npm.cmd exec -- tsc --noEmit` | 통과 |
| `npm.cmd run build` | 통과 |
| `mvn.cmd -q -DskipTests compile` | 통과 |
| `mvn.cmd -q test-compile` | 통과 |
| `git diff --check` | 공백 오류 없음. LF/CRLF 경고만 표시 |
| Swagger/API 런타임 확인 | 미수행. 사용자 직접 확인 필요 |
| 브라우저 화면 확인 | 미수행. 사용자 직접 확인 필요 |

## 6. 사용자 직접 확인 방법

Swagger 대표 예시:

`GET /api/admin/service-types`

인증:

- `admin@test.com / password1234` 로그인 후 access token 사용

기대 결과:

- `success: true`
- `data`에 활성/비활성 업무 유형이 모두 포함
- 비활성 항목은 `active: false`로 표시
- 개발 seed 기준 `DISABLED_TEST_SERVICE`가 `active: false`로 포함

브라우저 확인:

1. `/admin/service-types`에 접속한다.
2. 기본으로 활성 업무 탭이 표시되는지 확인한다.
3. 업무 유형을 비활성화한다.
4. 비활성 탭으로 자동 이동하고 비활성화한 업무가 보이는지 확인한다.
5. 재활성화 버튼으로 되돌린 뒤 활성 탭으로 자동 이동하는지 확인한다.

## 7. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 구현 중 | 관리자 업무 유형 목록 서버 필터 | 데이터가 많아질 경우 `active=true/false/all` query가 필요할 수 있음 | 후속 |
| 구현 중 | 업무 유형 코드 수정 정책 | 현재 생성 후 code는 수정하지 않음 | 후속 검토 |

## 8. 커밋 메시지 초안

```text
feat: 관리자 업무 유형 전체 조회와 재활성화 구현

- 관리자 업무 유형 전체 조회 API 추가
- 업무 유형 재활성화 API 추가
- 관리자 업무 유형 화면에 활성/비활성 탭 추가
- 비활성화와 재활성화 후 탭 이동 UX 반영
```
