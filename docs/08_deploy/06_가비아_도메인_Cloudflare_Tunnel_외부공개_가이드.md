# 가비아 도메인 Cloudflare Tunnel 외부 공개 가이드

## 1. 문서 목적

이 문서는 포트폴리오 시연을 위해 가비아에서 저렴한 도메인을 1년 단위로 구매하고, 집의 가상 환경 Ubuntu VM에 배포된 서비스를 Cloudflare Tunnel로 외부에 공개하는 실행 계획을 정리한다.

목표는 아래 상태를 만드는 것이다.

```text
외부 사용자
  -> 개인 도메인
  -> Cloudflare DNS / Tunnel
  -> 집 Ubuntu VM
  -> Docker Compose frontend/backend/postgresql
```

이 방식은 클라우드 서버 비용을 줄이면서도 이력서, Notion, GitHub README에 포트폴리오용 접속 주소를 적을 수 있다는 장점이 있다.

---

## 2. 최종 목표 구조

권장 도메인 구조:

```text
https://demo.<구매도메인>
  -> frontend container :3000

https://api.<구매도메인>
  -> backend container :8080
```

공개하지 않는 주소:

```text
Jenkins:    8081
PostgreSQL: 5432
Docker:     /var/run/docker.sock
```

예시:

```text
Frontend: https://demo.example.kr
Backend:  https://api.example.kr
Swagger:  https://api.example.kr/swagger-ui/index.html
```

Swagger는 포트폴리오 시연 기간에만 임시 공개하고, 장기 공개 시에는 막거나 별도 인증을 붙이는 방향을 추천한다.

---

## 3. 왜 이 방식을 선택하는가

| 방식 | 판단 |
|---|---|
| 가비아 도메인 1년 구매 | 포트폴리오 기간 동안만 짧게 쓰기 좋고, 국내 결제/관리 UI가 익숙함 |
| Cloudflare DNS | Cloudflare Tunnel과 public hostname 연결이 자연스럽고 HTTPS 처리가 쉬움 |
| Cloudflare Tunnel | 공유기 포트포워딩 없이 집 VM 서비스를 외부에 공개 가능 |
| 집 Ubuntu VM | 기존 Jenkins/Docker Compose 배포 구조를 그대로 활용 가능 |
| VPS 미사용 | 1차 시연 비용을 줄이고 로컬 VM 배포 경험을 살림 |

주의:

- 집 PC와 Ubuntu VM이 꺼지면 외부 사이트도 내려간다.
- 인터넷 회선, 공유기, VM, Docker 중 하나가 멈추면 접속이 끊긴다.
- 포트폴리오용 단기 시연에는 적합하지만, 장기 운영 서비스로는 VPS/클라우드가 더 안정적이다.

---

## 4. 사전 결정

### 4.1 도메인 이름

도메인은 포트폴리오용이므로 너무 긴 이름보다 기억하기 쉬운 이름을 고른다.

추천 기준:

- [ ] 1년 비용이 낮은 TLD를 우선 확인한다.
- [ ] 프로젝트 성격이 드러나는 단어를 넣는다.
- [ ] 한글 도메인보다 영문 도메인을 우선한다.
- [ ] 이력서에 적어도 어색하지 않은 이름을 고른다.
- [ ] 개인정보가 과하게 드러나는 이름은 피한다.

예시:

```text
healthqueue.kr
smartclinic.kr
hc-reservation.site
public-health-demo.com
```

가격은 시점과 이벤트에 따라 바뀌므로 구매 직전에 가비아 장바구니에서 1년 등록비와 2년차 연장비를 함께 확인한다. 첫해만 싼 도메인은 2년차 연장비가 높을 수 있다.

### 4.2 공개 hostname

이 프로젝트에서는 root domain보다 subdomain을 추천한다.

```text
demo.<domain>  -> frontend
api.<domain>   -> backend
```

이유:

- frontend와 backend를 명확히 분리할 수 있다.
- CORS 설정이 단순하다.
- 나중에 `www`, `blog`, `notion` 같은 다른 용도로 root domain을 남겨둘 수 있다.

---

## 5. 전체 작업 순서

```text
1. 가비아에서 도메인 1년 구매
2. Cloudflare에 사이트 추가
3. Cloudflare가 배정한 nameserver 확인
4. 가비아 도메인 관리에서 nameserver를 Cloudflare nameserver로 변경
5. Cloudflare에서 도메인 활성화 확인
6. Ubuntu VM에 cloudflared 설치
7. Cloudflare Tunnel 생성
8. public hostname 2개 연결
9. .env의 frontend/backend 공개 URL 반영
10. Jenkins Credentials .env 갱신
11. Jenkins Pipeline 또는 Docker Compose 재배포
12. 외부 네트워크에서 접속 검증
```

Cloudflare 공식 문서 기준으로, 도메인을 Cloudflare에 추가하면 Cloudflare가 해당 zone에 사용할 nameserver를 배정하고 등록기관에서 그 nameserver로 변경해야 한다. Tunnel public hostname을 추가하면 Cloudflare가 해당 hostname을 tunnel subdomain으로 라우팅하는 DNS 레코드를 만든다.

참고:

- Cloudflare nameserver 변경: https://developers.cloudflare.com/dns/nameservers/update-nameservers/
- Cloudflare Tunnel: https://developers.cloudflare.com/tunnel/
- Cloudflare Tunnel routing: https://developers.cloudflare.com/tunnel/routing/
- 가비아 네임서버 설정 페이지: https://domain.gabia.com/manage/changeinfo/nameserver

---

## 6. 가비아 도메인 구매와 Cloudflare 연결

### 6.1 가비아에서 도메인 구매

- [ ] 가비아에서 후보 도메인을 검색한다.
- [ ] 1년 등록 비용을 확인한다.
- [ ] 2년차 연장 비용도 함께 확인한다.
- [ ] 개인정보 보호 또는 소유자 정보 공개 범위를 확인한다.
- [ ] 결제 후 도메인 관리 화면에서 도메인이 보이는지 확인한다.

구매 후 바로 해야 할 일:

```text
도메인명:
등록기관: 가비아
등록기간: 1년
만료일:
자동연장 여부:
```

자동연장은 포트폴리오를 이번년도만 쓸 계획이면 꺼두거나, 만료 1개월 전 알림을 설정한다.

### 6.2 Cloudflare에 사이트 추가

Cloudflare Dashboard에서 진행한다.

- [ ] Cloudflare 계정 로그인
- [ ] Add a site
- [ ] 가비아에서 구매한 도메인 입력
- [ ] Free plan 선택
- [ ] DNS scan 결과 확인
- [ ] Cloudflare가 배정한 nameserver 2개 확인

Cloudflare가 보여주는 nameserver 예시:

```text
xxxx.ns.cloudflare.com
yyyy.ns.cloudflare.com
```

이 값은 계정과 zone마다 다르므로 문서 예시를 그대로 쓰지 않는다.

### 6.3 가비아 nameserver 변경

가비아 도메인 관리 화면에서 nameserver를 Cloudflare nameserver로 변경한다.

- [ ] 가비아 로그인
- [ ] 도메인 관리
- [ ] 네임서버 설정 또는 변경 메뉴 이동
- [ ] 기존 가비아 nameserver 대신 Cloudflare nameserver 2개 입력
- [ ] 변경 저장
- [ ] Cloudflare Dashboard에서 nameserver 확인 또는 Check nameservers 실행

주의:

- DNS 전파에는 시간이 걸릴 수 있다.
- 기존에 가비아 DNS에서 쓰던 메일, 블로그, 다른 서비스 레코드가 있으면 Cloudflare DNS로 옮겨야 한다.
- 이번 프로젝트만 쓰는 새 도메인이면 옮길 기존 레코드가 거의 없으므로 훨씬 단순하다.

---

## 7. Ubuntu VM에 cloudflared 설치

Ubuntu VM 안에서 진행한다. Windows 로컬이 아니라 실제 Docker Compose가 실행되는 VM에 설치한다.

확인:

```bash
hostname
docker ps
docker compose --env-file .env ps
```

설치 방식은 Cloudflare 공식 문서의 Linux 설치 기준을 따른다. 설치 후 아래를 확인한다.

```bash
cloudflared --version
```

로그인:

```bash
cloudflared tunnel login
```

이 명령은 브라우저 인증을 요구한다. VM이 GUI 없는 서버라면 출력되는 URL을 Windows 브라우저에서 열어 인증한다.

---

## 8. Tunnel 구성

### 8.1 Tunnel 생성

예시 이름:

```bash
cloudflared tunnel create health-center-demo
```

Tunnel 이름은 포트폴리오 프로젝트명을 드러내되 너무 길지 않게 둔다.

### 8.2 Public hostname 연결

Cloudflare Dashboard Zero Trust에서 public hostname을 추가한다.

권장 라우팅:

| Hostname | Service |
|---|---|
| `demo.<domain>` | `http://localhost:3000` |
| `api.<domain>` | `http://localhost:8080` |

VM에서 Docker Compose가 host port를 아래처럼 열고 있으므로 Tunnel은 VM의 localhost로 접근하면 된다.

```text
frontend: 3000:3000
backend:  8080:8080
```

Jenkins와 PostgreSQL은 public hostname을 만들지 않는다.

### 8.3 Tunnel 서비스 등록

VM 재부팅 후에도 Tunnel이 살아나야 하므로 system service로 등록한다.

예상 흐름:

```bash
sudo cloudflared service install
sudo systemctl status cloudflared
```

등록 방식은 Cloudflare 설치 방식과 tunnel 관리 방식에 따라 달라질 수 있으므로, 실제 명령은 Cloudflare Dashboard가 안내하는 connector command를 우선한다.

---

## 9. 프로젝트 환경변수 변경

외부 도메인으로 접속하려면 `.env` 값을 도메인 기준으로 바꾼다.

예시:

```env
NEXT_PUBLIC_API_BASE_URL=https://api.<domain>
CORS_ALLOWED_ORIGINS=https://demo.<domain>,http://localhost:3000
```

중요:

- `NEXT_PUBLIC_API_BASE_URL`은 브라우저 기준 backend 주소다.
- Next.js 클라이언트 번들에 들어가므로 값 변경 후 frontend 재빌드가 필요하다.
- Jenkins Pipeline이 `.env`를 Jenkins Credentials `health-center-env-file`에서 가져오면 Jenkins Credentials도 갱신해야 한다.

진행 순서:

- [ ] VM 로컬 `.env` 수정
- [ ] Jenkins Credentials의 `health-center-env-file`도 같은 값으로 갱신
- [ ] `docker compose --env-file .env config`로 치환 확인
- [ ] Jenkins Build Now 또는 main merge로 재배포
- [ ] frontend 컨테이너가 새 API URL로 빌드되었는지 확인

---

## 10. Jenkins와 Tunnel 관계

Jenkins는 외부 공개 대상이 아니다.

권장 운영:

```text
GitHub main merge
  -> VM Jenkins Poll SCM
  -> Docker Compose 재배포
  -> Cloudflare Tunnel은 계속 같은 VM port를 외부 도메인으로 연결
```

Jenkins가 컨테이너를 재시작해도 host port가 그대로라면 Tunnel public hostname은 유지된다.

단, 다음 값이 바뀌면 재확인한다.

- frontend host port
- backend host port
- `.env`의 `NEXT_PUBLIC_API_BASE_URL`
- `.env`의 `CORS_ALLOWED_ORIGINS`
- Cloudflare public hostname

---

## 11. 외부 검증 체크리스트

집 Wi-Fi가 아닌 환경에서 확인한다. 휴대폰 LTE/5G가 가장 간단하다.

### 11.1 접속

- [ ] `https://demo.<domain>` 접속
- [ ] 로그인 화면 표시
- [ ] `https://api.<domain>/swagger-ui/index.html` 접속 여부 확인
- [ ] Swagger를 공개하지 않기로 했다면 차단 또는 미공유 처리

### 11.2 API

- [ ] 브라우저 개발자 도구 Network에서 API 요청이 `https://api.<domain>`으로 나감
- [ ] CORS 오류 없음
- [ ] 로그인 성공
- [ ] accessToken 저장과 화면 이동 정상

### 11.3 대표 계정

```text
관리자: admin@test.com / password1234
직원: staff@test.com / password1234
시민: user@test.com / password1234
```

- [ ] 시민 예약 화면 확인
- [ ] 직원 접수/대기열 화면 확인
- [ ] 관리자 대시보드 화면 확인

---

## 12. 보안 체크리스트

- [ ] Jenkins `8081` public hostname을 만들지 않는다.
- [ ] PostgreSQL `5432` public hostname을 만들지 않는다.
- [ ] 공유기 포트포워딩을 열지 않는다.
- [ ] Cloudflare Tunnel connector token을 Git에 올리지 않는다.
- [ ] `.env`를 Git에 올리지 않는다.
- [ ] JWT secret을 예시 값에서 바꾼다.
- [ ] demo 계정 비밀번호는 포트폴리오용으로만 사용한다.
- [ ] 관리자 계정을 공개해야 한다면 DB에 민감 데이터가 없는지 확인한다.
- [ ] 시연 종료 후 Tunnel public hostname을 삭제하거나 cloudflared 서비스를 중지한다.

---

## 13. 시연 종료와 도메인 만료 관리

이번년도만 사용할 계획이면 종료 기준을 미리 적어둔다.

| 항목 | 결정 |
|---|---|
| 도메인 만료일 | |
| 자동 연장 | 사용 안 함 또는 만료 전 재검토 |
| 시연 종료일 | |
| Tunnel 중지 방법 | `sudo systemctl stop cloudflared` 또는 Cloudflare public hostname 삭제 |
| README URL 유지 여부 | 종료 후 URL 제거 또는 "현재 비공개"로 변경 |

시연 종료 작업:

- [ ] Cloudflare public hostname 삭제
- [ ] VM cloudflared 서비스 중지
- [ ] README와 포트폴리오 문서의 접속 가능 문구 수정
- [ ] 가비아 자동 연장 설정 확인
- [ ] Cloudflare zone 유지 또는 삭제 결정

---

## 14. 트러블슈팅

### 14.1 도메인이 Cloudflare에서 활성화되지 않음

확인:

- 가비아 nameserver가 Cloudflare에서 안내한 값과 정확히 일치하는지 확인
- Cloudflare에서 Check nameservers 실행
- DNS 전파 대기

### 14.2 frontend는 뜨는데 API가 실패함

확인:

- `.env`의 `NEXT_PUBLIC_API_BASE_URL`
- Jenkins Credentials `.env` 갱신 여부
- frontend 재빌드 여부
- backend public hostname 연결 여부
- `CORS_ALLOWED_ORIGINS`에 `https://demo.<domain>` 포함 여부

### 14.3 Tunnel은 살아 있는데 서비스가 안 뜸

확인:

```bash
docker compose --env-file .env ps
docker compose --env-file .env logs frontend
docker compose --env-file .env logs backend
sudo systemctl status cloudflared
```

### 14.4 집 PC를 끄면 접속이 안 됨

정상이다. 이 구조는 집 Ubuntu VM을 원본 서버로 쓰므로 PC/VM/Docker/Tunnel이 모두 실행 중이어야 한다.

---

## 15. 완료 기준

- [ ] 가비아에서 도메인 1년 구매 완료
- [ ] Cloudflare에 도메인 추가 완료
- [ ] 가비아 nameserver를 Cloudflare nameserver로 변경 완료
- [ ] Cloudflare zone 활성화 확인
- [ ] Ubuntu VM에 cloudflared 설치
- [ ] `demo.<domain>` public hostname이 frontend로 연결
- [ ] `api.<domain>` public hostname이 backend로 연결
- [ ] `.env`와 Jenkins Credentials가 공개 URL 기준으로 갱신
- [ ] Jenkins 또는 Docker Compose 재배포 성공
- [ ] 외부 네트워크에서 로그인과 대표 화면 확인
- [ ] Jenkins/PostgreSQL은 외부 비공개 유지

---

## 16. 다음 작업 후보

1순위:

```text
가비아에서 사용할 도메인 후보 3개와 1년/연장 비용을 비교한 뒤 최종 도메인을 고른다.
```

2순위:

```text
Cloudflare에 도메인을 추가하고 가비아 nameserver를 Cloudflare nameserver로 변경한다.
```

3순위:

```text
Ubuntu VM에 cloudflared를 설치하고 demo/api public hostname을 연결한 뒤 .env와 Jenkins Credentials를 갱신한다.
```
