# eGovFrame 샘플 API 정리 계획 Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `refactor/egov-sample-api-cleanup` |
| base 브랜치 | `main` |
| 작업 성격 | 샘플 API 제거 전 후보 목록과 안전 절차 문서화 |
| 작업 트리 | PR 문서 작성 전 문서 변경 있음 |
| 코드 삭제 | 없음 |
| 빌드 확인 | 문서 변경만 있어 미실행 |
| API 확인 | 문서 변경만 있어 미실행 |

## PR 제목

```text
docs: eGovFrame 샘플 API 정리 후보 문서화
```

## PR 본문

````markdown
## 개요

전자정부프레임워크 Simple Backend Template에 포함된 샘플 API를 바로 삭제하지 않고, 먼저 유지 대상과 제거 후보를 구분하는 정리 계획 문서를 추가했습니다.

이번 PR은 실제 코드 삭제가 아니라, 다음 브랜치에서 사용자가 선택한 기능 묶음 하나만 안전하게 제거하기 위한 사전 문서화 작업입니다.

## 변경 내용

- 브랜치 이름 규칙 문서 확인 기준 반영
- 전체 체크리스트의 `샘플 코드 정리 1차` 관련 진행 상황 보강
- eGovFrame 샘플 API 제거 후보 목록 작성
- 반드시 유지할 eGovFrame 기반 설정과 신규 도메인 코드 목록 작성
- 삭제 보류 또는 신중 검토 대상 정리
- 사용자가 선택할 수 있는 다음 삭제 작업 후보 정리
- 실제 삭제 요청을 받았을 때의 안전 절차 작성

## 제거 후보

- SNS 로그인 샘플
- 게시판 샘플
- 게시판 이용정보/사용자 정보 샘플
- 개인 일정 샘플
- 회원관리 샘플
- JPA/QueryDSL 테스트 샘플
- Selenium 테스트 샘플
- HSQL 및 벤더별 샘플 DB 자원

## 유지 대상

- `EgovBootApplication`
- `egovframework.com.config`
- MyBatis mapper 설정
- Security/JWT 골격
- Swagger/OpenAPI 설정
- `egovframework.healthcenter` 신규 도메인
- `egovframework/mapper/healthcenter` 신규 Mapper
- PostgreSQL 공통코드 SQL

## 검증

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 관련 항목 확인
- [x] `docs/10_backend_transition/03_샘플_코드_정리_범위_및_안전_절차.md` 확인
- [x] `rg` 기반 샘플 코드 구조 확인

## 미검증 사유

- 실제 코드 삭제가 없으므로 Maven 빌드와 API 호출은 수행하지 않았습니다.
- GitNexus 인덱스가 stale 상태였고, `npm.cmd exec -- gitnexus analyze`는 `Not inside a git repository`로 실패했습니다.
- 삭제 대상별 상세 impact 분석은 사용자가 실제 삭제 대상을 선택한 뒤 수행합니다.

## 후속 작업

- 사용자가 제거 후보 중 하나를 선택
- 선택한 기능 묶음의 영향 범위 재확인
- 한 번에 하나의 기능 묶음만 삭제
- 삭제 후 `mvn -q -DskipTests compile` 실행
- 가능하면 백엔드 실행과 공통코드 API 확인
````

## 변경 파일 요약

| 파일 | 내용 |
|---|---|
| `docs/11_implementation_log/06_eGovFrame_샘플_API_정리_계획_및_후보_목록.md` | 제거 후보, 유지 후보, 보류 후보, 안전 절차 문서화 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 샘플 API 제거 후보 선정 작업 기록 추가 |
| `docs/11_implementation_log/07_eGovFrame_샘플_API_정리_계획_PR_작성안.md` | 이번 PR 작성안 추가 |

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] `main` 최신화 확인
- [ ] 사용자가 다음 삭제 대상 선택
- [ ] 후속 브랜치를 `type/topic-kebab-case` 형식으로 생성

## 후속 브랜치 이름 추천

```text
refactor/remove-sns-login-sample
refactor/remove-selenium-test-sample
refactor/remove-jpa-querydsl-test-sample
```
