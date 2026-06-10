# union-app-backend

> **Union** 대학생 전용 미니앱 슈퍼앱 플랫폼의 Spring Boot 백엔드 서버입니다.  
> 단국대학교 캡스톤디자인 프로젝트

---

## 개요

퍼블리셔가 미니앱을 등록·배포하고, 사용자가 탐색·실행하는 전체 플로우를 담당하는 REST API 서버입니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.4.3 |
| Database | PostgreSQL (Neon) |
| Cache | Redis (Lettuce) |
| Storage | Google Cloud Storage + Cloud CDN |
| Push 알림 | Firebase Cloud Messaging (FCM) |
| Auth | JWT (JJWT 0.12.6), nimbus-jose-jwt |
| Build | Gradle |

## 아키텍처

```
iOS 앱 (사용자)          Next.js 대시보드 (퍼블리셔/어드민)
       │                              │
       │   JWT (User/Publisher)       │   Internal JWT (30초)
       └──────────────┬───────────────┘
                      │
               Spring Boot (:8080)
                      │
          ┌───────────┼───────────┐
          │           │           │
       PostgreSQL    Redis       GCS
      (데이터 저장)  (캐시/토큰) (번들 파일)
```

## 도메인 구조

```
src/main/java/com/union/union/domain/
├── auth/          # 이메일 인증 + JWT 로그인/로그아웃
├── user/          # 사용자 계정 관리
├── miniapp/       # 미니앱 등록, 탐색, 검색, 추천
├── appversion/    # 버전 업로드 → 테스트 → 배포 파이프라인
├── review/        # 심사 요청 및 어드민 승인/반려
├── notification/  # FCM 푸시 + 알림 인박스
└── analytics/     # 이벤트 수집 및 대시보드 통계
```

## 미니앱 배포 파이프라인

```
POST /app-versions              → AppVersion 생성 (DRAFT)
                                  GCS Signed PUT URL 발급 (5분)
PUT {signedUrl}                 → 브라우저에서 GCS 직접 업로드
POST /app-versions/{id}/confirm → 파일 존재 확인 → UPLOADED
POST /app-versions/{id}/test-session  → Redis 토큰 발급 → Universal Link 반환
iOS QR 스캔 → GET /app-versions/test-bundle?token=
                                  Redis 토큰 검증 → CDN Signed URL 반환
POST /app-versions/{id}/test-complete → testedAt 기록
POST /reviews                   → 심사 요청 → IN_REVIEW
[어드민 승인]
POST /app-versions/{id}/deploy  → DEPLOYED
POST /mini-apps/{id}/launch     → CDN Signed URL → 번들 실행
```

## API 엔드포인트

### 인증

| Method | Path | 설명 | 권한 |
|--------|------|------|------|
| POST | `/api/v1/auth/signup` | 사용자 회원가입 | - |
| POST | `/api/v1/auth/login` | 로그인 | - |
| POST | `/api/v1/auth/refresh` | 토큰 갱신 | - |
| POST | `/api/v1/auth/logout` | 로그아웃 | USER |
| POST | `/auth/email/send` | 이메일 인증코드 발송 | - |
| POST | `/auth/email/verify` | 이메일 인증코드 확인 | - |
| POST | `/api/v1/auth/publisher/email/send` | 퍼블리셔 OTP 발송 | - |
| POST | `/api/v1/auth/publisher/email/verify` | 퍼블리셔 OTP 확인 + 토큰 발급 | - |
| POST | `/api/v1/auth/publisher/refresh` | 퍼블리셔 토큰 갱신 | - |
| POST | `/api/v1/auth/publisher/logout` | 퍼블리셔 로그아웃 | PUBLISHER |

### 미니앱

| Method | Path | 설명 | 권한 |
|--------|------|------|------|
| GET | `/mini-apps` | 전체 목록 | - |
| GET | `/mini-apps/popular` | 인기 앱 | - |
| GET | `/mini-apps/discovery` | 디스커버리 (피처드 + 카테고리) | - |
| GET | `/mini-apps/recommendations` | 개인화 추천 | USER |
| GET | `/mini-apps/search` | 검색 | - |
| GET | `/mini-apps/search/preview` | 실시간 검색 미리보기 | - |
| GET | `/mini-apps/search/popular` | 인기 검색어 TOP 5 | - |
| GET | `/mini-apps/category/{categoryId}` | 카테고리 필터 | - |
| POST | `/mini-apps` | 미니앱 등록 | PUBLISHER |
| POST | `/mini-apps/{id}/launch` | 앱 실행 (CDN URL 반환) | USER |

### 앱 버전 / 배포

| Method | Path | 설명 | 권한 |
|--------|------|------|------|
| POST | `/app-versions` | 버전 생성 (DRAFT) | PUBLISHER |
| POST | `/app-versions/{id}/confirm` | 업로드 확정 (UPLOADED) | PUBLISHER |
| POST | `/app-versions/{id}/test-session` | 테스트 링크 발급 | PUBLISHER |
| GET | `/app-versions/test-bundle` | 토큰으로 번들 조회 | AUTH |
| POST | `/app-versions/{id}/test-complete` | 테스트 완료 기록 | PUBLISHER |
| POST | `/app-versions/{id}/deploy` | 배포 (DEPLOYED) | PUBLISHER |
| GET | `/app-versions/mini-app/{miniAppId}` | 버전 목록 | PUBLISHER |

### 심사

| Method | Path | 설명 | 권한 |
|--------|------|------|------|
| POST | `/reviews` | 심사 요청 | PUBLISHER |
| GET | `/reviews/pending` | 대기 중 심사 목록 | ADMIN |
| GET | `/reviews/{id}` | 심사 상세 | ADMIN |
| GET | `/reviews/{id}/test-link` | 어드민용 테스트 링크 | ADMIN |
| POST | `/reviews/versions/{versionId}/decision` | 승인 / 반려 | ADMIN |

### 알림

| Method | Path | 설명 | 권한 |
|--------|------|------|------|
| POST | `/api/v1/notifications/fcm/register` | FCM 토큰 등록 | USER |
| GET | `/api/v1/notifications/inbox` | 알림 인박스 조회 | USER |

### 애널리틱스

| Method | Path | 설명 | 권한 |
|--------|------|------|------|
| POST | `/api/v1/analytics/events` | 이벤트 배치 수집 | USER |
| GET | `/api/v1/analytics/apps/{appId}/summary` | 앱 통계 요약 | PUBLISHER |

---

## 로컬 개발 환경 설정

### 사전 요구사항

- Java 17
- PostgreSQL 또는 Neon DB 계정
- Redis
- GCP 서비스 계정 키 (GCS 접근용)

### 1. 환경변수 설정

`src/main/resources/application-local.yml` 파일을 아래 형식으로 작성합니다.

```yaml
spring:
  mail:
    username: your-email@gmail.com
    password: "gmail-app-password"

  datasource:
    url: jdbc:postgresql://{host}/{db}?sslmode=require
    username: db_user
    password: db_password

  cloud:
    gcp:
      storage:
        bucket: your-bucket-name
        credentials:
          location: file:/path/to/service-account-key.json

cdn:
  base-url: http://your-cdn-ip
  key-name: your-cdn-key-name
  key-value: your-cdn-key-value

internal-jwt:
  secret: your-256bit-secret

universal-link:
  base-url: union-app://test-app

mail:
  dev:
    log-only: true   # 로컬에서 실제 메일 없이 콘솔로 OTP 확인
```

### 2. Redis 실행

```bash
brew services start redis       # macOS
# 또는
docker run -p 6379:6379 redis
```

### 3. 서버 실행

```bash
./gradlew bootRun
```

### 4. 테스트 데이터 시드

```bash
curl -X POST http://localhost:8080/api/dev/seed
```

---

## 보안 구조

### JWT 이중 체계

| 구분 | 발급 대상 | 유효 시간 | 용도 |
|------|----------|----------|------|
| User JWT | iOS 앱 사용자 | 30분 (Access) / 14일 (Refresh) | 일반 API 인증 |
| Internal JWT | Next.js 대시보드 | 30초 | 대시보드 → Spring 통신 |

### Rate Limiting (Redis 기반)

| 엔드포인트 | 제한 |
|-----------|------|
| 로그인 | 10회 / 60초 |
| 회원가입 | 5회 / 60초 |
| 이메일 인증 발송 | 3회 / 60초 |

### 개인정보 보호

- 애널리틱스 이벤트에 userId 원문 비저장
- `SHA-256(userId:appId)` 해싱 후 저장

---

## 브랜치 전략

```
main         ← 프로덕션 배포
develop      ← 개발 통합
feature/*    ← 기능 개발 (PR → develop)
```

> `develop` 브랜치 직접 push 금지. 반드시 `feature/*` → PR → merge 순서로 진행합니다.
