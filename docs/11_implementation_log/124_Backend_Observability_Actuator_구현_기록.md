# Backend Observability Actuator 구현 기록

## 1. 작업 목표

- Actuator/Micrometer 기반의 운영 상태 확인 지점을 만든다.
- 이후 Prometheus, Grafana, Loki, k6 고도화로 이어질 수 있도록 `/actuator` 노출 범위와 보안 기준을 먼저 정한다.
- eGovFrame Simple Backend Template, Maven, MyBatis 기준은 유지하고 신규 도메인 코드는 추가하지 않는다.

## 2. 작업 범위

- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 관련 항목 확인
- [x] `DC-018` Actuator/Micrometer 고도화 후보 확인
- [x] Micrometer Prometheus registry 의존성 추가
- [x] Actuator health/info/metrics/prometheus 노출 설정
- [x] `/actuator/health`, `/actuator/info` 공개 정책 적용
- [x] `/actuator/metrics`, `/actuator/prometheus` ADMIN 보호 정책 적용
- [x] Docker Compose와 `.env.example` 운영 환경변수 반영
- [x] README와 백엔드 README 확인 기준 보강
- [x] 전체 체크리스트 갱신
- [x] PR 문서 초안 작성

제외한다.

- [ ] Prometheus 서비스 Docker Compose 추가
- [ ] Grafana 대시보드 구성
- [ ] Loki/Promtail 또는 Grafana Alloy 로그 수집 구성
- [ ] k6 부하 테스트 시나리오 작성
- [ ] 별도 management port 분리
- [ ] 서버 기동, Docker 실행, Swagger/브라우저 런타임 확인

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 확인
- [x] `docs/14_deferred_cleanup/01_보류_정리_목록.md` 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] 영향받는 파일 확인

## 4. 영향도 확인

| 대상 | GitNexus 결과 | 판단 |
|---|---|---|
| `SecurityConfig` | impactedCount 0, direct callers 0, affected processes 0, risk LOW | Actuator 경로 보안 정책 추가 |
| `filterChain` | impactedCount 0, direct callers 0, affected processes 0, risk LOW | 기존 API 권한 규칙은 유지하고 `/actuator/**` 규칙만 추가 |

참고: `gitnexus status`는 stale 상태를 경고했고 `gitnexus analyze`는 현재 세션에서 `Not inside a git repository`로 실패했다. 이후 `gitnexus impact --repo health-center-smart-reservation ...`는 정상 실행되어 영향도를 확인했다.

## 5. 구현 내용

| 파일 | 변경 내용 |
|---|---|
| `backend/pom.xml` | `micrometer-registry-prometheus` 추가 |
| `backend/src/main/resources/application.properties` | Actuator base path, 노출 endpoint, health detail, probe, application metric tag, info endpoint 설정 |
| `backend/src/main/resources/application-dev.properties` | 개발 환경 health detail 기본값을 `when_authorized`로 설정 |
| `backend/src/main/resources/application-prod.properties` | 운영 환경 health detail 기본값을 `never`로 설정 |
| `backend/src/main/java/egovframework/com/security/SecurityConfig.java` | health/info 공개, metrics/prometheus 및 기타 actuator endpoint ADMIN 보호 |
| `docker-compose.yml` | backend 서비스에 Actuator/Micrometer 환경변수 주입 |
| `.env.example` | 운영 관측성 환경변수 예시 추가 |
| `README.md`, `backend/README.md` | Actuator 확인 URL과 보안 기준 문서화 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 이번 브랜치 완료 상태와 후속 Prometheus/Grafana/Loki/k6 분리 |
| `docs/14_deferred_cleanup/01_보류_정리_목록.md` | `DC-018` 처리 결과 갱신 |

## 6. 운영 엔드포인트 기준

| Endpoint | 인증 | 목적 |
|---|---|---|
| `GET /actuator/health` | 공개 | 배포/터널/로드밸런서 상태 확인 |
| `GET /actuator/health/liveness` | 공개 | 애플리케이션 생존 상태 확인 |
| `GET /actuator/health/readiness` | 공개 | 준비 상태 확인 |
| `GET /actuator/info` | 공개 | 앱 이름, 설명, 버전 확인 |
| `GET /actuator/metrics` | ADMIN token 필요 | JVM/HTTP/DB 커넥션 등 메트릭 이름 확인 |
| `GET /actuator/metrics/{name}` | ADMIN token 필요 | 특정 메트릭 상세 확인 |
| `GET /actuator/prometheus` | ADMIN token 필요 | Prometheus scrape 형식 메트릭 확인 |

## 7. 검증 체크리스트

- [x] `git diff --check`
- [x] `mvn.cmd -q -DskipTests compile`
- [x] `mvn.cmd -q test-compile`
- [ ] 서버 기동 후 `/actuator/health` 확인
- [ ] 서버 기동 후 `/actuator/info` 확인
- [ ] ADMIN 토큰으로 `/actuator/metrics` 확인
- [ ] ADMIN 토큰으로 `/actuator/prometheus` 확인
- [ ] 미인증 상태에서 `/actuator/metrics`가 401 또는 403으로 보호되는지 확인

## 8. 사용자 확인 방법

서버 기동, Docker 실행, API 런타임 호출은 사용자가 직접 수행한다.

Swagger 우선 확인:

```text
1. http://localhost:8080/swagger-ui/index.html 접속
2. POST /api/auth/login 실행
3. admin@test.com / password1234로 로그인
4. Authorize에 Bearer {accessToken} 설정
```

대표 런타임 확인 예시 1개:

```text
GET http://localhost:8080/actuator/health
```

기대 결과:

```json
{
  "status": "UP"
}
```

ADMIN 토큰이 필요한 메트릭 endpoint는 Swagger에 자동 노출되지 않을 수 있으므로 브라우저 또는 API 도구에서 `Authorization: Bearer {accessToken}` 헤더를 붙여 확인한다.

## 9. 추가 테스트 체크리스트

- [ ] Happy: 미인증 사용자가 `/actuator/health`에서 `UP` 상태를 확인할 수 있다.
- [ ] Happy: 미인증 사용자가 `/actuator/info`에서 앱 정보를 확인할 수 있다.
- [ ] Happy: ADMIN 토큰으로 `/actuator/metrics/http.server.requests`를 확인할 수 있다.
- [ ] Happy: ADMIN 토큰으로 `/actuator/prometheus`를 확인할 수 있다.
- [ ] Edge: `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info`로 실행하면 metrics/prometheus endpoint가 노출되지 않는다.
- [ ] Bad: 미인증 사용자의 `/actuator/metrics` 접근은 차단된다.
- [ ] Bad: STAFF/CITIZEN 토큰의 `/actuator/prometheus` 접근은 차단된다.

## 10. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| Actuator 설정 중 | Prometheus scrape 인증 정책 결정 | 현재 `/actuator/prometheus`는 ADMIN 보호라 Prometheus가 수집하려면 bearer token 또는 내부 전용 management port 정책이 필요하다. | `chore/observability-compose-stack` 후속 작업으로 남김 |
| Actuator 설정 중 | Docker healthcheck 적용 여부 검토 | runtime image에 `curl/wget`이 없어 Compose healthcheck를 단순 추가하면 이미지 도구 의존성이 생길 수 있다. | 이번 브랜치 제외, 후속 배포 운영 작업에서 검토 |

## 11. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리

## 12. 커밋 메시지 초안

```text
feat: 백엔드 Actuator 관측성 기반 추가

- Micrometer Prometheus registry 추가
- Actuator health/info/metrics/prometheus 노출 설정
- health/info 공개와 metrics/prometheus 관리자 보호 정책 적용
- 운영 환경변수와 확인 문서 갱신
```
