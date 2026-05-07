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

```bash
npm.cmd exec -- gitnexus status
```

인덱스가 없거나 오래된 경우:

```bash
npm.cmd exec -- gitnexus analyze
```

주의:

- PowerShell 실행 정책에 따라 `npm` 대신 `npm.cmd`를 사용해야 할 수 있다.
- GitNexus가 stale 상태이거나 실패하면 작업을 중단하지 말고 실패 이유를 기록한다.
- GitNexus 결과만 믿지 않고 `rg`, 파일 직접 확인, 테스트를 함께 사용한다.
- 문서만 수정하는 작업에서는 GitNexus가 필수는 아니다.

## 샘플 코드 정리 전 확인

샘플 코드 삭제 전에는 아래 순서로 확인한다.

1. GitNexus 상태 확인
2. `rg`로 Controller, Service, DAO, Mapper, Test 참조 확인
3. 삭제 대상과 유지 대상을 문서에 먼저 기록
4. 한 기능 묶음씩 제거
5. 빌드와 신규 보건소 API 동작 확인

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
