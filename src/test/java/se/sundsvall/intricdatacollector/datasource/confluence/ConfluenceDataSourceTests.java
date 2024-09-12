package se.sundsvall.intricdatacollector.datasource.confluence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.intricdatacollector.datasource.confluence.model.EventType.PAGE_CREATED;
import static se.sundsvall.intricdatacollector.datasource.confluence.model.EventType.PAGE_REMOVED;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;
import se.sundsvall.intricdatacollector.core.intric.IntricIntegration;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceClient;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceClientRegistry;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceIntegrationProperties;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.PageRepository;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model.PageEntity;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model.PageEntityBuilder;
import se.sundsvall.intricdatacollector.datasource.confluence.model.EventType;

@ExtendWith({ MockitoExtension.class, ResourceLoaderExtension.class })
class ConfluenceDataSourceTests {

    private static final String INTRIC_GROUP_ID = "someIntricGroupId";

    @Mock
    private ConfluenceIntegrationProperties propertiesMock;
    @Mock
    private ConfluenceIntegrationProperties.Environment environmentMock;
    @Mock
    private ConfluenceClientRegistry confluenceClientRegistryMock;
    @Mock
    private ConfluenceClient confluenceClientMock;
    @Mock
    private PageRepository pageRepositoryMock;
    @Mock
    private IntricIntegration intricIntegrationMock;

    private ConfluenceDataSource dataSource;

    /*
        Mocked Confluence page structure:

        Test                         (98362)
         |- Prohibited root page     (1212426)
         |   |- Some prohibited page (1212428)
         |- Some root page           (98381)
         |   |- A subpage            (1212419)
         |   |   |- A subsubpage     (1212424)
         |   |- Another subpage      (1212421)
     */

    @BeforeEach
    void setUp() {
        when(propertiesMock.environments()).thenReturn(Map.of("1984", environmentMock));

        // Add the "probibited root page" to the list of blacklisted root ids
        when(environmentMock.blacklistedRootIds()).thenReturn(List.of("1212426"));
        // Set up a single mapping
        when(environmentMock.mappings()).thenReturn(List.of(
            new ConfluenceIntegrationProperties.Environment.Mapping(INTRIC_GROUP_ID, "98362")));

        dataSource = new ConfluenceDataSource(new ObjectMapper(), propertiesMock, confluenceClientRegistryMock, pageRepositoryMock, intricIntegrationMock);
    }

    @ParameterizedTest
    @EnumSource(EventType.class)
    void processPage(final EventType eventType, @Load("/confluence/get-content-1212424.json") String aSubsubPageJson) {
        var municipalityId = "1984";
        var pageId = "1212424";
        var blobId = "someBlobId";

        if (eventType != PAGE_REMOVED) {
            when(confluenceClientRegistryMock.getClient(municipalityId)).thenReturn(confluenceClientMock);
            when(confluenceClientMock.getContent(pageId)).thenReturn(aSubsubPageJson);
        }

        switch (eventType) {
            case PAGE_CREATED, PAGE_RESTORED -> {
                dataSource.processPage(municipalityId, eventType, pageId);

                verify(confluenceClientRegistryMock).getClient(municipalityId);
                verify(confluenceClientMock).getContent(pageId);
                verify(intricIntegrationMock).addInfoBlob(eq(INTRIC_GROUP_ID), anyString(), anyString(), anyString());
                verify(pageRepositoryMock).save(any(PageEntity.class));
                verifyNoMoreInteractions(confluenceClientRegistryMock, confluenceClientMock, intricIntegrationMock, pageRepositoryMock);
            }
            case PAGE_UPDATED -> {
                when(pageRepositoryMock.findByIdAndMunicipalityId(pageId, municipalityId)).thenReturn(Optional.of(PageEntityBuilder.create()
                    .withBlobId(blobId).build()));
                when(intricIntegrationMock.updateInfoBlob(eq(INTRIC_GROUP_ID), eq(blobId), anyString(), anyString(), anyString()))
                    .thenReturn(blobId);

                dataSource.processPage(municipalityId, eventType, pageId);

                verify(confluenceClientRegistryMock).getClient(municipalityId);
                verify(confluenceClientMock).getContent(pageId);
                verify(intricIntegrationMock).updateInfoBlob(eq(INTRIC_GROUP_ID), eq(blobId), anyString(), anyString(), anyString());
                verify(pageRepositoryMock).findByIdAndMunicipalityId(pageId, municipalityId);
                verify(pageRepositoryMock).save(any(PageEntity.class));
                verifyNoMoreInteractions(confluenceClientRegistryMock, confluenceClientMock, intricIntegrationMock, pageRepositoryMock);
            }
            default -> {
                verifyNoInteractions(confluenceClientRegistryMock, confluenceClientMock, intricIntegrationMock, pageRepositoryMock, confluenceClientRegistryMock);
            }
        }
    }

    @Test
    void processPageForPageWithProhibitedParent(@Load("/confluence/get-content-1212428.json") String someProhibitedPageJson) {
        var municipalityId = "1984";
        var pageId = "1212428";

        when(confluenceClientRegistryMock.getClient(municipalityId)).thenReturn(confluenceClientMock);
        when(confluenceClientMock.getContent(pageId)).thenReturn(someProhibitedPageJson);

        dataSource.processPage(municipalityId, PAGE_CREATED, pageId);

        verify(confluenceClientRegistryMock).getClient(municipalityId);
        verify(confluenceClientMock).getContent(pageId);
        verifyNoMoreInteractions(confluenceClientRegistryMock, confluenceClientMock);
        verifyNoInteractions(intricIntegrationMock, pageRepositoryMock);
    }

    @Test
    void processPageForUnmappedPage(@Load("/confluence/get-content-unmapped-page.json") String unmappedPageJson) {
        var municipalityId = "1984";
        var pageId = "1212433";

        when(confluenceClientRegistryMock.getClient(municipalityId)).thenReturn(confluenceClientMock);
        when(confluenceClientMock.getContent(pageId)).thenReturn(unmappedPageJson);

        dataSource.processPage(municipalityId, PAGE_CREATED, pageId);

        verify(confluenceClientRegistryMock).getClient(municipalityId);
        verify(confluenceClientMock).getContent(pageId);
        verifyNoMoreInteractions(confluenceClientRegistryMock, confluenceClientMock);
        verifyNoInteractions(intricIntegrationMock, pageRepositoryMock);
    }

    @Test
    void deletePage() {
        var municipalityId = "1984";
        var pageId = "somePageId";
        var blobId = "someBlobId";

        when(pageRepositoryMock.findByIdAndMunicipalityId(pageId, municipalityId))
            .thenReturn(Optional.of(PageEntityBuilder.create().withBlobId(blobId).build()));

        dataSource.deletePage(municipalityId, pageId);

        verify(pageRepositoryMock).findByIdAndMunicipalityId(pageId, municipalityId);
        verify(pageRepositoryMock).deleteByIdAndMunicipalityId(pageId, municipalityId);
        verify(intricIntegrationMock).deleteInfoBlob(blobId);
        verifyNoMoreInteractions(pageRepositoryMock, intricIntegrationMock);
        verifyNoInteractions(confluenceClientRegistryMock, confluenceClientMock);
    }

    @Test
    void processTree(
            @Load("/confluence/get-children-98362.json") String get_children_98362_json,
            @Load("/confluence/get-content-98362.json") String get_content_98362_json,

            @Load("/confluence/get-children-1212426.json") String get_children_1212426_json,
            @Load("/confluence/get-content-1212426.json") String get_content_1212426_json,
            @Load("/confluence/get-children-1212428.json") String get_children_1212428_json,
            @Load("/confluence/get-content-1212428.json") String get_content_1212428_json,

            @Load("/confluence/get-children-98381.json") String get_children_98381_json,
            @Load("/confluence/get-content-98381.json") String get_content_98381_json,
            @Load("/confluence/get-children-1212419.json") String get_children_1212419_json,
            @Load("/confluence/get-content-1212419.json") String get_content_1212419_json,
            @Load("/confluence/get-children-1212424.json") String get_children_1212424_json,
            @Load("/confluence/get-content-1212424.json") String get_content_1212424_json,
            @Load("/confluence/get-children-1212421.json") String get_children_1212421_json,
            @Load("/confluence/get-content-1212421.json") String get_content_1212421_json) {
        var municipalityId = "1984";
        var rootId = "98362";

        var prohibitedRootPageId = "1212426";
        var someProhibitedPageId = "1212428";

        var someRootPageId = "98381";
        var aSubPageId = "1212419";
        var aSubSubPageId = "1212424";
        var anotherSubPageId = "1212421";

        when(confluenceClientRegistryMock.getClient(municipalityId)).thenReturn(confluenceClientMock);
        when(confluenceClientMock.getChildren(rootId)).thenReturn(get_children_98362_json);
        when(confluenceClientMock.getChildren(prohibitedRootPageId)).thenReturn(get_children_1212426_json);
        when(confluenceClientMock.getChildren(someProhibitedPageId)).thenReturn(get_children_1212428_json);
        when(confluenceClientMock.getChildren(someRootPageId)).thenReturn(get_children_98381_json);
        when(confluenceClientMock.getChildren(aSubPageId)).thenReturn(get_children_1212419_json);
        when(confluenceClientMock.getChildren(aSubSubPageId)).thenReturn(get_children_1212424_json);
        when(confluenceClientMock.getChildren(anotherSubPageId)).thenReturn(get_children_1212421_json);

        when(confluenceClientMock.getContent(rootId)).thenReturn(get_content_98362_json);
        when(confluenceClientMock.getContent(prohibitedRootPageId)).thenReturn(get_content_1212426_json);
        when(confluenceClientMock.getContent(someProhibitedPageId)).thenReturn(get_content_1212428_json);
        when(confluenceClientMock.getContent(someRootPageId)).thenReturn(get_content_98381_json);
        when(confluenceClientMock.getContent(aSubPageId)).thenReturn(get_content_1212419_json);
        when(confluenceClientMock.getContent(aSubSubPageId)).thenReturn(get_content_1212424_json);
        when(confluenceClientMock.getContent(anotherSubPageId)).thenReturn(get_content_1212421_json);

        dataSource.processTree(municipalityId, rootId);

        verify(confluenceClientRegistryMock, times(14)).getClient(municipalityId);
        verify(confluenceClientMock, times(7)).getChildren(anyString());
        verify(confluenceClientMock, times(7)).getContent(anyString());
        verify(intricIntegrationMock, times(5)).addInfoBlob(anyString(), anyString(), anyString(), anyString());
        verify(pageRepositoryMock, times(5)).save(any(PageEntity.class));
    }

    @Test
    void getIntricGroupId() {
        assertThat(dataSource.getIntricGroupId("1984", "somePageId", List.of("1212419", "98381", "98362")))
            .isEqualTo(INTRIC_GROUP_ID);
        assertThat(dataSource.getIntricGroupId("1984", "98362", List.of()))
            .isEqualTo(INTRIC_GROUP_ID);
        assertThat(dataSource.getIntricGroupId("1984", "somePageId", List.of("unknownChildId", "unknownParentId", "unknownRootId")))
            .isNull();
    }
}
