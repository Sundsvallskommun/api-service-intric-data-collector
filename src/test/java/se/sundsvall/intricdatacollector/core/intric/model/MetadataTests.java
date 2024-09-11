package se.sundsvall.intricdatacollector.core.intric.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MetadataTests {

    @Test
    void testCreationAndAccessors() {
        var title = "someTitle";
        var url = "someUrl";

        var metadata = new Metadata(title, url);

        assertThat(metadata.title()).isEqualTo(title);
        assertThat(metadata.url()).isEqualTo(url);
    }
}
