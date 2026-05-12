# v0 MVP 프론트엔드 제작 프롬프트

## 1. 문서 목적

이 문서는 v0 무료 사용 기준으로 프론트엔드의 큰 틀을 빠르게 만든 뒤, 생성된 코드를 Codex로 구체화하고 백엔드 API와 연결하기 위한 프롬프트 가이드이다.

기존 문서 역할:

| 문서 | 역할 |
|---|---|
| `docs/05_frontend/01_화면_설계서_v0_프롬프트.md` | 화면별 상세 설계와 개별 화면 프롬프트 |
| `docs/05_frontend/02_UX_API_계약_우선순위.md` | 화면 흐름과 API 계약 우선순위 |
| 이 문서 | v0에서 한 번에 큰 골격을 만들기 위한 통합 프롬프트와 Codex 후속 구현 기준 |

## 2. 백엔드 MVP 준비 상태

프론트엔드 제작에 필요한 백엔드 API 계약은 MVP 화면을 구성할 수 있는 수준으로 준비되어 있다.

| 영역 | 프론트에서 사용할 기능 | API |
|---|---|---|
| 인증 | 로그인, 내 정보, 토큰 갱신, 로그아웃 | `POST /api/auth/login`, `GET /api/members/me`, `POST /api/auth/reissue`, `POST /api/auth/logout` |
| 예약 | 업무 유형 조회, 예약 슬롯 조회, 예약 신청, 내 예약 조회/상세/취소 | `GET /api/service-types`, `GET /api/reservation-slots`, `POST /api/reservations`, `GET /api/reservations/me`, `GET /api/reservations/{id}`, `DELETE /api/reservations/{id}` |
| 방문 | 예약자 체크인, 현장 접수 | `POST /api/visits/check-in`, `POST /api/visits/walk-in` |
| 대기열 | 대기열 조회, 호출, 시작, 완료, 보류, 미응답, 취소 | `GET /api/queues`, `POST /api/queues/{id}/call`, `POST /api/queues/{id}/start`, `POST /api/queues/{id}/complete`, `POST /api/queues/{id}/hold`, `POST /api/queues/{id}/no-show`, `POST /api/queues/{id}/cancel` |
| 혼잡도 | 시민용 현재 혼잡도 조회 | `GET /api/congestion/current` |
| 대시보드 | 관리자 요약/차트 지표 | `GET /api/dashboard/summary`, `GET /api/dashboard/hourly-visits`, `GET /api/dashboard/service-wait-times`, `GET /api/dashboard/visit-type-ratio`, `GET /api/dashboard/no-show-rate` |
| 관리자 기준정보 | 업무 유형, 예약 슬롯, 직원, 창구 조회/관리 | `POST /api/admin/service-types`, `PUT /api/admin/service-types/{id}`, `PATCH /api/admin/service-types/{id}/deactivate`, `POST /api/admin/reservation-slots`, `GET /api/admin/staff`, `GET /api/admin/service-windows` |

프론트엔드는 처음부터 모든 API를 완전 연동하기보다 v0에서 mock data 기반 화면 골격을 만든 뒤, Codex로 화면별 API 클라이언트와 실제 연동을 붙이는 방식으로 진행한다.

## 3. v0 사용 원칙

v0 공식 문서 기준으로 이 프로젝트는 여러 사용자 역할과 화면이 있는 복합 앱이므로, 한 번에 모든 기능을 완성시키기보다 계획과 큰 골격을 먼저 만들고 후속 프롬프트로 점진적으로 보강한다.

참고 공식 문서:

- v0 Docs: https://v0.app/docs
- Text Prompting: https://v0.app/docs/text-prompting
- Instructions: https://v0.app/docs/instructions

적용 원칙:

| 원칙 | 이 프로젝트 적용 |
|---|---|
| 구체적으로 요청 | 보건소 스마트 예약/대기/혼잡도 시스템, 역할, 화면, API 기준을 명시한다. |
| 복잡한 작업 나누기 | v0 첫 프롬프트는 앱 골격과 mock UI까지만 요청하고, API 연동은 Codex 작업으로 분리한다. |
| 기술 스택 명시 | Next.js App Router, TypeScript, Tailwind CSS, shadcn/ui, lucide-react, Recharts를 지정한다. |
| UX 선호 명시 | 공공서비스 톤, 업무용 밀도, 모바일 우선 시민 화면, 직원 화면 클릭 최소화를 명시한다. |
| 상태 처리 포함 | loading, empty, error, success toast 같은 화면 상태를 mock 기준으로 요구한다. |

## 4. v0 무료 사용 기준 작업 순서

무료 사용에서는 프롬프트 횟수와 수정 범위를 아껴야 하므로 아래 순서로 진행한다.

| 순서 | 작업 도구 | 목표 |
|---:|---|---|
| 1 | v0 | 라우팅, 레이아웃, 역할별 화면, mock data, 공통 컴포넌트 생성 |
| 2 | v0 또는 직접 수정 | 예약, 직원 대기열, 관리자 대시보드의 큰 UX만 보강 |
| 3 | Codex | 생성된 코드 구조 정리, 타입 분리, 컴포넌트 책임 정리 |
| 4 | Codex | `NEXT_PUBLIC_API_BASE_URL` 기준 API 클라이언트와 auth token 처리 구현 |
| 5 | Codex | mock data를 실제 API 호출로 한 화면씩 교체 |
| 6 | 직접 확인 후 Codex | 세부 스타일, 예외 처리, 백엔드 응답 차이 보정 |

## 5. v0 첫 프롬프트

아래 프롬프트는 v0에서 처음 앱 큰 틀을 만들 때 사용한다.

```text
보건소 스마트 예약·대기 및 혼잡도 분석 시스템의 프론트엔드 MVP 큰 틀을 만들어주세요.

목표:
- 백엔드 연동은 지금 하지 말고 mock data 기반으로 전체 화면 흐름을 확인할 수 있는 앱
- 이후 Codex로 REST API를 연결할 예정이므로 mock data와 화면 컴포넌트를 분리
- 무료 사용 기준이므로 세부 기능 완성보다 핵심 화면 골격, 라우팅, 공통 컴포넌트 완성 우선

기술 스택:
- Next.js App Router
- TypeScript
- Tailwind CSS
- shadcn/ui
- lucide-react
- Recharts
- 실제 API 호출 구현은 하지 말고, 나중에 Codex가 연결하기 쉽도록 mock data와 action handler만 분리

디자인 방향:
- 공공기관 서비스처럼 단정하고 신뢰감 있는 UI
- SaaS 랜딩페이지처럼 과한 hero나 마케팅 화면을 만들지 말고, 바로 사용할 수 있는 업무 앱 화면으로 시작
- 시민 화면은 모바일 우선, 큰 버튼과 명확한 안내
- 직원 화면은 반복 업무용으로 조밀하지만 읽기 쉽게 구성
- 관리자 화면은 KPI와 차트가 한눈에 보이는 운영 대시보드
- 텍스트가 버튼이나 카드 밖으로 넘치지 않게 반응형 처리

역할:
- CITIZEN: 예약 신청, 내 예약 조회/취소, 현재 혼잡도 조회
- STAFF: 예약자 체크인, 현장 접수, 대기열 관리
- ADMIN: 관리자 대시보드, 업무 유형/예약 슬롯/직원/창구 관리

라우팅 예시:
- /login
- /citizen/reservations/new
- /citizen/reservations
- /citizen/congestion
- /staff/check-in
- /staff/walk-in
- /staff/queues
- /admin/dashboard
- /admin/service-types
- /admin/reservation-slots
- /admin/staff
- /admin/service-windows

필수 화면:
1. 로그인 화면
   - 이메일, 비밀번호, 로그인 버튼
   - 테스트 계정 선택 버튼 또는 role switcher를 mock으로 제공

2. 시민 예약 신청 화면
   - 업무 유형 선택
   - 날짜 선택
   - 예약 가능한 시간 선택
   - 방문자 이름/전화번호 입력
   - 예약 신청
   - 예약 완료 상태

3. 시민 내 예약 화면
   - 예약 목록
   - 예약 상태 배지
   - 취소 가능 예약의 취소 버튼
   - 빈 상태와 오류 상태

4. 시민 현재 혼잡도 화면
   - 업무 유형별 대기 인원, 예상 대기시간, 혼잡도 배지
   - 여유/보통/혼잡을 명확하게 구분

5. 직원 체크인 화면
   - 예약번호 입력
   - 체크인 버튼
   - 성공 시 대기번호 크게 표시

6. 직원 현장 접수 화면
   - 방문자 이름, 휴대폰 번호, 업무 유형 선택
   - 접수 완료 시 대기번호 표시
   - 최근 접수 목록

7. 직원 대기열 관리 화면
   - 현재 대기, 호출 중, 처리 중, 보류 요약
   - 업무 유형/상태 필터
   - 대기자 테이블
   - 상태별 가능한 액션 버튼만 표시: 호출, 시작, 완료, 보류, 최종 미응답, 취소
   - 현재 호출 중인 대기번호 강조

8. 관리자 대시보드
   - 오늘 방문자 수, 현재 대기 인원, 평균 대기시간, 노쇼율 KPI
   - 시간대별 방문자 수 라인 차트
   - 업무별 평균 대기시간 막대 차트
   - 예약/현장 방문 비율 차트
   - 노쇼율 카드

9. 관리자 기준정보 화면
   - 업무 유형 관리
   - 예약 슬롯 관리
   - 직원 목록
   - 창구 업무 매핑 목록
   - 처음에는 mock CRUD UI로 구성

상태 처리:
- 모든 목록 화면에 loading, empty, error 상태를 만들어주세요.
- 로그인 필요 또는 권한 없음 상태는 mock 화면 상태로만 보여주세요.
- 주요 command 버튼은 실행 중 disabled 상태와 성공 toast를 보여주세요.

데이터 구조:
- mock data는 src/lib/mock-data.ts처럼 분리해주세요.
- 실제 fetch 호출은 작성하지 말고, 버튼 action handler와 mock service 함수를 분리해서 나중에 Codex가 API로 교체하기 쉽게 해주세요.
- 상태명은 WAITING, CALLED, IN_PROGRESS, HOLD, COMPLETED, NO_SHOW, CANCELED를 사용해주세요.
- 방문 유형은 RESERVED, WALK_IN을 사용해주세요.

결과물:
- 바로 미리보기 가능한 MVP 앱
- 역할별 사이드바 또는 탭 내비게이션
- 공통 StatusBadge, PageHeader, MetricCard, DataTable에 가까운 재사용 컴포넌트
- mock data에서 실제 API로 교체할 위치가 명확한 코드 구조
- 백엔드 연동, 인증 토큰 처리, 환경변수 설정은 구현하지 말고 남겨두기
```

## 6. Codex 후속 구현 기준

v0가 만든 코드는 아래 기준으로 Codex가 구체화한다. v0에는 API 연동까지 맡기지 않고, 화면 골격이 만들어진 뒤 이 기준을 사용해 브랜치 단위로 작게 구현한다.

### 6.1 API 클라이언트 구조 보강

```text
현재 mock data 기반 화면은 유지하고, Spring Boot REST API와 연결할 수 있게 API 클라이언트 구조를 추가해줘.

요구사항:
- NEXT_PUBLIC_API_BASE_URL 환경변수를 사용
- src/lib/api-client.ts에 공통 request 함수를 작성
- 공통 응답 형식은 { success, data, error } 입니다.
- accessToken은 localStorage에 저장된 값을 Authorization: Bearer {token}으로 전달
- 401이면 로그인 화면으로 보낼 수 있게 처리
- 403이면 권한 없음 화면 또는 메시지 표시
- 기존 mock data 함수는 유지하고, 실제 API 함수로 교체하기 쉬운 이름을 사용
```

### 6.2 시민 예약 화면 보강

```text
v0가 만든 시민 예약 화면의 mock data를 백엔드 API 호출로 교체해줘.

백엔드 API 기준:
- GET /api/service-types
- GET /api/reservation-slots?serviceTypeId=1&date=YYYY-MM-DD
- POST /api/reservations
- GET /api/reservations/me
- DELETE /api/reservations/{id}

요구사항:
- 업무 유형 선택 후 날짜와 시간 선택이 단계적으로 이어지게 구성
- 마감 슬롯은 disabled
- 예약 완료 후 내 예약 화면으로 이동할 수 있게 구성
- 취소 버튼은 RESERVED 상태에서만 노출
- loading, empty, error 상태 유지
```

### 6.3 직원 대기열 화면 보강

```text
v0가 만든 직원 대기열 관리 화면의 mock data와 action handler를 백엔드 API 호출로 교체해줘.

백엔드 API 기준:
- GET /api/queues
- POST /api/queues/{id}/call
- POST /api/queues/{id}/start
- POST /api/queues/{id}/complete
- POST /api/queues/{id}/hold
- POST /api/queues/{id}/no-show
- POST /api/queues/{id}/cancel

상태 전이:
- WAITING -> CALLED
- CALLED -> IN_PROGRESS
- IN_PROGRESS -> COMPLETED
- CALLED -> HOLD
- HOLD -> CALLED
- HOLD -> NO_SHOW
- WAITING/CALLED/HOLD -> CANCELED

요구사항:
- 상태별 가능한 버튼만 표시
- 위험 액션인 최종 미응답과 취소는 확인 다이얼로그 사용
- 대기시간이 긴 사람을 강조
- 현재 호출 중인 대기번호를 크게 표시
- 액션 성공 후 목록을 갱신하는 구조로 작성
```

### 6.4 관리자 대시보드 보강

```text
v0가 만든 관리자 대시보드의 mock data를 백엔드 지표 API 호출로 교체해줘.

백엔드 API 기준:
- GET /api/dashboard/summary
- GET /api/dashboard/hourly-visits
- GET /api/dashboard/service-wait-times
- GET /api/dashboard/visit-type-ratio
- GET /api/dashboard/no-show-rate

요구사항:
- 날짜 필터를 상단에 배치
- KPI 카드 4개와 차트 3개를 균형 있게 배치
- 데이터가 없을 때도 0값 차트 또는 빈 상태를 자연스럽게 표시
- Recharts를 사용
- 운영자가 빠르게 읽을 수 있는 밀도 높은 대시보드로 구성
```

## 7. v0 결과물 점검 체크리스트

v0가 생성한 결과는 아래 기준으로 확인한다.

- [ ] 첫 화면이 랜딩페이지가 아니라 실제 앱 화면이다.
- [ ] 시민, 직원, 관리자 화면이 분리되어 있다.
- [ ] mock data와 화면 컴포넌트가 분리되어 있다.
- [ ] 실제 fetch 호출이 과하게 섞이지 않았다.
- [ ] Codex가 API 클라이언트로 교체할 위치가 명확하다.
- [ ] loading, empty, error, unauthorized, forbidden 상태가 있다.
- [ ] 직원 대기열 버튼이 상태별로 다르게 보인다.
- [ ] 관리자 대시보드는 KPI와 차트가 한 화면에서 읽힌다.
- [ ] 모바일에서 시민 예약 흐름의 버튼과 텍스트가 겹치지 않는다.
- [ ] 과한 장식보다 보건소 업무 앱에 맞는 단정한 UI이다.

## 8. 백엔드 연동 시 우선순위

| 순서 | 연동 대상 | 이유 |
|---:|---|---|
| 1 | 로그인과 내 정보 | 역할별 화면 접근 기준 |
| 2 | 업무 유형/예약 슬롯 | 시민 예약 흐름 시작점 |
| 3 | 예약 신청/내 예약/취소 | 시민 MVP 핵심 |
| 4 | 체크인/현장 접수 | 직원 업무 시작점 |
| 5 | 대기열 조회/상태 변경 | 직원 운영 핵심 |
| 6 | 혼잡도 | 시민 방문 판단 기능 |
| 7 | 관리자 대시보드 | 포트폴리오 시각화 핵심 |
| 8 | 관리자 기준정보 | 운영 관리 기능 |

## 9. PR 작성용 요약

```text
## 개요
v0 무료 사용 기준으로 프론트엔드 MVP 큰 틀을 만들고, 이후 Codex로 백엔드 API 연동을 구체화하기 위한 프롬프트와 작업 기준을 정리합니다.

## 변경 내용
- 백엔드 MVP API 준비 상태 요약
- v0 공식 문서 기준 프롬프트 작성 원칙 정리
- 첫 v0 생성용 mock UI 중심 통합 프롬프트 작성
- v0 생성 결과를 Codex로 구체화할 API 클라이언트, 시민 예약, 직원 대기열, 관리자 대시보드 작업 기준 작성
- v0 결과물 점검 체크리스트와 백엔드 연동 우선순위 정리

## 검증
- 문서 링크와 기존 프론트엔드 문서 역할 중복 여부 확인
- API 명세 기준으로 프론트 화면에서 사용할 주요 API 목록 확인

## 후속 작업
- v0에서 첫 프롬프트 실행
- 생성된 프론트엔드 구조 확인
- Codex로 mock data를 API 클라이언트로 단계적 교체
```

## 10. 커밋 메시지

```text
docs: v0 MVP 프론트엔드 제작 프롬프트 정리

- v0 무료 사용 기준 mock UI 중심 첫 프롬프트 작성
- Codex 후속 구현 기준과 백엔드 API 연동 우선순위 정리
- README 문서 읽기 순서에 v0 프론트엔드 제작 문서 추가
```

이미 첫 문서 추가 커밋을 완료한 뒤 수정만 별도 커밋하는 경우:

```text
docs: v0 프론트엔드 프롬프트 Codex 연계 기준 보강

- v0 역할을 mock UI 큰 틀 생성으로 제한
- API 클라이언트와 실제 백엔드 연동은 Codex 후속 구현 기준으로 분리
- 결과물 점검 체크리스트를 Codex 인계 구조 중심으로 수정
```
