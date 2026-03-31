# 미니앱(.unionapp) 업로드 가이드 — Next.js 대시보드용

## 아키텍처 개요

```
[브라우저 CSR]                    [Next.js API Route]              [외부 서비스]
     │                                  │                              │
     │  1. 업로드 URL 요청                │                              │
     ├─────────────────────────────────►│                              │
     │                                  │  2. GCS Signed URL 생성       │
     │                                  ├─────────────────────────────►│ GCS
     │                                  │◄─────────────────────────────┤
     │  3. signedUrl 반환                │                              │
     │◄─────────────────────────────────┤                              │
     │                                  │                              │
     │  4. 브라우저에서 GCS 직접 업로드     │                              │
     ├────────────────────────────────────────────────────────────────►│ GCS
     │                                  │                              │
     │  5. 메타데이터 등록 요청            │                              │
     ├─────────────────────────────────►│                              │
     │                                  │  6. Spring API 호출            │
     │                                  ├─────────────────────────────►│ Spring
     │                                  │◄─────────────────────────────┤
     │  7. 완료                          │                              │
     │◄─────────────────────────────────┤                              │
```

## 핵심 원칙

| 항목 | 처리 위치 | 이유 |
|------|----------|------|
| Signed URL 생성 | **Next.js API Route (SSR)** | 서비스 계정 키가 서버에만 존재해야 함 |
| .unionapp 파일 업로드 | **브라우저 (CSR)** | 파일이 서버를 경유하면 메모리/대역폭 낭비. 브라우저→GCS 직접 전송 |
| 메타데이터 등록 | **Next.js API Route (SSR)** | Spring API 인증 토큰을 클라이언트에 노출하지 않기 위해 |

> ⚠️ **파일 업로드는 반드시 브라우저(CSR)에서 직접 GCS로 전송해야 합니다.**
> Next.js API Route(서버)를 경유하면 200MB 파일이 서버 메모리를 그대로 소모합니다.

---

## Step 1. Signed URL 발급 (Next.js API Route)

### `POST /api/miniapps/upload-url`

서버에서만 실행. `@google-cloud/storage` SDK 사용.

**요청:**
```json
{
  "filename": "com.union.sample-app-1.0.0.unionapp",
  "publisherId": "66d1bf78-29b5-45d8-bba7-f08f88bffa23"
}
```

**응답:**
```json
{
  "signedUrl": "https://storage.googleapis.com/union-app-miniapps/mini-apps/66d1bf78-.../com.union...?X-Goog-Signature=...",
  "objectPath": "mini-apps/66d1bf78-29b5-45d8-bba7-f08f88bffa23/com.union.sample-app-1.0.0.unionapp"
}
```

### 구현 시 주의사항

- `@google-cloud/storage` npm 패키지 사용
- 서비스 계정 키(JSON)는 환경변수로 주입 — **클라이언트에 절대 노출 금지**
- Signed URL 설정:
  - HTTP 메서드: `PUT`
  - 만료 시간: **5분**
  - Content-Type: `application/octet-stream`
  - 서명 버전: V4

### GCS 경로 규칙

```
gs://union-app-miniapps/mini-apps/{publisherId}/{filename}
```

---

## Step 2. 브라우저에서 GCS 직접 업로드 (CSR)

> ⚠️ **이 단계는 반드시 클라이언트(브라우저)에서 실행해야 합니다.**
> 서버를 경유하면 파일 크기만큼 서버 메모리를 소모합니다.

### fetch 사용 (간단)

```javascript
const response = await fetch(signedUrl, {
  method: 'PUT',
  headers: { 'Content-Type': 'application/octet-stream' },
  body: file,  // input[type=file]에서 받은 File 객체
});

if (!response.ok) {
  throw new Error(`업로드 실패: ${response.status}`);
}
```

### XMLHttpRequest 사용 (업로드 진행률 필요 시)

```javascript
const xhr = new XMLHttpRequest();
xhr.open('PUT', signedUrl);
xhr.setRequestHeader('Content-Type', 'application/octet-stream');

xhr.upload.onprogress = (e) => {
  const percent = Math.round((e.loaded / e.total) * 100);
  setProgress(percent);  // React state 업데이트
};

xhr.onload = () => {
  if (xhr.status >= 200 && xhr.status < 300) {
    // 업로드 성공 → Step 3으로
  }
};

xhr.send(file);
```

### 주의사항

- `Content-Type`은 반드시 `application/octet-stream` (Signed URL 생성 시 지정한 것과 **정확히 일치**해야 함)
- 최대 파일 크기: **200MB**
- Signed URL은 **5분간 유효** — 발급 후 바로 업로드 시작할 것

---

## Step 3. 메타데이터 등록 (Next.js API Route → Spring)

업로드 완료 후 Spring 백엔드에 미니앱 정보를 등록합니다.

### Spring API 스펙

```
POST {SPRING_API_URL}/mini-apps
Authorization: Bearer {퍼블리셔 JWT 토큰}
Content-Type: application/json
```

**요청:**
```json
{
  "name": "샘플 앱",
  "description": "앱 설명",
  "iconUrl": "https://example.com/icon.png",
  "launchUrl": "mini-apps/66d1bf78-.../com.union.sample-app-1.0.0.unionapp",
  "universityId": 17
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| name | string | ✅ | 앱 이름 (100자 이내) |
| description | string | - | 앱 설명 |
| iconUrl | string | - | 아이콘 URL (500자 이내) |
| launchUrl | string | ✅ | **Step 1에서 받은 `objectPath` 값 그대로** (500자 이내) |
| universityId | number | - | 대학 전용 앱이면 해당 대학 ID |

> 💡 `launchUrl`에는 CDN URL이 아닌 **GCS 오브젝트 경로**만 저장합니다.
> iOS 앱이 다운로드 요청할 때 Spring이 CDN Signed URL을 자동 생성합니다.

**응답:**
```json
{
  "id": 1,
  "name": "샘플 앱",
  "status": "PENDING",
  "publisherNickname": "이용찬",
  "createdAt": "2026-03-29T12:00:00"
}
```

등록 후 상태는 `PENDING` → 관리자가 승인 처리:
```
PATCH {SPRING_API_URL}/mini-apps/{id}/approve
Authorization: Bearer {ADMIN 토큰}
```

---

## 환경변수 (Next.js)

```env
# GCS (서버 전용 — 클라이언트에 노출 금지)
GCS_BUCKET_NAME=union-app-miniapps
GCS_KEY_JSON={"type":"service_account","project_id":"axial-device-460408-i9",...}

# Spring API
SPRING_API_URL=http://localhost:8080
```

> ⚠️ `GCS_KEY_JSON`은 `NEXT_PUBLIC_` 접두사를 **절대 붙이지 마세요.** 붙이면 클라이언트 번들에 포함됩니다.

---

## 대학교 ID 참조표

| id | 대학교 | id | 대학교 |
|----|--------|-----|--------|
| 1 | 서울대학교 | 11 | 건국대학교 |
| 2 | 연세대학교 | 12 | 동국대학교 |
| 3 | 고려대학교 | 13 | 홍익대학교 |
| 4 | 서강대학교 | 14 | 국민대학교 |
| 5 | 성균관대학교 | 15 | 숭실대학교 |
| 6 | 한양대학교 | 16 | 세종대학교 |
| 7 | 중앙대학교 | 17 | 단국대학교 |
| 8 | 경희대학교 | 18 | 인천대학교 |
| 9 | 한국외국어대학교 | 19 | 가천대학교 |
| 10 | 서울시립대학교 | 20 | 경기대학교 |

---

## 전체 흐름 요약

```
퍼블리셔가 대시보드에서 .unionapp 선택
    │
    ▼
[CSR] 파일 유효성 체크 (확장자, 200MB 이하)
    │
    ▼
[SSR] POST /api/miniapps/upload-url → GCS Signed URL 발급
    │
    ▼
[CSR] PUT {signedUrl} → 브라우저에서 GCS 직접 업로드 (진행률 표시)
    │
    ▼
[SSR] POST /api/miniapps/register → Spring POST /mini-apps (메타데이터 등록)
    │
    ▼
등록 완료 (status: PENDING) → 관리자 승인 대기
```
