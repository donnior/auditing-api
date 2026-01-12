package com.xingcanai.csqe.llm;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.annotation.Nullable;

/**
 * 平台定义的对基于大模型的交互的请求参数规范。
 */
public class ChatSpecs {

    private ChatSpecs() {}

    public static enum Role {
        USER("user"),
        ASSISTANT("assistant"),
        TOOL("tool"),
        SYSTEM("system");

        private final String value;

        Role(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static Role fromValue(String value) {
            for (Role role : values()) {
                if (role.value.equals(value)) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Unknown role: " + value);
        }
    }

    public static record Request(List<RoleMessage> messages, boolean stream) {}

    public static record RoleMessage(Role role, List<ContentItem> content) {}

    public static enum ContentType {
        TEXT("text"),
        IMAGE_URL("image_url"),
        IMAGE_FILE("image_file"),
        FILE("file"),
        VOICE_URL("voice_url");

        private final String value;

        ContentType(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static ContentType fromValue(String value) {
            for (ContentType contentType : values()) {
                if (contentType.value.equals(value)) {
                    return contentType;
                }
            }
            throw new IllegalArgumentException("Unknown content type: " + value);
        }
    }

    public static record ContentItem(ContentType type, @Nullable String text, @Nullable String imageUrl, @Nullable String fileId) {}

}
