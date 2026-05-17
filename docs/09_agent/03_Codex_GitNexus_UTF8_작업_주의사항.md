# Codex, GitNexus, UTF-8 작업 주의사항

## 문서 목적

이 문서는 코드 에이전트가 이 프로젝트에서 문서와 코드를 읽고 수정할 때 주의해야 할 작업환경 기준을 정리한다.

개발 기능 설계는 다른 문서에서 다루며, 이 문서는 도구 사용과 인코딩 문제를 줄이는 데 집중한다.

## PowerShell 한글 문서 읽기

한글 문서는 UTF-8 기준으로 읽는다.

권장 명령:

```powershell
Get-Content -Path docs/README.md -Encoding UTF8
```

주의:

- `-Encoding UTF8` 없이 읽으면 콘솔 환경에 따라 한글이 깨질 수 있다.
- 문서 내용을 판단하기 전에 반드시 한글이 정상 표시되는지 확인한다.
- 문서 파일을 새로 만들거나 수정할 때도 UTF-8을 유지한다.

## rg 사용

이 프로젝트의 코드 탐색은 `rg`를 우선 사용한다.

예시:

```bash
rg -n "CommonCodeController" backend/src docs
rg --files docs
```

사용 목적:

- 클래스/메서드 참조 확인
- 샘플 코드 제거 전 영향 범위 확인
- JPA/MyBatis 같은 기술 기준 충돌 확인
- 문서 간 중복 내용 확인

## GitNexus 사용

코드 구조 탐색, 영향도 분석, 대규모 샘플 코드 정리 전에는 GitNexus 상태를 확인한다.

권장 명령:

```powershell
gitnexus status
```

인덱스가 없거나 오래된 경우:

```powershell
gitnexus analyze
```

인덱싱된 저장소 목록 확인이 필요한 경우:

```powershell
gitnexus list
```

주의:

- 이 프로젝트는 로컬에 설치된 GitNexus CLI를 직접 사용하므로 `npm.cmd exec -- gitnexus ...` 또는 `npx gitnexus ...`를 기본 명령으로 쓰지 않는다.
- 현재 설치된 GitNexus CLI 버전에는 `detect-change`, `detect-changes`, `detect_changes` 명령이 없으므로 변경 범위 확인은 `git status`, `git diff --stat`, `git diff --check`, `rg`, Maven/Next build로 보완한다.
- GitNexus가 stale 상태이거나 실패하면 작업을 중단하지 말고 실패 이유를 기록한다.
- GitNexus 결과만 믿지 않고 `rg`, 파일 직접 확인, 테스트를 함께 사용한다.
- 문서만 수정하는 작업에서는 GitNexus가 필수는 아니다.

## 런타임 실행 작업 기준

이 프로젝트에서는 권한, 방화벽, 포트 점유, Docker Desktop 권한 문제로 에이전트가 직접 런타임 검증을 수행하면 불필요한 오류가 생길 수 있다.

따라서 에이전트는 아래 작업을 직접 실행하지 않는다.

| 직접 실행하지 않는 작업 | 대신 할 일 |
|---|---|
| `mvn spring-boot:run` 등 서버 기동 | 사용자가 VS Code Spring Boot Dashboard 등으로 실행할 기준과 기대 로그/URL 안내 |
| `docker compose up/down` | 사용자가 실행할 명령과 확인 방법 안내 |
| 실제 API 런타임 호출 | Swagger `Try it out` 대표 예시 1개와 기대 JSON 안내 |
| Swagger UI 브라우저 확인 | 접속 URL, 인증 방법, 확인할 Controller/Mapping 안내 |
| 포트 점유 프로세스 종료 | 포트 확인 명령과 종료 방법 안내 |
| 방화벽/관리자 권한이 필요한 작업 | 사용자가 직접 수행하도록 안내 |

에이전트가 직접 실행해도 되는 검증은 서버 기동이 필요 없는 명령으로 제한한다.

```powershell
git status --short
rg -n "검색어" backend/src docs
git diff --check
cd backend
mvn -q -DskipTests compile
mvn -q test-compile
```

런타임 확인이 필요한 경우 완료 보고에는 다음처럼 구분한다.

```text
빌드: 에이전트가 직접 확인
테스트 컴파일: 에이전트가 직접 확인
실행/API: 사용자 직접 확인 필요, Docker/Spring Boot Dashboard 기준과 Swagger 대표 예시 1개 제공
```

## 샘플 코드 정리 전 확인

샘플 코드 삭제 전에는 아래 순서로 확인한다.

1. GitNexus 상태 확인
2. `rg`로 Controller, Service, DAO, Mapper, Test 참조 확인
3. 삭제 대상과 유지 대상을 문서에 먼저 기록
4. 한 기능 묶음씩 제거
5. 빌드 확인
6. 신규 보건소 API 동작은 사용자가 Swagger에서 직접 확인할 수 있게 대표 예시 1개와 기대 결과 안내

참고 문서:

- `docs/10_backend_transition/03_샘플_코드_정리_범위_및_안전_절차.md`
- `docs/10_backend_transition/04_DB_접근_방식_JPA_MyBatis_판단.md`

## 현재 프로젝트 핵심 기준

코드 에이전트는 작업 전 아래 기준을 다시 확인한다.

- 백엔드는 eGovFrame Simple Backend Template 기반이다.
- Maven 구조를 유지한다.
- Spring Initializr로 새 백엔드 프로젝트를 만들지 않는다.
- Maven과 Gradle을 혼용하지 않는다.
- MVP 신규 도메인은 MyBatis 기준으로 구현한다.
- JPA/QueryDSL은 현재 템플릿 테스트/샘플 성격으로 보고 정리 후보로 둔다.
- 신규 보건소 도메인은 `egovframework.healthcenter` 하위에 작성한다.
- Mapper XML은 `src/main/resources/egovframework/mapper/healthcenter` 하위에 둔다.
- 신규 API 응답은 `success + data + error` 구조를 사용한다.

## 완료 보고 기준

에이전트는 작업 후 아래 항목을 간단히 보고한다.

- 변경한 문서 또는 코드 파일
- 기존 문서와 중복을 피한 방식
- 확인한 명령 또는 확인하지 못한 이유
- 다음 작업 추천
