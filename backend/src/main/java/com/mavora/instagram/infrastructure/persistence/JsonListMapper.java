package com.mavora.instagram.infrastructure.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JsonListMapper {

    private static final TypeReference<List<String>> STRINGS = new TypeReference<>() {
    };
    private static final TypeReference<List<UUID>> UUIDS = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public JsonListMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String strings(List<String> values) {
        return write(values == null ? List.of() : values);
    }

    public List<String> readStrings(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, STRINGS);
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid JSON string list", exception);
        }
    }

    public String uuids(List<UUID> values) {
        return write(values == null ? List.of() : values);
    }

    public List<UUID> readUuids(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, UUIDS);
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid JSON uuid list", exception);
        }
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize JSON list", exception);
        }
    }
}
