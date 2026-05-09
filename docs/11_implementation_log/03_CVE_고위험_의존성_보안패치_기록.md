# CVE 고위험 의존성 보안패치 기록

## 문서 목적

이 문서는 VS Code 보안 알림으로 확인된 Critical/High 취약점에 대해, 왜 수정이 필요했는지와 실제 수정 내역, 검증 결과를 한 번에 확인할 수 있도록 정리한 작업 기록이다.

## 작업 배경

프로젝트 의존성 점검 과정에서 `appmod-validate-cves-for-java` 결과, 다음 항목이 Critical/High로 보고되었다.

| 구분 | 심각도 | 내용 |
|---|---|---|
| Apache Tomcat Embed 10.1.52 | CRITICAL/HIGH | CLIENT_CERT 인증 우회, TLS cipher 우선순위, 로그 노출/이스케이프 관련 취약점 |
| PostgreSQL JDBC 42.7.3 | HIGH | SCRAM PBKDF2 iteration 기반 CPU 소모 DoS |
| Spring Boot DevTools 3.5.6 | HIGH | remote secret 비교 타이밍 공격 취약점 |

따라서 기능 변경이 아닌, 의존성 보안 패치 목적의 수정을 진행했다.

## 수정 내용 요약

### 1) Tomcat 버전 상향

파일:

```text
backend/pom.xml
```

변경:

```text
tomcat-embed.version: 10.1.52 -> 10.1.54
```

적용 대상:

- `tomcat-annotations-api`
- `tomcat-embed-core`
- `tomcat-embed-el`
- `tomcat-embed-jasper`
- `tomcat-embed-websocket`

### 2) PostgreSQL JDBC 버전 상향

파일:

```text
backend/pom.xml
```

변경:

```text
postgresql.version: 42.7.7 (신규 속성 추가)
org.postgresql:postgresql -> ${postgresql.version}
```

### 3) DevTools 취약점 추적 주석 추가

파일:

```text
backend/pom.xml
```

내용:

- CVE-2026-40972(HIGH) 추적 주석 추가
- 수정 권고 버전(3.5.14+) 미출시 상태를 기록
- DevTools는 `optional` 의존성으로 운영 배포에 포함되지 않음을 명시

## 왜 이렇게 수정했는가

1. Critical/High 취약점은 즉시 대응 대상이다.
2. 이번 이슈는 코드 로직 버그가 아니라 라이브러리 버전 취약점이 원인이다.
3. 가장 안전한 조치는 호환 가능한 패치 버전으로 의존성을 올리는 것이다.
4. 미출시 패치(DevTools)는 현재 적용 불가이므로 추적 가능하게 문서/주석으로 남긴다.

## 검증 결과

실행 명령:

```text
mvn clean test-compile
```

결과:

- BUILD SUCCESS
- 직접 의존성 확인 시 아래 버전 반영 확인
  - `org.apache.tomcat.embed:tomcat-embed-core:10.1.54`
  - `org.postgresql:postgresql:42.7.7`

## 영향 범위

### 코드 영향

- 비즈니스 로직(Java 클래스) 수정 없음
- 의존성 버전과 주석만 변경

### 생성 소스 폴더 영향

아래 경로는 빌드 산출물(자동 생성)이며, 수동 수정 대상이 아니다.

```text
backend/target/generated-test-sources/test-annotations
```

`QMember.java` 등 해당 경로 파일은 Querydsl 생성 결과이며, 이번 보안 패치의 원인 파일도 아니고 직접 수정된 파일도 아니다.

## 잔여 이슈

- `spring-boot-devtools`의 CVE-2026-40972는 수정 버전 공개 후 반영 필요
- 운영 배포 시 DevTools 비포함 원칙 유지 필요

## 결론

이번 작업은 기능 추가/변경이 아니라 의존성 보안 패치 작업이다. 핵심 취약점(Tomcat, PostgreSQL)은 패치 버전으로 즉시 반영했고, 미출시 패치(DevTools)는 추적 가능하도록 기록했다.