# Office Context 기준정보 API 구현 기록

## 1. 작업 목표

- 예약, 방문, 대기 기능이 참조할 업무 유형 기준정보를 먼저 고정한다.
- 공개 업무 유형 조회와 관리자 업무 유형 관리 API를 MyBatis 기준으로 구현한다.
- 관리자 직원 목록 조회와 창구/업무 매핑 조회의 최소 기반을 함께 만든다.

## 2. 작업 범위

- [x] 이번 브랜치에 포함: `GET /api/service-types`
- [x] 이번 브랜치에 포함: `POST /api/admin/service-types`
- [x] 이번 브랜치에 포함: `PUT /api/admin/service-types/{id}`
- [x] 이번 브랜치에 포함: `PATCH /api/admin/service-types/{id}/deactivate`
- [x] 이번 브랜치에 포함: `GET /api/admin/staff`
- [x] 이번 브랜치에 포함: 창구와 업무 유형 매핑 조회 API
- [x] 이번 브랜치에서 제외: 예약 슬롯 생성/조회 구현
- [x] 이번 브랜치에서 제외: 직원 담당 업무 배정 변경 API

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 관련 항목 확인
- [x] 관련 설계 문서 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] 영향받는 파일 확인

## 4. 구현 체크리스트

- [x] Controller 추가
- [x] Service 추가
- [x] Mapper 추가
- [x] Mapper XML 추가
- [x] DTO/VO 추가
- [x] DB schema/seed 보강
- [x] 예외 처리 확인
- [x] 공통 응답 형식 확인

## 5. 검증 체크리스트

- [x] Maven compile 확인
- [x] Maven test-compile 확인
- [x] `git diff --check` 확인
- [x] API 수동 호출 방법과 기대 결과 작성
- [x] Swagger 확인 URL과 확인 항목 작성
- [x] 프론트엔드 연결 영향 확인

## 6. 사용자 코드 점검 결과

| 점검 시점 | 사용자 의견 | 반영 여부 |
|---|---|---|
| 작업 전 | Office Context 기준정보 선행 구현 요청 | 반영 |

## 7. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 작업 전 | GitNexus MCP/CLI 확인 실패 | MCP 리소스가 비어 있고 CLI가 로컬 `gitnexus` 모듈을 찾지 못함 | `rg`와 Maven 검증으로 대체 |
| 구현 중 | 예약 슬롯 생성은 후속 분리 | 업무 유형 기준정보와 슬롯 생성까지 한 번에 구현하면 범위가 커짐 | 후속 작업으로 기록 |
| 구현 중 | 창구/업무 매핑 변경 API는 후속 분리 | 매핑 변경은 운영 정책과 UI 흐름 결정이 필요함 | 조회 기반만 구현 |

## 8. 구현 내용

### 8.1 API

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/service-types` | 활성 업무 유형 조회 | PUBLIC |
| POST | `/api/admin/service-types` | 업무 유형 생성 | ADMIN |
| PUT | `/api/admin/service-types/{id}` | 업무 유형 수정 | ADMIN |
| PATCH | `/api/admin/service-types/{id}/deactivate` | 업무 유형 비활성화 | ADMIN |
| GET | `/api/admin/staff` | 직원 목록 조회 | ADMIN |
| GET | `/api/admin/service-windows` | 창구와 담당 업무 매핑 조회 | ADMIN |

### 8.2 DB

- `service_types` 테이블 추가
- `service_windows` 테이블 추가
- `service_window_service_types` 매핑 테이블 추가
- 업무 유형, 창구, 창구/업무 매핑 seed 추가

### 8.3 영향 분석

GitNexus 전용 impact 도구는 현재 세션에서 사용할 수 없었다.

대체 확인:

- 신규 심볼은 기존 호출자가 없으므로 직접 호출자 없음으로 판단
- `/api/service-types`는 기존 SecurityConfig 공개 경로에 이미 포함됨
- `/api/admin/**`는 기존 SecurityConfig에서 ADMIN 권한으로 제한됨
- `members` 테이블은 직원 목록 조회에서 읽기만 수행함

## 9. 사용자 직접 확인 방법

### 9.1 서버 기동

```powershell
cd backend
mvn spring-boot:run
```

기대 결과:

- 애플리케이션이 8080 포트로 기동된다.
- Swagger UI 접근 가능: `http://localhost:8080/swagger-ui/index.html`

### 9.2 공개 업무 유형 조회

```powershell
Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/service-types'
```

기대 결과:

- `success`가 `true`
- `data`에 `VACCINATION`, `HEALTH_CHECK`, `HEALTH_CONSULT`가 포함

### 9.3 관리자 API 확인

```powershell
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"admin@test.com","password":"password1234"}'
$token = $login.data.accessToken
Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/admin/staff' -Headers @{Authorization="Bearer $token"}
Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/admin/service-windows' -Headers @{Authorization="Bearer $token"}
```

기대 결과:

- 직원 목록에 `staff@test.com`이 포함
- 창구 목록에 기본 창구와 담당 업무 유형이 포함

## 10. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
