# PotatoChip

> 못난이 농산물을 사고파는 마켓플레이스 플랫폼입니다. (팀 프로젝트)

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?logo=springsecurity&logoColor=white)
![JPA](https://img.shields.io/badge/Spring_Data_JPA-59666C?logo=hibernate&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?logo=thymeleaf&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-003545?logo=mariadb&logoColor=white)

## 소개

`PotatoChip`은 상품성이 떨어진다는 이유로 버려지는 못난이 농산물을 판매자와 구매자가 직접 거래하도록 잇는 마켓플레이스입니다. 대우능력개발원 교육 과정의 **팀 프로젝트**로 개발했습니다.

## 기술 스택

| 구분 | 사용 기술 |
|------|-----------|
| Language | Java 21 |
| Framework | Spring Boot (Web, JPA, AOP, Thymeleaf, WebSocket) |
| 인증 | Spring Security, JWT (jjwt 0.11.5) |
| 데이터 | Spring Data JPA, MariaDB |
| AI | Spring AI (Anthropic Claude) |
| 인프라 | Spring Cloud AWS |
| 기타 | ModelMapper, Lombok, springdoc-openapi, Kakao Map API |

## 주요 기능

- **상품** - 상품 CRUD, 판매자 전용 권한 제어, 카테고리 / 키워드 검색
- **장바구니** - 담은 시점의 가격을 유지하는 **가격 스냅샷 기반 CartItem** 구조, 즉시 구매와 요약 금액 계산
- **판매 랭킹** - 기간별 판매 집계
- **지도** - Kakao Map API로 상품별 픽업 위치 표시
- **관리자** - 회원 및 상품 관리
- **부가** - 게시판, 문의, 알림(WebSocket), AI(Claude) 기능

## 디렉토리 구조

```
src/main/java/com/example/potatochip
├─ auth          # 인증 (JWT 필터, 설정)
├─ admin         # 관리자
├─ cart, cartitem # 장바구니, 가격 스냅샷 아이템
├─ board         # 게시판
├─ inquiry       # 문의
├─ notification  # 알림 (WebSocket)
├─ ai/claude     # Spring AI (Anthropic Claude)
└─ ...           # 상품, 주문 등 도메인
```

## 실행 방법

```bash
# 1. 저장소 클론
git clone https://github.com/Jihwan0210/PotatoChip.git

# 2. application.yml(properties)에 MariaDB, JWT, AI 키, Kakao Map 키 설정

# 3. 실행
./gradlew bootRun
```

## 담당 (유지환)

**팀장**이자 **상품 도메인 오너**로 프로젝트 전반을 주도했습니다.

- 상품 CRUD와 판매자 전용 권한 제어
- 가격 스냅샷 기반 CartItem 구조 설계 (담은 시점 가격 유지)
- 즉시 구매 흐름과 장바구니 요약 금액 계산
- 기간별 판매 랭킹 집계
- develop / feature 브랜치 전략 수립, PR 기반 코드 리뷰 운영
- 일정 조율과 문서 총괄
