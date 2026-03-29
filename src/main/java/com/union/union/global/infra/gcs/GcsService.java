package com.union.union.global.infra.gcs;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.union.union.global.infra.gcs.dto.GcsSignedUrlResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GcsService {

    private final Storage storage;
    private final CdnSignedUrlProperties cdnProperties;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

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

        // 업로드 후 CDN Signed URL로 접근할 수 있도록 다운로드 URL도 함께 반환
        String downloadUrl = generateCdnSignedUrl(blobName, 3600);

        return new GcsSignedUrlResponseDto(signedUrl.toString(), downloadUrl);
    }

    /**
     * 다운로드용 CDN Signed URL (GET)
     * @param objectPath GCS 오브젝트 경로 (예: mini-apps/{publisherId}/{filename})
     * @param expirationSeconds URL 유효 시간 (초)
     */
    public String generateCdnSignedUrl(String objectPath, long expirationSeconds) {
        String urlPrefix = cdnProperties.baseUrl() + "/" + objectPath;
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

    /**
     * 오브젝트 경로로 CDN Signed URL 생성 (1시간 유효)
     */
    public String getCdnDownloadUrl(String objectPath) {
        return generateCdnSignedUrl(objectPath, 3600);
    }
}
