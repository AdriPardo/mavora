package com.mavora.instagram.application;

import com.mavora.shared.domain.OrganizationId;
import java.util.UUID;

public interface MediaStorage {

    StoredFile store(OrganizationId organizationId, UUID assetId, String filename, String contentType, byte[] bytes);

    byte[] read(String storagePath);

    void delete(String storagePath);

    record StoredFile(String storagePath, long byteSize) {
    }
}
