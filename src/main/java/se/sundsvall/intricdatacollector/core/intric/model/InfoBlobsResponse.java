package se.sundsvall.intricdatacollector.core.intric.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InfoBlobsResponse(List<Item> items, int count) {

    public record Item(

            String id,

            String text,

            Metadata metadata,

            @JsonProperty("created_at")
            String createdAt,

            @JsonProperty("updated_at")
            String updatedAt,

            @JsonProperty("group_id")
            String groupId,

            @JsonProperty("website_id")
            String websiteId) {

        public record Metadata(

            @JsonProperty("embedding_model_id")
            String embeddingModelId,

            String title,

            String url,

            int size) { }
    }
}
