package se.sundsvall.aidatacollector.integration.eneo.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class InfoBlobsResponseTests {

	@Test
	void creationAndAccessors() {
		final var embeddingModelId = "someEmbeddingModelId";
		final var title = "someTitle";
		final var url = "someUrl";
		final var size = 50;
		final var id = "someId";
		final var text = "someText";
		final var createdAt = "someCreatedAt";
		final var updatedAt = "someUpdatedAt";
		final var groupId = "someGroupId";
		final var websiteId = "someWebsiteId";
		final var count = 1;

		final var itemMetadata = new InfoBlobsResponse.Item.Metadata(embeddingModelId, title, url, size);
		final var response = new InfoBlobsResponse(List.of(
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
