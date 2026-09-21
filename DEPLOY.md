# 배포 가이드

> PotatoChip의 배포 구조와 운영 방법을 정리한 문서입니다.

## 목차

- [아키텍처](#아키텍처)
- [배포 파이프라인](#배포-파이프라인)
- [설정 관리](#설정-관리)
- [파일 저장](#파일-저장)
- [배포하기](#배포하기)
- [롤백](#롤백)
- [트러블슈팅](#트러블슈팅)
- [최초 구축](#최초-구축)

---

## 아키텍처

```
  개발자 PC
     │ git push (main)
     ▼
  GitHub Actions ──── 1. 테스트 (실패 시 여기서 중단)
     │                2. Docker 이미지 빌드
     │                3. Docker Hub push
     │ ssh
     ▼
  EC2 (ap-northeast-2)
     ├─ nginx  :443 ──── HTTPS 종료, 리버스 프록시
     │    └─ 127.0.0.1:8080
     ├─ Docker 컨테이너 potatochip
     │    ├─ --env-file /home/ubuntu/.env      설정 주입
     │    └─ -v /home/ubuntu/uploads           기존 업로드 파일
     │
     ├──▶ RDS (MariaDB)                        데이터
     └──▶ S3 (potatochip-induk-prod-images)    신규 업로드 이미지
```

| 항목 | 값 |
|------|-----|
| 도메인 | https://uglycrop.duckdns.org |
| 리전 | ap-northeast-2 (서울) |
| 컨테이너 이름 | `potatochip` |
| 이미지 | `{DOCKERHUB_USER}/potatochip:latest` |
| 앱 포트 | 8080 (127.0.0.1 에만 바인딩) |

> 앱 포트를 `127.0.0.1:8080` 으로 묶어서 외부에서 직접 접근할 수 없습니다. 반드시 nginx 를 거칩니다.

---

## 배포 파이프라인

정의 파일: [`.github/workflows/deploy.yml`](.github/workflows/deploy.yml)

### 트리거

| 이벤트 | test | deploy |
|--------|:----:|:------:|
| `main` 에 push | O | O |
| `main` 으로 PR | O | X |
| 수동 실행 (`workflow_dispatch`) | O | O |

### job 구성

**1. `test`** — 배포 게이트

```
checkout → JDK 21 설치 → ./gradlew test
```

실패하면 `deploy` job 이 아예 실행되지 않습니다. 실패 시 테스트 리포트가
`test-report` 아티팩트로 업로드되므로 Actions 화면에서 원인을 확인할 수 있습니다.

**2. `deploy`** — `needs: test`

```
이미지 빌드 → Docker Hub push → EC2 ssh → pull → 컨테이너 교체
```

이미지 빌드를 GitHub 러너에서 수행합니다. EC2(프리티어)에서 Gradle 빌드를
돌리면 메모리가 부족하기 때문입니다.

### GitHub Secrets

| 이름 | 용도 |
|------|------|
| `DOCKERHUB_USER` | Docker Hub 계정 |
| `DOCKERHUB_TOKEN` | Docker Hub 액세스 토큰 |
| `EC2_HOST` | EC2 퍼블릭 IP |
| `EC2_SSH_KEY` | EC2 접속용 개인키 (pem 전문) |

---

## 설정 관리

### 운영

설정값은 **EC2 의 `/home/ubuntu/.env`** 에만 존재합니다. git 에도 Docker 이미지에도
포함되지 않습니다.

```
application.properties  →  .gitignore, .dockerignore 로 제외
/home/ubuntu/.env       →  docker run --env-file 로 주입
```

Spring Boot 의 relaxed binding 이 환경변수 이름을 되돌려 읽습니다.

```
kakao.pay.admin-key   ←   KAKAO_PAY_ADMIN_KEY
```

> **주의** `docker --env-file` 은 따옴표를 값의 일부로 취급합니다.
> `JWT_SECRET="abc"` 로 쓰면 값이 `"abc"` 가 되어 JWT 서명이 깨집니다.
> 따옴표 없이 `JWT_SECRET=abc` 로 작성하세요.
> 줄바꿈도 반드시 LF 여야 합니다. CRLF 면 모든 값 끝에 `\r` 이 붙습니다.

### 테스트

[`src/test/resources/application.properties`](src/test/resources/application.properties)

CI 러너에는 운영 설정이 없으므로 테스트 전용 설정을 별도로 둡니다.
H2 인메모리 DB 와 더미값만 들어 있어 git 에 포함해도 안전합니다.

### 프로파일

| 값 | FileService 구현체 | 저장 위치 |
|----|--------------------|-----------|
| `local` | `LocalFileService` | EC2 디스크 `/app/uploads/products/` |
| `prod` | `S3FileService` | S3 `products/` |

`SPRING_PROFILES_ACTIVE` 로 전환합니다. 두 구현체 모두 `@Profile` 로 갈리므로
값을 바꾸고 컨테이너만 재시작하면 됩니다.

---

## 파일 저장

### S3

| 항목 | 값 |
|------|-----|
| 버킷 | `potatochip-induk-prod-images` |
| 리전 | ap-northeast-2 |
| 키 접두사 | `products/` |
| 객체 소유권 | ACL 비활성화 |
| 공개 범위 | `s3:GetObject` 만 공개 |

인증은 **IAM 역할**(`PotatoChipEC2Role`)로 처리합니다. 액세스 키를 서버에 두지
않으므로 `.env` 에 AWS 자격증명이 없습니다.

버킷 정책은 읽기만 허용하고 `ListBucket` 은 포함하지 않습니다. URL 을 아는 파일만
접근할 수 있고 버킷 안의 목록은 조회할 수 없습니다.

### 업로드 볼륨

```
-v /home/ubuntu/uploads:/app/uploads
```

S3 전환 이전에 업로드된 이미지가 들어 있습니다. DB 의 `imageUrl` 에
`/uploads/products/...` 형태로 저장된 기존 데이터가 계속 동작하려면 이 볼륨과
`WebMvcConfig` 의 리소스 핸들러가 필요합니다.

템플릿이 `imageUrl` 을 `src` 에 그대로 넣기 때문에, 기존 상대경로와 신규 S3
절대 URL 이 섞여 있어도 양쪽 모두 정상 렌더링됩니다. 그래서 전환 시 DB 마이그레이션이
필요하지 않았습니다.

> **볼륨을 삭제하지 마세요.** 기존 이미지가 유실됩니다.

---

## 배포하기

### 코드 변경 배포

```bash
git push origin main
```

이후 자동으로 진행됩니다. Actions 탭에서 진행 상황을 확인합니다.

### 설정만 변경

이미지 재빌드가 필요 없으므로 EC2 에서 직접 처리합니다.

```bash
# 1. 값 수정
nano /home/ubuntu/.env

# 2. 재시작
docker restart potatochip

# 3. 확인
docker logs --tail 30 potatochip | grep -iE 'started|error'
```

### 배포 확인

```bash
docker ps --format '{{.Names}}\t{{.Status}}'
docker logs --tail 50 potatochip | grep -i 'started'
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8080/
```

로그에 남지 않는 항목은 브라우저에서 직접 확인해야 합니다.

- 카카오 / 네이버 로그인
- 카카오페이 결제
- 상품 이미지 업로드

> `@Value` 의 절반 가량에 기본값이 지정되어 있어, 설정 키가 누락되어도 앱은 정상
> 기동합니다. 대신 해당 기능만 조용히 동작하지 않습니다. 배포 후 수동 확인이
> 필요한 이유입니다.

---

## 롤백

### 이전 이미지로

```bash
docker pull {DOCKERHUB_USER}/potatochip:latest
docker rm -f potatochip
# 이전 이미지 태그를 지정해 재실행
```

### 설정 되돌리기

```bash
# 프로파일 원복 (S3 → 로컬 저장)
sed -i 's/^SPRING_PROFILES_ACTIVE=.*/SPRING_PROFILES_ACTIVE=local/' ~/.env
docker restart potatochip
```

### 설정 주입 방식 되돌리기

`/home/ubuntu/config/` 디렉터리에 이전 방식의 `application.properties` 가
남아 있습니다. `deploy.yml` 에서 `--env-file` 줄을 아래로 교체하고 push 합니다.

```yaml
-v /home/ubuntu/config:/app/config
```

---

## 트러블슈팅

| 증상 | 원인 | 조치 |
|------|------|------|
| Actions 에서 `test` 실패 | 테스트 깨짐 | `test-report` 아티팩트 확인 |
| `ERROR: /home/ubuntu/.env 없음` | `.env` 누락 | 파일 생성 후 재실행. 기존 컨테이너는 살아 있음 |
| 컨테이너가 바로 종료 | 설정 오류 | `docker logs potatochip` 확인 |
| 로그인만 실패 | 키 이름 오타 → 기본값 폴백 | `cut -d= -f1 ~/.env` 로 키 이름 대조 |
| 이미지 업로드 403 | 버킷 정책 / IAM 역할 | 아래 확인 명령 참조 |
| JWT 서명 오류 | `.env` 값에 따옴표나 `\r` | `cat -A ~/.env` 로 `^M` 확인 |

### 상태 확인 명령

```bash
# IAM 역할
TOKEN=$(curl -sX PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 60")
curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/iam/security-credentials/

# S3 권한
aws s3 ls s3://potatochip-induk-prod-images/products/ --region ap-northeast-2

# 활성 프로파일
docker logs potatochip 2>&1 | grep -i 'profile is active'
```

---

## 최초 구축

새 환경에 처음 배포할 때만 필요한 절차입니다.

### 1. EC2

```bash
# Docker
sudo apt update && sudo apt install -y docker.io
sudo usermod -aG docker ubuntu

# 디렉터리
mkdir -p /home/ubuntu/uploads

# 설정
nano /home/ubuntu/.env
chmod 600 /home/ubuntu/.env
```

### 2. S3 버킷

| 설정 | 값 |
|------|-----|
| 이름 | `potatochip-induk-prod-images` |
| 리전 | ap-northeast-2 |
| 객체 소유권 | ACL 비활성화됨 (권장) |
| 퍼블릭 액세스 차단 | 해제 |

버킷 정책:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadProductImages",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::potatochip-induk-prod-images/*"
    }
  ]
}
```

### 3. IAM

정책 `PotatoChipS3Access`:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["s3:PutObject", "s3:GetObject", "s3:DeleteObject"],
      "Resource": "arn:aws:s3:::potatochip-induk-prod-images/*"
    },
    {
      "Effect": "Allow",
      "Action": "s3:ListBucket",
      "Resource": "arn:aws:s3:::potatochip-induk-prod-images"
    }
  ]
}
```

역할 `PotatoChipEC2Role` (신뢰 엔터티: EC2)을 만들어 위 정책을 연결하고
EC2 인스턴스에 부착합니다. 재부팅은 필요하지 않습니다.

### 4. GitHub Secrets

위 [GitHub Secrets](#github-secrets) 표의 4개 항목을 등록합니다.

---

## 알려진 이슈

| 내용 | 영향 |
|------|------|
| 게시판 이미지 경로가 윈도우 절대경로로 하드코딩 (`BoardController`, `WebConfig`) | 리눅스에서 게시판 이미지가 표시되지 않음 |
| `ClaudeClient` 는 `anthropic.api-key` 를 읽지만 설정에는 `spring.ai.anthropic.api-key` 로 되어 있음 | AI 리뷰 총평 생성 실패 |
| 업로드 시 리사이징 없음 | 목록 화면에서도 원본 크기를 내려받아 로딩이 느림 |
