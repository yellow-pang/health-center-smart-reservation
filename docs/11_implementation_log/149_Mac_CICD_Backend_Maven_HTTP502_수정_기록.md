# Mac CI/CD 백엔드 Maven HTTP 502 수정 기록

작성일: 2026-10-04 (KST). 작업 브랜치: `dev`.

## 배경과 실패 범위

CI/CD를 main에 반영한 첫 [Deploy production 실행](https://github.com/yellow-pang/health-center-smart-reservation/actions/runs/37192152322)에서 `Build and publish backend`가 실패했다. 실행 커밋은 `4e1add0dc948bfaf05f25f6174c0d162ea8f1572`, 실패한 이미지 job은 [Publish ARM64 images](https://github.com/yellow-pang/health-center-smart-reservation/actions/runs/37192152322/job/111406596858)다. 2026-10-04 18:26 KST에 시작해 18:28 KST에 실패했다.

Backend/Frontend/Deployment verification과 CI required는 모두 성공했다. Backend 이미지 빌드의 `dependency:go-offline` 단계에서 중단되어 frontend 이미지 발행과 `Deploy to Mac mini`는 건너뛰었다. 이 실행에서는 운영 앱 교체, DB 백업·최초 Flyway 채택을 시작하지 않았다.

main 보호, production의 main 전용 정책과 Health Center runner Online은 PR 작성 전 GitHub 조회로 확인했다. [148 HTTP 422 수정 기록](148_GitHub_Main_Protection_HTTP422_수정_기록.md)의 최초 설정 오류 이후 실제 설정이 진행된 상태다.

## 원인과 로그 근거

사용자가 전달한 마지막 `buildx failed` 메시지는 Maven 명령이 실패했다는 요약이다. 전체 Actions 로그의 직접 원인은 다음과 같다.

```text
Failed to execute goal org.apache.maven.plugins:maven-dependency-plugin:3.8.1:go-offline
Failed to collect dependencies at org.egovframe.rte:egovframe-rte-fdl-crypto:jar:5.0.0
  -> org.egovframe.rte:egovframe-rte-fdl-property:jar:5.0.0
Could not transfer artifact org.egovframe.rte:egovframe-rte-fdl-property:pom:5.0.0
from/to egovframe2 (https://maven.egovframe.go.kr/maven/):
status code: 502, reason phrase: Bad Gateway (502)
```

전자정부프레임워크 저장소가 필요한 POM 다운로드 요청에 HTTP 502를 반환했다. 이 로그는 Java 컴파일이나 ARM64 지원 오류를 가리키지 않는다. 외부 저장소의 오류가 계속되는지는 별도 확인 대상이며, 이번 변경은 일시적인 전송 오류에 대응하는 재시도다.

CI의 Maven 검증과 Docker builder는 의존성 캐시가 별개다. Docker 이미지 단계는 builder 내부 저장소에서 다운로드하므로 CI가 성공해도 해당 시점의 외부 저장소 응답에 영향을 받는다. `go-offline`을 생략하는 것만으로는 필수 의존성의 다운로드 문제를 해결하지 못하므로 원래 의존성과 패키징 요구를 유지했다.

## 변경 내용

- [backend/scripts/maven-retry.sh](../../backend/scripts/maven-retry.sh): artifact/metadata 전송의 HTTP 408/429/500/502/503/504 및 연결 timeout/reset/refused/DNS 오류에 한해 최대 3회 실행. 재시도 전 10초, 20초 대기.
- 매 시도에 `-U`를 전달해 다운로드 실패가 캐시된 missing release를 다시 확인. `-q` 대신 batch/no-transfer-progress로 Maven 빌드와 실패 로그를 보존.
- 컴파일·설정·404/401 오류는 즉시 실패. 재시도 소진과 중단 시 Maven 종료 코드를 유지. 서로 다른 로그 줄의 404와 무관한 502를 묶어 재시도하지 않음.
- [backend/Dockerfile](../../backend/Dockerfile): 의존성 사전 다운로드와 실제 JAR 패키징에 같은 helper 적용. 두 단계 모두 성공해야 이미지 빌드가 진행됨.
- [CI workflow](../../.github/workflows/ci.yml): helper의 POSIX shell syntax 검사 추가. 기존 CI helper test discovery로 새 회귀 테스트도 실행.

`-U` 동작의 근거는 [Maven CLI 공식 문서](https://maven.apache.org/ref/3.9.9/maven-embedder/cli.html)의 missing release 재조회 설명이다. 저장소 URL, dependency 버전, CI 테스트와 운영 배포 조건은 변경하지 않았다.

## 검증 결과

| 검증 | 결과와 범위 |
|---|---|
| Maven retry 회귀 테스트 | 8건 통과. 실제 형태의 502 후 성공, 최대 3회, 10/20초 대기, 마지막 실패 코드, `-U`와 인자 보존, 로그 보존, compile/404 즉시 실패, 무관한 502, metadata 429, 연결 오류, 중단 코드 확인 |
| CI helper tests | 기존 current-main 3건 포함 총 11건 통과 |
| 실제 Maven 다운로드 | 공식 체크섬을 확인한 임시 JDK 17.0.20.1와 Maven 3.9.11, 새 로컬 저장소로 수정 helper의 `dependency:go-offline` 성공 |
| 실제 JAR 빌드 | 같은 환경에서 `-DskipTests package` 성공. main Java 167개와 test Java 9개 컴파일, Spring Boot JAR 재패키징 완료. 단위/DB 런타임 테스트를 새로 실행한 결과는 아님 |
| Actions 및 shell syntax | actionlint, POSIX `sh -n` 통과 |
| 변경 형식 | `git diff --check` 통과 |
| Linux ARM64 Docker/GHCR/운영 | 수정 후 재실행은 아직 미확인. macOS ARM64 Maven 성공과 구분 |

실제 Maven 검증 명령은 새 임시 로컬 저장소를 지정해 두 단계 모두 실행했다.

```bash
sh backend/scripts/maven-retry.sh -f backend/pom.xml \
  -Dmaven.repo.local='<fresh-temporary-repository>' -DskipTests dependency:go-offline
sh backend/scripts/maven-retry.sh -f backend/pom.xml \
  -Dmaven.repo.local='<same-temporary-repository>' -DskipTests package
```

## 재배포와 완료 기준

수정 커밋을 dev에 push하고 CI가 통과하면 dev → main PR로 반영한다. 이후 새 main 실행의 `Build and publish backend`, frontend 이미지 발행과 Mac 배포를 확인한다. 기존 실패 실행의 Re-run은 이전 커밋의 Dockerfile을 사용하며 새 수정이 적용되지 않는다. main이 전진하면 기존 실행은 최신 main SHA 검사에도 걸릴 수 있다.

최초 DB 채택은 이번 실패에서 실행되지 않았으므로 운영 가이드의 기존 준비를 유지하고 첫 성공 배포에서 백업·데이터 보존·baseline false·배포 SHA를 확인한다. 저장소가 계속 502를 반환하면 최대 3회 후 실패하며 앱 교체는 진행되지 않는다.

- [x] 실제 Actions 로그와 실패 범위 확인
- [x] 제한된 재시도·실패 캐시 갱신·로그 개선과 회귀 검증
- [x] 빈 캐시 Maven 의존성 다운로드 및 JAR 패키징 확인
- [ ] 수정 후 Linux ARM64 이미지 빌드와 GHCR 발행 성공
- [ ] 최초 Mac 배포, 공개 UI/API·버전·기존 데이터 보존 확인

운영 절차는 [14 GitHub Actions Mac CI/CD](../08_deploy/14_GitHub_Actions_Mac_CICD.md)를 따른다.
