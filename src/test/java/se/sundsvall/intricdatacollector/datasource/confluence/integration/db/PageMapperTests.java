package se.sundsvall.intricdatacollector.datasource.confluence.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.intricdatacollector.datasource.confluence.integration.db.DbIntegrationTests.INTRIC_BLOB_ID;
import static se.sundsvall.intricdatacollector.datasource.confluence.integration.db.DbIntegrationTests.INTRIC_GROUP_ID;
import static se.sundsvall.intricdatacollector.datasource.confluence.integration.db.DbIntegrationTests.MUNICIPALITY_ID;
import static se.sundsvall.intricdatacollector.datasource.confluence.integration.db.DbIntegrationTests.PAGE_ID;
import static se.sundsvall.intricdatacollector.datasource.confluence.integration.db.DbIntegrationTests.UPDATED_AT;

import org.junit.jupiter.api.Test;

import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model.PageEntityBuilder;
import se.sundsvall.intricdatacollector.datasource.confluence.model.PageBuilder;

class PageMapperTests {

	private final PageMapper mapper = new PageMapper();

	@Test
	void toPage() {
		var pageEntity = PageEntityBuilder.create()
			.withPageId(PAGE_ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withIntricGroupId(INTRIC_GROUP_ID)
			.withIntricBlobId(INTRIC_BLOB_ID)
			.withUpdatedAt(UPDATED_AT)
			.build();

		assertThat(mapper.toPage(pageEntity)).satisfies(page -> {
			assertThat(page.pageId()).isEqualTo(PAGE_ID);
			assertThat(page.municipalityId()).isEqualTo(MUNICIPALITY_ID);
			assertThat(page.intricGroupId()).isEqualTo(INTRIC_GROUP_ID);
			assertThat(page.intricBlobId()).isEqualTo(INTRIC_BLOB_ID);
			assertThat(page.updatedAt()).isEqualTo(UPDATED_AT);
		});
	}

	@Test
	void toPageEntity() {
		var page = PageBuilder.create()
			.withPageId(PAGE_ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withIntricGroupId(INTRIC_GROUP_ID)
			.withIntricBlobId(INTRIC_BLOB_ID)
			.withUpdatedAt(UPDATED_AT)
			.build();

		assertThat(mapper.toPageEntity(page)).satisfies(pageEntity -> {
			assertThat(pageEntity.getPageId()).isEqualTo(PAGE_ID);
			assertThat(pageEntity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
			assertThat(pageEntity.getIntricGroupId()).isEqualTo(INTRIC_GROUP_ID);
			assertThat(pageEntity.getIntricBlobId()).isEqualTo(INTRIC_BLOB_ID);
			assertThat(pageEntity.getUpdatedAt()).isEqualTo(UPDATED_AT);
		});
	}
}
