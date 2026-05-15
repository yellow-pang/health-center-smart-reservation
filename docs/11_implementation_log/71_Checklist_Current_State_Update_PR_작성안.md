# Checklist Current State Update Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `docs/check-list-update` |
| base 브랜치 | `main` 추정 |
| 작업 트리 | 문서 갱신 진행 |
| 주요 커밋 | 아직 커밋하지 않음 |
| 백엔드 compile | `mvn.cmd -q -DskipTests compile` 통과 |
| 백엔드 test-compile | `mvn.cmd -q test-compile` 통과 |
| 프론트 빌드 | `npm.cmd run build` 통과 |
| 정적 공백 확인 | `git diff --check` 통과. 기존 문서 CRLF 변환 경고만 표시 |
| 실행/API 확인 | 미수행. 사용자가 Docker/Spring Boot Dashboard/Swagger/브라우저로 직접 확인 필요 |

## PR 제목

```text
docs: 전체 체크리스트 현재 구현 상태 갱신
```

## PR 본문

````markdown
## 개요

현재 코드와 구현 기록을 기준으로 전체 작업 체크리스트를 다시 정리합니다.

이미 구현 및 정적 검증이 끝난 백엔드/프론트/DB 항목을 체크하고, 아직 사용자가 직접 확인해야 하는 Swagger, 브라우저, Docker, 운영 문서, 테스트 보강 작업은 별도 남은 작업 리스트로 분리했습니다.

## 변경 내용

- 전체 체크리스트에 현재 구현 확인 요약 추가
- 백엔드 Context 구현 항목의 완료 상태 갱신
- 프론트엔드 화면 및 API 연동 항목의 완료 상태 갱신
- PostgreSQL schema, seed, MyBatis Mapper, 백엔드 빌드 검증 상태 갱신
- 런타임 검증, 브라우저 검증, 테스트 보강, 운영/배포 문서, 정책 결정 작업을 남은 작업 리스트로 정리
- 이번 브랜치 구현 기록과 PR 작성안 추가

## 검증

- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [x] `npm.cmd run build`
- [x] `git diff --check`
- [ ] Swagger 대표 흐름 확인
- [ ] 브라우저 주요 화면 확인

## 미검증 사유

- 서버 기동, Docker 실행, Swagger Try it out, 브라우저 확인은 프로젝트 운영 기준상 사용자가 직접 수행합니다.
- 이번 브랜치는 문서 갱신 작업이므로 신규 API 구현이나 런타임 호출은 포함하지 않았습니다.

## 추가 테스트 체크리스트

- [ ] Swagger 로그인 대표 흐름 확인
- [ ] Swagger 예약 신청/조회/취소 대표 흐름 확인
- [ ] Swagger 체크인/현장 접수/대기열 대표 흐름 확인
- [ ] Swagger 관리자 기준정보 대표 흐름 확인
- [ ] 브라우저 시민 예약 흐름 확인
- [ ] 브라우저 직원 접수/대기열 흐름 확인
- [ ] 브라우저 관리자 대시보드/기준정보 흐름 확인

## 남은 위험

- 체크된 구현 항목은 정적 확인과 빌드 기준이며, 실제 운영 흐름의 성공을 의미하지 않습니다.
- Swagger와 브라우저 확인 결과에 따라 체크리스트 일부가 다시 조정될 수 있습니다.
- Docker Compose, 환경변수, 실행 README 정리가 아직 남아 있습니다.

## 후속 작업

- Docker PostgreSQL 기준 런타임 검증
- Swagger 주요 API 대표 흐름 확인
- 브라우저 역할별 화면 확인
- 핵심 정책 테스트 추가
- 실행/운영 README와 포트폴리오 문서 보강
````

## 커밋 메시지

제목:

```text
docs: 전체 체크리스트 현재 구현 상태 갱신
```

본문:

```text
- 현재 코드와 구현 기록을 기준으로 전체 작업 체크리스트의 완료 상태를 재분류
- 백엔드/프론트/DB 구현 및 정적 검증 완료 항목과 런타임 미확인 항목을 분리
- Swagger, 브라우저, 테스트, 운영/배포 문서, 정책 결정 후속 작업 목록 추가
- 이번 브랜치 구현 기록과 PR 작성안 문서 추가
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 후속 런타임 검증 브랜치 생성 여부 결정
