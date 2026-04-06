package com.union.union.domain.dev.service;

import com.union.union.domain.appversion.entity.AppVersion;
import com.union.union.domain.appversion.repository.AppVersionRepository;
import com.union.union.domain.dev.dto.DevSeedResponseDto;
import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.miniapp.entity.MiniAppCategory;
import com.union.union.domain.miniapp.entity.MiniAppStatus;
import com.union.union.domain.miniapp.repository.MiniAppCategoryRepository;
import com.union.union.domain.miniapp.repository.MiniAppRepository;
import com.union.union.domain.publisher.entity.Publisher;
import com.union.union.domain.publisher.entity.PublisherStatus;
import com.union.union.domain.publisher.repository.PublisherRepository;
import com.union.union.domain.workspace.entity.Workspace;
import com.union.union.domain.workspace.repository.WorkspaceRepository;
import com.union.union.global.infra.gcs.GcsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Profile("local")
@Transactional
@RequiredArgsConstructor
public class DevSeedService {

    private final PublisherRepository publisherRepository;
    private final WorkspaceRepository workspaceRepository;
    private final MiniAppCategoryRepository categoryRepository;
    private final MiniAppRepository miniAppRepository;
    private final AppVersionRepository appVersionRepository;
    private final GcsService gcsService;
    private final PasswordEncoder passwordEncoder;

    public DevSeedResponseDto seed(
            String appName,
            String appId,
            String description,
            String categoryName,
            String tags,
            String versionNumber,
            String releaseNotes,
            String iconUrl,
            String publisherName,
            MultipartFile file
    ) throws IOException {

        // 1. Publisher 생성 (ACTIVE, ROLE_PUBLISHER)
        UUID publisherUuid = UUID.randomUUID();
        String resolvedPublisherName = (publisherName != null && !publisherName.isBlank())
                ? publisherName
                : "Dev Publisher - " + appName;

        Publisher publisher = Publisher.builder()
                .publisherId(publisherUuid)
                .name(resolvedPublisherName)
                .email("dev+" + publisherUuid + "@union-dev.com")
                .password(passwordEncoder.encode("dev-password"))
                .description("Dev seed publisher for " + appName)
                .pubstatus(PublisherStatus.ACTIVE)
                .role("ROLE_PUBLISHER")
                .build();
        publisher = publisherRepository.save(publisher);

        // 2. Workspace 생성
        Workspace workspace = Workspace.builder()
                .workspaceId(UUID.randomUUID())
                .name(appName + " Workspace")
                .description("Dev seed workspace for " + appName)
                .contactEmail(publisher.getEmail())
                .color("#3B82F6")
                .owner(publisher)
                .build();
        workspace = workspaceRepository.save(workspace);

        // 3. Category 찾거나 생성
        MiniAppCategory category = categoryRepository.findByName(categoryName)
                .orElseGet(() -> categoryRepository.save(
                        MiniAppCategory.builder()
                                .name(categoryName)
                                .displayName(categoryName)
                                .iconUrl(null)
                                .isActive(true)
                                .build()
                ));

        // 4. MiniApp 생성 (APPROVED)
        MiniApp miniApp = MiniApp.builder()
                .name(appName)
                .description(description)
                .iconUrl(iconUrl)
                .category(category)
                .tags(tags)
                .workspace(workspace)
                .status(MiniAppStatus.APPROVED)
                .build();
        miniApp.updateAppId(appId);
        miniApp = miniAppRepository.save(miniApp);

        // 5. GCS에 파일 직접 업로드
        String originalFilename = (file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank())
                ? file.getOriginalFilename()
                : appName.toLowerCase().replaceAll("\\s+", "-") + ".unionapp";

        String objectPath = String.format("mini-apps/%s/%d/seed/%s",
                publisherUuid, miniApp.getId(), originalFilename);

        String buildFileUrl = gcsService.uploadFile(file, objectPath);
        long bundleSize = file.getSize();

        // 6. AppVersion 생성 → 상태 머신 전체 통과 → DEPLOYED
        AppVersion version = AppVersion.builder()
                .miniApp(miniApp)
                .versionNumber((versionNumber != null && !versionNumber.isBlank()) ? versionNumber : "1.0.0")
                .releaseNotes((releaseNotes != null && !releaseNotes.isBlank()) ? releaseNotes : "Initial dev seed release")
                .build();

        version.confirmUpload(buildFileUrl, bundleSize);   // DRAFT → UPLOADED
        version.markTested();                               // testedAt 설정
        version.submitForReview();                          // UPLOADED → IN_REVIEW
        version.accept();                                   // IN_REVIEW → ACCEPTED
        version.deploy();                                   // ACCEPTED → DEPLOYED
        version = appVersionRepository.save(version);

        // 7. CDN 다운로드 URL 조회
        String downloadUrl = gcsService.getCdnDownloadUrl(buildFileUrl);

        return new DevSeedResponseDto(
                publisher.getId(),
                publisher.getPublisherId(),
                workspace.getWorkspaceId(),
                miniApp.getId(),
                miniApp.getAppId(),
                miniApp.getName(),
                version.getId(),
                version.getVersionNumber(),
                version.getStatus().name(),
                version.getBuildFileUrl(),
                downloadUrl,
                bundleSize
        );
    }
}
