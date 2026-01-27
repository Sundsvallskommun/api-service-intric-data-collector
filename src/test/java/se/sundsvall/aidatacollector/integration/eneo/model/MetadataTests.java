package se.sundsvall.aidatacollector.integration.eneo.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MetadataTests {

	@Test
	void creationAndAccessors() {
		final var title = "someTitle";
		final var url = "someUrl";

		final var metadata = new Metadata(title, url);

		assertThat(metadata.title()).isEqualTo(title);
		assertThat(metadata.url()).isEqualTo(url);
	}
}
