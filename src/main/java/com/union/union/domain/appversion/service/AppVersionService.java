package com.union.union.domain.appversion.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.union.union.domain.appversion.dto.AppVersionResponseDto;
import com.union.union.domain.appversion.dto.CreateVersionRequestDto;
import com.union.union.domain.appversion.dto.CreateVersionResponseDto;
import com.union.union.domain.appversion.entity.AppVersion;
import com.union.union.domain.appversion.repository.AppVersionRepository;
import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.miniapp.repository.MiniAppRepository;
import com.union.union.domain.user.entity.User;
import com.union.union.domain.user.repository.UserRepository;
import com.union.union.global.infra.gcs.GcsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppVersionService {

    private final AppVersionRepository appVersionRepository;
    private final MiniAppRepository miniAppRepository;
    private final UserRepository userRepository;
    private final GcsService gcsService;
    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    @Transactional
    public CreateVersionResponseDto createVersion(CreateVersionRequestDto request, UUID publisherId) {
        User publisher = userRepository.findById(publisherId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        MiniApp miniApp = miniAppRepository.findById(request.miniAppId())
                .orElseThrow(() -> new IllegalArgumentException("MiniApp을 찾을 수 없습니다"));

        if (!miniApp.getPublisher().getId().equals(publisherId)) {
            throw new IllegalArgumentException("본인의 MiniApp에만 버전을 추가할 수 있습니다");
        }

        AppVersion version = AppVersion.builder()
                .miniApp(miniApp)
                .publisher(publisher)
                .versionNumber(request.versionNumber())
                .releaseNotes(request.releaseNotes())
                .build();

        appVersionRepository.save(version);

        String filename = String.format("mini-apps/%s/versions/%s/bundle.zip",
                miniApp.getId(), version.getId());
        var signedUrlResponse = gcsService.getMiniAppSignedUrl(publisherId, filename);

        log.info("AppVersion 생성 (DRAFT). versionId={}, miniAppId={}", version.getId(), miniApp.getId());

        return new CreateVersionResponseDto(version.getId(), signedUrlResponse.signedUrl());
    }

    @Transactional
    public AppVersionResponseDto confirmUpload(UUID versionId, UUID publisherId) {
        AppVersion version = getVersionWithOwnerCheck(versionId, publisherId);

        String objectPath = String.format("mini-apps/%s/versions/%s/bundle.zip",
                version.getMiniApp().getId(), version.getId());

        Blob blob = storage.get(bucketName, objectPath);
        if (blob == null || !blob.exists()) {
            throw new IllegalStateException("GCS에 파일이 존재하지 않습니다. 업로드를 먼저 완료해주세요");
        }

        version.confirmUpload(objectPath, blob.getSize());

        log.info("AppVersion 업로드 확정 (UPLOADED). versionId={}, size={}bytes", versionId, blob.getSize());

        return AppVersionResponseDto.from(version);
    }

    @Transactional
    public AppVersionResponseDto markTested(UUID versionId, UUID publisherId) {
        AppVersion version = getVersionWithOwnerCheck(versionId, publisherId);
        version.markTested();

        log.info("AppVersion 테스트 완료 기록. versionId={}", versionId);

        return AppVersionResponseDto.from(version);
    }

    public String getBundleDownloadUrl(UUID versionId) {
        AppVersion version = appVersionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("AppVersion을 찾을 수 없습니다"));

        if (version.getBuildFileUrl() == null) {
            throw new IllegalStateException("아직 업로드되지 않은 버전입니다");
        }

        return gcsService.getCdnDownloadUrl(version.getBuildFileUrl());
    }

    public List<AppVersionResponseDto> getVersionsByMiniApp(Long miniAppId) {
        return appVersionRepository.findByMiniAppIdOrderByCreatedAtDesc(miniAppId)
                .stream()
                .map(AppVersionResponseDto::from)
                .collect(Collectors.toList());
    }

    public AppVersionResponseDto getVersion(UUID versionId) {
        AppVersion version = appVersionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("AppVersion을 찾을 수 없습니다"));
        return AppVersionResponseDto.from(version);
    }

    private AppVersion getVersionWithOwnerCheck(UUID versionId, UUID publisherId) {
        AppVersion version = appVersionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("AppVersion을 찾을 수 없습니다"));

        if (!version.getPublisher().getId().equals(publisherId)) {
            throw new IllegalArgumentException("본인의 버전만 수정할 수 있습니다");
        }

        return version;
    }
}
