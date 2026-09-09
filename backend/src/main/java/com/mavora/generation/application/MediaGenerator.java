package com.mavora.generation.application;

import com.mavora.instagram.domain.MediaKind;

public interface MediaGenerator {

    GeneratedMedia generate(GenerateCommand command);

    record GenerateCommand(
            MediaKind kind,
            String prompt,
            String aspectRatio,
            String brandContext
    ) {
    }

    record GeneratedMedia(
            MediaKind kind,
            String contentType,
            String filename,
            byte[] bytes,
            String prompt
    ) {
    }
}
