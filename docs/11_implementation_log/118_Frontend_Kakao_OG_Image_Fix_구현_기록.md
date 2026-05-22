# 118. 프론트엔드 카카오톡 OG 이미지 표시 오류 수정 구현 기록

## 1. 작업 목표

- 카카오톡 링크 공유 시 제목과 설명은 보이지만 이미지 영역이 비는 원인을 정리한다.
- 외부 크롤러가 안정적으로 가져갈 수 있도록 Open Graph 이미지 경로와 Docker 빌드 환경변수를 보정한다.

## 2. 작업 범위

- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 관련 항목 확인
- [x] `frontend/app/layout.tsx` 메타데이터 확인
- [x] `frontend/Dockerfile`, `docker-compose.yml`의 빌드 환경변수 전달 여부 확인
- [x] 영문 파일명 OG 이미지와 Apple Touch 이미지 추가
- [x] Open Graph/Twitter 이미지 경로를 영문 파일명으로 변경
- [x] Docker 빌드 단계에 `NEXT_PUBLIC_APP_URL` 전달
- [x] README와 브랜치 기록 갱신
- [ ] 카카오톡 링크 미리보기 캐시 초기화 및 실제 공유 확인

## 3. 작업 전 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `dev` |
| 작업 트리 | 시작 시 변경 없음 |
| GitNexus 상태 | 인덱스 stale 경고 확인 |
| GitNexus analyze | `Not inside a git repository` 오류로 갱신 실패 |
| GitNexus impact | repo 지정 후에도 결과 없이 실패 |
| 대체 확인 | `rg`, 파일 직접 확인, `git diff`, `npm.cmd run build`로 보완 |

## 4. 원인 정리

| 원인 후보 | 확인 내용 | 처리 |
|---|---|---|
| 배포 빌드 환경변수 누락 | 루트 `.env`에는 `NEXT_PUBLIC_APP_URL`이 있으나 `frontend/Dockerfile`과 `docker-compose.yml`이 프론트 빌드 단계로 전달하지 않았음 | Docker build args와 runtime env에 추가 |
| OG 이미지 한글 파일명 | `openGraph.images`가 `/보건소예약앱아이콘2.png`를 사용해 외부 크롤러 URL 인코딩/접근성 이슈 가능 | `/og-image.png` 영문 파일명으로 교체 |
| Apple Touch 한글 파일명 | `icons.apple`과 manifest가 한글 파일명을 참조 | `/apple-touch-icon.png` 영문 파일명으로 교체 |

카카오톡 화면에서 제목/설명은 표시되는데 이미지 영역만 비는 증상은, 메타태그 자체는 읽혔지만 `og:image` URL을 크롤러가 가져오지 못했을 때와 맞다.

## 5. 구현 내용

| 파일 | 내용 |
|---|---|
| `frontend/app/layout.tsx` | `appUrl`, `ogImage` 상수 추가, `openGraph.url/siteName/alt`와 `twitter` 메타데이터 추가 |
| `frontend/public/og-image.png` | 기존 고해상도 아이콘을 영문 파일명 OG 이미지로 복사 |
| `frontend/public/apple-touch-icon.png` | 기존 고해상도 아이콘을 영문 파일명 Apple Touch 이미지로 복사 |
| `frontend/public/manifest.json` | 고해상도 아이콘 경로를 영문 파일명으로 변경 |
| `frontend/Dockerfile` | `NEXT_PUBLIC_APP_URL` build arg/env 추가 |
| `docker-compose.yml` | frontend build args/runtime env에 `NEXT_PUBLIC_APP_URL` 추가 |
| `README.md` | 배포 환경변수 설명에 `NEXT_PUBLIC_APP_URL`과 OG 이미지 주의사항 추가 |

## 6. 검증 결과

| 항목 | 결과 |
|---|---|
| `npm.cmd run build` | 통과 |
| `git diff --check` | 통과 |
| 카카오톡 실제 공유 | 미확인, 사용자가 배포 후 확인 필요 |

## 7. 사용자 확인 방법

1. `.env`의 `NEXT_PUBLIC_APP_URL`을 실제 프론트 공개 URL로 설정한다.
2. `docker compose --env-file .env up -d --build frontend`로 프론트 이미지를 다시 빌드/배포한다.
3. 카카오 디벨로퍼스 공유 디버거에서 `https://demo.healthq.store` 캐시를 초기화한다.
4. 카카오톡 대화창에 링크를 다시 붙여 이미지가 표시되는지 확인한다.

## 8. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 구현 중 | Docker 프론트 빌드에 `NEXT_PUBLIC_APP_URL` 전달 | 배포 `.env` 값이 있어도 빌드 단계에 전달되지 않으면 OG URL이 localhost로 생성될 수 있음 | 이번 브랜치에서 처리 |
| 구현 중 | OG/Apple 이미지 파일명 영문화 | 외부 크롤러와 모바일 환경에서 한글 경로보다 안정적 | 이번 브랜치에서 처리 |

## 9. 남은 위험

- 카카오톡은 링크 미리보기 캐시가 남아 있을 수 있어 코드 수정 후에도 캐시 초기화 전까지 기존 결과가 보일 수 있다.
- Cloudflare 또는 프론트 서버가 `/og-image.png`를 외부에서 200 OK로 제공하는지 배포 후 확인해야 한다.

## 10. 커밋 메시지 초안

```text
fix: 카카오톡 공유 이미지 메타데이터 보정

- OG/Apple 이미지 경로를 영문 파일명으로 교체
- Docker 프론트 빌드에 NEXT_PUBLIC_APP_URL 전달
- README와 브랜치 기록에 배포 확인 기준 추가
```
