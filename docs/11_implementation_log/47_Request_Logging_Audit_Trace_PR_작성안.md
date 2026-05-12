# Request Logging Audit Trace Pull Request 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `feat/request-logging-audit-trace` |
| base 브랜치 | `main` |
| 작업 트리 | 요청 로깅 필터, 감사성 상태 변경 로그, logback 패턴, 보류/일정 문서 수정 |
| 주요 커밋 | 사용자 커밋 예정 |
| 빌드 확인 | `mvn.cmd`, `mvn` 명령이 현재 쉘에서 미인식되어 미확인 |
| 테스트 확인 | `mvn` 명령 미인식으로 미확인 |
| 실행/API 확인 | 사용자가 Spring Boot Dashboard와 Swagger 기준으로 확인 |

## PR 제목

```text
feat: 요청 로깅과 상태 변경 추적 로그 구현
```

## PR 본문

```markdown
## 개요

운영 관측성 고도화의 첫 단계로 API 요청 단위 `traceId`와 핵심 상태 변경 로그를 추가합니다.

## 변경 내용

- `RequestLoggingFilter` 추가
- 요청별 `X-Trace-Id` 응답 헤더와 MDC `traceId` 설정
- 요청 완료 로그에 method, uri, query, status, latencyMs, memberId, role, healthCenterId 출력
- 요청 처리 중 예외 발생 시 traceId와 요청 정보를 함께 로그로 출력
- query string의 password/token/phone 계열 값 마스킹
- 예약 생성/취소 성공 로그 추가
- 예약자 체크인/현장 접수 성공 로그 추가
- 대기 호출/시작/완료/보류/노쇼/취소 성공 로그 추가
- Logback 콘솔/파일 패턴에 traceId 출력 추가
- 운영 로그와 DB 감사 이력 저장 전략 문서화
- 보류 목록과 전체 체크리스트 갱신

## 검증

- [x] `git diff --check`
- [x] `rg`로 요청/상태 변경 로그 추가 위치 확인
- [ ] `mvn -q -DskipTests compile`
- [ ] `mvn -q test-compile`
- [ ] Swagger에서 `POST /api/queues/{queueTicketId}/call` 실행 후 로그 확인

## 미검증 사유

- 현재 쉘에서 `mvn.cmd`, `mvn` 명령이 인식되지 않아 Maven compile/test-compile은 미확인입니다.
- 서버 기동과 Swagger 런타임 확인은 사용자가 직접 수행합니다.

## Swagger 대표 확인

1. `POST /api/auth/login`에서 `staff@test.com / password1234`로 로그인합니다.
2. Swagger Authorize에 accessToken 값만 입력합니다.
3. `GET /api/queues?status=WAITING`으로 `queueTicketId`를 확인합니다.
4. `POST /api/queues/{queueTicketId}/call`을 실행합니다.

기대 결과:

- 응답 헤더에 `X-Trace-Id`가 포함됩니다.
- 요청 완료 로그에 `event=http.request.completed`, `status=200`, `latencyMs`가 남습니다.
- 상태 변경 로그에 `event=queue.called`, `previousStatus`, `status=CALLED`가 남습니다.
- 방문자 이름과 전화번호는 로그에 남지 않습니다.

## 추가 테스트 체크리스트

| 케이스 | 확인 API | 기대 결과 |
|---|---|---|
| 예약 생성 | `POST /api/reservations` | `event=reservation.created` 로그 |
| 예약 취소 | `DELETE /api/reservations/{id}` | `event=reservation.canceled` 로그 |
| 예약자 체크인 | `POST /api/visits/check-in` | `event=visit.checked_in` 로그 |
| 현장 접수 | `POST /api/visits/walk-in` | `event=visit.walk_in_created` 로그 |
| 대기 보류 | `POST /api/queues/{id}/hold` | `event=queue.held` 로그 |
| 대기 노쇼 | `POST /api/queues/{id}/no-show` | `event=queue.no_show` 로그 |
| 대기 취소 | `POST /api/queues/{id}/cancel` | `event=queue.canceled` 로그 |
| 인증 없는 요청 | 보호 API 호출 | 요청 완료 로그에 memberId/role이 `-`로 출력 |
| 오류 요청 | 상태 전이 불가 API 호출 | 요청 완료 또는 예외 로그에 traceId 포함 |

## 후속 작업

- PostgreSQL 기반 감사/업무 이력 테이블 설계
- JSON 로그 encoder 도입 검토
- Actuator/Micrometer와 Prometheus endpoint 구성
- Grafana/Loki 로그·메트릭 연계
- k6 부하 테스트 시나리오 작성
```

## Merge 후 브랜치 정리 기준

- [ ] PR merge 완료
- [ ] main 최신화 확인
- [ ] 전체 체크리스트 반영
- [ ] 후속 브랜치 생성 또는 다음 작업 문서화

## 커밋 메시지 초안

```text
feat: 요청 로깅과 상태 변경 추적 로그 구현

- traceId 기반 요청 완료 로그와 응답 헤더 추가
- 예약/방문/대기 command 성공 로그 추가
- 로그 저장 전략과 운영 고도화 문서 갱신
```
