# Mac CI/CD Docker Keychain 오류 수정 기록

작성일: 2026-10-04 (KST). 작업 브랜치: `dev`.

후속 확인: [실행 37199699624](https://github.com/yellow-pang/health-center-smart-reservation/actions/runs/37199699624)에서 Mac registry 로그인, 이미지 다운로드, DB 백업·앱 교체와 내부 readiness·버전 검사가 통과했다. 마지막 공개 검사 실패로 workflow 전체는 실패했고, 후속 직접 검사에서 HTTP 403을 재현했다. 원인 대조와 후속 수정은 [151 공개 검증 HTTP 403 기록](151_Mac_CICD_공개_검증_HTTP403_오류_수정_기록.md)을 따른다. 아래 최초 Keychain 실패 기록은 보존한다.

## 배경과 실제 실패 범위

PR #71 반영 후 [Deploy production 실행](https://github.com/yellow-pang/health-center-smart-reservation/actions/runs/37195551675)은 CI와 backend/frontend ARM64 이미지 빌드·GHCR 발행에 성공했다. 실행 SHA는 `f7b4abe8995461b183ef21e415d7df35727a5565`다.

[Deploy to Mac mini job](https://github.com/yellow-pang/health-center-smart-reservation/actions/runs/37195551675/job/111417355859)의 Docker engine/Compose 확인과 최신 main 검사는 통과했지만, `docker/login-action`에서 다음 오류가 발생했다. 실행은 19:29:58 KST에 시작했으며 로그인 오류는 19:35:16 KST에 기록됐다.

```text
error saving credentials: error storing credentials - err: exit status 1,
out: User interaction is not allowed. (-25308)
```

`Pull, back up, deploy and verify`는 건너뛰었다. 따라서 이 실행에서는 이미지 다운로드·운영 DB 백업·앱 교체·최초 Flyway 채택을 시작하지 않았다. 앞선 Maven HTTP 502 문제와 다른 실패 단계다.

## 원인

workflow가 임시 `DOCKER_CONFIG` 디렉터리를 만들었지만 초기 `config.json`은 없었다. Docker CLI는 인증 설정이 비어 있으면 macOS 기본 `osxkeychain` helper를 탐지한다. 그 결과 runner의 무인 실행에서 키체인 저장이 차단됐다.

이 오류는 registry 인증 정보를 로컬에 저장하는 과정의 실패이며 추가 GitHub Secret 등록으로 해결하는 문제가 아니다. `GITHUB_TOKEN`은 작업별 자동 발급 토큰이고 Mac job에는 이미 `packages: read`가 있다. 빈 `credsStore` 값만 지정해도 기본 helper 탐지를 막을 수 없다.

근거: [Docker 로그인 기본 동작](https://docs.docker.com/reference/cli/docker/login/#default-behavior), [Docker CLI config 로딩](https://github.com/docker/cli/blob/master/cli/config/config.go), [ContainsAuth 판정](https://github.com/docker/cli/blob/master/cli/config/configfile/file.go), [GitHub 자동 토큰](https://docs.github.com/en/actions/concepts/security/github_token).

## 수정 내용

- `.github/workflows/deploy.yml`의 Mac 준비 단계에서 `{"auths":{"ghcr.io":{}}}`로 임시 config를 초기화한다. 비어 있지 않은 `auths`가 기본 helper 탐지를 막으며 GHCR 항목에는 아직 토큰이 없다.
- 임시 디렉터리 mode 700과 config 파일 mode 600을 적용한다. 기존 사용자 Docker config는 복사하거나 변경하지 않고 Compose plugin 경로만 연결한다.
- Mac `docker/login-action`에 `logout: false`를 지정한다. workflow 마지막 `always()` 단계가 해당 임시 config를 삭제하며, 삭제 뒤 액션 post hook이 다시 기본 키체인을 탐지하지 않도록 한다.
- 이미지 발행 job의 로그인은 기존 기본 logout 동작을 사용한다.
- `scripts/ci/tests/test_registry_credentials.py`에 실제 workflow shell을 임시 파일 환경에서 실행하는 회귀 테스트를 추가했다. 기존 CI helper discovery에서 자동 실행된다.

파일에 저장되는 인증 정보는 암호화 저장소가 아니므로 작업 중 접근 권한을 제한하고 작업 종료 시 제거한다. 토큰 값은 로그·Git·운영 환경 파일에 기록하지 않는다. 사용자 결정에 따라 공개 저장소와 공개 GHCR 이미지를 사용한다.

## 검증 결과와 한계

| 검증 | 결과 |
|---|---|
| 임시 인증 설정 회귀 테스트 | 핵심 3건 통과. GHCR 초기 설정·700/600 권한·기존 config 보존, always 정리·사용자 config 보호·로그 미노출, Mac logout 설정 확인 |
| CI helper tests | 기존 11건 포함 총 14건 통과 |
| workflow 문법 | 공식 체크섬을 확인한 actionlint 통과 |
| 변경 형식 | `git diff --check` 통과 |
| 실제 Mac registry 로그인·내부 배포 | 후속 실행 37199699624에서 로그인·다운로드·백업·앱 교체·내부 readiness/버전 통과. 공개 검사 실패는 151 기록 참고 |

회귀 테스트는 Docker·키체인·서버를 실행하지 않는다. 실제 로그인과 내부 배포 성공은 후속 main 실행에서 별도로 확인했으며 공개 검사까지 포함한 전체 성공은 남아 있다. 공개 이미지의 내용 검토는 별도 보안 점검 범위이며 이 회귀 테스트로 대체하지 않는다.

기존 CI/setup/deploy 테스트에는 Mac registry 인증 설정 검사가 없어 이 파일은 해당 실패를 재발 방지하는 검사로 유지한다. 이번 수정에 직접 필요한 3건만 남기고, 변경하지 않은 재실행 디렉터리·미설정 cleanup 등의 별도 사례는 제거했다.

## 포트폴리오 공개 범위와 비용 관련 점검

사용자는 전자정부프레임워크 학습·포트폴리오 데모로 공개 저장소와 공개 GHCR을 사용하며, 개발 기본값·샘플 자료는 실제 서비스의 개선 참고 사항으로 남기기로 했다. 이번 추가 점검은 실제 외부 서비스 인증정보가 공개 파일이나 빌드 산출물에 포함되는지에 집중했다.

- 현재 사용하는 Kakao/Naver/Google OAuth 비밀값을 메모리에서 비교했으며 Git 추적 파일 627개와 로컬 JAR 앱 항목 200개에서 일치 항목을 발견하지 못했다. 실제 값은 출력하거나 기록하지 않았다.
- frontend 빌드 입력은 공개 API·사이트 주소 두 개이며, `.env`는 제외된다. OAuth secret은 backend 실행 시 주입하며 이미지 빌드 인자로 전달하지 않는다.
- 현재 앱/빌드 코드에서 AI·결제·SMS·메일·클라우드 관리용 유료 API 인증정보가 포함되는 경로를 발견하지 못했다. 호스트의 별도 인증 파일과 계정의 과금 상태까지 확인한 결과는 아니다.
- 전체 GHCR 레이어·과거 Git 이력은 감사하지 않았다. 현재 소스·빌드 설정·로컬 산출물 확인을 비용 발생 가능성이 전혀 없다는 보증으로 확대하지 않는다.

공개 표준 runner와 self-hosted runner의 Actions 사용, 공개 패키지 사용은 현재 GitHub 정책상 무료다. 특히 GHCR container 이미지의 저장·대역폭은 현재 무료로 안내된다. 별도 서비스·계정 설정과 저장소 전체 사용량은 이 설명의 범위에 포함하지 않는다. [Actions 과금](https://docs.github.com/en/billing/concepts/product-billing/github-actions), [Packages 과금](https://docs.github.com/en/billing/concepts/product-billing/github-packages).

다음 실제 서비스 프로젝트에서는 prod의 공개 기본 비밀값 사용 거부, 운영 자격증명 분리·교체, 가상 데이터만 포함하는 빌드, 유료 API의 권한·사용량 한도를 준비 항목으로 삼는다.

## 재배포와 완료 기준

1. 수정 커밋을 dev에 push하고 PR CI를 확인한다.
2. dev → main PR 반영 후 새 배포 실행에서 Mac 로그인과 임시 config 정리를 확인한다.
3. 이전 두 실패 실행에서는 최초 DB 채택을 시작하지 않았으나 후속 실행은 백업·앱 교체·내부 검사까지 진행했다. 현재 재개 위치와 공개 검사 완료 기준은 151 기록을 따르며 데이터 보존·운영 설정·공개 URL 확인은 따로 완료한다.

이전 실패 실행의 Re-run은 이전 workflow를 사용하므로 이번 수정이 적용되지 않는다. main이 전진하면 최신 main 검사에서도 거부될 수 있다. SIGKILL·전원 중단처럼 정리 단계가 실행되지 못하는 경우는 `always()`만으로 제거를 보장하지 않는다.

- [x] 실제 로그·원인·실패 범위 확인
- [x] 임시 config 초기화와 cleanup 담당 단계 수정
- [x] 회귀 테스트·workflow 문법 검증
- [x] 수정 후 실제 Mac registry 로그인 성공
- [ ] 공개 검사까지 포함한 자동 배포 전체 성공 (151 후속 기록)

관련 문서: [운영 가이드](../08_deploy/14_GitHub_Actions_Mac_CICD.md), [이전 Maven HTTP 502 기록](149_Mac_CICD_Backend_Maven_HTTP502_수정_기록.md).
