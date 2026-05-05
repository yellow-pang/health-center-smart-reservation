# 보건소 스마트 예약·대기 및 혼잡도 분석 시스템

## 1. 프로젝트 소개

보건소의 예방접종, 건강검진/검사, 건강상담 업무를 대상으로 예약, 현장 접수, 대기번호 발급, 직원 호출, 처리 완료, 혼잡도 분석을 제공하는 공공보건 운영 지원 시스템입니다.

## 2. 핵심 목표

- 보건소 방문자의 대기시간 감소
- 예약자와 현장 방문자의 통합 관리
- 혼잡 데이터 기반 운영 개선
- 직원의 현장 업무 효율화
- 관리자 대시보드 제공

## 3. 기술 스택

| 영역 | 기술 |
|---|---|
| Frontend | React |
| Backend | 전자정부프레임워크 기반 Spring Boot |
| DB | PostgreSQL |
| ORM | JPA |
| Auth | Spring Security + Access Token / Refresh Token |
| Deploy | Docker Compose |

## 4. Repository 구조

```text
health-center-smart-reservation
 ├─ backend
 ├─ frontend
 ├─ docs
 ├─ docker-compose.yml
 └─ README.md
```

## 5. 문서

설계 문서는 `docs` 폴더에서 관리합니다.

먼저 확인할 문서:

1. `docs/01_planning/01_프로젝트_기획서.md`
2. `docs/01_planning/02_요구사항_정의서.md`
3. `docs/02_domain/01_Bounded_Context_명세서.md`
4. `docs/04_api/01_API_명세서.md`
5. `docs/09_agent/02_에이전트_프롬프트_및_코드작성_지침.md`

## 6. 현재 상태

현재는 설계 문서 정리 단계입니다.

다음 단계:

1. 백엔드 프로젝트 생성
2. 프론트엔드 프로젝트 생성
3. Docker Compose 구성
4. 공통 응답/예외 구조 구현
5. 인증 기능 구현
