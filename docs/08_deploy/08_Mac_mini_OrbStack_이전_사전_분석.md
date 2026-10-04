# Mac mini + OrbStack 이전 사전 분석

## 문서 목적과 분석 범위

Windows 노트북의 Linux VM에서 운영하던 Health Center를 **현재 저장소 + 기존 Docker/Compose 구성 + 기존 환경변수 + OrbStack + 새 DB**로 재현할 수 있는지 판단한다. 새 배포 시스템의 설계나 구현 계획은 이 문서의 범위가 아니다.

결론은 **저장소 수정 없이 Mac에서 Compose build/up을 먼저 시도할 수 있다**는 것이다. Jenkins 이전이나 새 CI/CD 구축은 선행 조건이 아니다. 다만 실제 `.env` 내용과 빌드·기동 결과를 확인하지 않았으므로, 기존 환경변수까지 그대로 사용한 정상 동작을 보장하지는 않는다.

| 항목 | 기준 |
|---|---|
| 분석일 | 2026-09-20 |
| 분석 기준 브랜치 | `dev` |
| 분석 기준 HEAD | `c300bf0342ee2b8558776639a4c3a19af72a659c` |
| 문서 작업 브랜치 | `docs/mac-orbstack-deployment-analysis` |
| 대상 | Health Center 저장소와 로컬 Mac의 OrbStack |
| DB 전제 | 과거 데이터 보존은 필수가 아니며 새 DB 우선 |
| Secret 처리 | 실제 `.env` 내용 미열람. 이 문서에 Secret 값 미기재 |
| 직접 수행한 확인 | 저장소 정적 분석, 공개 이미지 태그 메타데이터 조회, OrbStack 상태 읽기 |
| 수행하지 않은 작업 | build/up, migration·seed 실행, Docker 리소스 삭제, 기존 VM·Cloudflare·GitHub·Jenkins 설정 변경, commit/push |

이후 코드나 이미지 태그가 변경되면 해당 변경을 기준으로 재확인한다. 기존 VM의 실제 배포 커밋·환경변수·Tunnel 설정은 현재 저장소와 동일하다고 가정하지 않는다.

## 1. 현재 저장소 구조

### 1.1 저장소 및 환경 상태

| 항목 | 분석 당시 결과 |
|---|---|
| 작업 경로 | `/Users/tro/dev/health-center-smart-reservation` |
| `git status --short --untracked-files=all` | 출력 없음. 분석 시작·종료 당시 깨끗함 |
| 현재 브랜치 | 분석 당시 `dev`, 문서 작성 시 위 문서 작업 브랜치로 전환 |
| HEAD 설명 | 타 프로젝트 연동 설정 롤백 및 기존 구조 원복 |
| origin | `https://github.com/yellow-pang/health-center-smart-reservation.git` |
| 로컬 remote-tracking refs | `origin/dev`, `origin/main`이 분석 HEAD와 같음. fetch하지 않아 원격 최신 상태는 미확인 |
| 지침 | 루트 `AGENTS.md` 없음. `START_HERE.md`, `docs/09_agent/`에 프로젝트 지침 존재 |
| `.env` | 파일 존재만 확인 |
| Docker context | `orbstack` |
| Compose | `v5.1.2` |
| Engine | OrbStack, `aarch64`, 10 CPU, 메모리 `8393289728` bytes(약 7.8 GiB) |
| Docker 로그 | driver `json-file`, Docker root `/var/lib/docker` |
| 기존 Mac 리소스 | `health-center` 이름으로 조회한 컨테이너·volume 없음 |

마지막 항목은 모든 포트가 비어 있거나 다른 프로젝트의 리소스가 없다는 뜻은 아니다.

### 1.2 주요 디렉터리

```text
health-center-smart-reservation/
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/main/java/egovframework/
│   ├── src/main/resources/
│   │   ├── application*.properties
│   │   ├── db/postgresql/{schema,data}.sql
│   │   └── egovframework/mapper/healthcenter/
│   ├── DATABASE/
│   └── .github/workflows/maven.yml
├── frontend/
│   ├── Dockerfile
│   ├── app/
│   ├── src/
│   ├── public/
│   ├── package.json
│   ├── package-lock.json
│   └── pnpm-lock.yaml
├── infra/
│   ├── jenkins/
│   └── observability/{grafana,prometheus,loki,promtail}/
├── docs/
├── performance/k6/
├── docker-compose.yml
├── Jenkinsfile
└── .env.example
```

Dockerfile은 Backend, Frontend, Jenkins, 모니터링 4종의 총 7개다. Compose는 루트 애플리케이션용과 `infra/jenkins/docker-compose.jenkins.yml` 2개다. 모두 내용을 확인했다.

환경변수 예제는 루트 `.env.example`, `frontend/.env.example`, `performance/k6/k6.env.example`에 있다.

루트 `.github/workflows/`는 없다. `backend/.github/workflows/maven.yml`은 Maven CI 예제지만 저장소 루트의 workflow가 아니므로 현재 저장소의 GitHub Actions 배포 경로로 볼 수 없다.

### 1.3 문서와 실행 설정의 차이

[Backend README](../../backend/README.md)는 Compose가 DB만 실행하며 서비스명이 `postgres`라고 설명한다. 실제 [루트 Compose](../../docker-compose.yml)는 7개 서비스를 정의하고 DB 서비스명은 `postgresql`이다. 과거 설계 문서의 제안과 현재 구현이 다를 때는 실제 실행 파일을 기준으로 판단했다.

주요 근거 문서:

- [루트 README](../../README.md)
- [Ubuntu VM/Jenkins 배포 계획서](02_Ubuntu_VM_Jenkins_Docker_Compose_배포_계획서.md)
- [Jenkins VM 운영 가이드](04_Jenkins_VM_배포_운영_가이드.md)
- [Cloudflare Tunnel 외부 공개 가이드](06_가비아_도메인_Cloudflare_Tunnel_외부공개_가이드.md)
- [배포 후 상태 확인 가이드](07_배포후_상태확인_문제해결_가이드.md)

## 2. 현재 배포 흐름

[Jenkinsfile](../../Jenkinsfile)의 실제 단계는 다음과 같다.

```text
GitHub main 변경
→ Jenkins 변경 감지
→ checkout scm
→ main 브랜치 검사
→ Java/Maven/Node/npm/Docker/Compose 버전 확인
→ backend: mvn -q test-compile
→ backend: mvn -q -DskipTests package
→ frontend: npm ci → npm run build
→ Jenkins Secret file을 workspace/.env로 복사, chmod 600
→ docker compose --env-file .env --profile observability config
→ docker compose --env-file .env --profile observability build
→ docker compose --env-file .env --profile observability up -d --remove-orphans
→ compose ps
→ docker image prune -f
```

이 흐름은 현재 파일의 설명이며, 위 명령을 이번 분석에서 실행한 것은 아니다.

- 운영 가이드는 Poll SCM `H/5 * * * *`를 안내한다. Jenkinsfile에는 trigger 선언이 없으므로 실제 polling/webhook 구성은 Jenkins 인스턴스 확인이 필요하다.
- 배포 브랜치는 `main`, Compose project name은 `health-center`다.
- `test-compile`은 테스트 코드 컴파일이며 테스트 실행이 아니다. package에서도 테스트 실행을 건너뛴다.
- Dockerfile에서 소스를 다시 빌드한다. registry push/pull 기반 배포는 없다.
- Jenkins는 `observability` profile을 지정하므로 기본 3종과 모니터링 4종이 배포 대상이다.
- Backend는 DB healthcheck 성공을 기다리지만 Frontend의 Backend 의존성은 기동 순서 수준이다.
- Cloudflare Tunnel은 이 pipeline과 애플리케이션 Compose 밖의 구성이다.

사용자가 확인한 기존 경로 `/var/jenkins_home/workspace/health-center-deploy/docker-compose.yml`은 workspace에서 Compose를 실행하는 방식과 부합한다. 기존 VM이 현재 HEAD를 사용했는지는 미확인이다.

## 3. Jenkins가 담당하는 실제 역할

근거: [Jenkins Dockerfile](../../infra/jenkins/Dockerfile), [Jenkins Compose](../../infra/jenkins/docker-compose.jenkins.yml).

| 역할 | 현재 구현 |
|---|---|
| Jenkins 상태·workspace 보관 | `jenkins-home:/var/jenkins_home` |
| Docker Engine 제어 | `/var/run/docker.sock` 마운트 |
| Maven 캐시 | `jenkins-maven-cache:/root/.m2` |
| npm 캐시 | `jenkins-npm-cache:/root/.npm` |
| 실행 사용자 | Compose에서 `root` |
| 빌드 도구 | JDK 17, Maven, Node 20, Docker CLI, Compose plugin |
| 환경변수 공급 | Credentials의 `health-center-env-file` |

Jenkins는 호스트 socket에 연결된 Docker Engine에 이미지 빌드와 컨테이너 생성을 요청한다. Gradle은 사용하지 않는다.

Jenkins 내부의 Maven/npm 빌드와 Dockerfile의 빌드는 별개다. Jenkins 캐시 volume을 Dockerfile 빌드 단계에 연결하는 설정은 없다. Docker 빌드 캐시는 Engine 측의 별도 캐시다.

현재 애플리케이션 Compose는 Jenkins workspace를 bind mount하지 않는다.

- Backend JAR은 이미지에 포함된다.
- Frontend 빌드 결과·의존성·정적 파일은 이미지에 포함된다.
- 모니터링 설정·대시보드는 각 Dockerfile의 `COPY`로 이미지에 포함된다.
- 영속 데이터는 named volume에 보관한다.

따라서 실행 중인 서비스에 workspace 소스가 계속 남아 있어야 하는 구조가 아니다. 재빌드·Compose 관리를 위한 소스와 `.env`는 필요하지만 Jenkins 경로일 필요는 없다.

Jenkins를 중단하면 변경 감지·자동 빌드·Credentials 공급이 중단된다. 애플리케이션 컨테이너가 Jenkins 중단만으로 종료되지는 않는다. Jenkins home 삭제는 Job·Credentials·workspace 손실을 수반하는 별개 작업이며 이번 분석의 실행 대상이 아니다.

## 4. DB 초기화 및 영속 데이터 구조

### 4.1 새 DB 초기화

[application.properties](../../backend/src/main/resources/application.properties)는 다음을 설정한다.

```properties
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:/db/postgresql/schema.sql
spring.sql.init.data-locations=classpath:/db/postgresql/data.sql
```

PostgreSQL 이미지가 `POSTGRES_*` 값으로 DB와 사용자를 초기화하고, Backend가 `DB_*` 값으로 접속하여 Spring SQL 초기화를 수행한다. Compose에는 `/docker-entrypoint-initdb.d` SQL mount가 없다.

[schema.sql](../../backend/src/main/resources/db/postgresql/schema.sql)은 공통코드, 보건소, 회원, 인증 토큰, 소셜 계정, 업무 유형, 예약 슬롯, 예약, 방문, 대기열, 창구 등을 포함한 15개 테이블을 생성한다. `IF NOT EXISTS`와 일부 조건부 컬럼 추가를 사용한다.

[data.sql](../../backend/src/main/resources/db/postgresql/data.sql)은 다음 데이터를 제공한다.

- 공통코드, 기본 보건소, 역할별 시연 계정
- 업무 유형, 창구, 창구별 업무 매핑
- 실행 당일부터 14일 후까지의 예약 슬롯
- Swagger 확인용 예약·대기열
- 대시보드 시연용 예약·방문·처리 데이터

별도 수동 seed가 기본 재현의 선행 조건은 아니다. 단, DB 접속 설정이 맞고 초기화 SQL이 실제로 성공해야 한다.

### 4.2 Migration과 재시작 시 동작

Flyway/Liquibase 의존성이나 버전별 migration 구조는 발견되지 않았다. 자동 SQL 초기화와 migration 변경 이력 관리는 구분해야 한다.

`always`는 prod에서도 적용되므로 최초 실행뿐 아니라 Backend 재시작 때도 SQL이 실행된다. Seed는 단순한 누락 데이터 추가가 아니다. 기본 계정·기준정보를 갱신하고 특정 시연 예약·방문·대기열을 삭제·재생성한다. 운영 데이터 보존 정책은 후속 검토 대상이다.

### 4.3 pgvector와 영속성

현재 SQL에는 `CREATE EXTENSION vector`와 vector 컬럼이 없고 애플리케이션에서 vector 검색을 사용하는 근거도 없다. 프로젝트 지침은 향후 AI 확장 가능성 때문에 pgvector 이미지를 사용한다고 설명한다.

현재 기능은 pgvector extension에 의존하지 않지만, Mac 이전을 위해 이미지를 교체할 필요도 확인되지 않았다.

DB volume은 `health-center-postgres-data:/var/lib/postgresql`이다. 과거 회원·예약 이력을 보존하지 않아도 된다는 전제에서는 기존 DB volume을 복사할 필요가 없다. 새 named volume에서 SQL로 기본 서비스를 구성할 수 있는 구조다.

실제 `.env`를 읽지 않았으므로 `POSTGRES_DB/USER/PASSWORD`와 `DB_NAME/USERNAME/PASSWORD`의 일치 여부는 미확인이다. 새 DB의 사용자에게 schema 생성 권한도 있어야 한다.

## 5. 모니터링 구조

| 구성 | 역할 | 업무 서비스 기동에 필수인가 |
|---|---|---|
| Prometheus | Backend `/actuator/prometheus`와 자체 지표를 15초마다 수집 | 아니요 |
| Grafana | 지표·로그 조회와 대시보드 제공 | 아니요 |
| Loki | 파일시스템 기반 로그 저장 | 아니요 |
| Promtail | Docker 컨테이너 발견 및 로그 수집·전송 | 아니요 |

모두 기존 `observability` profile 아래에 있다. Backend/Frontend는 모니터링 서비스에 의존하지 않는다. 선택 실행을 위해 새 profile을 만들거나 스택을 제거할 필요가 없다. 지표·로그를 연속 수집하려는 기간에는 해당 구성도 계속 실행해야 한다.

설정 근거:

- [Prometheus](../../infra/observability/prometheus/prometheus.yml): `backend:8080` scrape, 인증 설정 없음.
- [Grafana datasource](../../infra/observability/grafana/provisioning/datasources/datasources.yml): `prometheus:9090`, `loki:3100` 내부 주소 사용.
- [Grafana dashboard](../../infra/observability/grafana/dashboards/backend-overview.json): 요청률·평균 지연·JVM heap·Backend 오류 로그.
- [Loki](../../infra/observability/loki/loki-config.yml): 인증 비활성, filesystem 저장, 명시적 보존 기간 없음.
- [Promtail](../../infra/observability/promtail/promtail-config.yml): Docker socket discovery, 컨테이너 로그 경로 relabel, positions `/tmp/positions.yaml`.

Prometheus/Loki/Grafana는 각각 named volume을 사용한다. Promtail positions에는 별도 영속 volume이 없다. Promtail은 `/var/run/docker.sock`, `/var/lib/docker/containers`를 읽기 전용 bind mount한다. 실제 OrbStack 접근 및 로그 전달은 미검증이다.

지표 수집에는 기존 `OBSERVABILITY_PROMETHEUS_SCRAPE_ENABLED=true` 설정이 필요하다. [SecurityConfig](../../backend/src/main/java/egovframework/com/security/SecurityConfig.java)는 이 옵션이 켜지면 요청 출처를 Prometheus로 한정하지 않고 `/actuator/prometheus` 접근을 허용한다. 외부 Backend 공개 시 이 경로의 공개 범위도 확인해야 한다.

### Mac 16GB 자원 판단

실측 없이 충분하거나 부족하다고 확정할 수 없다. 현재 Engine에 보이는 메모리는 약 7.8 GiB다. Maven/Next 빌드와 전체 서비스의 동시 실행, Loki/Prometheus 데이터와 Docker 로그 누적을 확인해야 한다.

Compose에는 CPU·메모리 제한과 Docker 로그 회전 설정이 없다. 현재 단계에서 자원 제한을 새로 설계할 필요는 없으며, 기본 서비스와 기존 profile의 실측 결과를 바탕으로 판단한다.

## 6. 현재 네트워크 / 외부 공개 구조

| 서비스 | Compose 기본 publish | 필요한 접근 |
|---|---|---|
| Frontend | `3000:3000` | 브라우저 또는 Tunnel |
| Backend | `8080:8080` | 브라우저 API 요청 또는 Tunnel |
| PostgreSQL | `5432:5432` | Backend는 Docker 내부 통신만으로 충분 |
| Grafana | `3001:3000` | 운영자 조회용. 일반 사용자 공개 필수 아님 |
| Prometheus | `9090:9090` | Grafana는 내부 통신만으로 충분. UI publish는 선택 |
| Loki | `3100:3100` | Promtail/Grafana 내부 통신만으로 충분 |
| Promtail | 없음 | 내부 통신 |

기본 mapping에는 host IP가 없어 localhost 한정 publish가 아니다. 실제 `.env`가 포트 문자열을 바꿀 수 있으므로 실효 bind 주소는 미확인이다. 인터넷 도달 가능성은 호스트 방화벽과 네트워크에도 달려 있다.

[Frontend API client](../../frontend/src/lib/api-client.ts)는 브라우저에서 `NEXT_PUBLIC_API_BASE_URL`로 Backend를 직접 호출한다. 현재 구조에서는 Backend도 브라우저에서 접근 가능해야 한다.

Nginx 서비스·설정이나 Next.js API reverse proxy/rewrites는 발견되지 않았다. `.env.example`의 `BACKEND_INTERNAL_URL`은 현재 코드에서 사용처가 발견되지 않았다.

Cloudflare 문서는 다음을 안내한다.

```text
demo.<domain> → VM localhost:3000
api.<domain>  → VM localhost:8080
```

실제 Tunnel ID·ingress 설정이나 `cloudflared` Compose 서비스는 저장소에 없다. 기존 Cloudflare와 현재 저장소의 일치는 확인하지 않았다.

Mac 호스트에서 cloudflared를 실행한다면 Frontend/Backend의 localhost publish로 연결할 수 있다. cloudflared가 별도 컨테이너라면 그 안의 localhost는 Mac이 아니므로 내부 서비스 주소 등 실행 위치에 맞는 주소가 필요하다.

현재 포트 변수는 문자열 전체를 받으므로 `FRONTEND_PORT=127.0.0.1:3000`과 같은 실행 환경 override로 Compose 파일 수정 없이 localhost bind를 시험할 수 있다.

## 7. Mac mini + OrbStack에서 그대로 사용할 수 있는 부분

- Java 17/Maven Backend 다단계 빌드와 JAR 실행
- Node 20 Alpine/npm Frontend 다단계 빌드
- PostgreSQL/pgvector 이미지와 named volume
- Spring SQL schema/seed 초기화
- Docker bridge network와 서비스 DNS
- 모니터링 설정을 이미지에 포함하는 방식
- 기존 `observability` profile
- 로컬 Compose build/up 배포

호스트 Mac에 Java/Node를 별도로 설치해야 하는 Dockerfile 구조가 아니다. [OrbStack 공식 문서](https://docs.orbstack.dev/docker/)는 Docker Engine, Compose, volume, bind mount, port forwarding 지원을 설명한다.

## 8. Mac 이전을 위해 반드시 수정해야 하는 부분

**정적 분석에서 확인된 필수 저장소 수정은 없다.** 다음은 기존 값에 따라 조정해야 하는 실행 환경 조건이다.

| 대상 | 필요한 조건 |
|---|---|
| Backend DB 주소 | 새 Compose DB 사용 시 `DB_HOST=postgresql`, 내부 포트 `5432` |
| DB 이름·계정 | PostgreSQL 초기화 값과 Backend 접속 값 일치 |
| API 주소 | Mac의 Backend를 가리키는 브라우저 기준 URL |
| CORS | 실제 Frontend origin 허용 |
| Frontend 공개 URL | 로컬 검증이면 localhost 기준 |
| OAuth | 해당 기능 검증 시 제공자 등록 URI와 실행 주소 일치 |
| Prometheus | 모니터링 검증 시 기존 scrape 허용 옵션 활성화 |

`NEXT_PUBLIC_*`는 Frontend 빌드 시 주입된다. 실행 시 환경변수만 변경해서 기존 브라우저 번들의 주소가 바뀐다고 가정하면 안 된다.

Promtail의 mount/socket은 실검증 대상이다. 실패가 확인되면 기존 설정에서 필요한 부분만 조정할 수 있는지 판단한다. 아직 필수 변경으로 단정하지 않는다.

## 9. Mac 이전과 별개인 후속 개선

- Jenkins와 Dockerfile의 중복 Maven/npm 빌드 정리
- Jenkins `compose config` 전체 출력의 Secret 노출 가능성 개선
- 재시작 때 적용되는 시연 seed와 운영 초기화 정책 분리
- 필요 시 migration 변경 이력 관리 도입 검토
- 외부 공개 시 DB/Prometheus/Loki publish와 Actuator 접근 범위 축소
- 실측에 따른 자원 제한과 로그 회전·보존 정책
- Java/Node/관측성 구성요소의 runtime upgrade 및 지원 주기 검토
- 실제 설정과 어긋난 문서 갱신

GHCR, GitHub Actions 전환, rollback 자동화, 멀티아키텍처 CI, Kubernetes는 Mac 재현의 선행 조건이 아니다.

과설계 방지 판단:

- Mac 이전과 무관한 구조 개선은 위 후속 항목으로 분리했다.
- Runtime upgrade는 실행 장애가 확인되지 않는 한 별도 작업이다.
- 새 CI/CD 없이 기존 Compose로 먼저 실행할 수 있다.
- 새 script/abstraction이나 유사 코드 추가가 필요하다는 근거가 없다.
- 애플리케이션 서비스 로직 변경이 필요하다는 근거가 없다.
- 이번 단계에 새 테스트를 작성할 필요가 없다. 필요한 것은 실제 빌드·기동 검증이다.

## 10. 제거 가능한 기존 VM/Jenkins 종속 요소

여기서 제거 후보는 **Mac 재현에 가져오지 않아도 되는 요소**를 뜻한다. 기존 VM 리소스 삭제 승인을 뜻하지 않는다.

| 대상 | 이유 |
|---|---|
| Jenkins 컨테이너·Job·SCM polling | 수동 Compose 실행에는 불필요 |
| Jenkins workspace 경로 | 실행 컨테이너의 소스 bind mount 의존성 없음 |
| Jenkins Maven/npm cache | 앱 실행 필수 데이터가 아닌 빌드 성능 최적화 |
| Jenkins Secret file 공급 과정 | Mac의 `.env`로 공급 가능 |
| Windows/VirtualBox 포트포워딩 | Mac OrbStack 재현에는 불필요 |
| Ubuntu cloudflared 설치·systemd 절차 | 로컬 검증에 불필요. 외부 공개 시 Mac 환경 기준 별도 확인 |
| 기존 DB volume | 과거 이력 보존이 필요 없다는 전제 |
| 기존 관측성 volume | 과거 로그·지표 이력이 필요 없으면 새로 시작 가능 |

Promtail의 Docker socket은 Jenkins 종속성이 아니므로 모니터링을 유지하면 별도 검증해야 한다.

## 11. arm64 관련 확인 결과

### 11.1 이미지 메타데이터

분석일에 공개 Docker Hub API의 `/v2/repositories/{repository}/tags/{tag}`를 조회했다. 아래 정확한 태그 모두 platform 목록에 `linux/arm64`가 포함되어 있었다. 이미지 다운로드나 실행 검증은 하지 않았다.

| 용도 | 이미지 | 공개 레지스트리 확인 | 실제 빌드·실행 |
|---|---|---|---|
| Backend 빌드 | `maven:3.9-eclipse-temurin-17` | arm64 제공 | 미검증 |
| Backend 실행 | `eclipse-temurin:17-jre` | arm64 제공 | 미검증 |
| Frontend | `node:20-alpine` | arm64 제공 | 미검증 |
| DB | `pgvector/pgvector:0.8.2-pg18` | arm64 제공 | 미검증 |
| Grafana | `grafana/grafana:11.3.0` | arm64 제공 | 미검증 |
| Prometheus | `prom/prometheus:v2.55.1` | arm64 제공 | 미검증 |
| Loki | `grafana/loki:3.2.1` | arm64 제공 | 미검증 |
| Promtail | `grafana/promtail:3.2.1` | arm64 제공 | 미검증 |
| Jenkins | `jenkins/jenkins:lts-jdk17` | arm64 제공 | 추가 도구 설치 포함 미검증 |

태그는 digest 고정이 아니므로 조회 시점의 결과다. 재조회 예시는 다음과 같다. 공개 메타데이터만 읽으며 컨테이너를 생성하지 않는다.

```bash
docker buildx imagetools inspect pgvector/pgvector:0.8.2-pg18
```

### 11.2 저장소 정적 확인

- Dockerfile/Compose에 amd64 강제 설정 없음.
- 애플리케이션 Dockerfile에 x86 전용 바이너리 다운로드·실행 명령 없음.
- Jenkins Dockerfile은 Docker apt 저장소 아키텍처를 `dpkg --print-architecture`로 결정.
- Ubuntu 문서의 amd64 cloudflared 설치 예제는 앱 빌드 경로 밖의 운영 절차.
- Backend 직접 의존성과 소스에서 별도 JNI/native 로딩을 발견하지 못함. 전체 전이 의존성의 런타임 호환성은 미검증.
- [Frontend lockfile](../../frontend/package-lock.json)에 SWC, Sharp/libvips, Tailwind Oxide, Lightning CSS의 Linux arm64/musl 패키지 존재.
- [Frontend dockerignore](../../frontend/.dockerignore)는 호스트 `node_modules`, `.next`를 제외. 컨테이너 안에서 `npm ci` 실행.

amd64 에뮬레이션을 먼저 강제할 근거는 없다. 다만 arm64 이미지 제공과 lockfile 항목의 존재가 native 모듈 로딩·빌드·JVM 실행 성공까지 보장하지는 않는다.

## 12. 추가 실환경 검증이 필요한 항목

- [ ] 기존 `.env`의 DB 이름·계정 쌍이 일치하는지 사용자 확인
- [ ] 필요한 로컬 포트가 사용 가능한지 확인
- [ ] 이미지 다운로드와 Maven/eGovFrame/npm 저장소 접근 성공
- [ ] Frontend Google Font 빌드 다운로드 성공
- [ ] Backend/Frontend arm64 빌드 성공
- [ ] 새 DB에서 schema/seed 초기화 성공
- [ ] Backend health `UP`, 재시작 반복 없음
- [ ] Frontend가 Mac Backend를 호출하고 CORS 오류 없음
- [ ] 일반 로그인과 업무 유형·예약 슬롯 조회 성공
- [ ] Prometheus target `UP`, Grafana 지표 조회 성공
- [ ] Promtail Docker 접근 및 Loki 로그 전달 성공
- [ ] 빌드·전체 스택의 메모리 압력과 디스크 사용량 확인
- [ ] 소셜 로그인 검증 시 OAuth 설정 확인
- [ ] 외부 공개 검증 시 실제 Tunnel 위치·route·접근 정책 확인

Backend/Frontend에는 Compose healthcheck가 없다. `ps`의 `Up`만으로 서비스 정상 동작을 판단하면 안 된다. PostgreSQL healthcheck 성공도 앱 테이블 초기화 완료를 의미하지 않는다.

## 분류 요약

| 분류 | 대상 | 이유 |
|---|---|---|
| 유지 | Dockerfile·Compose·현재 Java/Node·pgvector 이미지 | Mac 재현을 막는 확정적 구조 문제 없음 |
| 유지 | DB 초기화 SQL·named volume·observability profile | 새 DB와 선택적 관측성 구성이 이미 있음 |
| 수정 필요 | 기존 주소가 남아 있는 경우 DB/API/CORS/OAuth 설정 | 새 Mac 서비스와 접속 대상 일치 필요. 환경변수 override부터 가능 |
| 후속 개선 | seed 정책·Secret 로그 출력·공개 포트·자원/보존 정책 | 이전 자체와 분리할 운영 개선 |
| 제거 후보 | Jenkins·workspace 운영 경로·VM 포트포워딩 | Mac에서 직접 Compose 실행 시 불필요 |
| 실검증 필요 | arm64 빌드·새 DB 초기화·Promtail·메모리·외부 연동 | 정적 분석과 메타데이터만으로 성공 보장 불가 |

## 최종 판단과 사용자 실행 확인 명령

**현재 저장소를 변경하지 않고 Mac mini + OrbStack에서 먼저 `docker compose build/up`을 시도할 수 있는가? — 예.**

기존 `.env`까지 그대로 사용해 정상 재현된다는 뜻은 아니다. DB 계정 쌍의 일치를 본인이 확인하고, 기존 VM API를 호출하지 않도록 로컬 주소를 명시할 수 있다. 아래는 추후 사용자가 실행할 확인 명령이며 구현 계획이나 실행 완료 기록이 아니다.

실행하면 이미지·컨테이너·새 DB volume이 생성되고 Backend가 schema/seed를 자동 적용한다. 분석 단계에서는 실행하지 않았다. 소셜 로그인과 Cloudflare는 이 기본 검증에서 사용하지 않는다.

```bash
cd /Users/tro/dev/health-center-smart-reservation

(
  set -e
  export DOCKER_DEFAULT_PLATFORM=linux/arm64
  export SPRING_PROFILES_ACTIVE=prod
  export DB_HOST=postgresql DB_PORT=5432

  export NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
  export NEXT_PUBLIC_APP_URL=http://localhost:3000
  export CORS_ALLOWED_ORIGINS=http://localhost:3000

  export POSTGRES_PORT=127.0.0.1:5432
  export BACKEND_PORT=127.0.0.1:8080
  export FRONTEND_PORT=127.0.0.1:3000

  docker --context orbstack compose -p health-center-mac-check \
    --env-file .env config --quiet

  docker --context orbstack compose -p health-center-mac-check \
    --env-file .env build backend frontend

  docker --context orbstack compose -p health-center-mac-check \
    --env-file .env up -d postgresql backend frontend

  docker --context orbstack compose -p health-center-mac-check \
    --env-file .env ps
)
```

`set -e`는 앞 단계 실패 후 기동을 계속하지 않게 한다. `config --quiet`는 해석된 환경변수 전체 출력을 피한다. 새 project name은 기본 named volume을 구분하지만 고정 `container_name`과 이미지 이름까지 분리하지는 않는다. 이후 실행 시에도 같은 이름의 기존 컨테이너·volume 유무를 다시 확인한다.

최초 확인 결과는 다음이면 충분하다.

1. Backend/Frontend build 성공.
2. PostgreSQL `healthy`.
3. `http://localhost:8080/actuator/health`에서 `UP`.
4. `http://localhost:3000/login` 접근 및 일반 로그인 성공.
5. 업무 유형·예약 슬롯 조회 성공.
6. 브라우저 Network에서 API 대상이 `localhost:8080`임을 확인.

모니터링은 같은 주소 override를 유지한 상태에서 기존 `--profile observability`와 `OBSERVABILITY_PROMETHEUS_SCRAPE_ENABLED=true`를 사용하여 별도로 확인할 수 있다. 외부 공개 설정 변경은 기본 로컬 재현과 분리한다.
