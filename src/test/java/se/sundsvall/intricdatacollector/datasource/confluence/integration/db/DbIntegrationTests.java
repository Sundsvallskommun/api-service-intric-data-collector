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

	static final String PAGE_ID = "somePageId";
	static final String MUNICIPALITY_ID = "someMunicipalityId";
	static final String INTRIC_GROUP_ID = "someIntricGroupId";
	static final String INTRIC_BLOB_ID = "someIntricBlobId";
	static final LocalDateTime UPDATED_AT = LocalDateTime.now().minusMonths(3);

	@Mock
	private PageRepository pageRepositoryMock;
	@Mock
	private PageMapper pageMapperMock;

	@InjectMocks
	private DbIntegration dbIntegration;

	@Test
	void getBlobId() {
		when(pageRepositoryMock.findBlobIdByPageIdAndMunicipalityId(PAGE_ID, MUNICIPALITY_ID)).thenReturn(of(INTRIC_BLOB_ID));

		assertThat(dbIntegration.getBlobId(PAGE_ID, MUNICIPALITY_ID)).hasValue(INTRIC_BLOB_ID);

		verify(pageRepositoryMock).findBlobIdByPageIdAndMunicipalityId(PAGE_ID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(pageRepositoryMock);
	}

	@Test
	void getPage() {
		var pageEntity = PageEntityBuilder.create()
			.withPageId(PAGE_ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withIntricGroupId(INTRIC_GROUP_ID)
			.withIntricBlobId(INTRIC_BLOB_ID)
			.withUpdatedAt(UPDATED_AT)
			.build();

		when(pageRepositoryMock.findPageEntityByPageIdAndMunicipalityId(PAGE_ID, MUNICIPALITY_ID)).thenReturn(of(pageEntity));
		when(pageMapperMock.toPage(pageEntity)).thenCallRealMethod();

		var page = dbIntegration.getPage(PAGE_ID, MUNICIPALITY_ID);

		assertThat(page).isNotEmpty().hasValueSatisfying(actualPage -> {
			assertThat(actualPage.pageId()).isEqualTo(PAGE_ID);
			assertThat(actualPage.municipalityId()).isEqualTo(MUNICIPALITY_ID);
			assertThat(actualPage.intricGroupId()).isEqualTo(INTRIC_GROUP_ID);
			assertThat(actualPage.intricBlobId()).isEqualTo(INTRIC_BLOB_ID);
			assertThat(actualPage.updatedAt()).isEqualTo(UPDATED_AT);
		});

		verify(pageRepositoryMock).findPageEntityByPageIdAndMunicipalityId(PAGE_ID, MUNICIPALITY_ID);
		verify(pageMapperMock).toPage(pageEntity);
		verifyNoMoreInteractions(pageRepositoryMock, pageMapperMock);
	}

	@Test
	void savePage() {
		var pageEntityCaptor = ArgumentCaptor.forClass(PageEntity.class);

		var page = PageBuilder.create()
			.withPageId(PAGE_ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withIntricGroupId(INTRIC_GROUP_ID)
			.withIntricBlobId(INTRIC_BLOB_ID)
			.withUpdatedAt(UPDATED_AT)
			.build();

		when(pageMapperMock.toPageEntity(page)).thenCallRealMethod();

		dbIntegration.savePage(page);

		verify(pageRepositoryMock).save(pageEntityCaptor.capture());
		verify(pageMapperMock).toPageEntity(page);
		verifyNoMoreInteractions(pageRepositoryMock, pageMapperMock);

		assertThat(pageEntityCaptor.getValue()).satisfies(pageEntity -> {
			assertThat(pageEntity.getPageId()).isEqualTo(PAGE_ID);
			assertThat(pageEntity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
			assertThat(pageEntity.getIntricGroupId()).isEqualTo(INTRIC_GROUP_ID);
			assertThat(pageEntity.getIntricBlobId()).isEqualTo(INTRIC_BLOB_ID);
			assertThat(pageEntity.getUpdatedAt()).isEqualTo(UPDATED_AT);
		});
	}

	@Test
	void deletePage() {
		doNothing().when(pageRepositoryMock).deletePageEntityByPageIdAndMunicipalityId(PAGE_ID, MUNICIPALITY_ID);

		dbIntegration.deletePage(PAGE_ID, MUNICIPALITY_ID);

		verify(pageRepositoryMock).deletePageEntityByPageIdAndMunicipalityId(PAGE_ID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(pageRepositoryMock);
	}
}
