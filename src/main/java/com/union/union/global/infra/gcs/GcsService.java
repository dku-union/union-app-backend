package com.union.union.global.infra.gcs;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.union.union.global.infra.gcs.dto.GcsSignedUrlResponseDto;
import com.union.union.global.infra.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GcsService {

    private final Storage storage;
    private final CdnSignedUrlProperties cdnProperties;
    private final RedisService redisService;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    /**
     * 배너 이미지 전용 버킷 — 미니앱 번들 버킷과 분리.
     * 라이프사이클 정책(배너=long-cache, 미니앱=immutable) 및 권한 정책 분리 목적.
     */
    @Value("${banner.bucket.name:union-app-banners}")
    private String bannerBucketName;

    private static final String CDN_URL_CACHE_PREFIX = "cdn:signed-url:";
    private static final long CDN_URL_EXPIRATION_SECONDS = 3600;      // CDN URL 유효: 1시간
    private static final long REDIS_CACHE_SECONDS = 3000;             // Redis 캐시: 50분 (10분 버퍼)

    /**
     * 업로드용 GCS Signed URL (PUT, 5분)
     */
    public GcsSignedUrlResponseDto getMiniAppSignedUrl(UUID publisherId, String filename) {
        String blobName = String.format("mini-apps/%s/%s", publisherId, filename);

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, blobName).build();

        URL signedUrl = storage.signUrl(
                blobInfo,
                5,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withV4Signature()
        );

        String downloadUrl = getCdnDownloadUrl(blobName);

        return new GcsSignedUrlResponseDto(signedUrl.toString(), downloadUrl);
    }

    /**
     * 서버에서 직접 GCS에 파일 업로드 (dev seed 전용)
     * @return GCS object path (buildFileUrl로 저장)
     */
    public String uploadFile(MultipartFile file, String objectPath) throws IOException {
        BlobId blobId = BlobId.of(bucketName, objectPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .build();
        storage.create(blobInfo, file.getBytes());
        return objectPath;
    }

    /**
     * 배너 이미지를 배너 버킷에 업로드하고 공개 HTTPS URL 반환.
     * 버킷은 사전에 public read(allUsers:objectViewer) 권한이 설정되어 있어야 함.
     *
     * @return https://storage.googleapis.com/{bannerBucket}/{objectPath}
     */
    public String uploadBannerImage(MultipartFile file, String objectPath) throws IOException {
        BlobId blobId = BlobId.of(bannerBucketName, objectPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType() != null ? file.getContentType() : "image/png")
                .setCacheControl("public, max-age=86400")
                .build();
        storage.create(blobInfo, file.getBytes());
        return getBannerPublicUrl(objectPath);
    }

    /**
     * 배너 이미지 공개 URL. 배너 버킷이 public read 면 그대로 fetch 가능.
     */
    public String getBannerPublicUrl(String objectPath) {
        return "https://storage.googleapis.com/" + bannerBucketName + "/" + objectPath;
    }

    /**
     * CDN Signed URL 조회 (Redis 캐시 → 미스 시 생성)
     */
    public String getCdnDownloadUrl(String objectPath) {
        String cacheKey = CDN_URL_CACHE_PREFIX + objectPath;

        return redisService.get(cacheKey)
                .orElseGet(() -> {
                    String signedUrl = generateCdnSignedUrl(objectPath, CDN_URL_EXPIRATION_SECONDS);
                    redisService.setValuesWithTimeout(cacheKey, signedUrl, Duration.ofSeconds(REDIS_CACHE_SECONDS));
                    return signedUrl;
                });
    }

    /**
     * CDN Signed URL 생성 (HMAC-SHA1)
     */
    private String generateCdnSignedUrl(String objectPath, long expirationSeconds) {
        // 한글 등 non-ASCII 문자가 path에 포함되면 클라이언트가 percent-encode 후 요청한다.
        // CDN은 요청 시점의 (encoded) path 기준으로 서명을 검증하므로,
        // 동일하게 encoded 형태로 서명해야 mismatch 가 발생하지 않는다. (`/` 는 그대로 보존)
        String encodedPath = UriUtils.encodePath(objectPath, StandardCharsets.UTF_8);
        String urlPrefix = cdnProperties.baseUrl() + "/" + encodedPath;
        long expiration = Instant.now().getEpochSecond() + expirationSeconds;

        String urlToSign = urlPrefix + "?Expires=" + expiration + "&KeyName=" + cdnProperties.keyName();

        try {
            byte[] keyBytes = Base64.getUrlDecoder().decode(cdnProperties.keyValue());
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA1"));
            byte[] signature = mac.doFinal(urlToSign.getBytes());
            String encodedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(signature);

            return urlToSign + "&Signature=" + encodedSignature;
        } catch (Exception e) {
            throw new RuntimeException("CDN Signed URL 생성 실패", e);
        }
    }
}
