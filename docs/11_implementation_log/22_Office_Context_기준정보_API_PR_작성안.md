# Office Context 기준정보 API Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/egov-auth-sample-cleanup` |
| 권장 브랜치명 | `feat/office-context-api` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | Office Context 신규 파일과 DB/API/체크리스트 문서 변경 있음 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | `mvn -q -DskipTests compile` 성공 |
| 테스트 확인 | `mvn -q test-compile` 성공 |
| 정적 확인 | `git diff --check` 성공 |
| 실행/API 확인 | 서버 기동과 API 런타임 호출은 사용자 직접 확인 필요 |

## PR 제목

```text
feat: 업무 유형 기준정보 API 구현
```

## PR 본문

```markdown
## 개요

예약 슬롯, 예약 신청, 방문 접수, 대기열 기능이 공통으로 참조할 Office Context 기준정보 API를 추가합니다.

## 변경 내용

- `GET /api/service-types` 공개 업무 유형 조회 API 추가
- 관리자 업무 유형 생성, 수정, 비활성화 API 추가
- 관리자 직원 목록 조회 API 추가
- 관리자 창구 업무 매핑 조회 API 추가
- `service_types`, `service_windows`, `service_window_service_types` 스키마와 seed 추가
- API 명세서, ERD, 전체 체크리스트, 브랜치 구현 기록 갱신

## 검증

- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [x] `git diff --check`
- [ ] 서버 기동 후 `GET /api/service-types` 확인
- [ ] 관리자 토큰으로 `/api/admin/staff`, `/api/admin/service-windows` 확인
- [ ] Swagger UI에서 Office/Admin API 노출 확인

## 미검증 사유

- 프로젝트 운영 기준에 따라 서버 기동, API 런타임 호출, Swagger/브라우저 확인은 에이전트가 직접 실행하지 않고 사용자가 직접 확인합니다.
- GitNexus MCP 리소스가 비어 있고 CLI가 로컬 `gitnexus` 모듈을 찾지 못해 전용 impact 도구 대신 `rg`와 정적 검증으로 대체했습니다.

## 후속 작업

- 예약 슬롯 생성/조회 API 구현
- 업무 유형 비활성화 전 예약 슬롯/예약 참조 여부 정책 확정
- 창구/업무 매핑 변경 API와 직원 담당 업무 배정 API 구현 여부 결정
- Office API 런타임 호출 결과를 구현 기록에 추가
```

## 사용자 직접 확인 명령

```powershell
cd backend
mvn spring-boot:run
```

```powershell
Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/service-types'
```

```powershell
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body '{"email":"admin@test.com","password":"password1234"}'
$token = $login.data.accessToken
Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/admin/staff' -Headers @{Authorization="Bearer $token"}
Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/admin/service-windows' -Headers @{Authorization="Bearer $token"}
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영
- [ ] 후속 브랜치 생성 또는 다음 작업 문서화
