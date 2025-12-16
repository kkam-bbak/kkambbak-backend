# 깜빡 (Kkambbak) Backend

## 목차

- [프로젝트 소개](#프로젝트-소개)
- [주요 기능](#주요-기능)
- [스크린샷](#스크린샷)
- [아키텍처](#아키텍처)
- [기술 스택](#기술-스택)
- [개발 규칙](#개발-규칙)
- [모니터링](#모니터링)
- [지원](#지원)

## 프로젝트 소개

깜빡은 K-콘텐츠로 한국어에 대한 관심은 커졌지만 초보자가 발음, 문법 난이도를 넘지 못해 학습을 포기하는 문제를 해결하기 위해 개발된 **한국어에 관심 있는 외국인을 대상으로 실생활 기반의 한국어 학습 서비스**입니다.

### 팀 구성

- **PM**: 1명
- **Designer**: 1명
- **Frontend**: 2명
- **Backend**: 3명 (개발 팀장: 김준형)

### 개발 기간

2025.09 - 2025.11 (3개월)

## 주요 기능

### 인증 및 사용자 관리
- **구글 OAuth2 소셜 로그인**: Google 계정 기반 간편 로그인
- **게스트 로그인**: 회원가입 없이 서비스 체험
- **게스트 계정 업그레이드**: 게스트에서 정회원으로 전환
- **이메일 OTP 인증**: 안전한 이메일 인증 시스템
- **JWT 기반 인증**: Access/Refresh 토큰 관리

### 학습 기능
- **한국어 이름 생성**: AI 기반 한국어 이름 추천
- **학습 콘텐츠**: 단계별 한국어 학습 콘텐츠
- **발음 평가**: Azure Speech Service 기반 실시간 발음 채점
- **롤플레이**: AI와 대화하며 실생활 한국어 연습

### 설문 및 커뮤니티
- **사용자 설문**: 학습 수준 및 관심사 파악
- **맞춤형 콘텐츠 추천**: 설문 기반 개인화 학습

### 결제 및 구독
- **KakaoPay 결제 연동**: 간편 결제 지원
- **구독 관리**: 자동 갱신 및 만료 처리
- **결제 내역 조회**: 결제 이력 관리

### 파일 관리
- **이미지 업로드**: Cloudflare R2 기반 파일 저장
- **WebP 자동 변환**: 이미지 용량 최적화 (최대 96% 감소)
- **자동 리사이징**: 최대 1920x1920 크기 제한

## 스크린샷

<div align="center">

| 로그인 | OTP 인증 | 회원가입 |
|-------|---------|--------|
| ![Login](https://raw.githubusercontent.com/kkam-bbak/kkambbak/main/images/login.png) | ![OTP](https://raw.githubusercontent.com/kkam-bbak/kkambbak/main/images/otp.png) | ![Profile Creation](https://raw.githubusercontent.com/kkam-bbak/kkambbak/main/images/profile_creation.png) |

| 한국어 이름 생성 | 사용자 설문 | 학습 진행 | 학습 완료 |
|---------------|----------|---------|---------|
| ![Korean Name](https://raw.githubusercontent.com/kkam-bbak/kkambbak/main/images/korean_name.png) | ![Survey](https://raw.githubusercontent.com/kkam-bbak/kkambbak/main/images/survey.png) | ![Learning](https://raw.githubusercontent.com/kkam-bbak/kkambbak/main/images/learning.png) | ![Learning Complete](https://raw.githubusercontent.com/kkam-bbak/kkambbak/main/images/learning_complete.png) |

| 사용자 프로필 | 롤플레이 | 롤플레이 완료 | 결제 |
|-----------|---------|-----------|-----|
| ![Profile](https://raw.githubusercontent.com/kkam-bbak/kkambbak/main/images/profile.png) | ![Roleplay](https://raw.githubusercontent.com/kkam-bbak/kkambbak/main/images/roleplay.png) | ![Roleplay Complete](https://raw.githubusercontent.com/kkam-bbak/kkambbak/main/images/roleplay_complete.png) | ![Payment](https://raw.githubusercontent.com/kkam-bbak/kkambbak/main/images/payment.png) |

</div>

## 아키텍처

### 시스템 아키텍처

<div align="center">

![Backend Architecture](https://raw.githubusercontent.com/kkam-bbak/kkambbak/main/images/kkambbak-Architecture.png)

</div>

### 멀티모듈 구조

본 프로젝트는 **관심사 분리**와 **의존성 최소화**를 위해 4개의 모듈로 구성된 멀티모듈 아키텍처를 채택했습니다.

```
kkambbak-backend/
├── kkambbak/                    # 메인 API 모듈
│   ├── domain/
│   │   ├── auth/                # 인증 (OAuth2, JWT, 이메일 OTP)
│   │   ├── user/                # 사용자 관리
│   │   ├── payment/             # 결제 및 구독
│   │   ├── learning/            # 학습 관리
│   │   ├── roleplay/            # 롤플레이
│   │   ├── survey/              # 설문
│   │   ├── name/                # 한국어 이름 생성
│   │   └── upload/              # 파일 업로드
│   └── global/                  # 공통 설정, 필터, 인터셉터, 예외 처리
│
├── kkambbak-core/               # 도메인 모델 및 레포지토리 모듈
│   ├── entity/                  # JPA 엔티티
│   │   ├── user/
│   │   ├── payment/
│   │   ├── learning/
│   │   ├── roleplay/
│   │   ├── survey/
│   │   └── name/
│   ├── repository/              # JPA Repository
│   └── code/                    # 공통 코드 및 Enum
│
├── kkambbak-client/             # 외부 서비스 통합 모듈
│   ├── azure/                   # Azure Speech Service
│   ├── openai/                  # OpenAI API
│   ├── payment/                 # KakaoPay API
│   ├── r2/                      # Cloudflare R2
│   ├── mail/                    # Gmail SMTP
│   └── discord/                 # Discord Webhook
│
└── kkambbak-scheduler/          # 백그라운드 작업 모듈
    ├── domain/
    │   ├── otp/                 # OTP 만료 처리
    │   └── payment/             # 구독 갱신 및 만료
    ├── aop/                     # 모니터링 및 예외 처리
    └── config/                  # 스케줄러 설정

```

#### 의존성 관계

```
kkambbak (API)
  ├─► kkambbak-core (Domain & Repository)
  └─► kkambbak-client (External Services)

kkambbak-scheduler
  ├─► kkambbak-core
  ├─► kkambbak-client
  └─► kkambbak (Service Layer 재사용)

kkambbak-client
  └─► kkambbak-core (Entity 참조)
```

#### 멀티모듈 설계 이유

1. **외부 의존성 격리** (`kkambbak-client`)
   - 외부 서비스 클라이언트를 별도 모듈에서 관리
   - 서비스 변경 시 영향 범위 최소화 (예: Azure → Google Cloud Speech 전환 시 client 모듈만 수정)
   - 새로운 외부 서비스 추가 및 변경이 간단함

2. **독립적 배포** (API + Scheduler)
   - Docker Compose에서 별도 이미지로 구성
   - API 서버는 빠른 응답에 최적화, Scheduler는 배경작업 처리에 리소스 집중
   - 각 모듈의 스케일링 전략을 독립적으로 구성 가능

3. **공유 코어 모듈** (`kkambbak-core`)
   - JPA 엔티티, Repository 중앙화로 데이터 계층 일관성 보장
   - 순환 의존성 방지
   - 모든 모듈이 동일한 도메인 모델 사용

### 인프라 구성

- **웹 서버**: Nginx (80/443 포트, SSL/TLS 처리, 리버스 프록시)
- **애플리케이션**: Spring Boot API + Scheduler
- **데이터베이스**: PostgreSQL + Redis
- **모니터링**: Prometheus (메트릭) + Loki (로그) + Grafana (시각화) + Promtail (로그 수집)
- **클라우드**: Naver Cloud Platform + Neon Database + Cloudflare R2

## 스케줄러 아키텍처

깜빡 Scheduler는 **독립 배포된 Spring Boot 애플리케이션**으로 백그라운드 작업을 안정적으로 처리합니다.

### 핵심 특징

#### 1. Spring Boot `@Scheduled` 기반
- Cron 표현식을 통한 작업 스케줄링
- Spring의 표준 스케줄링 메커니즘 활용

#### 2. Thread Pool 관리 (10개 스레드)
- 동시성 제어로 효율적인 리소스 사용
- 작업별 독립 스레드 할당으로 격리 보장

#### 3. AOP 기반 횡단 관심사 처리

##### ScheduledTaskTracingAspect
- 모든 스케줄 작업의 실행 시간 자동 측정
- 작업 시작/종료 로그 자동 기록
- Discord로 실시간 성공 알림 전송

##### ExceptionHandlingAspect
- 모든 스케줄 작업의 예외 자동 포착
- Discord로 즉시 에러 알림 전송
- 에러 로그 자동 기록

##### OnlyForActiveUserAspect
- 활성 사용자만 대상으로 배치 작업 수행
- 불필요한 처리 방지로 성능 최적화

#### 4. 주요 스케줄 작업

##### OTP 관리
- **OtpExpireJob**: 만료된 OTP 정리 (매 3분)

##### 구독 관리
- **SubscriptionJob**: 구독 자동 갱신 (매일 자정)
  - KakaoPay API 호출하여 자동 결제
  - 실패 시 재시도 상태로 전환
  - 개별 실패가 전체 배치에 영향 없도록 격리

- **SubscriptionExpiryJob**: 구독 만료 처리 (매일 자정)

- **SubscriptionExpiryReminderJob**: 만료 예정 알림 (매일 오전 9시)

#### 5. 배치 처리 전략
- **개별 예외 격리**: 한 건의 실패가 전체 배치에 영향 없음
- **Discord 실시간 알림**: 성공/실패 건수 즉시 보고
- **재시도 메커니즘**: 실패한 구독은 RETRY 상태로 전환 후 다음 실행 시 재시도

## 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.6
- **Language**: Java 21
- **Security**: Spring Security, OAuth2.0, JWT
- **Database**: PostgreSQL (Neon), Redis
- **ORM**: Spring Data JPA, QueryDSL
- **Validation**: Hibernate Validator
- **Image Processing**: Scrimage (WebP conversion)
- **Documentation**: Spring REST Docs

### DevOps
- **Containerization**: Docker, Docker Compose
- **CI/CD**: GitHub Actions
- **Monitoring**: Prometheus, Loki, Grafana, Promtail
- **Logging**: Logback, Logstash Encoder
- **Cloud**: Naver Cloud Platform

### External Services
- **Azure Speech Service**: 발음 평가
- **OpenAI API**: AI 기반 기능
- **KakaoPay API**: 결제 시스템
- **Cloudflare R2**: 파일 스토리지
- **Discord Webhook**: 알림 시스템
- **Gmail SMTP**: 이메일 발송

## 개발 규칙

### 브랜치 전략 (Branch Strategy)

Git Flow 전략을 기반으로 main/develop 브랜치 구조를 유지하고, develop에서 파생된 feature/*, fix/* 브랜치에서 기능 개발 후 PR을 통한 코드 리뷰를 거쳐 develop으로 Squash Merge합니다.

[자세히 보기 →](https://github.com/kkam-bbak/kkambbak/blob/main/docs/branch-strategy.md)

### 커밋 컨벤션 (Commit Convention)

`<타입>: [ID] 요약` 형식을 사용하고 Feat/Fix/Update/Refactor/Docs 등의 타입으로 커밋 목적을 명확히 합니다.

**예시**: `Feat: [REQ-0001] 로그인 API 구현`

[자세히 보기 →](https://github.com/kkam-bbak/kkambbak/blob/main/docs/commit-convention.md)

## 모니터링

### Prometheus
- **메트릭 수집**: JVM, HTTP 요청, 데이터베이스 커넥션 등
- **Endpoint**: `/actuator/prometheus`
- **Port**: 9090

### Loki + Promtail
- **로그 수집**: 애플리케이션 로그를 중앙 집중화
- **MDC 기반 TraceID**: 요청 추적 용이
- **개인정보 마스킹**: 정규식 기반 자동 마스킹

### Grafana
- **대시보드**: 시스템 메트릭 및 로그 시각화
- **알림**: 임계값 초과 시 Discord 알림
- **Port**: 3001

## 지원

[![Gmail Badge](https://img.shields.io/badge/Gmail-d14836?style=for-the-badge&logo=Gmail&logoColor=white&link=mailto:kjunh972@gmail.com)](mailto:kjunh972@gmail.com)