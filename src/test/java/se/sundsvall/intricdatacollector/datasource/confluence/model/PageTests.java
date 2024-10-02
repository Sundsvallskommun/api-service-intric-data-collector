package se.sundsvall.intricdatacollector.datasource.confluence.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class PageTests {

    @Test
    void creationAndAccessors() {
        var municipalityId = "someMunitipalityId";
        var pageId = "somePageId";
        var title = "someTitle";
        var body = "someBody";
        var baseUrl = "someBaseUrl";
        var path = "somePath";
        var updatedAt = LocalDateTime.now();
        var ancestorIds = List.of("someAncestorId", "someOtherAncestorId");
        var intricGroupId = "someIntricGroupId";
        var intricBlobId = "someIntricBlobId";

        var page = new Page(municipalityId, pageId, title, body, baseUrl, path, updatedAt, ancestorIds, intricGroupId, intricBlobId);

        assertThat(page.municipalityId()).isEqualTo(municipalityId);
        assertThat(page.pageId()).isEqualTo(pageId);
        assertThat(page.title()).isEqualTo(title);
        assertThat(page.body()).isEqualTo(body);
        assertThat(page.baseUrl()).isEqualTo(baseUrl);
        assertThat(page.path()).isEqualTo(path);
        assertThat(page.updatedAt()).isEqualTo(updatedAt);
        assertThat(page.ancestorIds()).isEqualTo(ancestorIds);
        assertThat(page.intricGroupId()).isEqualTo(intricGroupId);
        assertThat(page.intricBlobId()).isEqualTo(intricBlobId);
    }

    @Test
    void bodyAsText() {
        var body = "<p>some text</p><ul><li>item 1</li><li>item 2</li></ul>";

        var page = new Page(null, null, null, body, null, null, null, null, null, null);

        assertThat(page.bodyAsText()).isEqualTo("some text item 1 item 2");
    }

    @Test
    void url() {
        var baseUrl = "someBaseUrl";
        var path = "somePath";

        var page = new Page(null, null, null, null, baseUrl, path, null, null, null, null);

        assertThat(page.url()).isEqualTo(baseUrl.concat(path));
    }
}
