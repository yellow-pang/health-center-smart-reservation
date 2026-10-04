# GitHub Actions + GHCR + Mac CI/CD

작성일: 2026-09-27. 구현 브랜치: `feat/mac-cicd`.

## 1. 구현과 활성화 상태

CI/CD 코드와 격리 검증을 추가했다. GitHub에 commit/push, runner 등록, GitHub 설정 변경, 운영 배포는 이 구현 작업에서 수행하지 않았다. 아래 최초 설정을 마친 뒤 `dev → main` 머지로 운영 앱이 갱신된다. 기존 Mac 서비스는 유지된다.

후속 확인: PR #69 dev 병합과 실제 PR/dev CI 성공은 확인했다. 2026-10-04 최초 설정 중 `prepare-mac.sh`가 성공했으며 main 보호 요청의 HTTP 422를 수정했다. 발생 당시 상태, 수정·검증 및 남은 활성화 작업은 [148 HTTP 422 수정 기록](../11_implementation_log/148_GitHub_Main_Protection_HTTP422_수정_기록.md)을 따른다.

이후 main 보호/production 정책과 runner Online을 확인했다. 첫 main 배포는 CI 성공 후 eGov Maven 저장소 HTTP 502로 backend 이미지 빌드에서 중단됐으며 Mac 배포는 실행되지 않았다. 재시도 처리와 빈 캐시 Maven 빌드 검증, 재배포 확인 항목은 [149 Maven HTTP 502 수정 기록](../11_implementation_log/149_Mac_CICD_Backend_Maven_HTTP502_수정_기록.md)에 기록했다.

PR #71 후속 실행은 CI와 두 ARM64 이미지 발행에 성공했으나 Mac registry 로그인에서 Keychain `-25308`로 중단됐다. 임시 Docker config 초기화·정리 수정과 회귀 검증은 [150 Docker Keychain 수정 기록](../11_implementation_log/150_Mac_CICD_Docker_Keychain_오류_수정_기록.md)을 따른다. 실제 첫 자동 배포 완료는 아직 확인하지 않았다.

실행 37199699624에서는 Mac 로그인·다운로드·DB 백업·앱 교체와 내부 readiness·버전 검사가 통과했다. 마지막 공개 검사가 Cloudflare의 Python 기본 User-Agent 차단(HTTP 403, 오류 1010)으로 실패했다. 같은 호스트에서 배포용 식별 User-Agent로 세 공개 URL의 정상 응답을 확인했으며 수정·검증과 남은 전체 배포 확인은 [151 공개 검증 HTTP 403 기록](../11_implementation_log/151_Mac_CICD_공개_검증_HTTP403_오류_수정_기록.md)을 따른다. 정상 앱은 유지한다.

| 이벤트 | 동작 |
|---|---|
| `dev` push | Java 17 단위/DB migration 테스트, prod 기동 검사, Node 24 lint/typecheck/build, 배포·설정 스크립트 검사 |
| `dev` 또는 `main` 대상 PR | 동일 CI. 운영 비밀값이나 Mac runner를 사용하지 않음 |
| `main` push | 동일 커밋 CI 성공 → Linux ARM64 이미지 빌드 → GHCR 발행 → Mac pull/백업/교체/검증 |
| Deploy production 수동 실행 | `main`의 최신 SHA만 허용. 최초 DB 채택 flag를 명시할 수 있음 |

`main` push는 dev PR 머지 외의 main 변경도 포함한다. main은 PR + `CI required` 검사 통과를 필수로 설정한다. 기본 브랜치 dev를 변경할 필요는 없다. CI required는 모든 검증 job의 성공을 확인하며, 취소/실패/skip을 성공으로 처리하지 않는다.

## 2. 파일과 운영 계약

- `.github/workflows/ci.yml`: PR/dev CI 및 main에서 재사용하는 검사.
- `.github/workflows/deploy.yml`: 검증, GHCR 발행, production 환경 배포. 액션은 조회한 commit SHA로 고정.
- `docker-compose.deploy.yml`: backend/frontend 두 서비스만 관리. 빌드하지 않고 digest 이미지 실행.
- `scripts/deploy/deploy.sh`: 최초 채택, 배포, 실패 복구, 수동 복구 진입점.
- `scripts/setup/*`: 운영 디렉터리, GitHub 정책, runner 최초 설정.
- `backend/src/main/resources/db/migration`: Flyway V1 스키마/V2 필수 공통코드. 이후 변경은 새 버전 파일로 추가.

보존 대상은 Compose project `health-center`, network `health-center_health-center-network`, PostgreSQL 컨테이너 `health-center-postgres`, volume `health-center_health-center-postgres-data`다. 앱은 기존 network의 `postgresql:5432`에 연결한다. DB/Prometheus/Grafana/Loki/Promtail은 기존 Compose에 남는다. 새 배포는 `down`, `--remove-orphans`, volume/image prune을 사용하지 않는다.

외부 주소는 frontend `https://demo.healthq.store`, backend `https://api.healthq.store`이며 localhost publish는 각각 3000/8080이다. Cloudflare route/Access 설정은 바꾸지 않는다. `/actuator/prometheus` 외부 보호와 내부 scrape는 기존대로 유지한다.

## 3. 최초 활성화 순서

이 절차는 코드가 검토되어 dev에 반영된 뒤 실행한다. 실행 계정은 현재 OrbStack과 운영 컨테이너를 사용하는 `tro`이며 관리자 `sudo`를 쓰지 않는다.

1. 작업 브랜치를 PR로 dev에 반영한다. PR의 `CI required`가 통과하는지 확인한다. GitHub에서 실제 실행 전에는 로컬 검사만으로 Actions 성공을 기록하지 않는다.
2. Mac의 해당 코드 checkout에서 현재 운영 `.env`를 준비한다. 실제 값을 출력하거나 GitHub Secret에 통째로 올리지 않는다.

```bash
bash scripts/setup/prepare-mac.sh --allow-initial-adoption .env
```

이 명령은 `/Users/tro/services/health-center/shared/production.env`를 mode 600으로 복사하고 운영 디렉터리를 준비한다. 기존 파일은 덮어쓰지 않는다. `--allow-initial-adoption`은 최초 배포에서 백업 후 기존 DB를 Flyway 관리에 편입하도록 허용하는 1회용 파일을 만든다. 이 시점에는 컨테이너나 DB를 변경하지 않는다. flag를 생략하면 첫 배포 전에 별도 `workflow_dispatch`에서 `bootstrap_existing_db=true`를 지정해야 한다.

현재 `.env`의 DB/JWT/OAuth/CORS 값과 운영 공개 URL을 유지한다. 필수 키는 `infra/deploy/production.env.example`을 참고한다. 운영 Compose는 `prod`, SQL seed 금지, 재설정 토큰 노출 금지를 강제한다. frontend에는 DB/JWT/OAuth secret이 전달되지 않는다.

3. GitHub CLI가 해당 저장소 관리자 권한으로 로그인된 상태에서 정책을 적용한다.

```bash
bash scripts/setup/configure-github.sh --apply
```

`HTTP 422`와 `More than one subschema in "oneOf" matched`가 나오면 `required_status_checks`에 `contexts`와 `checks`를 함께 보내던 이전 스크립트인지 확인한다. 수정된 스크립트는 GitHub Actions 앱에 연결된 `checks`만 보낸다. 해당 오류로 중단된 경우 위 명령을 다시 실행하면 이미 적용된 외부 PR 승인 정책은 유지하고 남은 설정을 진행한다. 성공한 `prepare-mac.sh`를 다시 실행할 필요는 없다.

원본 오류, 실패한 API 요청, 부분 적용 상태와 회귀 검증은 [148 HTTP 422 수정 기록](../11_implementation_log/148_GitHub_Main_Protection_HTTP422_수정_기록.md)에 보존한다. 로컬 테스트 통과와 실제 GitHub 재실행 성공은 구분해 확인한다.

production은 main branch만 허용한다. main은 PR, 최신 base 기준 CI, GitHub Actions 앱이 보고한 `CI required`를 요구하고 force push/삭제를 금지한다. 개인 저장소에서 본인 PR을 처리할 수 있도록 필수 승인 인원은 0명이며, 자동 배포 전에 별도 수동 environment 승인을 추가하지 않는다. 기존 정책과 충돌하면 덮어쓰지 않고 중단한다.

공개 저장소의 모든 외부 기여자 PR workflow는 실행 승인 대상으로 설정한다. runner label과 environment만으로 악성 PR을 격리할 수는 없다. 외부 PR 승인 전 workflow 변경과 self-hosted 사용 여부를 검토해야 한다. 운영 runner에서 임의 PR 코드를 실행하지 않는다.

4. Health Center 전용 runner를 설치한다.

```bash
bash scripts/setup/install-mac-runner.sh
```

공식 macOS ARM64 archive의 SHA-256을 확인한 뒤 `/Users/tro/actions-runner-health-center`에 등록한다. 이름은 `health-center-mac-mini`, 추가 label은 `health-center-prod`다. 기존 RWR runner를 공유하지 않는다. 사용자 LaunchAgent이므로 로그인 후 실행되며, 현재 OrbStack의 로그인 의존성도 유지된다.

5. GitHub Settings → Actions → Runners에서 Online을 확인하고 dev → main PR을 머지한다. 기존 VM Jenkins Job은 비활성화한다. 저장소 Jenkinsfile에도 중단 안내가 있어 옛 polling Job이 같은 main을 VM에 재배포하지 않는다. Jenkins home/VM 데이터는 자동 삭제하지 않는다.
6. 첫 자동 배포의 모든 job과 공개 URL/버전을 확인한다. 최초 성공 후 채택 허용 파일은 소비되고 backend의 baseline 권한은 false로 되돌린다. 이후 main 머지는 추가 입력 없이 배포된다.

GHCR push는 해당 workflow의 `GITHUB_TOKEN`과 `packages: write`, Mac pull은 `packages: read`를 쓴다. 장기 PAT를 운영 env에 넣을 필요는 없다. 신규 패키지의 repository 연결/Actions 읽기 권한을 유지한다. Docker 자격증명은 runner 임시 디렉터리에만 두고 종료 시 제거한다. 별도 Docker config에서도 OrbStack socket과 Compose plugin 경로를 지정한다.

Mac 임시 config는 `{"auths":{"ghcr.io":{}}}`로 초기화해 기본 Keychain helper 탐지를 막는다. 디렉터리 700/파일 600을 적용하고 Mac login-action의 `logout: false`와 마지막 `always()` 삭제를 함께 사용한다. `User interaction is not allowed. (-25308)`은 추가 Secret 등록 대신 이 저장 방식이 적용됐는지 확인한다. 사용자 Docker config나 키체인 전체를 초기화하지 않는다.

현재 프로젝트는 사용자 결정에 따라 공개 포트폴리오 데모로 운영한다. 실제 외부 서비스 비밀값은 Git·이미지 빌드에 포함하지 않고, 개발 기본값·시연 자료와 실제 서비스 준비 항목은 [150 점검 범위](../11_implementation_log/150_Mac_CICD_Docker_Keychain_오류_수정_기록.md)에 구분해 기록했다.

## 4. DB 전환과 배포 순서

1. 최신 main SHA인지 검사. 배포 직렬화와 호스트 lock 적용.
2. 기존 DB healthy, project/network/volume, env 권한과 DB명 확인.
3. private release 생성 및 출력 없는 Compose validation.
4. 두 이미지 digest pull. Linux ARM64와 OCI revision이 배포 SHA와 일치하는지 확인.
5. 현재 이미지 ID를 복구 태그로 보존. 초기 `:local` 이미지도 포함.
6. 기존 PostgreSQL에서 custom-format `pg_dump`, 파일 fsync, `pg_restore --list` 확인. 실패하면 앱 교체를 시작하지 않음.
7. 앱만 `up --no-deps --no-build --pull never`로 교체. 짧은 중단이 발생할 수 있으며 무중단 배포는 아님.
8. 컨테이너 상태/이미지 ID, Backend health/버전, Frontend 응답 확인. 최초 DB 전환은 baseline=false로 다시 기동해 일반 모드도 확인.
9. 정상 release를 current로 기록하고 공개 frontend/API health/버전 확인.

prod에서는 `schema.sql`/`data.sql`을 매 기동마다 실행하지 않는다. Flyway 최초 baseline은 버전 0이며 V1의 기존 스키마와 V2 필수 공통코드를 적용한다. V2는 기존 공통코드 값을 덮어쓰지 않는다. 운영 계정, 예약, 대기 기록을 시연 seed로 초기화하지 않는다. 빈 DB에는 demo 관리자/보건소/업무/예약 슬롯을 자동 생성하지 않으며 실제 초기 데이터 준비가 별도 필요하다. 기존 예약 슬롯 이후 날짜의 운영 슬롯 관리도 별도로 수행한다.

백업 archive 확인은 실제 restore drill 완료와 다르다. 이미지 복구는 DB migration/data를 되돌리지 않는다. 이후 migration은 직전 앱과 호환되게 추가하고, 파괴적 변경은 별도 데이터 복원 계획을 세운다. DB 백업을 운영 DB에 자동 덮어쓰지 않는다.

## 5. 배포 이력과 복구

```text
/Users/tro/services/health-center/
├── shared/production.env
├── releases/<SHA>-<content-hash>/
│   ├── compose.yml
│   ├── runtime.env
│   ├── images.env
│   └── release.json
├── attempts/<attempt-id>/
│   ├── database.dump
│   ├── rollback/
│   ├── rollback.json
│   └── result.json
└── current -> releases/<SHA>-<content-hash>
```

SHA가 같아도 이미지 digest, Compose, 운영 env가 바뀌면 별도 release를 만든다. 따라서 Actions 재실행이 이전 정상 release를 덮어쓰지 않는다. 백업과 env 사본은 private 디렉터리에만 남으며 artifact/log로 업로드하지 않는다.

앱 교체/readiness 실패는 직전 실제 이미지로 자동 복구를 시도하고 workflow를 실패 처리한다. 복구 자체가 실패하면 DB 백업과 결과를 남기고 운영자 확인이 필요하다. 로컬 origin이 정상인 상태에서 공개 검사만 실패하면 정상 앱을 유지하고 workflow를 실패 처리한다. Cloudflare/DNS/Access를 확인한 뒤 최신 main workflow를 재실행한다.

수동 앱 복구는 되돌릴 배포 시도의 디렉터리 이름을 명시한다.

```bash
ls -1 /Users/tro/services/health-center/attempts
DEPLOY_ROOT=/Users/tro/services/health-center \
  bash scripts/deploy/deploy.sh --rollback '<attempt-directory-name>'
```

해당 시도 직전 앱/설정을 복원한다. 수동 복구도 현재 DB를 새로 백업하고 복구 이미지 보존/검증을 수행한다. 최신 main 재실행은 최신 버전을 다시 배포하므로 수동 복구를 영구적인 소스 수정으로 취급하지 않는다. 코드 결함은 dev에서 수정해 main에 반영한다.

강제 종료/SIGKILL/전원 중단으로 `.deploy-lock`이 남으면 실행 중인 job/프로세스와 컨테이너·attempt 상태를 확인한 후에만 stale lock을 제거한다. 자동 timeout/취소가 항상 복구를 끝낼 수 있다고 가정하지 않는다. 백업/이미지의 자동 삭제는 구현하지 않았으므로 사용량과 보관 정책을 별도로 관리한다.

## 6. 검증 및 완료 조건

`Build and publish backend`에서 `dependency:go-offline`이 실패하면 `buildx failed` 위쪽의 Maven `[ERROR]`를 확인한다. `egovframe2`의 HTTP 502는 외부 저장소 다운로드 오류다. 수정된 Docker 빌드는 전송 오류에 한해 최대 3회 재시도하고 실패 캐시를 갱신한다. 지속적인 저장소 오류나 컴파일 오류는 빌드를 실패 처리한다. 상세 로그·수정·검증 및 최신 커밋 재배포 절차는 [149 수정 기록](../11_implementation_log/149_Mac_CICD_Backend_Maven_HTTP502_수정_기록.md)을 따른다.

내부 readiness가 정상인데 공개 검사만 HTTP 403이면 endpoint별 상태와 Cloudflare 응답을 확인한다. 이번 Python 기본 User-Agent 차단은 배포용 식별 User-Agent로 정상 응답을 확인했다. 원인별 비교와 안전한 진단은 [151 기록](../11_implementation_log/151_Mac_CICD_공개_검증_HTTP403_오류_수정_기록.md)을 참고하며 공개 검사를 건너뛰거나 metrics 보호를 해제하지 않는다.

로컬/격리 검증: Java 단위 26건, PostgreSQL 18 migration 2건, prod 컨테이너 health, frontend lint/typecheck/build, ARM64 Docker build/healthcheck, workflow actionlint, 배포 실패/복구와 설정 정책 테스트.

운영 완료 확인:

- [ ] PR CI required 및 main CI/이미지 발행 통과
- [ ] runner Online, production main-only, 외부 PR workflow 승인 정책
- [ ] 최초 백업 archive와 기존 데이터 보존 확인
- [ ] backend prod, SQL init never, baseline false, 버전 main SHA
- [ ] frontend/API 공개 응답, 기존 관측성 내부 scrape/외부 metrics 보호 유지
- [ ] 두 번째 dev → main 변경이 자동 배포됨
- [ ] 실제 운영 복구 절차 검증 (격리/mock 테스트와 구분)

참고: [GitHub reusable workflows](https://docs.github.com/en/actions/how-tos/reuse-automations/reuse-workflows), [ARM64 hosted runners](https://docs.github.com/en/actions/reference/runners/github-hosted-runners), [self-hosted runner 관리](https://docs.github.com/en/actions/how-tos/manage-runners/self-hosted-runners/manage-access), [GHCR 인증](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry), [Node 지원 버전](https://nodejs.org/en/about/previous-releases).
