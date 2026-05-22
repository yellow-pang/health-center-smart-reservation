# 보건소 스마트 예약·대기 및 혼잡도 분석 시스템

## 1. 프로젝트 소개

보건소의 예방접종, 건강검진/검사, 건강상담 업무를 대상으로 예약, 현장 접수, 대기번호 발급, 직원 호출, 처리 완료, 혼잡도 분석을 제공하는 공공보건 운영 지원 시스템입니다.

온라인 예약에서 끝나는 서비스가 아니라, 실제 방문 이후의 체크인, 현장 접수, 대기열 처리, 관리자 지표까지 하나의 흐름으로 연결하는 것을 목표로 했습니다.

## 2. 현재 상태

MVP 구현, 프론트엔드 실제 API 연동, Docker Compose 기반 배포, 외부 접속, 데이터 저장 확인까지 완료했습니다.

사용자가 직접 확인한 항목:

- 외부 공개 URL에서 프론트엔드 접속
- 로그인과 역할별 화면 진입
- 예약 신청, 조회, 취소
- 직원 체크인, 현장 접수, 대기열 상태 변경
- 관리자 대시보드와 기준정보 화면
- PostgreSQL 데이터 저장
- Docker Compose/Jenkins 기반 배포 흐름

남은 작업은 신규 MVP 기능 구현이 아니라 문서 마감, 포트폴리오 정리, 보안/운영성 고도화입니다.

## 3. 핵심 기능

| 영역 | 기능 |
|---|---|
| Auth/Member | 로그인, 토큰 재발급, 로그아웃, 역할별 접근 제어 |
| Office | 업무 유형, 직원, 창구, 예약 슬롯 기준정보 관리 |
| Reservation | 예약 가능 시간 조회, 예약 신청, 내 예약 조회, 예약 상세, 예약 취소 |
| Visit | 예약자 체크인, 현장 접수 |
| Queue | 대기열 조회, 호출, 처리 시작, 처리 완료, 보류, 노쇼, 취소 |
| Dashboard | 오늘 방문자 수, 현재 대기 인원, 평균 대기시간, 시간대별 방문자, 업무별 대기시간, 예약/현장 비율, 노쇼율, 현재 혼잡도 |
| Frontend | 시민, 직원, 관리자 화면과 실제 REST API 연동 |

## 4. 기술 스택

| 영역 | 기술 |
|---|---|
| Frontend | Next.js, React, TypeScript, Tailwind CSS |
| Backend | eGovFrame Simple Backend Template 기반 Spring Boot REST API |
| Build | Maven, npm |
| DB | PostgreSQL 18 + pgvector Docker 이미지 |
| DB Access | MyBatis |
| Auth | Spring Security + Access Token / Refresh Token |
| API Docs | Springdoc OpenAPI / Swagger UI |
| Deploy | Docker Compose, Jenkins Pipeline, Cloudflare Tunnel |

MVP에서는 JPA, AI/pgvector 기능, MSA, Kubernetes, 실제 문자/카카오 알림, 키오스크 하드웨어 연동은 제외했습니다.

## 5. Repository 구조

```text
health-center-smart-reservation
 ├─ backend
 ├─ frontend
 ├─ docs
 ├─ infra
 │   └─ jenkins
 ├─ docker-compose.yml
 ├─ Jenkinsfile
 ├─ .env.example
 └─ README.md
```

## 6. 실행 방법

`.env.example`을 복사해 `.env`를 만든 뒤 환경에 맞게 값을 수정합니다.

```bash
cp .env.example .env
docker compose --env-file .env up -d --build
```

주요 접속 URL:

```text
Frontend: http://localhost:3000
Backend:  http://localhost:8080
Swagger:  http://localhost:8080/swagger-ui/index.html
```

Ubuntu VM이나 외부 공개 환경에서는 `.env`의 아래 값을 접속 기준 URL로 맞춥니다.

```text
NEXT_PUBLIC_API_BASE_URL=http://<backend-host>:8080
NEXT_PUBLIC_APP_URL=http://<frontend-host>:3000
CORS_ALLOWED_ORIGINS=http://<frontend-host>:3000
APP_TIME_ZONE=Asia/Seoul
DB_TIME_ZONE=Asia/Seoul
```

`APP_TIME_ZONE`과 `DB_TIME_ZONE`은 접수/체크인 저장 시각과 대시보드 시간대별 집계 기준을 맞추기 위한 값입니다.

Cloudflare Tunnel 공개 시에는 예를 들어 아래처럼 설정합니다.

```text
NEXT_PUBLIC_API_BASE_URL=https://api.<domain>
NEXT_PUBLIC_APP_URL=https://demo.<domain>
CORS_ALLOWED_ORIGINS=https://demo.<domain>,http://localhost:3000
```

`NEXT_PUBLIC_APP_URL`은 카카오톡 등 외부 서비스가 Open Graph 이미지를 가져갈 때 사용하는 프론트엔드 공개 URL입니다. Docker 이미지 빌드 전에 실제 공개 도메인으로 설정해야 링크 미리보기 이미지가 `localhost`가 아닌 배포 URL로 생성됩니다.

## 7. 기본 계정

개발/시연용 seed 계정입니다.

| 역할 | 계정 |
|---|---|
| 관리자 | `admin@test.com / password1234` |
| 직원 | `staff@test.com / password1234` |
| 시민 | `citizen@test.com / password1234` |
| 보호자 | `guardian@test.com / password1234` |

## 8. Swagger 확인 순서

1. `POST /api/auth/login`으로 로그인한다.
2. 응답의 access token을 Swagger `Authorize`에 `Bearer {token}` 형식으로 입력한다.
3. 대표 흐름을 확인한다.

대표 확인 흐름:

```text
GET /api/service-types
GET /api/reservation-slots
POST /api/reservations
GET /api/reservations/me
DELETE /api/reservations/{id}
POST /api/visits/check-in
POST /api/visits/walk-in
GET /api/queues
POST /api/queues/{id}/call
POST /api/queues/{id}/start
POST /api/queues/{id}/complete
GET /api/dashboard/summary
GET /api/congestion/current
```

## 9. 주요 문서

| 문서 | 목적 |
|---|---|
| `docs/README.md` | 전체 문서 읽는 순서 |
| `docs/13_schedule/02_전체_작업_체크리스트.md` | 전체 구현/검증 현황 |
| `docs/08_deploy/02_Ubuntu_VM_Jenkins_Docker_Compose_배포_계획서.md` | Ubuntu VM + Jenkins + Docker Compose 배포 계획 |
| `docs/08_deploy/03_dev_to_main_배포전_확인_체크리스트.md` | dev to main 배포 전 점검 |
| `docs/08_deploy/04_Jenkins_VM_배포_운영_가이드.md` | Jenkins VM 운영 |
| `docs/08_deploy/06_가비아_도메인_Cloudflare_Tunnel_외부공개_가이드.md` | 외부 공개 절차 |
| `docs/12_portfolio/01_포트폴리오_구현_스토리라인.md` | 포트폴리오 설명 흐름 |
| `docs/14_deferred_cleanup/01_보류_정리_목록.md` | MVP 이후 고도화 후보 |

## 10. 다음 단계

MVP는 종료 상태로 보고, 후속 작업은 아래 순서로 진행합니다.

1. 객체 단위 권한, 예외 응답, 트랜잭션 경계 재점검
2. Actuator, Micrometer, Prometheus/Grafana, Loki, k6 같은 운영성 고도화
3. 소셜 로그인 연결 추가
4. 챗봇 기능 추가로 ux 향상
