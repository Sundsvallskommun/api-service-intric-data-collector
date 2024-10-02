package se.sundsvall.intricdatacollector.core.intric.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class InfoBlobsRequestTests {

    @Test
    void creationAndAccessors() {
        var request = new InfoBlobsRequest(List.of(
            new InfoBlob("someText", new Metadata("someTitle", "someUrl")),
            new InfoBlob("someOtherText", new Metadata("someOtherTitle", "someOtherUrl"))
        ));

        assertThat(request.infoBlobs()).hasSize(2);
        assertThat(request.infoBlobs()).extracting(InfoBlob::text).containsExactlyInAnyOrder("someText", "someOtherText");
        assertThat(request.infoBlobs()).extracting(InfoBlob::metadata).extracting(Metadata::title).containsExactlyInAnyOrder("someTitle", "someOtherTitle");
        assertThat(request.infoBlobs()).extracting(InfoBlob::metadata).extracting(Metadata::url).containsExactlyInAnyOrder("someUrl", "someOtherUrl");
    }
}
