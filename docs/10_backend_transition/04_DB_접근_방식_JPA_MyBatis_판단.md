# DB 접근 방식 판단: JPA와 MyBatis

## 문서 목적

이 문서는 전자정부프레임워크 기반 백엔드에서 JPA와 MyBatis 중 어떤 방식을 이 프로젝트의 MVP 기본 DB 접근 방식으로 사용할지 판단한 내용을 정리한다.

결론부터 말하면, 전자정부프레임워크가 JPA만 사용해야 하는 것은 아니다. 전자정부프레임워크 실행환경은 데이터 접근 계층에서 MyBatis 계열 SQL Mapper와 ORM 계열 기술을 모두 사용할 수 있다. 현재 이 프로젝트의 MVP 기준은 MyBatis를 기본 DB 접근 방식으로 유지하는 것이 적합하다.

## 결론

| 항목 | 판단 |
|---|---|
| 전자정부프레임워크와 JPA | 사용 가능 |
| 전자정부프레임워크와 MyBatis | 사용 가능 |
| 현재 백엔드 템플릿 실제 구조 | MyBatis mapper XML 중심 |
| 현재 프로젝트 MVP 기본 방식 | MyBatis |
| JPA/QueryDSL | 현재는 템플릿 테스트/샘플 성격으로 보고 정리 후보 |
| 장기 확장 | 필요 시 특정 조회나 별도 모듈에서 JPA 검토 가능 |

## 왜 혼란이 생겼는가

현재 `backend/pom.xml`에는 JPA와 QueryDSL 관련 의존성이 존재한다.

```xml
<artifactId>spring-boot-starter-data-jpa</artifactId>
<scope>test</scope>

<artifactId>querydsl-jpa</artifactId>
<scope>test</scope>
```

또한 QueryDSL annotation processor 설정도 남아 있다.

하지만 현재 실제 운영 코드의 DB 접근 구조는 아래처럼 MyBatis 중심이다.

| 파일/위치 | 의미 |
|---|---|
| `EgovConfigAppMapper` | `SqlSessionFactoryBean`, `SqlSessionTemplate` 설정 |
| `egovframework/mapper/config/mapper-config.xml` | MyBatis 설정 |
| `egovframework/mapper/let/**/*_*.xml` | 템플릿 샘플 MyBatis mapper |
| `egovframework/mapper/healthcenter/common/CommonCode_SQL_postgresql.xml` | 신규 보건소 MyBatis mapper |
| `CommonCodeMapper` | 신규 공통코드 조회 mapper |

따라서 `pom.xml`에 JPA 의존성이 있다고 해서 이 프로젝트가 JPA 기반으로 구현되어야 한다는 뜻은 아니다. 현재 코드 구조와 문서 기준은 MyBatis 중심이다.

## 전자정부프레임워크 관점

전자정부프레임워크의 데이터 처리 계층은 데이터 접근, ORM, 트랜잭션, DataSource 같은 영역을 제공한다. 관련 문서와 구성 설명을 보면 MyBatis와 ORM 계열 기술이 함께 언급된다.

따라서 선택지는 다음처럼 이해하는 것이 맞다.

```text
전자정부프레임워크 = JPA만 사용
```

이 아니라,

```text
전자정부프레임워크 = MyBatis도 가능하고 JPA/ORM도 가능
```

이다.

## 이 프로젝트에서 MyBatis가 적합한 이유

### 1. 현재 템플릿이 MyBatis 중심이다

eGovFrame Simple Backend Template의 샘플 기능은 mapper XML을 많이 포함하고 있다. 이미 mapper 설정과 XML 로딩 구조가 잡혀 있으므로, 신규 도메인도 같은 패턴으로 작성하면 전환 비용이 낮다.

### 2. 대시보드와 통계 쿼리가 중요하다

이 프로젝트는 단순 CRUD만 있는 시스템이 아니다.

- 예약 가능 슬롯 조회
- 업무별 대기열 조회
- 평균 대기시간 계산
- 시간대별 방문자 수
- 예약/현장 방문 비율
- 노쇼율

이런 기능은 SQL을 명확하게 작성하고 튜닝하는 일이 중요하다. MyBatis는 이런 조회 중심 기능과 잘 맞는다.

### 3. 공통코드와 기준정보는 명시적 SQL이 단순하다

공통코드, 업무 유형, 예약 슬롯 같은 기준정보는 화면 표시와 필터링에 자주 사용된다. 필요한 컬럼을 명시적으로 조회하는 MyBatis 방식이 현재 프로젝트에 적합하다.

### 4. 포트폴리오 설명에 유리하다

공공 SI와 전자정부프레임워크 맥락에서는 MyBatis 기반 SQL Mapper 경험도 중요하게 평가될 수 있다.

포트폴리오에서는 다음처럼 설명할 수 있다.

```text
전자정부프레임워크 기반 템플릿의 MyBatis 구조를 유지하면서, 예약 가능 시간 조회와 대시보드 통계처럼 SQL 제어가 중요한 기능을 명시적 Mapper 중심으로 구현했습니다.
```

## JPA를 쓰면 좋은 경우

JPA가 나쁘다는 뜻은 아니다. 아래 상황에서는 JPA가 더 적합할 수 있다.

| 상황 | 이유 |
|---|---|
| 도메인 객체 상태 변경이 중심인 시스템 | Entity 중심 모델링이 유리 |
| 복잡한 객체 그래프를 일관되게 다룰 때 | 연관관계 매핑을 활용 가능 |
| CRUD가 많고 SQL 튜닝 부담이 낮을 때 | Repository 패턴 생산성이 높음 |
| 팀이 JPA 운영 경험이 충분할 때 | N+1, 영속성 컨텍스트, 트랜잭션 이해 필요 |

하지만 이 프로젝트는 예약, 대기, 통계처럼 SQL 명시성과 상태 정책이 중요하고, 템플릿도 MyBatis 중심이므로 MVP에서는 MyBatis를 유지한다.

## JPA를 당장 섞지 않는 이유

| 이유 | 설명 |
|---|---|
| 복잡도 증가 | MyBatis와 JPA를 함께 쓰면 트랜잭션, 모델, 테스트 기준이 둘로 나뉜다. |
| 팀/에이전트 기준 혼선 | 어떤 도메인을 어떤 방식으로 구현할지 매번 판단해야 한다. |
| 템플릿 정리 부담 | 현재 샘플은 MyBatis mapper가 많아 먼저 정리해야 한다. |
| MVP 집중 | 예약·대기 핵심 흐름 구현이 우선이다. |

## 정리 대상

샘플 정리 단계에서 아래 항목을 검토한다.

| 항목 | 판단 |
|---|---|
| `spring-boot-starter-data-jpa` | 테스트 샘플 외 사용 없으면 제거 후보 |
| `querydsl-jpa` | 테스트 샘플 외 사용 없으면 제거 후보 |
| `querydsl-apt` | QueryDSL 테스트 제거 시 함께 정리 후보 |
| `src/test/java/egovframework/study/jpa` | 샘플 테스트 제거 후보 |
| QueryDSL annotation processor | QueryDSL 미사용 시 제거 후보 |

단, 제거는 실제 빌드 영향 확인 후 진행한다.

## 최종 기준

앞으로 에이전트가 코드를 작성할 때 기준은 다음과 같다.

1. 신규 보건소 MVP 도메인은 MyBatis로 구현한다.
2. Mapper XML은 `src/main/resources/egovframework/mapper/healthcenter` 하위에 둔다.
3. Mapper Interface는 `egovframework.healthcenter.{context}.mapper` 하위에 둔다.
4. Request/Response/Command DTO는 `record`를 우선 사용한다.
5. Mapper 조회 결과 VO는 MyBatis 매핑 편의를 위해 Setter를 제한적으로 허용한다.
6. JPA Entity와 Repository는 MVP 신규 도메인에 만들지 않는다.
7. JPA/QueryDSL 관련 샘플은 샘플 정리 단계에서 제거 여부를 확정한다.

## 참고 출처

- 전자정부프레임워크 포털: https://www.egovframe.go.kr/
- 전자정부프레임워크 MyBatis 가이드: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte2:psl:data:jpa:mybatis
- 전자정부프레임워크 Data Access 가이드: https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte2:psl:data_access
