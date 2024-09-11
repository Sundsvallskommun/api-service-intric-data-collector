package se.sundsvall.intricdatacollector.core.intric.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class InfoBlobsResponseTests {

    @Test
    void testCreationAndAccessors() {
        var embeddingModelId = "someEmbeddingModelId";
        var title = "someTitle";
        var url = "someUrl";
        var size = 50;
        var id = "someId";
        var text = "someText";
        var createdAt = "someCreatedAt";
        var updatedAt = "someUpdatedAt";
        var groupId = "someGroupId";
        var websiteId = "someWebsiteId";
        var count = 1;

        var itemMetadata = new InfoBlobsResponse.Item.Metadata(embeddingModelId, title, url, size);
        var response = new InfoBlobsResponse(List.of(
            new InfoBlobsResponse.Item(id, text, itemMetadata, createdAt, updatedAt, groupId, websiteId)),
            count);

        assertThat(response.count()).isEqualTo(count);
        assertThat(response.items()).hasSize(1).first().satisfies(item -> {
            assertThat(item.id()).isEqualTo(id);
            assertThat(item.text()).isEqualTo(text);
            assertThat(item.metadata()).satisfies(metadata -> {
                assertThat(metadata.embeddingModelId()).isEqualTo(embeddingModelId);
                assertThat(metadata.title()).isEqualTo(title);
                assertThat(metadata.url()).isEqualTo(url);
                assertThat(metadata.size()).isEqualTo(size);
            });
            assertThat(item.createdAt()).isEqualTo(createdAt);
            assertThat(item.updatedAt()).isEqualTo(updatedAt);
            assertThat(item.groupId()).isEqualTo(groupId);
            assertThat(item.websiteId()).isEqualTo(websiteId);
        });
    }
}
