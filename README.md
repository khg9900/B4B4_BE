# 🚒 삐뽀삐4 (B4B4)

> **재난의 골든타임을 지킨다, 모두가 함께.**  
> 개인(IND) · 공공기관(GOV) · 민간단체(NGO)를 하나로 연결하는 **실시간 재난 긴급 지원 플랫폼**


## 📖 서비스 소개

**“삐뽀삐4”** 는 재난 발생 직후부터 복구·지원까지 모든 단계를 아우르는 통합 플랫폼입니다.  
한 개인의 신고가 공공기관과 민간단체를 움직이고, 봉사자들은 안전하게 조직되어 현장으로 향합니다.

👉 **우리의 목표**는 기술로 사람을 연결하고 생명을 구하는 것입니다.

![img.png](docs/img_0.png)

## ✨ 주요 기능

### 🔐 인증 / 인가
- JWT 기반 로그인 / 회원가입
- AccessToken + RefreshToken 구조
- Redis 기반 RefreshToken 저장 및 재발급
- Role 기반 접근 제어 (IND, GOV, NGO)


### 📍 위치
- 사용자 주변 nkm 반경 내 재난 히트맵 표시
- 사용자 근처 대피소 정보 제공 (카카오 맵 연동)


### 🚨 신고
- 재난 신고 등록 기능
- Kafka 기반 실시간 알림 → 공공기관 전달
- 신고 상태 관리 (PENDING → RECEIVED → CLOSED)
- 신고 목록 및 상세 조회
- Kafka DLQ 기반 실패/예외 처리


### ❤️ 봉사
- 민간단체: 봉사 모집글 생성 / 수정 / 조회
- 개인: 모집글 참가 / 취소
- Redis Lua Script 기반 실시간 팀 인원 관리 (INCR/DECR)
- 중복 및 정원 초과 방지
- Kafka + DLQ 기반 재처리


### 🔔 알림
- FCM 디바이스 토큰 등록
- 재난 신고 실시간 알림 (관할 공공기관 → 푸시 전송)
- 동일 유형 신고 다수 발생 시 전국 단위 알림 전송
- 봉사 모집글 수정 시 신청자 대상 알림
- 사용자별 수신 알림 조회 (최대 30건)


##  기술 스택

**Backend**

<img width="95" height="28" alt="image" src="https://github.com/user-attachments/assets/6fd1259e-6773-4ae4-a676-7da25b74b92e"/>
<img width="172" height="28" alt="image" src="https://github.com/user-attachments/assets/bab60c84-dd9b-4624-8983-52e35fd31b74" />
<img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
<img width="165" height="28" alt="image" src="https://github.com/user-attachments/assets/67862191-7aa7-4562-a5f5-f63f6877a567" />
<img width="94" height="28" alt="image" src="https://github.com/user-attachments/assets/dd8c2c1a-0c88-481b-99ff-f17c3b15e380" />



**Security**

<img width="167" height="28" alt="image" src="https://github.com/user-attachments/assets/ea27dfa7-e997-4d50-be5a-e648c3d14b1c" />
<img width="71" height="28" alt="image" src="https://github.com/user-attachments/assets/d86de37a-b193-41e7-9e54-9e1c32647e18" />



**DB & ORM**

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![PostGIS](https://img.shields.io/badge/PostGIS-3982CE?style=for-the-badge&logo=postgresql&logoColor=white)
<img width="124" height="28" alt="image" src="https://github.com/user-attachments/assets/c8447186-b4e2-475e-a708-0e969e45e6ae" />
<img width="115" height="28" alt="image" src="https://github.com/user-attachments/assets/f7f3778d-c613-4ee6-95b0-06a0d703d872" />


**Message Broker**

![Kafka](https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)

**Real-Time**

![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=socketdotio&logoColor=white)
![FCM](https://img.shields.io/badge/Firebase%20Cloud%20Messaging-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)

**Infra**

![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![AWS EC2](https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)


**Collaboration**

![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
<img width="98" height="28" alt="image" src="https://github.com/user-attachments/assets/aab34b88-cb8a-4399-bb23-3b07539236e9" />
<img width="86" height="28" alt="image" src="https://github.com/user-attachments/assets/935fd7b3-e11c-4627-8650-db85114a0b84" />
<img width="87" height="28" alt="image" src="https://github.com/user-attachments/assets/ef81f407-95b2-4018-b0dc-b588af503293" />
<img width="99" height="28" alt="image" src="https://github.com/user-attachments/assets/d1492cb2-783c-4fed-87c8-8426fd93135e" />



## 📌 아키텍처

### Cloud Architecture
V1  
![img_1.png](docs/img_1.png)

V2  
![img_2.png](docs/img_2.png)


## 📌 ERD
<img width="3342" height="3040" alt="image" src="https://github.com/user-attachments/assets/a644f0df-44c7-4273-b515-92ec1a18aa44" />



## 📌 와이어프레임
- Figma: https://embed.figma.com/board/CTz4eGFcktnV4imyqsNnT8/%EC%82%904%EC%82%904?node-id=0-1&t=kxXtaoKOu8ie5Ncs-1&embed-host=notion&footer=false&theme=system

## 📌 API 명세서

- Notion: https://www.notion.so/1f5859344fa4810f814ed30ee25a0fc9?v=1f5859344fa4814fbea8000c4ab5c5c2

## 📌 실행 방법

```bash
# 프로젝트 클론
git clone https://github.com/{username}/{repo}.git

# 환경 변수 설정
cp .env.example .env

# 빌드 및 실행
./gradlew build
java -jar build/libs/b4b4-0.0.1-SNAPSHOT.jar
