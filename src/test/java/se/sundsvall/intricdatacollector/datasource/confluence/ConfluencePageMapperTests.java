package se.sundsvall.intricdatacollector.datasource.confluence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConfluencePageMapperTests {

    @Mock
    private JsonUtil jsonUtilMock;
    @Mock
    private JsonUtil.Document documentMock;

    @InjectMocks
    private ConfluencePageMapper pageMapper;

    @Test
    void newPage() {
        var pageId = "somePageId";
        var municipalityId = "someMunicipalityId";

        var page = pageMapper.newPage(municipalityId, pageId);

        assertThat(page).hasAllNullFieldsOrPropertiesExcept("municipalityId", "pageId");
        assertThat(page.municipalityId()).isEqualTo(municipalityId);
        assertThat(page.pageId()).isEqualTo(pageId);
    }

    @Test
    void toPage() {
        var json = "someJson";
        var pageId = "somePageId";
        var municipalityId = "someMunicipalityId";
        var title = "someTitle";
        var body = "someBody";
        var baseUrl = "someBaseUrl";
        var path = "somePath";
        var updatedAt = OffsetDateTime.now().minusDays(4);
        var ancestorIds = List.of("someAncestorId", "someOtherAncestorId");

        when(jsonUtilMock.parse(json)).thenReturn(documentMock);
        when(documentMock.getTitle()).thenReturn(title);
        when(documentMock.getBody()).thenReturn(body);
        when(documentMock.getBaseUrl()).thenReturn(baseUrl);
        when(documentMock.getPath()).thenReturn(path);
        when(documentMock.getUpdatedAt()).thenReturn(updatedAt.toString());
        when(documentMock.getAncestorIds()).thenReturn(ancestorIds);

        assertThat(pageMapper.toPage(municipalityId, pageId, json)).satisfies(page -> {
            assertThat(page.pageId()).isEqualTo(pageId);
            assertThat(page.municipalityId()).isEqualTo(municipalityId);
            assertThat(page.title()).isEqualTo(title);
            assertThat(page.body()).isEqualTo(body);
            assertThat(page.baseUrl()).isEqualTo(baseUrl);
            assertThat(page.path()).isEqualTo(path);
            assertThat(page.updatedAt()).isEqualTo(updatedAt.toLocalDateTime());
            assertThat(page.ancestorIds()).isEqualTo(ancestorIds);
        });

        verify(jsonUtilMock).parse(json);
        verify(documentMock).getTitle();
        verify(documentMock).getBody();
        verify(documentMock).getBaseUrl();
        verify(documentMock).getPath();
        verify(documentMock).getUpdatedAt();
        verify(documentMock).getAncestorIds();
    }

    @Test
    void toPageWhenUpdatedAtIsMissing() {
        var json = "someJson";
        var pageId = "somePageId";
        var municipalityId = "someMunicipalityId";

        when(jsonUtilMock.parse(json)).thenReturn(documentMock);
        when(documentMock.getUpdatedAt()).thenReturn(null);

        var page = pageMapper.toPage(municipalityId, pageId, json);

        assertThat(page.updatedAt()).isNull();
    }
}