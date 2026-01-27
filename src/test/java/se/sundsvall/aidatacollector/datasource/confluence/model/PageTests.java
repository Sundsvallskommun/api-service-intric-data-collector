package se.sundsvall.aidatacollector.datasource.confluence.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class PageTests {

	@Test
	void creationAndAccessors() {
		final var municipalityId = "someMunitipalityId";
		final var pageId = "somePageId";
		final var title = "someTitle";
		final var body = "someBody";
		final var baseUrl = "someBaseUrl";
		final var path = "somePath";
		final var updatedAt = LocalDateTime.now();
		final var ancestorIds = List.of("someAncestorId", "someOtherAncestorId");
		final var eneoGroupId = "someEneoGroupId";
		final var eneoBlobId = "someEneoBlobId";

		final var page = new Page(municipalityId, pageId, title, body, baseUrl, path, updatedAt, ancestorIds, eneoGroupId, eneoBlobId);

		assertThat(page.municipalityId()).isEqualTo(municipalityId);
		assertThat(page.pageId()).isEqualTo(pageId);
		assertThat(page.title()).isEqualTo(title);
		assertThat(page.body()).isEqualTo(body);
		assertThat(page.baseUrl()).isEqualTo(baseUrl);
		assertThat(page.path()).isEqualTo(path);
		assertThat(page.updatedAt()).isEqualTo(updatedAt);
		assertThat(page.ancestorIds()).isEqualTo(ancestorIds);
		assertThat(page.eneoGroupId()).isEqualTo(eneoGroupId);
		assertThat(page.eneoBlobId()).isEqualTo(eneoBlobId);
	}

	@Test
	void bodyAsText() {
		final var body = "<p>some text</p><ul><li>item 1</li><li>item 2</li></ul>";

		final var page = new Page(null, null, null, body, null, null, null, null, null, null);

		assertThat(page.bodyAsText()).isEqualTo("some text item 1 item 2");
	}

	@Test
	void url() {
		final var baseUrl = "someBaseUrl";
		final var path = "somePath";

		final var page = new Page(null, null, null, null, baseUrl, path, null, null, null, null);

		assertThat(page.url()).isEqualTo(baseUrl.concat(path));
	}
}
