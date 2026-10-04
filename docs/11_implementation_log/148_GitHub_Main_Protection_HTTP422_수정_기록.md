# GitHub main 보호 설정 HTTP 422 수정 기록

작성일: 2026-10-04 (KST). 작업 브랜치: `dev`, 기준 커밋: `750e4e3`.

## 1. 발생 상황과 확인 시점

Mac CI/CD 최초 활성화 중 `configure-github.sh --apply`가 main 보호 설정 요청에서 중단됐다. 다음 기록은 사용자 터미널 출력과 오류 진단 시점의 GitHub 조회 결과를 기준으로 한다. 사용자가 이후 진행한 설정 결과는 확인되면 추가한다.

| 시점/근거 | 확인 결과 |
|---|---|
| 2026-09-27 GitHub 기록 | [PR #69](https://github.com/yellow-pang/health-center-smart-reservation/pull/69)가 dev에 병합됨. [PR CI](https://github.com/yellow-pang/health-center-smart-reservation/actions/runs/36318098232)와 [dev push CI](https://github.com/yellow-pang/health-center-smart-reservation/actions/runs/36318224955) 모두 성공 |
| 2026-10-04 사용자 실행 출력 | `prepare-mac.sh --allow-initial-adoption .env` 성공. 운영 env 복사, 운영 디렉터리 준비와 최초 DB 채택 허용 파일 생성 완료. 컨테이너/DB 내용 변경은 수행하지 않음 |
| 같은 세션의 사용자 실행 출력 | `configure-github.sh --apply`에서 아래 HTTP 422 발생 |
| 오류 진단 시점 GitHub 조회 | 외부 PR 승인 정책은 `all_external_contributors`로 적용됨. main은 `protected: false`, environments는 0개로 production 미생성 |
| 수정 후 로컬 검증 | 설정 스크립트 모의 API 테스트 14건, shell syntax, `git diff --check` 통과 |

실패 요청: `PUT /repos/yellow-pang/health-center-smart-reservation/branches/main/protection`.

```text
gh: Invalid request.
No subschema in "anyOf" matched.
More than one subschema in "oneOf" matched.
Not all subschemas of "allOf" matched.
For 'anyOf/1', {"strict" => true, "contexts" => [], "checks" => [{"context" => "CI required", "app_id" => 15368}]} is not a null. (HTTP 422)
```

## 2. 원인과 수정

`required_status_checks`에 기존 `contexts` 형식과 앱을 지정하는 `checks` 형식을 함께 보냈다. 실제 서버의 `oneOf` 오류와 요청 내용을 대조하여 두 형식이 동시에 일치하는 문제로 진단했다. `contexts`를 제거하고 `strict`, `CI required`, GitHub Actions의 `app_id`는 유지했다.

```json
{
  "required_status_checks": {
    "strict": true,
    "checks": [{"context": "CI required", "app_id": 15368}]
  }
}
```

변경 파일:

- [configure-github.sh](../../scripts/setup/configure-github.sh): main 보호 요청에서 `contexts` 제거.
- [test_setup.py](../../scripts/setup/tests/test_setup.py): 두 형식을 함께 보내면 HTTP 422를 반환하도록 모의 API 보강. 부분 적용 이후 재실행 시 기존 외부 PR 승인 정책을 다시 쓰지 않는 테스트 추가.
- [운영 가이드](../08_deploy/14_GitHub_Actions_Mac_CICD.md): 오류 검색 문구와 재실행 절차 추가.

기존 테스트의 `gh` 모의 도구는 모든 PUT 요청을 성공으로 처리해 이 요청 형식 오류를 발견하지 못했다. 보강한 모의 도구로 수정 전 신규 설정/부분 적용 재시도 테스트 2건 실패를 확인했고, 수정 후 14건이 통과했다. 이는 회귀 검증이며 실제 GitHub API의 수정본 적용 성공을 증명하지는 않는다.

참고: [GitHub branch protection API](https://docs.github.com/en/rest/branches/branch-protection#update-branch-protection)는 앱을 지정하는 경우 `checks` 사용을 권장한다. 진단 근거는 실제 오류 응답이며 공개 API 문서만으로 서버 오류를 재현했다고 기록하지 않는다.

## 3. 재개 절차와 확인 기준

성공한 `prepare-mac.sh`를 다시 실행할 필요는 없다. 수정된 checkout에서 다음 명령을 재실행한다. 스크립트는 적용된 외부 PR 승인 정책을 확인해 건너뛰고 main 보호, production 환경과 main 전용 배포 정책을 설정한다.

```bash
bash scripts/setup/configure-github.sh --apply
```

성공 출력:

```text
Verified production main-only, main PR/CI protection, and approval for every external contributor workflow.
```

재실행 후 조사 시 확인할 항목:

- main 보호: `strict: true`, `CI required`의 GitHub Actions 앱 연결, PR 필수, 관리자 포함 적용, force push/삭제 금지.
- production 환경: custom branch policy 사용, 허용된 배포 branch는 `main`만 존재.
- 외부 PR 승인 정책: `all_external_contributors` 유지.
- 다음 단계: `bash scripts/setup/install-mac-runner.sh` 실행 후 runner Online 확인. 전체 배포 순서는 운영 가이드를 따른다.

## 4. 완료 상태와 후속 기록

- [x] 원인·원본 오류·실패 요청·부분 적용 상태 기록
- [x] 요청 수정과 회귀 테스트, 재실행 절차 기록
- [ ] 수정된 스크립트의 실제 GitHub 적용 성공 확인
- [ ] Health Center 전용 Mac runner 등록 및 Online 확인
- [ ] main 최초 자동 배포와 공개 URL/버전/기존 데이터 보존 확인
- [ ] 두 번째 자동 배포와 실제 운영 복구 검증

실제 재실행 결과는 아직 전달받지 않았다. main 보호/production 미설정은 오류 진단 시점의 상태이며 이후 실행 결과로 갱신해야 한다. 최초 DB 채택 허용 파일 생성은 DB 채택 완료와 별도이며, 실제 배포에서 백업·전환·검증 후 소비된다. 기존 구현 당시 상태는 [146 구현 기록](146_Mac_CICD_구현_기록.md)에 보존한다.
