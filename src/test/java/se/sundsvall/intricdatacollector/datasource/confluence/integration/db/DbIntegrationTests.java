package se.sundsvall.intricdatacollector.datasource.confluence.integration.db;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model.PageEntity;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model.PageEntityBuilder;
import se.sundsvall.intricdatacollector.datasource.confluence.model.PageBuilder;

@ExtendWith(MockitoExtension.class)
class DbIntegrationTests {

    @Mock
    private PageRepository pageRepositoryMock;

    @InjectMocks
    private DbIntegration dbIntegration;

    @Test
    void getBlobId() {
        var pageId = "somePageId";
        var municipalityId = "someMunicipalityId";
        var blobId = "someBlobId";

        when(pageRepositoryMock.findBlobIdByPageIdAndMunicipalityId(pageId, municipalityId)).thenReturn(of(blobId));

        assertThat(dbIntegration.getBlobId(pageId, municipalityId)).hasValue(blobId);

        verify(pageRepositoryMock).findBlobIdByPageIdAndMunicipalityId(pageId, municipalityId);
        verifyNoMoreInteractions(pageRepositoryMock);
    }

    @Test
    void getPage() {
        var pageId = "somePageId";
        var municipalityId = "someMunicipalityId";
        var intricGroupId = "someGroupId";
        var intricBlobId = "someBlobId";
        var updatedAt = LocalDateTime.now();

        var pageEntity = PageEntityBuilder.create()
            .withPageId(pageId)
            .withMunicipalityId(municipalityId)
            .withIntricGroupId(intricGroupId)
            .withIntricBlobId(intricBlobId)
            .withUpdatedAt(updatedAt)
            .build();

        when(pageRepositoryMock.findPageEntityByPageIdAndMunicipalityId(pageId, municipalityId)).thenReturn(of(pageEntity));

        var page = dbIntegration.getPage(pageId, municipalityId);

        assertThat(page).isNotEmpty().hasValueSatisfying(actualPage -> {
            assertThat(actualPage.pageId()).isEqualTo(pageId);
            assertThat(actualPage.municipalityId()).isEqualTo(municipalityId);
            assertThat(actualPage.intricGroupId()).isEqualTo(intricGroupId);
            assertThat(actualPage.intricBlobId()).isEqualTo(intricBlobId);
            assertThat(actualPage.updatedAt()).isEqualTo(updatedAt);
        });

        verify(pageRepositoryMock).findPageEntityByPageIdAndMunicipalityId(pageId, municipalityId);
        verifyNoMoreInteractions(pageRepositoryMock);
    }

    @Test
    void savePage() {
        var pageId = "somePageId";
        var municipalityId = "someMunicipalityId";
        var intricGroupId = "someGroupId";
        var intricBlobId = "someBlobId";
        var updatedAt = LocalDateTime.now();

        var pageEntityCaptor = ArgumentCaptor.forClass(PageEntity.class);

        var page = PageBuilder.create()
            .withPageId(pageId)
            .withMunicipalityId(municipalityId)
            .withIntricGroupId(intricGroupId)
            .withIntricBlobId(intricBlobId)
            .withUpdatedAt(updatedAt)
            .build();

        dbIntegration.savePage(page);

        verify(pageRepositoryMock).save(pageEntityCaptor.capture());
        verifyNoMoreInteractions(pageRepositoryMock);

        assertThat(pageEntityCaptor.getValue()).satisfies(pageEntity -> {
            assertThat(pageEntity.getPageId()).isEqualTo(pageId);
            assertThat(pageEntity.getMunicipalityId()).isEqualTo(municipalityId);
            assertThat(pageEntity.getIntricGroupId()).isEqualTo(intricGroupId);
            assertThat(pageEntity.getIntricBlobId()).isEqualTo(intricBlobId);
            assertThat(pageEntity.getUpdatedAt()).isEqualTo(updatedAt);
        });
    }

    @Test
    void deletePage() {
        var pageId = "somePageId";
        var municipalityId = "someMunicipalityId";

        doNothing().when(pageRepositoryMock).deletePageEntityByPageIdAndMunicipalityId(pageId, municipalityId);

        dbIntegration.deletePage(pageId, municipalityId);

        verify(pageRepositoryMock).deletePageEntityByPageIdAndMunicipalityId(pageId, municipalityId);
        verifyNoMoreInteractions(pageRepositoryMock);
    }
}
