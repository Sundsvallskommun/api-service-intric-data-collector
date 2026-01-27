package se.sundsvall.aidatacollector.integration.eneo.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InfoBlobTests {

	@Test
	void creationAndAccessors() {
		final var text = "someText";
		final var title = "someTitle";
		final var url = "someUrl";

		final var infoBlob = new InfoBlob(text, new Metadata(title, url));

		assertThat(infoBlob.text()).isEqualTo(text);
		assertThat(infoBlob.metadata()).satisfies(metadata -> {
			assertThat(metadata.title()).isEqualTo(title);
			assertThat(metadata.url()).isEqualTo(url);
		});
	}
}
