package com.union.union.domain.auth.controller;

import com.union.union.domain.appversion.entity.AppVersion;
import com.union.union.domain.appversion.repository.AppVersionRepository;
import com.union.union.domain.banner.entity.Banner;
import com.union.union.domain.banner.entity.BannerLinkType;
import com.union.union.domain.banner.repository.BannerRepository;
import com.union.union.domain.banner.service.BannerService;
import com.union.union.domain.dev.dto.DevSeedResponseDto;
import com.union.union.domain.dev.service.DevSeedService;
import com.union.union.global.infra.gcs.GcsService;
import com.union.union.domain.miniapp.entity.MiniApp;
import com.union.union.domain.miniapp.entity.MiniAppCategory;
import com.union.union.domain.miniapp.entity.MiniAppStatus;
import com.union.union.domain.miniapp.repository.MiniAppCategoryRepository;
import com.union.union.domain.miniapp.repository.MiniAppRepository;
import com.union.union.domain.publisher.entity.Publisher;
import com.union.union.domain.publisher.entity.PublisherStatus;
import com.union.union.domain.publisher.repository.PublisherRepository;
import com.union.union.domain.workspace.entity.Workspace;
import com.union.union.domain.workspace.entity.WorkspaceMember;
import com.union.union.domain.workspace.repository.WorkspaceMemberRepository;
import com.union.union.domain.workspace.repository.WorkspaceRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * 개발 환경 전용 — 테스트 데이터(퍼블리셔, 워크스페이스, 카테고리, 미니앱 5개, 버전) 시딩
 *
 * POST /api/dev/seed
 */
@Slf4j
@Profile("local")
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevSeedController {

    private final PublisherRepository publisherRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final MiniAppCategoryRepository miniAppCategoryRepository;
    private final MiniAppRepository miniAppRepository;
    private final AppVersionRepository appVersionRepository;
    private final EntityManager entityManager;
    private final DevSeedService devSeedService;
    private final BannerRepository bannerRepository;
    private final BannerService bannerService;
    private final GcsService gcsService;

    private static final UUID TEST_PUBLISHER_ID = UUID.fromString("66d1bf78-29b5-45d8-bba7-f08f88bffa23");
    private static final UUID TEST_WORKSPACE_ID = UUID.fromString("77d1bf78-29b5-45d8-bba7-f08f88bffa23");

    @PostMapping("/seed")
    @Transactional
    public ResponseEntity<Map<String, Object>> seed() {

        // 0. app_versions 테이블에 잘못된 publisher_id 컬럼이 있으면 제거
        dropLegacyColumn();

        // 1. Publisher
        Publisher publisher = publisherRepository.findByPublisherId(TEST_PUBLISHER_ID)
                .orElseGet(() -> publisherRepository.save(Publisher.builder()
                        .publisherId(TEST_PUBLISHER_ID)
                        .name("Union Dev")
                        .email("dev@union.app")
                        .password("$2a$10$dummyhashfordevonly000000000000000000000000000")
                        .description("Union 개발팀 테스트 퍼블리셔")
                        .pubstatus(PublisherStatus.ACTIVE)
                        .role("ROLE_PUBLISHER")
                        .build()));
        log.info("[Seed] Publisher ready: id={}, publisherId={}", publisher.getId(), publisher.getPublisherId());

        // 2. Workspace
        Workspace workspace = workspaceRepository.findById(TEST_WORKSPACE_ID)
                .orElseGet(() -> workspaceRepository.save(Workspace.builder()
                        .workspaceId(TEST_WORKSPACE_ID)
                        .name("Union Dev Studio")
                        .description("Union 개발팀 테스트 워크스페이스")
                        .contactEmail("dev@union.app")
                        .color("#3B5BFF")
                        .owner(publisher)
                        .build()));
        log.info("[Seed] Workspace ready: id={}", workspace.getWorkspaceId());

        // 3. WorkspaceMember
        if (!workspaceMemberRepository.existsByWorkspace_WorkspaceIdAndPublisher_PublisherId(
                workspace.getWorkspaceId(), publisher.getPublisherId())) {
            workspaceMemberRepository.save(WorkspaceMember.builder()
                    .workspace(workspace)
                    .publisher(publisher)
                    .role("OWNER")
                    .build());
            log.info("[Seed] WorkspaceMember created");
        }

        // 4. Categories (idempotent — findByName 으로 중복 방지)
        Map<String, MiniAppCategory> cats = ensureCategories();

        // 5. MiniApps + AppVersions (5개)
        List<Map<String, Object>> createdApps = new ArrayList<>();
        createdApps.add(seedApp(workspace, cats.get("FESTIVAL"),
                "축제 웨이팅", "주점·부스 대기열 실시간 관리. 순서가 오면 푸시 알림!",
                "축제,웨이팅,대기열", "1.0.0"));
        createdApps.add(seedApp(workspace, cats.get("MEAL"),
                "오늘의 학식", "단국대 학생식당 메뉴를 한눈에. 영양 정보와 리뷰까지",
                "학식,메뉴,식당", "1.0.0"));
        createdApps.add(seedApp(workspace, cats.get("STUDY"),
                "스터디 모집", "같은 과목 스터디 그룹 찾기. 시간표 기반 매칭",
                "스터디,모집,시간표", "1.0.0"));
        createdApps.add(seedApp(workspace, cats.get("MARKET"),
                "캠퍼스 중고거래", "교내 중고 물품 거래. 같은 캠퍼스라 직거래가 편해요",
                "중고거래,직거래,마켓", "1.0.0"));
        createdApps.add(seedApp(workspace, cats.get("SOCIAL"),
                "단국 소개팅", "단국대 재학생 인증 기반 소개팅 매칭",
                "소개팅,매칭,만남", "1.0.0"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("publisher", Map.of("id", publisher.getId(), "publisherId", publisher.getPublisherId()));
        result.put("workspace", Map.of("id", workspace.getWorkspaceId(), "name", workspace.getName()));
        result.put("categories", cats.keySet());
        result.put("miniApps", createdApps);

        log.info("[Seed] 완료 — 미니앱 {}개 준비됨", createdApps.size());
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/dev/seed/app
     *
     * 실제 파일을 업로드하여 가상의 Publisher → Workspace → MiniApp → AppVersion(DEPLOYED)을 한 번에 생성합니다.
     * Content-Type: multipart/form-data
     */
    @PostMapping(value = "/seed/app", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DevSeedResponseDto> seedApp(
            @RequestParam("appName") String appName,
            @RequestParam("appId") String appId,
            @RequestParam("description") String description,
            @RequestParam("categoryName") String categoryName,
            @RequestParam(value = "tags", defaultValue = "") String tags,
            @RequestParam(value = "versionNumber", defaultValue = "1.0.0") String versionNumber,
            @RequestParam(value = "releaseNotes", defaultValue = "Initial release") String releaseNotes,
            @RequestParam(value = "iconUrl", required = false) String iconUrl,
            @RequestParam(value = "publisherName", required = false) String publisherName,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        DevSeedResponseDto response = devSeedService.seed(
                appName, appId, description, categoryName, tags,
                versionNumber, releaseNotes, iconUrl, publisherName, file
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 배너 시드 — 이미지 파일과 메타데이터를 받아 GCS 업로드 + DB INSERT.
     *
     * Content-Type: multipart/form-data
     * required:  file
     * optional:  title, subtitle, linkType (default NONE), linkTarget,
     *            sortOrder (default 0), targetUniversity
     *
     * 반환: { id, imageUrl }
     */
    @PostMapping(value = "/seed/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<Map<String, Object>> seedBanner(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String subtitle,
            @RequestParam(required = false, defaultValue = "NONE") BannerLinkType linkType,
            @RequestParam(required = false) String linkTarget,
            @RequestParam(required = false, defaultValue = "0") int sortOrder,
            @RequestParam(required = false) String targetUniversity
    ) throws IOException {
        String safeName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "banner.png";
        String objectPath = "banners/" + UUID.randomUUID() + "-" + safeName.replaceAll("[^A-Za-z0-9._-]", "_");
        String imageUrl = gcsService.uploadBannerImage(file, objectPath);

        Banner banner = Banner.builder()
                .imageUrl(imageUrl)
                .title(title)
                .subtitle(subtitle)
                .linkType(linkType)
                .linkTarget(linkTarget)
                .sortOrder(sortOrder)
                .isActive(true)
                .targetUniversity(targetUniversity)
                .build();
        bannerRepository.save(banner);
        bannerService.invalidateCache();

        log.info("Banner seed 완료. id={}, imageUrl={}", banner.getId(), imageUrl);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", banner.getId());
        body.put("imageUrl", imageUrl);
        body.put("linkType", banner.getLinkType());
        body.put("sortOrder", banner.getSortOrder());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /**
     * 개발용 — 등록된 퍼블리셔 목록을 ID/이메일/상태와 함께 반환.
     * iOS 퍼블리셔 로그인 검증 시 어떤 이메일로 발송 가능한지 빠르게 확인하기 위함.
     */
    @GetMapping("/publishers")
    public ResponseEntity<List<Map<String, Object>>> listPublishers() {
        List<Map<String, Object>> result = publisherRepository.findAll().stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("publisherId", p.getPublisherId());
                    m.put("name", p.getName());
                    m.put("email", p.getEmail());
                    m.put("status", p.getPubstatus());
                    m.put("role", p.getRole());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    /**
     * 개발용 — 퍼블리셔 이메일/상태를 임의로 갱신.
     * iOS 로그인 검증 시 자기 이메일로 OTP를 받기 위해 사용한다.
     */
    @PatchMapping("/publishers/{publisherId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updatePublisher(
            @PathVariable UUID publisherId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String status
    ) {
        Publisher publisher = publisherRepository.findByPublisherId(publisherId)
                .orElseThrow(() -> new IllegalArgumentException("publisher not found: " + publisherId));

        if (email != null && !email.isBlank()) {
            entityManager.createNativeQuery(
                    "UPDATE publishers SET email = :email WHERE publisher_id = :pid"
            )
            .setParameter("email", email.trim().toLowerCase())
            .setParameter("pid", publisherId)
            .executeUpdate();
        }
        if (status != null && !status.isBlank()) {
            entityManager.createNativeQuery(
                    "UPDATE publishers SET pubstatus = :status WHERE publisher_id = :pid"
            )
            .setParameter("status", status.toUpperCase())
            .setParameter("pid", publisherId)
            .executeUpdate();
        }
        entityManager.flush();
        entityManager.clear();

        Publisher refreshed = publisherRepository.findByPublisherId(publisherId).orElseThrow();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("publisherId", refreshed.getPublisherId());
        result.put("name", refreshed.getName());
        result.put("email", refreshed.getEmail());
        result.put("status", refreshed.getPubstatus());
        return ResponseEntity.ok(result);
    }

    // ── helpers ──────────────────────────────────────────────

    private void dropLegacyColumn() {
        try {
            // publisher_id 컬럼 존재 여부 확인 후 제거 (publisher는 miniApp → workspace → owner 로 연결)
            entityManager.createNativeQuery(
                    "ALTER TABLE app_versions DROP COLUMN IF EXISTS publisher_id"
            ).executeUpdate();
            entityManager.flush();
            log.info("[Seed] Dropped legacy publisher_id column from app_versions");
        } catch (Exception e) {
            log.warn("[Seed] Could not drop publisher_id column: {}", e.getMessage());
        }
    }

    private Map<String, MiniAppCategory> ensureCategories() {
        record CatDef(String name, String displayName) {}
        List<CatDef> defs = List.of(
                new CatDef("FESTIVAL", "축제"),
                new CatDef("MEAL", "학식"),
                new CatDef("STUDY", "스터디"),
                new CatDef("MARKET", "거래"),
                new CatDef("SOCIAL", "소통"),
                new CatDef("ETC", "기타")
        );

        Map<String, MiniAppCategory> result = new LinkedHashMap<>();
        for (CatDef def : defs) {
            MiniAppCategory cat = miniAppCategoryRepository.findByName(def.name())
                    .orElseGet(() -> miniAppCategoryRepository.save(MiniAppCategory.builder()
                            .name(def.name())
                            .displayName(def.displayName())
                            .iconUrl(null)
                            .isActive(true)
                            .build()));
            result.put(def.name(), cat);
        }
        log.info("[Seed] Categories ready: {}", result.keySet());
        return result;
    }

    private Map<String, Object> seedApp(Workspace workspace, MiniAppCategory category,
                                         String name, String description,
                                         String tags, String version) {
        // 이미 동일 이름의 DEPLOYED 앱이 있으면 스킵
        Optional<MiniApp> existing = miniAppRepository.findByStatus(MiniAppStatus.DEPLOYED)
                .stream()
                .filter(a -> a.getName().equals(name) &&
                        a.getWorkspace().getWorkspaceId().equals(workspace.getWorkspaceId()))
                .findFirst();

        if (existing.isPresent()) {
            MiniApp app = existing.get();
            log.info("[Seed] MiniApp already exists: id={}, name={}", app.getId(), app.getName());
            return Map.of("id", app.getId(), "name", app.getName(), "status", "ALREADY_EXISTS");
        }

        // MiniApp 생성 + 승인
        MiniApp app = miniAppRepository.save(MiniApp.builder()
                .name(name)
                .description(description)
                .iconUrl(null)
                .category(category)
                .tags(tags)
                .workspace(workspace)
                .status(MiniAppStatus.PENDING)
                .build());
        app.deploy();
        miniAppRepository.save(app);

        // AppVersion: DRAFT → UPLOADED → tested → IN_REVIEW → ACCEPTED → DEPLOYED
        String buildFileUrl = String.format("mini-apps/%s/%s-%s.unionapp",
                TEST_PUBLISHER_ID, name.replaceAll("\\s+", "-"), version);

        AppVersion appVersion = AppVersion.builder()
                .miniApp(app)
                .versionNumber(version)
                .releaseNotes("초기 릴리즈")
                .build();
        appVersion.confirmUpload(buildFileUrl, 1024L * 100);  // 100KB
        appVersion.markTested();
        appVersion.submitForReview();
        appVersion.accept();
        appVersion.deploy();
        appVersionRepository.save(appVersion);

        log.info("[Seed] MiniApp created: id={}, name={}, versionId={}", app.getId(), app.getName(), appVersion.getId());
        return Map.of("id", app.getId(), "name", app.getName(), "version", version, "status", "CREATED");
    }
}
