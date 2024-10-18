package se.sundsvall.intricdatacollector.datasource.confluence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;
import se.sundsvall.intricdatacollector.core.intric.IntricIntegration;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceClient;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceClientRegistry;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceIntegrationProperties;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.DbIntegration;
import se.sundsvall.intricdatacollector.datasource.confluence.model.Page;
import se.sundsvall.intricdatacollector.datasource.confluence.model.PageBuilder;

@ExtendWith({ MockitoExtension.class, ResourceLoaderExtension.class })
class ConfluenceWorkerTests {

    private static final String MUNICIPALITY_ID = "someMunicipalityId";

    private static final String ROOT_ID = "98362";
    private static final String BLACKLISTED_ROOT_ID = "1212426";
    private static final String INTRIC_GROUP_ID = "someIntricGroupId";

    @Mock
    private ConfluenceIntegrationProperties propertiesMock;
    @Mock
    private ConfluenceIntegrationProperties.Environment environmentMock;
    @Mock
    private ConfluenceClientRegistry confluenceClientRegistryMock;
    @Mock
    private ConfluencePageMapper pageMapperMock;
    @Mock
    private JsonUtil jsonUtilMock;
    @Mock
    private JsonUtil.Document documentMock;
    @Mock
    private DbIntegration dbIntegrationMock;
    @Mock
    private ConfluenceClient confluenceClientMock;
    @Mock
    private IntricIntegration intricIntegrationMock;

    private ConfluenceWorker worker;

    @BeforeEach
    void setUp() {
        when(propertiesMock.environments()).thenReturn(Map.of(MUNICIPALITY_ID, environmentMock));

        // Add the "probibited root page" to the list of blacklisted root ids
        when(environmentMock.blacklistedRootIds()).thenReturn(List.of("1212426"));
        // Set up a single mapping
        when(environmentMock.mappings()).thenReturn(List.of(
            new ConfluenceIntegrationProperties.Environment.Mapping(INTRIC_GROUP_ID, ROOT_ID)));

        when(confluenceClientRegistryMock.getClient(MUNICIPALITY_ID)).thenReturn(confluenceClientMock);

        worker = new ConfluenceWorker(MUNICIPALITY_ID, propertiesMock, confluenceClientRegistryMock, pageMapperMock, intricIntegrationMock, dbIntegrationMock, jsonUtilMock);
    }

    @Test
    void run() {
        var workerSpy = spy(worker);

        doNothing().when(workerSpy).processTree(ROOT_ID);

        workerSpy.run();

        verify(workerSpy).processTree(ROOT_ID);
    }

    @Test
    void processTree() {
        var pageId = "somePageId";
        var otherPageId = "someOtherPageId";

        var workerSpy = spy(worker);

        when(workerSpy.isBlacklisted(anyString())).thenReturn(false, true);
        doNothing().when(workerSpy).processPage(pageId);
        doNothing().when(workerSpy).processChildren(pageId);

        workerSpy.processTree(pageId);
        workerSpy.processTree(otherPageId);

        verify(workerSpy).isBlacklisted(pageId);
        verify(workerSpy).isBlacklisted(otherPageId);
        verify(workerSpy).processPage(pageId);
        verify(workerSpy).processChildren(pageId);
        verify(workerSpy, never()).processPage(otherPageId);
        verify(workerSpy, never()).processChildren(otherPageId);
    }

    @Test
    void processChildren() {
        var pageId = "somePageId";
        var childIds = List.of("someChildId", "someOtherChildId");
        var pageJson = "{\"someKey\": \"someValue\"}";

        var workerSpy = spy(worker);

        when(confluenceClientMock.getChildren(pageId)).thenReturn(Optional.of(pageJson));
        when(jsonUtilMock.parse(pageJson)).thenReturn(documentMock);
        when(documentMock.getChildIds()).thenReturn(childIds);

        workerSpy.processChildren(pageId);

        verify(workerSpy).processTree("someChildId");
        verify(workerSpy).processTree("someOtherChildId");
    }

    @Test
    void processChildrenWhenThereAreNoChildren() {
        var pageId = "somePageId";
        var pageJson = "{\"someKey\": \"someValue\"}";

        var workerSpy = spy(worker);

        when(confluenceClientMock.getChildren(pageId)).thenReturn(Optional.of(pageJson));
        when(jsonUtilMock.parse(pageJson)).thenReturn(documentMock);
        when(documentMock.getChildIds()).thenReturn(List.of());

        workerSpy.processChildren(pageId);

        verify(workerSpy, never()).processTree(anyString());
    }

    @Test
    void processPage() {
        var pageId = "somePageId";
        var pageJson = "{\"someKey\": \"someValue\"}";
        var updatedAt = "2024-10-17T15:55:43.819+02:00";

        var page = PageBuilder.create()
            .withUpdatedAt(LocalDateTime.now().minusMonths(1))
            .build();

        var workerSpy = spy(worker);

        when(confluenceClientMock.getContentVersion(pageId)).thenReturn(Optional.of(pageJson));
        when(jsonUtilMock.parse(pageJson)).thenReturn(documentMock);
        when(documentMock.getUpdatedAt()).thenReturn(updatedAt);
        when(dbIntegrationMock.getPage(pageId, MUNICIPALITY_ID)).thenReturn(Optional.of(page));

        workerSpy.processPage(pageId);

        verify(workerSpy).updatePage(pageId);
        verify(confluenceClientMock).getContentVersion(pageId);
        verify(dbIntegrationMock).getPage(pageId, MUNICIPALITY_ID);
    }

    @Test
    void processPageWhenPageIsMissingLocally() {
        var pageId = "somePageId";
        var pageJson = "{\"someKey\": \"someValue\"}";
        var updatedAt = "2024-10-17T15:55:43.819+02:00";

        var workerSpy = spy(worker);

        when(confluenceClientMock.getContentVersion(pageId)).thenReturn(Optional.of(pageJson));
        when(jsonUtilMock.parse(pageJson)).thenReturn(documentMock);
        when(documentMock.getUpdatedAt()).thenReturn(updatedAt);
        when(dbIntegrationMock.getPage(pageId, MUNICIPALITY_ID)).thenReturn(Optional.empty());
        when(pageMapperMock.newPage(MUNICIPALITY_ID, pageId)).thenCallRealMethod();

        workerSpy.processPage(pageId);

        verify(workerSpy).insertPage(pageId);
        verify(confluenceClientMock).getContentVersion(pageId);
        verify(dbIntegrationMock).getPage(pageId, MUNICIPALITY_ID);
    }

    @Test
    void getPageFromConfluence() {
        var pageId = "somePageId";
        var intricGroupId = "someIntricGroupId";
        var ancestorIds = List.of("someAncestorId", "someOtherAncestorId");
        var pageJson = "{\"someKey\": \"someValue\"}";
        var page = PageBuilder.create()
            .withPageId(pageId)
            .withAncestorIds(ancestorIds)
            .build();

        var workerSpy = spy(worker);

        when(confluenceClientMock.getContent(pageId)).thenReturn(Optional.of(pageJson));
        when(pageMapperMock.toPage(MUNICIPALITY_ID, pageId, pageJson)).thenReturn(page);
        when(workerSpy.isBlacklisted(pageId, ancestorIds)).thenReturn(false);
        when(workerSpy.getIntricGroupId(pageId, ancestorIds)).thenReturn(intricGroupId);

        var result = workerSpy.getPageFromConfluence(pageId);

        assertThat(result).hasValueSatisfying(actualResult -> assertThat(actualResult.intricGroupId()).isEqualTo(intricGroupId));

        verify(confluenceClientMock).getContent(pageId);
        verify(pageMapperMock).toPage(MUNICIPALITY_ID, pageId, pageJson);
        verify(workerSpy).isBlacklisted(pageId, ancestorIds);
        verify(workerSpy).getIntricGroupId(pageId, ancestorIds);
    }

    @Test
    void getPageFromConfluenceWhenPageIsBlacklisted() {
        var pageId = "somePageId";
        var ancestorIds = List.of("someAncestorId", "someOtherAncestorId");
        var pageJson = "{\"someKey\": \"someValue\"}";
        var page = PageBuilder.create()
            .withPageId(pageId)
            .withAncestorIds(ancestorIds)
            .build();

        var workerSpy = spy(worker);

        when(confluenceClientMock.getContent(pageId)).thenReturn(Optional.of(pageJson));
        when(pageMapperMock.toPage(MUNICIPALITY_ID, pageId, pageJson)).thenReturn(page);
        when(workerSpy.isBlacklisted(pageId, ancestorIds)).thenReturn(true);

        assertThat(workerSpy.getPageFromConfluence(pageId)).isEmpty();

        verify(confluenceClientMock).getContent(pageId);
        verify(pageMapperMock).toPage(MUNICIPALITY_ID, pageId, pageJson);
        verify(workerSpy).isBlacklisted(pageId, ancestorIds);
    }

    @Test
    void getPageFromConfluenceWhenNoMatchingIntricGroupIdIsFound() {
        var pageId = "somePageId";
        var ancestorIds = List.of("someAncestorId", "someOtherAncestorId");
        var pageJson = "{\"someKey\": \"someValue\"}";
        var page = PageBuilder.create()
            .withPageId(pageId)
            .withAncestorIds(ancestorIds)
            .build();

        var workerSpy = spy(worker);

        when(confluenceClientMock.getContent(pageId)).thenReturn(Optional.of(pageJson));
        when(pageMapperMock.toPage(MUNICIPALITY_ID, pageId, pageJson)).thenReturn(page);
        when(workerSpy.isBlacklisted(pageId, ancestorIds)).thenReturn(false);
        when(workerSpy.getIntricGroupId(pageId, ancestorIds)).thenReturn(null);

        assertThat(workerSpy.getPageFromConfluence(pageId)).isEmpty();

        verify(confluenceClientMock).getContent(pageId);
        verify(pageMapperMock).toPage(MUNICIPALITY_ID, pageId, pageJson);
        verify(workerSpy).isBlacklisted(pageId, ancestorIds);
        verify(workerSpy).getIntricGroupId(pageId, ancestorIds);
    }

    @Test
    void insertPage() {
        var pageId = "somePageId";
        var intricGroupId = "someIntricGroupId";
        var intricBlobId = "someIntricBlobId";
        var title = "someTitle";
        var body = "someBody";
        var baseUrl = "someBaseUrl";
        var path = "somePath";

        var page = PageBuilder.create()
            .withPageId(pageId)
            .withIntricGroupId(intricGroupId)
            .withIntricBlobId(intricBlobId)
            .withTitle(title)
            .withBody(body)
            .withBaseUrl(baseUrl)
            .withPath(path)
            .build();

        var workerSpy = spy(worker);
        var pageArgumentCaptor = ArgumentCaptor.forClass(Page.class);

        when(workerSpy.getPageFromConfluence(pageId)).thenReturn(Optional.of(page));
        when(intricIntegrationMock.addInfoBlob(intricGroupId, title, body, baseUrl.concat(path))).thenReturn(intricBlobId);

        workerSpy.insertPage(pageId);

        verify(workerSpy).getPageFromConfluence(pageId);
        verify(dbIntegrationMock).savePage(pageArgumentCaptor.capture());
        verify(intricIntegrationMock).addInfoBlob(intricGroupId, title, body, baseUrl.concat(path));
        verifyNoMoreInteractions(dbIntegrationMock, intricIntegrationMock);

        var savedPage = pageArgumentCaptor.getValue();
        assertThat(savedPage.intricBlobId()).isEqualTo(intricBlobId);
    }

    @Test
    void updatePage() {
        var pageId = "somePageId";
        var intricGroupId = "someIntricGroupId";
        var intricBlobId = "someIntricBlobId";
        var newIntricBlobId = "someNewIntricBlobId";
        var title = "someTitle";
        var body = "someBody";
        var baseUrl = "someBaseUrl";
        var path = "somePath";

        var page = PageBuilder.create()
            .withPageId(pageId)
            .withIntricGroupId(intricGroupId)
            .withIntricBlobId(intricBlobId)
            .withTitle(title)
            .withBody(body)
            .withBaseUrl(baseUrl)
            .withPath(path)
            .build();

        var workerSpy = spy(worker);
        var pageArgumentCaptor = ArgumentCaptor.forClass(Page.class);

        when(workerSpy.getPageFromConfluence(pageId)).thenReturn(Optional.of(page));
        when(dbIntegrationMock.getBlobId(pageId, MUNICIPALITY_ID)).thenReturn(Optional.of(intricBlobId));
        when(intricIntegrationMock.updateInfoBlob(intricGroupId, intricBlobId, title, body, baseUrl.concat(path))).thenReturn(newIntricBlobId);

        workerSpy.updatePage(pageId);

        verify(workerSpy).getPageFromConfluence(pageId);
        verify(dbIntegrationMock).getBlobId(pageId, MUNICIPALITY_ID);
        verify(dbIntegrationMock).savePage(pageArgumentCaptor.capture());
        verify(intricIntegrationMock).updateInfoBlob(intricGroupId, intricBlobId, title, body, baseUrl.concat(path));
        verifyNoMoreInteractions(dbIntegrationMock, intricIntegrationMock);

        var updatedPage = pageArgumentCaptor.getValue();
        assertThat(updatedPage.intricBlobId()).isEqualTo(newIntricBlobId);
    }

    @Test
    void deletePage() {
        var pageId = "somePageId";
        var intricBlobId = "someIntricBlobId";

        when(dbIntegrationMock.getBlobId(pageId, MUNICIPALITY_ID)).thenReturn(Optional.of(intricBlobId));

        worker.deletePage(pageId);

        verify(dbIntegrationMock).getBlobId(pageId, MUNICIPALITY_ID);
        verify(dbIntegrationMock).deletePage(pageId, MUNICIPALITY_ID);
        verify(intricIntegrationMock).deleteInfoBlob(intricBlobId);
        verifyNoMoreInteractions(dbIntegrationMock, intricIntegrationMock);
    }

    @Test
    void isBlacklisted() {
        assertThat(worker.isBlacklisted(BLACKLISTED_ROOT_ID)).isTrue();
        assertThat(worker.isBlacklisted(ROOT_ID)).isFalse();
    }

    @Test
    void isBlackListedByAncestry() {
        assertThat(worker.isBlacklisted(ROOT_ID, List.of(BLACKLISTED_ROOT_ID))).isTrue();
    }

    @Test
    void getIntricGroupId() {
        assertThat(worker.getIntricGroupId("somePageId", List.of("1212419", "98381", ROOT_ID))).isEqualTo(INTRIC_GROUP_ID);
        assertThat(worker.getIntricGroupId(ROOT_ID, List.of())).isEqualTo(INTRIC_GROUP_ID);
        assertThat(worker.getIntricGroupId("somePageId", List.of("unknownChildId", "unknownParentId", "unknownRootId"))).isNull();
    }

}
