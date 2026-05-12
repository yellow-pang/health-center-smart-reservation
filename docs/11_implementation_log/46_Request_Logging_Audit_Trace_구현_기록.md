# Request Logging Audit Trace 구현 기록

## 1. 작업 목표

- API 요청 단위로 `traceId`를 부여하고 요청 완료 로그에 method, URI, status, latency를 남긴다.
- 예약/방문/대기 상태 변경 command에 개인정보를 제외한 운영 추적 로그를 남긴다.
- Actuator, Prometheus, Loki, k6 같은 후속 관측성 작업의 기반을 만든다.

## 2. 작업 범위

- [x] 이번 브랜치에 포함: `X-Trace-Id` 요청 헤더 수용 및 응답 헤더 반환
- [x] 이번 브랜치에 포함: 요청 완료 로그와 예외 로그 추가
- [x] 이번 브랜치에 포함: 예약 생성/취소, 체크인/현장접수, 대기 상태 전이 성공 로그 추가
- [x] 이번 브랜치에 포함: `logback-spring.xml` 패턴에 MDC `traceId` 출력 추가
- [x] 이번 브랜치에 포함: 보류 목록과 전체 체크리스트 갱신
- [x] 이번 브랜치에서 제외: JSON 로그 encoder 도입
- [x] 이번 브랜치에서 제외: 로그 DB 이력 테이블, Loki, Prometheus, k6 구현

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/14_deferred_cleanup/01_보류_정리_목록.md` DC-017 확인
- [x] 현재 브랜치 확인: `feat/request-logging-audit-trace`
- [x] 영향받는 파일 확인

## 4. 영향 범위

| 파일 | 변경 내용 | 위험도 |
|---|---|---|
| `SecurityConfig` | JWT 인증 필터 뒤에 요청 로깅 필터 추가 | MEDIUM |
| `RequestLoggingFilter` | 요청별 traceId, status, latency 로그 | MEDIUM |
| `AuditLogSupport` | command 로그에서 traceId/member/role 값 포맷 보조 | LOW |
| `ReservationCommandService` | 예약 생성/취소 성공 로그 추가 | LOW |
| `VisitCommandService` | 체크인/현장접수 성공 로그 추가 | LOW |
| `QueueCommandService` | 대기 상태 전이 성공 로그 추가 | LOW |
| `logback-spring.xml` | 콘솔/파일 로그 패턴에 traceId 출력 | LOW |

## 5. 구현 체크리스트

- [x] `RequestTraceConstants` 추가
- [x] `RequestLoggingFilter` 추가
- [x] Spring Security filter chain에 요청 로깅 필터 연결
- [x] servlet filter 자동 중복 등록 방지
- [x] `AuditLogSupport` 추가
- [x] 예약 command 성공 로그 추가
- [x] 방문 command 성공 로그 추가
- [x] 대기 command 성공 로그 추가
- [x] 로그 패턴에 MDC `traceId` 추가
- [x] 보류 목록 DC-017 갱신
- [x] 전체 체크리스트 갱신
- [x] 커밋 메시지 초안 작성

## 6. 로그 정책

요청 로그:

```text
event=http.request.completed traceId={} method={} uri={} query={} status={} latencyMs={} memberId={} role={} healthCenterId={}
```

상태 변경 로그:

```text
event=reservation.created
event=reservation.canceled
event=visit.checked_in
event=visit.walk_in_created
event=queue.called
event=queue.started
event=queue.completed
event=queue.held
event=queue.no_show
event=queue.canceled
```

개인정보 기준:

- 방문자 이름과 전화번호는 로그에 남기지 않는다.
- 인증된 사용자는 `memberId`, `role`, `healthCenterId`만 남긴다.
- 예약번호는 이번 브랜치 로그에 남기지 않는다.
- 대기번호는 현장 운영 식별자로 사용되므로 `ticketNumber`만 남긴다.
- query string의 `password`, `token`, `phone`, `visitorPhone` 값은 `***`로 마스킹한다.

## 7. 로그 저장 전략

전자정부프레임워크 기반 단일 백엔드 MVP에서는 로그 저장을 아래처럼 두 층으로 분리한다.

| 구분 | 저장 위치 | 목적 | 이번 브랜치 범위 |
|---|---|---|---|
| 운영 로그 | Logback 파일 로그 또는 표준 출력(stdout) | 장애 분석, 요청 추적, 성능 확인 | 요청 완료 로그와 상태 변경 로그를 파일/콘솔 패턴에 남김 |
| 감사/업무 이력 | PostgreSQL 이력 테이블 | 누가 언제 예약/방문/대기 상태를 바꿨는지 업무 데이터로 조회 | 이번 브랜치 제외, DC-014 후속 |
| 로그 검색/시각화 | Loki, ELK, OpenSearch, CloudWatch 등 | 장기 보관, 검색, 대시보드, 부하 테스트 분석 | 이번 브랜치 제외, DC-020 후속 |
| 비정형 이벤트 분석 | MongoDB 같은 문서 DB | 이벤트 스키마가 자주 바뀌는 분석성 데이터 저장 | 현재 프로젝트 기준에서는 우선 도입하지 않음 |

판단 기준:

- 애플리케이션 요청/예외 로그는 DB 트랜잭션과 분리하기 위해 파일 로그 또는 stdout으로 남긴다.
- 대량 발생할 수 있는 요청 로그를 업무 DB에 바로 저장하지 않는다.
- 예약 취소, 체크인, 호출, 노쇼, 완료 같은 업무 상태 변경 이력은 나중에 PostgreSQL 테이블로 분리한다.
- MongoDB 같은 별도 저장소는 운영 포인트가 늘어나므로, PostgreSQL + MyBatis 기준 MVP에서는 후순위로 둔다.
- 장기 검색과 대시보드는 Logback 파일/stdout 로그를 Loki 같은 수집기로 가져가는 방향을 우선 검토한다.

권장 확장 순서:

```text
Logback 파일/stdout 로그
  -> PostgreSQL 감사 이력 테이블
  -> Loki/Grafana 로그 수집과 검색
  -> OpenTelemetry/APM 추적 고도화
```

## 8. 검증 체크리스트

- [x] `git diff --check`
- [x] `rg`로 요청/상태 변경 로그 추가 위치 확인
- [ ] `mvn -q -DskipTests compile`
- [ ] `mvn -q test-compile`
- [ ] Swagger에서 대표 API 실행 후 로그 확인

검증 결과:

| 항목 | 결과 |
|---|---|
| `git diff --check` | 성공 |
| `rg` 정적 확인 | `RequestLoggingFilter`, `AuditLogSupport`, command 로그 위치 확인 |
| `npm.cmd exec -- gitnexus status` | stale index 확인 |
| `mvn.cmd -q -DskipTests compile` | 현재 쉘에서 `mvn.cmd` 명령 미인식 |
| `mvn -q -DskipTests compile` | 현재 쉘에서 `mvn` 명령 미인식 |

## 9. Swagger 대표 확인 기준

서버 실행 후 Swagger에서 아래 대표 예시 1개로 로그를 확인한다.

1. `POST /api/auth/login`에서 `staff@test.com / password1234`로 로그인한다.
2. Swagger Authorize에 accessToken 값만 입력한다.
3. `GET /api/queues?status=WAITING`으로 대기표를 조회한다.
4. 조회한 `queueTicketId`로 `POST /api/queues/{queueTicketId}/call`을 실행한다.

기대 결과:

- 응답 헤더에 `X-Trace-Id`가 포함된다.
- 요청 완료 로그에 `event=http.request.completed`, `method=POST`, `uri=/api/queues/{id}/call`, `status=200`이 남는다.
- 상태 변경 로그에 `event=queue.called`, `queueTicketId`, `previousStatus`, `status=CALLED`가 남는다.
- 방문자 이름과 전화번호는 로그에 남지 않는다.

## 10. 사용자 코드 점검 결과

| 점검 시점 | 사용자 의견 | 반영 여부 |
|---|---|---|
| 브랜치 시작 전 | 프론트/테스트 대신 기능 추가 위주로 로깅 작업 진행 요청 | 반영 |
| 문서 보강 중 | 운영 로그 저장 방식과 DB 감사 이력 분리 기준 추가 요청 | 반영 |

## 11. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 요청 로깅 구현 중 | JSON 로그 encoder 도입 | Loki/Grafana 연계 시 구조화 로그 수집이 쉬워짐 | DC-017 후속으로 유지 |
| 요청 로깅 구현 중 | Actuator/Micrometer 구성 | HTTP 요청 메트릭과 로그를 함께 봐야 운영성이 좋아짐 | DC-018 후속 |
| 문서 보강 중 | PostgreSQL 감사 이력 테이블 | 업무 상태 변경은 운영 로그와 별도로 조회 가능한 이력 데이터가 필요함 | DC-014 후속 |

## 12. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리

## 13. 커밋 메시지 초안

```text
feat: 요청 로깅과 상태 변경 추적 로그 구현

- traceId 기반 요청 완료 로그와 응답 헤더 추가
- 예약/방문/대기 command 성공 로그 추가
- 로그 패턴과 운영 고도화 문서 갱신
```
