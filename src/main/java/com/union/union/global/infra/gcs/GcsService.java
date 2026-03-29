package com.union.union.global.infra.gcs;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.union.union.global.infra.gcs.dto.GcsSignedUrlResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GcsService {

    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    public GcsSignedUrlResponseDto getMiniAppSignedUrl(UUID publisherId, String filename) {
        String blobName = String.format("mini-apps/%s/%s", publisherId, filename);
        
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, blobName).build();
        
        // Signed URL 생성 (PUT 방식, 5분 유효)
        URL signedUrl = storage.signUrl(
                blobInfo,
                5,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withV4Signature()
        );

        String publicUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, blobName);

        return new GcsSignedUrlResponseDto(signedUrl.toString(), publicUrl);
    }
}
