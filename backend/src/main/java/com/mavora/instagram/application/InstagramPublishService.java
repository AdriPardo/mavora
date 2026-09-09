package com.mavora.instagram.application;

import com.mavora.instagram.domain.InstagramAccount;
import com.mavora.instagram.domain.InstagramAccountRepository;
import com.mavora.instagram.domain.InstagramFormat;
import com.mavora.instagram.domain.InstagramProvider;
import com.mavora.instagram.domain.InstagramSlot;
import com.mavora.instagram.domain.InstagramSlotRepository;
import com.mavora.instagram.domain.MediaAsset;
import com.mavora.instagram.domain.MediaAssetRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstagramPublishService {

    private static final Logger log = LoggerFactory.getLogger(InstagramPublishService.class);

    private final InstagramSlotRepository slotRepository;
    private final InstagramAccountRepository accountRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final MediaUrlSigner mediaUrlSigner;
    private final List<InstagramPublisher> publishers;
    private final InstagramAccountService accountService;
    private final MetaConnectionService metaConnectionService;
    private final Clock clock;

    public InstagramPublishService(
            InstagramSlotRepository slotRepository,
            InstagramAccountRepository accountRepository,
            MediaAssetRepository mediaAssetRepository,
            MediaUrlSigner mediaUrlSigner,
            List<InstagramPublisher> publishers,
            InstagramAccountService accountService,
            MetaConnectionService metaConnectionService,
            Clock clock
    ) {
        this.slotRepository = slotRepository;
        this.accountRepository = accountRepository;
        this.mediaAssetRepository = mediaAssetRepository;
        this.mediaUrlSigner = mediaUrlSigner;
        this.publishers = publishers;
        this.accountService = accountService;
        this.metaConnectionService = metaConnectionService;
        this.clock = clock;
    }

    @Transactional
    public int processDue() {
        int published = 0;
        for (int i = 0; i < 8; i++) {
            if (!processNext()) {
                return published;
            }
            published++;
        }
        return published;
    }

    @Transactional
    public boolean processNext() {
        Instant now = clock.instant();
        var claimed = slotRepository.lockNextDue(now);
        if (claimed.isEmpty()) {
            return false;
        }
        InstagramSlot slot = claimed.get();
        InstagramAccount account = accountRepository.findByOrganization(slot.organizationId()).orElse(null);
        if (account == null || !account.canAutoPublish()) {
            // Sin cuenta o sin autonomía: el calendario se queda para subir a mano.
            return false;
        }
        try {
            slot.claimForPublish();
            slotRepository.save(slot);
            InstagramPublisher.PublishResult result = publisherFor(account.provider()).publish(toCommand(slot, account));
            slot.markPublished(result.igMediaId(), clock.instant());
            slotRepository.save(slot);
            return true;
        } catch (RuntimeException exception) {
            log.warn("Instagram slot {} failed: {}", slot.id(), exception.getMessage());
            slot.markFailed(exception.getMessage());
            slotRepository.save(slot);
            return true;
        }
    }

    private InstagramPublisher.PublishCommand toCommand(InstagramSlot slot, InstagramAccount account) {
        List<String> images = new ArrayList<>();
        String video = null;
        for (var assetId : slot.mediaAssetIds()) {
            MediaAsset asset = mediaAssetRepository.findById(assetId, slot.organizationId())
                    .orElseThrow(() -> new IllegalStateException("Media asset missing for slot"));
            String url = mediaUrlSigner.sign(asset.id()).url();
            if (asset.isVideo()) {
                video = url;
            } else {
                images.add(url);
            }
        }
        InstagramFormat format = slot.format();
        if (format == InstagramFormat.CAROUSEL && images.size() < 2) {
            format = InstagramFormat.FEED;
        }
        String caption = slot.hook() + "\n\n" + slot.caption() + "\n\n" + slot.cta();
        if (!slot.hashtags().isEmpty()) {
            caption = caption + "\n\n" + String.join(" ", slot.hashtags());
        }
        if (caption.length() > 2200) {
            caption = caption.substring(0, 2200);
        }
        String graphVersion = metaConnectionService.resolve(account.organizationId())
                .map(MetaAppCredentials::graphVersion)
                .orElse("v21.0");
        return new InstagramPublisher.PublishCommand(
                account.igUserId(),
                accountService.decryptToken(account),
                format,
                caption,
                images,
                video,
                graphVersion
        );
    }

    private InstagramPublisher publisherFor(InstagramProvider provider) {
        return publishers.stream()
                .filter(candidate -> candidate.supports(provider))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Instagram publisher for " + provider));
    }
}
