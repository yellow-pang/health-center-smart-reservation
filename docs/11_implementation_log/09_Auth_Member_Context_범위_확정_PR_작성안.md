# Auth/Member Context 범위 확정 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/auth-member-domain` |
| base 브랜치 | `main` 예정 |
| 작업 트리 | 문서 변경 후 확인 필요 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 빌드 확인 | 문서 작업이므로 미실행 |
| 테스트 확인 | 문서 작업이므로 미실행 |
| GitNexus 확인 | `gitnexus status`는 stale, `gitnexus analyze`는 `Not inside a git repository`, `gitnexus detect_changes`는 `unknown command 'detect_changes'`로 실패 |
| 대체 확인 | `rg`, 파일 직접 확인 |

## PR 제목

```text
docs: Auth Member 구현 범위 확정
```

## PR 본문

```markdown
## 개요

Auth/Member API를 구현하기 전에 보건소 MVP 기준 사용자 유형, 인증 API, 권한 범위, 기존 eGovFrame 인증 샘플의 전환 방향을 확정했습니다.

이번 PR은 문서 범위 확정 작업이며 실제 Auth/Member API 구현, DB schema 변경, Security/JWT 코드 변경은 포함하지 않습니다.

## 변경 내용

- 전체 체크리스트의 Auth/Member 관련 항목 확인
- 기존 eGovFrame 로그인, SNS 로그인, 관리자 샘플 코드 유지 여부 검토
- MVP 사용자 유형을 `CITIZEN`, `GUARDIAN`, `STAFF`, `ADMIN`으로 정리
- 인증 API 범위를 `/api/auth/login`, `/api/auth/reissue`, `/api/auth/logout`, `/api/members/me`로 제안
- `Authorization: Bearer {accessToken}` 형식과 역할별 권한 매핑 방향 확정
- 이번 브랜치에서 구현할 것과 구현하지 않을 것 구분
- 다음 구현 브랜치 단위를 로그인 기반, 토큰 재발급/로그아웃, Security 전환으로 분리
- Auth/Member 브랜치 체크리스트와 PR 작성안 추가
- 전체 체크리스트에 범위 확정 진행 상태 반영

## 검증

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` Auth/Member 항목 확인
- [x] Auth/Member 관련 설계 문서 확인
- [x] `rg` 기반 기존 로그인/SNS/관리자 샘플 참조 확인
- [x] 남은 Mapper XML 목록 확인
- [ ] Maven compile
- [ ] API 수동 호출
- [ ] Swagger 런타임 확인

## 미검증 사유

- 이번 PR은 문서 범위 확정 작업이므로 Maven compile, API 호출, Swagger 런타임 확인은 수행하지 않았습니다.
- GitNexus MCP 리소스가 현재 세션에 노출되지 않았습니다.
- `gitnexus status`는 stale 상태를 반환했지만, `gitnexus analyze`는 `Not inside a git repository`로 실패했습니다.
- `gitnexus detect_changes`는 `unknown command 'detect_changes'`로 실패했습니다.
- GitNexus impact 분석과 detect_changes는 수행하지 못했고, `rg`와 파일 직접 확인으로 대체했습니다.

## 후속 작업

- `members`, `refresh_tokens` schema와 seed 작성
- `egovframework.healthcenter.member` 하위 Auth/Member 패키지 구현
- `/api/auth/login`, `/api/auth/reissue`, `/api/auth/logout`, `/api/members/me` 구현
- `Authorization: Bearer {accessToken}` 처리 방식 통일
- `ROLE_CITIZEN`, `ROLE_GUARDIAN`, `ROLE_STAFF`, `ROLE_ADMIN` 권한 매핑
- 신규 Auth 구현 후 기존 `/auth/login-jwt`, `/jwtAuthAPI`, `/admin/password`, SNS 샘플 유지/비노출/삭제 재판단
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 후속 Auth/Member 구현 브랜치 생성 또는 다음 작업 문서화

## 변경 파일 요약

| 파일 | 내용 |
|---|---|
| `docs/11_implementation_log/08_Auth_Member_Context_범위_확정_및_브랜치_체크리스트.md` | Auth/Member 구현 범위, 기존 샘플 검토, MVP 권한 정책, 브랜치 체크리스트 작성 |
| `docs/11_implementation_log/09_Auth_Member_Context_범위_확정_PR_작성안.md` | PR 제목, 본문, 검증, 미검증 사유, 후속 작업 초안 작성 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | Auth/Member 범위 확정 진행 상태와 후속 구현 연결 문서 반영 |

## 커밋 메시지 초안

```text
docs: Auth Member 구현 범위 확정

- MVP 사용자 유형과 인증 범위 정리
- 기존 eGovFrame 로그인/SNS/관리자 샘플 유지 여부 검토
- 이번 브랜치 포함/제외 작업과 후속 구현 단위 작성
- Auth/Member 브랜치 체크리스트와 PR 작성안 추가
- 전체 체크리스트에 범위 확정 진행 상태 반영
```
