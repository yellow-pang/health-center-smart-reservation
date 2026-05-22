# 119. 프론트엔드 카카오톡 OG 이미지 표시 오류 수정 PR 작성안

## 브랜치 상태 확인

| 항목 | 확인 결과 |
|---|---|
| 현재 브랜치 | `dev` |
| base 브랜치 | `dev` 기준 작업 |
| 작업 트리 | 시작 시 변경 없음 |
| 빌드 확인 | `npm.cmd run build` 통과 |
| 정적 확인 | `git diff --check` 통과 |
| 실행 확인 | 카카오톡/브라우저 런타임 확인은 사용자가 배포 후 직접 수행 |

## PR 제목

```text
fix: 카카오톡 공유 이미지 메타데이터 보정
```

## PR 본문

```markdown
## 개요

카카오톡 링크 공유 시 제목과 설명은 표시되지만 이미지 영역이 비어 보이는 문제를 수정합니다.
원인은 프론트 Docker 빌드 단계에 `NEXT_PUBLIC_APP_URL`이 전달되지 않아 OG 이미지 URL이 배포 도메인이 아닌 기본값으로 생성될 수 있고, OG 이미지 경로가 한글 파일명이라 외부 크롤러 접근 안정성이 낮은 점으로 정리했습니다.

## 변경 내용

- `og:image`와 Twitter 카드 이미지를 `/og-image.png` 영문 경로로 변경
- Apple Touch/manifest 고해상도 이미지 경로를 `/apple-touch-icon.png`로 변경
- frontend Docker build args와 runtime env에 `NEXT_PUBLIC_APP_URL` 추가
- README에 외부 공개 환경의 `NEXT_PUBLIC_APP_URL` 설정 기준 추가
- 카카오톡 캐시 초기화와 재확인 절차 문서화

## 검증

- [x] `npm.cmd run build`
- [x] `git diff --check`
- [ ] 배포 환경에서 `/og-image.png` 직접 접속 시 200 OK 확인
- [ ] 페이지 HTML의 `og:image`가 `https://demo.healthq.store/og-image.png`로 생성되는지 확인
- [ ] 카카오 디벨로퍼스 공유 디버거에서 캐시 초기화
- [ ] 카카오톡 대화창 링크 미리보기 이미지 표시 확인

## 미검증 사유

- 서버 기동, Docker 실행, 브라우저/카카오톡 런타임 확인은 프로젝트 운영 기준에 따라 사용자가 직접 수행합니다.

## 후속 작업

- 실제 배포 후 카카오톡 캐시 초기화 결과를 구현 기록에 반영합니다.
```

## 참고 문서

- `docs/11_implementation_log/118_Frontend_Kakao_OG_Image_Fix_구현_기록.md`
