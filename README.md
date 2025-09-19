# Style My Closet 👗

날씨 기반 AI 의상 추천 및 OOTD(Outfit Of The Day) 공유 플랫폼

## 📋 프로젝트 소개

Style My Closet는 사용자의 옷장과 현재 날씨 정보를 바탕으로 AI가 적절한 의상을 추천하고, OOTD를 공유할 수 있는 소셜 플랫폼입니다. Hugging Face AI 모델을 활용하여 개인화된 의상 추천을 제공하며, 실시간 알림 시스템을 통해 사용자 참여도를 높입니다.

## ✨ 주요 기능

### 🤖 AI 의상 추천 시스템
- **Hugging Face Zero-shot Classification**: `facebook/bart-large-mnli` 모델 활용
- **날씨 기반 추천**: 현재 날씨와 사용자 정보를 고려한 맞춤형 의상 추천
- **규칙 기반 백업 로직**: AI 신뢰도가 낮을 때 패션 규칙으로 대체 추천
- **실시간 날씨 연동**: 기상청 API를 통한 정확한 날씨 정보 제공

### 📱 OOTD 소셜 피드
- **피드 작성/조회**: 의상과 날씨 정보를 포함한 OOTD 공유
- **좋아요/댓글**: 사용자 간 상호작용 기능
- **커서 페이징**: 대용량 데이터에서 일정한 응답시간 보장
- **검색/필터링**: 키워드, 날씨, 작성자별 피드 검색

### 🔔 실시간 알림 시스템
- **Redis Stream**: 사용자별 알림 데이터 관리 (최대 500개)
- **SSE (Server-Sent Events)**: 실시간 알림 전송
- **이벤트 기반 아키텍처**: Spring Event로 비동기 알림 처리
- **Redis Pub/Sub**: 알림 이벤트 분산 처리

### 👤 사용자 관리
- **OAuth2 인증**: Google, Kakao 소셜 로그인 지원
- **JWT 토큰**: 안전한 인증/인가 처리
- **프로필 관리**: 사용자 정보 및 옷장 관리

## 🛠 기술 스택

### Backend
- **Java 21** + **Spring Boot 3.5.4**
- **Spring Data JPA** + **QueryDSL**
- **PostgreSQL** (pgVector 확장)
- **Redis** (Stream, Pub/Sub, Cache)
- **Spring Security** + **OAuth2**

### AI/ML
- **Hugging Face API** (Zero-shot Classification)
- **자연어 프롬프트 엔지니어링**

### Infrastructure
- **AWS S3** (이미지 저장)
- **Docker** + **Docker Compose**
- **Flyway** (데이터베이스 마이그레이션)
- **TestContainers** (통합 테스트)

## 🚀 성능 최적화

### N+1 쿼리 문제 해결
- **fetchJoin 도입**: 의상 조회 시 쿼리 수 80% 감소 (5개 → 1개)
- **2단계 최적화**: 피드 조회에서 ID 조회 → fetchJoin 패턴 적용
- **PostgreSQL 인덱스**: 복합 인덱스로 조회 성능 향상

### 실시간 시스템 최적화
- **Redis Stream**: `MAXLEN 500` 설정으로 메모리 효율성 확보
- **SSE 타임아웃**: 5분 타임아웃과 자동 재연결
- **비동기 처리**: `@TransactionalEventListener`로 8개 이벤트 리스너 구현

### AI API 최적화
- **타임아웃 설정**: 15초 타임아웃으로 응답 시간 제어
- **재시도 로직**: `@Retryable`로 API 안정성 확보
- **백업 시스템**: AI 신뢰도 0.7 미만 시 규칙 기반 로직 적용

## 📁 프로젝트 구조

```
src/main/java/com/stylemycloset/
├── auth/                 # 인증/인가
├── binarycontent/        # 이미지 업로드 (S3)
├── clothes/             # 의상 관리
├── common/              # 공통 유틸리티
├── directmessage/       # DM 기능
├── follow/              # 팔로우 기능
├── location/            # 위치 관리
├── notification/        # 알림 시스템
├── ootd/               # OOTD 피드
├── recommendation/      # AI 추천 시스템
├── security/           # 보안 설정
├── sse/               # 실시간 알림 (SSE)
└── weather/           # 날씨 API 연동
```

## 📊 API 문서

[SwaggerAPI](https://project.sb.sprint.learn.codeit.kr/sb/otboo/api/swagger-ui/index.html)

---


```

## 🔧 개발 환경

- **Java**: 21
- **Gradle**: 8.x
- **PostgreSQL**: 16 (pgVector 확장)
- **Redis**: 7.x
- **Docker**: 20.x+

## 👥 팀원

- [@LeejunHyeok7170](https://github.com/LeejunHyeok7170)
- [@GunsanHaribo](https://github.com/GunsanHaribo)
- [@seonseon933](https://github.com/seonseon933)
- [@20184415](https://github.com/20184415)
- [@JasonHeo1125](https://github.com/JasonHeo1125)
- [@minhyuksim](https://github.com/minhyuksim)

---

**Style My Closet** - AI가 추천하는 나만의 스타일링 ✨
