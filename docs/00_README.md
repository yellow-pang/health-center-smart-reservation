# 설계 문서 안내

이 폴더는 보건소 스마트 예약·대기 및 혼잡도 분석 시스템의 설계 문서 모음입니다.

## 문서 읽는 순서

1. `01_planning/01_프로젝트_기획서.md`
2. `01_planning/02_요구사항_정의서.md`
3. `02_domain/01_Bounded_Context_명세서.md`
4. `02_domain/02_업무_흐름도.md`
5. `02_domain/04_공통코드_관리_설계서.md`
6. `03_database/01_ERD_및_테이블_명세서.md`
7. `04_api/01_API_명세서.md`
8. `09_agent/01_코드_에이전트_작업_가이드.md`
9. `09_agent/02_에이전트_프롬프트_및_코드작성_지침.md`

## 코드 작성 전 반드시 확인할 문서

- `09_agent/01_코드_에이전트_작업_가이드.md`
- `09_agent/02_에이전트_프롬프트_및_코드작성_지침.md`
- `02_domain/04_공통코드_관리_설계서.md`

## 개발 기준

- MVP는 단일 보건소 기준으로 구현한다.
- DB 구조는 여러 보건소 확장이 가능하도록 설계한다.
- 의료정보, 검사 결과, 처방 정보는 저장하지 않는다.
- 실제 구현은 모듈형 단일 애플리케이션으로 진행한다.
- Bounded Context 기준으로 패키지와 책임을 분리한다.
- AI 기능은 MVP에서 제외하고 향후 확장으로 둔다.

## 백엔드 기준

- 백엔드는 전자정부프레임워크 공식 GitHub의 Simple Backend Template을 기반으로 구성한다.
- 빌드 도구는 템플릿 기준인 Maven을 유지한다.
- Spring Initializr 기반 일반 Spring Boot 프로젝트로 새로 생성하지 않는다.
- Maven과 Gradle을 혼용하지 않는다.
- 전자정부프레임워크 관련 핵심 설정은 임의로 제거하지 않는다.
- 샘플 기능 제거가 필요한 경우, 제거 대상 목록을 먼저 제안한 뒤 진행한다.
- 템플릿은 먼저 HSQL 기준으로 실행 확인한 뒤 PostgreSQL로 전환한다.
- MVP에서는 JPA를 사용하지 않고 MyBatis를 기본 DB 접근 방식으로 사용한다.
- 신규 보건소 도메인은 `egovframework.healthcenter` 하위 패키지에 작성한다.
- Mapper XML은 `src/main/resources/egovframework/mapper/healthcenter` 하위에 둔다.
- MVP DB는 PostgreSQL 18 + pgvector Docker 이미지를 사용하되, AI 기능과 vector 컬럼은 향후 확장 기능으로 둔다.
