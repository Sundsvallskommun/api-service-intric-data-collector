package se.sundsvall.intricdatacollector.core.intric.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InfoBlobTests {

	@Test
	void creationAndAccessors() {
		var text = "someText";
		var title = "someTitle";
		var url = "someUrl";

		var infoBlob = new InfoBlob(text, new Metadata(title, url));

		assertThat(infoBlob.text()).isEqualTo(text);
		assertThat(infoBlob.metadata()).satisfies(metadata -> {
			assertThat(metadata.title()).isEqualTo(title);
			assertThat(metadata.url()).isEqualTo(url);
		});
	}
}
