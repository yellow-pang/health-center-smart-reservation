# Auth/Member 역할별 접근 규칙 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/auth-member-domain` |
| base 브랜치 | `main` 예정 |
| 작업 트리 | SecurityConfig와 문서 변경 있음 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | `mvn -q -DskipTests compile` 성공 |
| 테스트 확인 | `mvn -q test-compile` 성공 |
| API 확인 | 사용자가 직접 확인할 명령 작성 |
| GitNexus 확인 | MCP 리소스 없음, CLI 모듈 없음으로 실패, `rg` 대체 확인 |

## PR 제목

```text
feat: Auth Member 역할별 접근 규칙 추가
```

## PR 본문

```markdown
## 개요

보건소 MVP API 명세의 권한 표를 기준으로 Spring Security URL 접근 규칙을 1차 반영했습니다.

이번 PR에서는 공개 API, 로그인 사용자 API, 직원/관리자 API의 큰 권한 경계를 설정했습니다. 예약자 본인 여부 같은 객체 단위 권한 정책은 각 Context 구현 시 Service 정책으로 보강합니다.

## 변경 내용

- `/api/service-types`, `/api/congestion/current` 공개 경로 추가
- `/api/admin/**`, `/api/dashboard/**`를 `ADMIN`으로 제한
- `/api/visits/**`, `/api/queues/**`를 `STAFF`, `ADMIN`으로 제한
- 예약 생성/조회/취소 경로의 1차 권한 규칙 추가
- `/api/members/me`, `/api/auth/logout` 인증 필요 경로 명시
- 역할별 접근 확인 방법과 미검증 사유 문서화

## 검증

- [x] `rg` 기반 Security/Auth/권한 참조 확인
- [x] `git diff --check`
- [x] `mvn -q -DskipTests compile`
- [x] `mvn -q test-compile`
- [ ] 사용자 직접 역할별 API 접근 확인
- [ ] 사용자 직접 Swagger UI 확인

## 미검증 사유

- 서버 기동, API 런타임 호출, Swagger 브라우저 확인은 사용자 직접 수행 기준에 따라 에이전트가 실행하지 않았습니다.
- GitNexus MCP 리소스가 현재 세션에 노출되지 않았고, CLI도 로컬 모듈을 찾지 못해 detect_changes는 수행하지 못했습니다.
- 아직 구현되지 않은 API는 404가 날 수 있으므로 역할별 런타임 검증은 각 Controller 구현 후 재확인합니다.

## 후속 작업

- Reservation Context에서 예약자 본인/직원/관리자 객체 권한 정책 구현
- 기존 eGovFrame 로그인/관리자/SNS 샘플 비노출 또는 삭제 판단
- 사용자 직접 역할별 접근 확인 결과 반영
```

## 변경 파일 요약

| 파일 | 내용 |
|---|---|
| `backend/src/main/java/egovframework/com/security/SecurityConfig.java` | 보건소 MVP 공개/역할별 API 접근 규칙 추가 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | Auth/Member 진행 상태 갱신 |
| `docs/11_implementation_log/14_Auth_Member_역할별_접근_규칙_구현_기록.md` | 구현 기록 추가 |
| `docs/11_implementation_log/15_Auth_Member_역할별_접근_규칙_PR_작성안.md` | PR 작성안 추가 |

## 커밋 메시지 초안

```text
feat: Auth Member 역할별 접근 규칙 추가

- 보건소 MVP 공개 API와 인증 필요 API 경로 정리
- 관리자, 직원, 예약 API 역할별 Security 규칙 추가
- 역할별 접근 확인 방법과 남은 객체 권한 정책 문서화
```
