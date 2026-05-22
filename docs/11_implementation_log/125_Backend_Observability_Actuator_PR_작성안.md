# Backend Observability Actuator Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/backend-observability-actuator` |
| base 브랜치 | `dev` |
| 작업 트리 | Actuator/Micrometer 설정, 보안 정책, 환경변수, 문서 갱신 |
| 주요 커밋 | 커밋 전 |
| 빌드 확인 | `mvn.cmd -q -DskipTests compile` 통과 |
| 테스트 확인 | `mvn.cmd -q test-compile` 통과 |
| 실행/API 확인 | 서버 기동, Docker 실행, 런타임 호출은 사용자 직접 수행 |

## PR 제목

```text
feat: 백엔드 Actuator 관측성 기반 추가
```

## PR 본문

```markdown
## 개요

Actuator/Micrometer 기반의 운영 상태 확인 endpoint를 정리했습니다.

이번 PR은 Prometheus/Grafana/k6 고도화의 첫 단계로, 백엔드에서 health, info, metrics, prometheus endpoint를 노출하고 공개/보호 범위를 분리합니다.

## 변경 내용

- Micrometer Prometheus registry 추가
- Actuator endpoint 노출 설정 추가
  - `/actuator/health`
  - `/actuator/health/liveness`
  - `/actuator/health/readiness`
  - `/actuator/info`
  - `/actuator/metrics`
  - `/actuator/prometheus`
- Actuator 보안 정책 정리
  - health/info 공개
  - metrics/prometheus 및 기타 actuator endpoint ADMIN 보호
- Docker Compose와 `.env.example`에 운영 환경변수 추가
- README, 백엔드 README, 전체 체크리스트, 보류 목록, 구현 기록 갱신

## 검증

- [x] `git diff --check`
- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [ ] 서버 기동 후 `/actuator/health` 확인
- [ ] 서버 기동 후 `/actuator/info` 확인
- [ ] ADMIN 토큰으로 `/actuator/metrics` 확인
- [ ] ADMIN 토큰으로 `/actuator/prometheus` 확인
- [ ] 미인증 또는 비관리자 토큰의 metrics/prometheus 접근 차단 확인

## 런타임 확인 기준

### Swagger 로그인

```text
1. http://localhost:8080/swagger-ui/index.html 접속
2. POST /api/auth/login 실행
3. admin@test.com / password1234로 로그인
4. Authorize에 Bearer {accessToken} 설정
```

### 대표 예시

```text
GET http://localhost:8080/actuator/health
```

기대 응답:

```json
{
  "status": "UP"
}
```

## 추가 테스트 체크리스트

- [ ] Happy: 미인증 사용자가 `/actuator/health`에서 `UP` 상태를 확인할 수 있다.
- [ ] Happy: 미인증 사용자가 `/actuator/info`에서 앱 정보를 확인할 수 있다.
- [ ] Happy: ADMIN 토큰으로 `/actuator/metrics/http.server.requests`를 확인할 수 있다.
- [ ] Happy: ADMIN 토큰으로 `/actuator/prometheus`를 확인할 수 있다.
- [ ] Edge: 노출 endpoint 환경변수를 `health,info`로 줄이면 metrics/prometheus가 닫힌다.
- [ ] Bad: 미인증 사용자의 `/actuator/metrics` 접근은 차단된다.
- [ ] Bad: STAFF/CITIZEN 토큰의 `/actuator/prometheus` 접근은 차단된다.

## 미검증 사유

- 프로젝트 운영 기준상 서버 기동, Docker 실행, API 런타임 호출, Swagger/브라우저 확인은 사용자가 직접 수행한다.
- Prometheus/Grafana/Loki/k6 구성은 후속 브랜치에서 진행한다.
- Docker backend healthcheck는 runtime image 도구 의존성이 생길 수 있어 이번 브랜치에서 제외했다.

## 후속 작업

- Prometheus scrape 인증 또는 내부 전용 management port 정책 결정
- Docker Compose에 Prometheus/Grafana/Loki 구성 추가
- Grafana에서 JVM, HTTP request, error rate, latency 대시보드 구성
- k6 예약/방문/대기/대시보드 부하 테스트 시나리오 작성
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] dev 최신화 확인
- [ ] 전체 체크리스트 반영 확인
- [ ] 후속 관측성 브랜치 생성 여부 결정

## 커밋 메시지 초안

제목:

```text
feat: 백엔드 Actuator 관측성 기반 추가
```

본문:

```text
- Micrometer Prometheus registry 추가
- Actuator health/info/metrics/prometheus 노출 설정
- health/info 공개와 metrics/prometheus 관리자 보호 정책 적용
- 운영 환경변수와 확인 문서 갱신
```
