package com.mavora.instagram.infrastructure.storage;

import com.mavora.instagram.application.MediaStorage;
import com.mavora.instagram.application.MediaUrlSigner;
import com.mavora.instagram.application.TokenProtector;
import com.mavora.shared.domain.OrganizationId;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalMediaStorage implements MediaStorage, MediaUrlSigner {

    private final Path root;
    private final TokenProtector tokenProtector;
    private final Clock clock;
    private final String publicApiUrl;

    public LocalMediaStorage(
            @Value("${mavora.media.directory:data/media}") String directory,
            @Value("${mavora.public.api-url:http://localhost:8080}") String publicApiUrl,
            TokenProtector tokenProtector,
            Clock clock
    ) {
        this.root = Path.of(directory).toAbsolutePath().normalize();
        this.publicApiUrl = trimSlash(publicApiUrl);
        this.tokenProtector = tokenProtector;
        this.clock = clock;
        try {
            Files.createDirectories(this.root);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create media directory", exception);
        }
    }

    @Override
    public StoredFile store(OrganizationId organizationId, UUID assetId, String filename, String contentType, byte[] bytes) {
        Path path = root.resolve(organizationId.value().toString()).resolve(assetId + "-" + filename).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("Invalid media path");
        }
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
            return new StoredFile(root.relativize(path).toString(), bytes.length);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store media", exception);
        }
    }

    @Override
    public byte[] read(String storagePath) {
        Path path = resolve(storagePath);
        try {
            return Files.readAllBytes(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read media", exception);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Files.deleteIfExists(resolve(storagePath));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not delete media", exception);
        }
    }

    @Override
    public SignedUrl sign(UUID assetId) {
        long exp = clock.instant().plus(Duration.ofDays(7)).getEpochSecond();
        String signature = tokenProtector.sign(assetId + ":" + exp);
        String url = publicApiUrl + "/api/v1/media/" + assetId + "/file?exp=" + exp + "&sig=" + signature;
        return new SignedUrl(url, exp, signature);
    }

    @Override
    public boolean verify(UUID assetId, long expiresEpochSeconds, String signature) {
        if (clock.instant().getEpochSecond() > expiresEpochSeconds) {
            return false;
        }
        return tokenProtector.verify(assetId + ":" + expiresEpochSeconds, signature);
    }

    private Path resolve(String storagePath) {
        Path path = root.resolve(storagePath).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("Invalid media path");
        }
        return path;
    }

    private static String trimSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
