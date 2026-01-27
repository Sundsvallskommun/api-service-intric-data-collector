package se.sundsvall.aidatacollector.datasource.confluence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.aidatacollector.datasource.confluence.integration.confluence.ConfluenceClient;
import se.sundsvall.aidatacollector.datasource.confluence.integration.confluence.ConfluenceClientRegistry;
import se.sundsvall.aidatacollector.datasource.confluence.integration.confluence.ConfluenceIntegrationProperties;
import se.sundsvall.aidatacollector.datasource.confluence.integration.db.DbIntegration;
import se.sundsvall.aidatacollector.datasource.confluence.model.Page;
import se.sundsvall.aidatacollector.datasource.confluence.model.PageBuilder;
import se.sundsvall.aidatacollector.integration.eneo.EneoIntegration;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

@ExtendWith({
	MockitoExtension.class, ResourceLoaderExtension.class
})
class ConfluenceWorkerTests {

	private static final String MUNICIPALITY_ID = "someMunicipalityId";

	private static final String ROOT_ID = "98362";
	private static final String BLACKLISTED_ROOT_ID = "1212426";
	private static final String ENEO_GROUP_ID = "someEneoGroupId";

	@Mock
	private ConfluenceDataSourceHealthIndicator healthIndicatorMock;
	@Mock
	private ConfluenceIntegrationProperties propertiesMock;
	@Mock
	private ConfluenceIntegrationProperties.Environment environmentMock;
	@Mock
	private ConfluenceClientRegistry confluenceClientRegistryMock;
	@Mock
	private ConfluencePageMapper pageMapperMock;
	@Mock
	private PageJsonParser pageJsonParserMock;
	@Mock
	private PageJsonParser.PageJson pageJsonMock;
	@Mock
	private DbIntegration dbIntegrationMock;
	@Mock
	private ConfluenceClient confluenceClientMock;
	@Mock
	private EneoIntegration eneoIntegrationMock;

	private ConfluenceWorker worker;

	@BeforeEach
	void setUp() {
		when(propertiesMock.environments()).thenReturn(Map.of(MUNICIPALITY_ID, environmentMock));

		// Add the "probibited root page" to the list of blacklisted root ids
		when(environmentMock.blacklistedRootIds()).thenReturn(List.of("1212426"));
		// Set up a single mapping
		when(environmentMock.mappings()).thenReturn(List.of(
			new ConfluenceIntegrationProperties.Environment.Mapping(ENEO_GROUP_ID, ROOT_ID)));

		when(confluenceClientRegistryMock.getClient(MUNICIPALITY_ID)).thenReturn(confluenceClientMock);

		worker = new ConfluenceWorker(MUNICIPALITY_ID, propertiesMock, healthIndicatorMock, confluenceClientRegistryMock, pageMapperMock, eneoIntegrationMock, dbIntegrationMock, pageJsonParserMock);
	}

	@Test
	void run() {
		final var workerSpy = spy(worker);

		doNothing().when(workerSpy).processTree(ROOT_ID);

		workerSpy.run();

		verify(workerSpy).processTree(ROOT_ID);
	}

	@Test
	void processTree() {
		final var pageId = "somePageId";
		final var otherPageId = "someOtherPageId";

		final var workerSpy = spy(worker);

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
		final var pageId = "somePageId";
		final var childIds = List.of("someChildId", "someOtherChildId");
		final var pageJson = "{\"someKey\": \"someValue\"}";

		final var workerSpy = spy(worker);

		when(confluenceClientMock.getChildren(pageId)).thenReturn(Optional.of(pageJson));
		when(pageJsonParserMock.parse(pageJson)).thenReturn(pageJsonMock);
		when(pageJsonMock.getChildIds()).thenReturn(childIds);

		workerSpy.processChildren(pageId);

		verify(workerSpy).processTree("someChildId");
		verify(workerSpy).processTree("someOtherChildId");
	}

	@Test
	void processChildrenWhenThereAreNoChildren() {
		final var pageId = "somePageId";
		final var pageJson = "{\"someKey\": \"someValue\"}";

		final var workerSpy = spy(worker);

		when(confluenceClientMock.getChildren(pageId)).thenReturn(Optional.of(pageJson));
		when(pageJsonParserMock.parse(pageJson)).thenReturn(pageJsonMock);
		when(pageJsonMock.getChildIds()).thenReturn(List.of());

		workerSpy.processChildren(pageId);

		verify(workerSpy, never()).processTree(anyString());
	}

	@Test
	void processPage() {
		final var pageId = "somePageId";
		final var pageJson = "{\"someKey\": \"someValue\"}";
		final var updatedAt = OffsetDateTime.now().toString();

		final var page = PageBuilder.create()
			.withUpdatedAt(LocalDateTime.now().minusMonths(1))
			.build();

		final var workerSpy = spy(worker);

		when(confluenceClientMock.getContentVersion(pageId)).thenReturn(Optional.of(pageJson));
		when(pageJsonParserMock.parse(pageJson)).thenReturn(pageJsonMock);
		when(pageJsonMock.getUpdatedAt()).thenReturn(updatedAt);
		when(dbIntegrationMock.getPage(pageId, MUNICIPALITY_ID)).thenReturn(Optional.of(page));

		workerSpy.processPage(pageId);

		verify(workerSpy).updatePage(pageId);
		verify(confluenceClientMock).getContentVersion(pageId);
		verify(dbIntegrationMock).getPage(pageId, MUNICIPALITY_ID);
	}

	@Test
	void processPageWhenPageIsMissingLocally() {
		final var pageId = "somePageId";
		final var pageJson = "{\"someKey\": \"someValue\"}";
		final var updatedAt = OffsetDateTime.now().toString();

		final var workerSpy = spy(worker);

		when(confluenceClientMock.getContentVersion(pageId)).thenReturn(Optional.of(pageJson));
		when(pageJsonParserMock.parse(pageJson)).thenReturn(pageJsonMock);
		when(pageJsonMock.getUpdatedAt()).thenReturn(updatedAt);
		when(dbIntegrationMock.getPage(pageId, MUNICIPALITY_ID)).thenReturn(Optional.empty());
		when(pageMapperMock.newPage(MUNICIPALITY_ID, pageId)).thenCallRealMethod();

		workerSpy.processPage(pageId);

		verify(workerSpy).insertPage(pageId);
		verify(confluenceClientMock).getContentVersion(pageId);
		verify(dbIntegrationMock).getPage(pageId, MUNICIPALITY_ID);
	}

	@Test
	void getPageFromConfluence() {
		final var pageId = "somePageId";
		final var eneoGroupId = "someEneoGroupId";
		final var ancestorIds = List.of("someAncestorId", "someOtherAncestorId");
		final var pageJson = "{\"someKey\": \"someValue\"}";
		final var page = PageBuilder.create()
			.withPageId(pageId)
			.withAncestorIds(ancestorIds)
			.build();

		final var workerSpy = spy(worker);

		when(confluenceClientMock.getContent(pageId)).thenReturn(Optional.of(pageJson));
		when(pageMapperMock.toPage(MUNICIPALITY_ID, pageId, pageJson)).thenReturn(page);
		when(workerSpy.isBlacklisted(pageId, ancestorIds)).thenReturn(false);
		when(workerSpy.getEneoGroupId(pageId, ancestorIds)).thenReturn(eneoGroupId);

		final var result = workerSpy.getPageFromConfluence(pageId);

		assertThat(result).hasValueSatisfying(actualResult -> assertThat(actualResult.eneoGroupId()).isEqualTo(eneoGroupId));

		verify(confluenceClientMock).getContent(pageId);
		verify(pageMapperMock).toPage(MUNICIPALITY_ID, pageId, pageJson);
		verify(workerSpy).isBlacklisted(pageId, ancestorIds);
		verify(workerSpy).getEneoGroupId(pageId, ancestorIds);
	}

	@Test
	void getPageFromConfluenceWhenPageIsBlacklisted() {
		final var pageId = "somePageId";
		final var ancestorIds = List.of("someAncestorId", "someOtherAncestorId");
		final var pageJson = "{\"someKey\": \"someValue\"}";
		final var page = PageBuilder.create()
			.withPageId(pageId)
			.withAncestorIds(ancestorIds)
			.build();

		final var workerSpy = spy(worker);

		when(confluenceClientMock.getContent(pageId)).thenReturn(Optional.of(pageJson));
		when(pageMapperMock.toPage(MUNICIPALITY_ID, pageId, pageJson)).thenReturn(page);
		when(workerSpy.isBlacklisted(pageId, ancestorIds)).thenReturn(true);

		assertThat(workerSpy.getPageFromConfluence(pageId)).isEmpty();

		verify(confluenceClientMock).getContent(pageId);
		verify(pageMapperMock).toPage(MUNICIPALITY_ID, pageId, pageJson);
		verify(workerSpy).isBlacklisted(pageId, ancestorIds);
	}

	@Test
	void getPageFromConfluenceWhenNoMatchingEneoGroupIdIsFound() {
		final var pageId = "somePageId";
		final var ancestorIds = List.of("someAncestorId", "someOtherAncestorId");
		final var pageJson = "{\"someKey\": \"someValue\"}";
		final var page = PageBuilder.create()
			.withPageId(pageId)
			.withAncestorIds(ancestorIds)
			.build();

		final var workerSpy = spy(worker);

		when(confluenceClientMock.getContent(pageId)).thenReturn(Optional.of(pageJson));
		when(pageMapperMock.toPage(MUNICIPALITY_ID, pageId, pageJson)).thenReturn(page);
		when(workerSpy.isBlacklisted(pageId, ancestorIds)).thenReturn(false);
		when(workerSpy.getEneoGroupId(pageId, ancestorIds)).thenReturn(null);

		assertThat(workerSpy.getPageFromConfluence(pageId)).isEmpty();

		verify(confluenceClientMock).getContent(pageId);
		verify(pageMapperMock).toPage(MUNICIPALITY_ID, pageId, pageJson);
		verify(workerSpy).isBlacklisted(pageId, ancestorIds);
		verify(workerSpy).getEneoGroupId(pageId, ancestorIds);
	}

	@Test
	void insertPage() {
		final var pageId = "somePageId";
		final var eneoGroupId = "someEneoGroupId";
		final var eneoBlobId = "someEneoBlobId";
		final var title = "someTitle";
		final var body = "someBody";
		final var baseUrl = "someBaseUrl";
		final var path = "somePath";

		final var page = PageBuilder.create()
			.withPageId(pageId)
			.withEneoGroupId(eneoGroupId)
			.withEneoBlobId(eneoBlobId)
			.withTitle(title)
			.withBody(body)
			.withBaseUrl(baseUrl)
			.withPath(path)
			.build();

		final var workerSpy = spy(worker);
		final var pageArgumentCaptor = ArgumentCaptor.forClass(Page.class);

		when(workerSpy.getPageFromConfluence(pageId)).thenReturn(Optional.of(page));
		when(eneoIntegrationMock.addInfoBlob(MUNICIPALITY_ID, eneoGroupId, title, body, baseUrl.concat(path))).thenReturn(eneoBlobId);

		workerSpy.insertPage(pageId);

		verify(workerSpy).getPageFromConfluence(pageId);
		verify(dbIntegrationMock).savePage(pageArgumentCaptor.capture());
		verify(eneoIntegrationMock).addInfoBlob(MUNICIPALITY_ID, eneoGroupId, title, body, baseUrl.concat(path));
		verifyNoMoreInteractions(dbIntegrationMock, eneoIntegrationMock);

		final var savedPage = pageArgumentCaptor.getValue();
		assertThat(savedPage.eneoBlobId()).isEqualTo(eneoBlobId);
	}

	@Test
	void updatePage() {
		final var pageId = "somePageId";
		final var eneoGroupId = "someEneoGroupId";
		final var eneoBlobId = "someEneoBlobId";
		final var newEneoBlobId = "someNewEneoBlobId";
		final var title = "someTitle";
		final var body = "someBody";
		final var baseUrl = "someBaseUrl";
		final var path = "somePath";

		final var page = PageBuilder.create()
			.withPageId(pageId)
			.withEneoGroupId(eneoGroupId)
			.withEneoBlobId(eneoBlobId)
			.withTitle(title)
			.withBody(body)
			.withBaseUrl(baseUrl)
			.withPath(path)
			.build();

		final var workerSpy = spy(worker);
		final var pageArgumentCaptor = ArgumentCaptor.forClass(Page.class);

		when(workerSpy.getPageFromConfluence(pageId)).thenReturn(Optional.of(page));
		when(dbIntegrationMock.getBlobId(pageId, MUNICIPALITY_ID)).thenReturn(Optional.of(eneoBlobId));
		when(eneoIntegrationMock.updateInfoBlob(MUNICIPALITY_ID, eneoGroupId, eneoBlobId, title, body, baseUrl.concat(path))).thenReturn(newEneoBlobId);

		workerSpy.updatePage(pageId);

		verify(workerSpy).getPageFromConfluence(pageId);
		verify(dbIntegrationMock).getBlobId(pageId, MUNICIPALITY_ID);
		verify(dbIntegrationMock).savePage(pageArgumentCaptor.capture());
		verify(eneoIntegrationMock).updateInfoBlob(MUNICIPALITY_ID, eneoGroupId, eneoBlobId, title, body, baseUrl.concat(path));
		verifyNoMoreInteractions(dbIntegrationMock, eneoIntegrationMock);

		final var updatedPage = pageArgumentCaptor.getValue();
		assertThat(updatedPage.eneoBlobId()).isEqualTo(newEneoBlobId);
	}

	@Test
	void deletePage() {
		final var pageId = "somePageId";
		final var eneoBlobId = "someEneoBlobId";

		when(dbIntegrationMock.getBlobId(pageId, MUNICIPALITY_ID)).thenReturn(Optional.of(eneoBlobId));

		worker.deletePage(pageId);

		verify(dbIntegrationMock).getBlobId(pageId, MUNICIPALITY_ID);
		verify(dbIntegrationMock).deletePage(pageId, MUNICIPALITY_ID);
		verify(eneoIntegrationMock).deleteInfoBlob(MUNICIPALITY_ID, eneoBlobId);
		verifyNoMoreInteractions(dbIntegrationMock, eneoIntegrationMock);
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
	void getEneoGroupId() {
		assertThat(worker.getEneoGroupId("somePageId", List.of("1212419", "98381", ROOT_ID))).isEqualTo(ENEO_GROUP_ID);
		assertThat(worker.getEneoGroupId(ROOT_ID, List.of())).isEqualTo(ENEO_GROUP_ID);
		assertThat(worker.getEneoGroupId("somePageId", List.of("unknownChildId", "unknownParentId", "unknownRootId"))).isNull();
	}

}
