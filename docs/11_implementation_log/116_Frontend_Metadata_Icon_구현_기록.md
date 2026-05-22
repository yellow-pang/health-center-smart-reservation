# 116. 프론트엔드 메타데이터 및 아이콘 설정 구현 기록

## 브랜치 정보

- **브랜치**: `chore/frontend-metadata-icon`
- **작업 일자**: 2026-05-22
- **작업 유형**: chore (빌드·설정·의존성)

---

## 작업 목적

- v0 생성 흔적인 `generator: 'v0.app'` 제거
- 기존 icon-light/dark-32x32.png 대신 프로젝트 전용 아이콘으로 교체
- 사이트 설명 문구를 사람이 쓰는 자연스러운 문장으로 수정
- Open Graph 메타태그 추가 (소셜 미디어 링크 공유 시 썸네일 표시)
- `manifest.json` 생성으로 모바일 홈 화면 추가 지원

---

## 작업 전 체크리스트

- [x] 현재 `layout.tsx` 메타데이터 상태 확인
- [x] `public/` 아래 아이콘 파일 4종 시각 확인
- [x] 사용자 의견 확인 (아이콘 스타일, 문구, OG·manifest 추가 여부)

---

## 변경 내역

### `frontend/app/layout.tsx`

| 항목 | 변경 전 | 변경 후 |
|---|---|---|
| `generator` | `'v0.app'` | 제거 |
| `description` | `'보건소 예약, 대기, 혼잡도 분석을 위한 통합 시스템'` | `'가까운 보건소 예약을 미리 잡고 대기 없이 방문하세요'` |
| `metadataBase` | 없음 | `NEXT_PUBLIC_APP_URL` 환경변수 (기본 `http://localhost:3000`) |
| `icons.icon` | `icon-light/dark-32x32.png`, `icon.svg` | `/health_reservation_icon_128.png` |
| `icons.apple` | `/apple-icon.png` | `/보건소예약앱아이콘2.png` |
| `openGraph` | 없음 | title, description, image(1080×1080), locale, type 추가 |
| `manifest` | 없음 | `/manifest.json` |

### `frontend/public/manifest.json` (신규)

```json
{
  "name": "보건소 스마트 예약 시스템",
  "short_name": "보건소 예약",
  "description": "가까운 보건소 예약을 미리 잡고 대기 없이 방문하세요",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#0d9488",
  "lang": "ko",
  "icons": [128px, 1080px]
}
```

### `frontend/.env.example`

`NEXT_PUBLIC_APP_URL=http://localhost:3000` 항목 추가

---

## 아이콘 파일 선택 근거

| 파일 | 크기 | 용도 | 선택 이유 |
|---|---|---|---|
| `health_reservation_icon_128.png` | 128×128px | favicon (브라우저 탭) | 크기 최적, 영문 파일명으로 안정적 |
| `보건소예약앱아이콘2.png` | 1080×1080px | Apple Touch / OG / manifest | 고해상도, 앱 아이콘 스타일, "보건소" 텍스트 포함으로 직관적 |

---

## 검증 결과

| 항목 | 결과 |
|---|---|
| `npm run build` | 통과 (`Compiled successfully`) |
| `metadataBase` 경고 | 제거됨 (env 변수로 연결) |
| TypeScript 오류 | 없음 |

---

## 배포 시 확인 사항

- `.env` 또는 서버 환경변수에 `NEXT_PUBLIC_APP_URL=https://demo.<domain>` 설정 필요
- 설정하지 않으면 OG 이미지 URL이 `http://localhost:3000`으로 생성됨
- 브라우저 탭 아이콘, 모바일 홈 화면 추가 아이콘은 `NEXT_PUBLIC_APP_URL` 미설정에도 정상 동작

---

## 확인하지 못한 항목

| 항목 | 사유 |
|---|---|
| 브라우저 탭 아이콘 실제 표시 | 런타임 확인은 사용자 직접 수행 |
| Apple Touch 홈 화면 추가 | iOS Safari에서 사용자 직접 확인 |
| OG 이미지 소셜 미디어 표시 | 실제 배포 URL 연결 후 확인 가능 |
| PWA 설치 배너 | Android Chrome에서 사용자 직접 확인 |

---

## 다음 작업 후보

1. 배포 시 `NEXT_PUBLIC_APP_URL` 환경변수 설정 확인
2. OG 디버그 도구(Facebook Debugger, Twitter Card Validator)로 메타태그 확인
3. 포트폴리오 정리 작업
