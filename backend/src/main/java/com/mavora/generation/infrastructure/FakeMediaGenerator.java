package com.mavora.generation.infrastructure;

import com.mavora.generation.application.MediaGenerator;
import com.mavora.instagram.domain.MediaKind;
import java.util.Base64;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mavora.fal.provider", havingValue = "fake", matchIfMissing = true)
public class FakeMediaGenerator implements MediaGenerator {

    static final byte[] JPEG = Base64.getDecoder().decode(
            "/9j/4AAQSkZJRgABAQAAAQABAAD/2wAAAQIBAQEBAQIBAQECAgICAgQDAwIDBQQFBQUFBwcGBwcHBwcI"
                    + "CAgICAgKCgoKCgoLCwsLCw8PDw8PDw8PDw8P/8AAEQgAAQABAwERAAIRAQMRAf/EABQAAQAAAAAAAAAA"
                    + "AAAAAAAAAAj/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAGf/8QAFBABAAAAAAAAAAAA"
                    + "AAAAAAAAAP/aAAgBAQABPwB//9k="
    );

    @Override
    public GeneratedMedia generate(GenerateCommand command) {
        String prompt = command.prompt() == null || command.prompt().isBlank()
                ? "Instagram visual"
                : command.prompt();
        String slug = prompt.replaceAll("[^A-Za-z0-9]+", "-");
        if (slug.length() > 40) {
            slug = slug.substring(0, 40);
        }
        return new GeneratedMedia(
                MediaKind.IMAGE,
                "image/jpeg",
                "fal-" + slug + ".jpg",
                JPEG,
                prompt
        );
    }
}
