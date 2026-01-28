package se.sundsvall.aidatacollector.datasource.confluence.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.aidatacollector.datasource.confluence.integration.db.DbIntegrationTests.ENEO_BLOB_ID;
import static se.sundsvall.aidatacollector.datasource.confluence.integration.db.DbIntegrationTests.ENEO_GROUP_ID;
import static se.sundsvall.aidatacollector.datasource.confluence.integration.db.DbIntegrationTests.MUNICIPALITY_ID;
import static se.sundsvall.aidatacollector.datasource.confluence.integration.db.DbIntegrationTests.PAGE_ID;
import static se.sundsvall.aidatacollector.datasource.confluence.integration.db.DbIntegrationTests.UPDATED_AT;

import org.junit.jupiter.api.Test;
import se.sundsvall.aidatacollector.datasource.confluence.integration.db.model.PageEntity;
import se.sundsvall.aidatacollector.datasource.confluence.model.Page;

class PageMapperTests {

	private final PageMapper mapper = new PageMapper();

	@Test
	void toPage() {
		final var pageEntity = PageEntity.create()
			.withPageId(PAGE_ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withEneoGroupId(ENEO_GROUP_ID)
			.withEneoBlobId(ENEO_BLOB_ID)
			.withUpdatedAt(UPDATED_AT);

		assertThat(mapper.toPage(pageEntity)).satisfies(page -> {
			assertThat(page.getPageId()).isEqualTo(PAGE_ID);
			assertThat(page.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
			assertThat(page.getEneoGroupId()).isEqualTo(ENEO_GROUP_ID);
			assertThat(page.getEneoBlobId()).isEqualTo(ENEO_BLOB_ID);
			assertThat(page.getUpdatedAt()).isEqualTo(UPDATED_AT);
		});
	}

	@Test
	void toPageEntity() {
		final var page = Page.create()
			.withPageId(PAGE_ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.withEneoGroupId(ENEO_GROUP_ID)
			.withEneoBlobId(ENEO_BLOB_ID)
			.withUpdatedAt(UPDATED_AT);

		assertThat(mapper.toPageEntity(page)).satisfies(pageEntity -> {
			assertThat(pageEntity.getPageId()).isEqualTo(PAGE_ID);
			assertThat(pageEntity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
			assertThat(pageEntity.getEneoGroupId()).isEqualTo(ENEO_GROUP_ID);
			assertThat(pageEntity.getEneoBlobId()).isEqualTo(ENEO_BLOB_ID);
			assertThat(pageEntity.getUpdatedAt()).isEqualTo(UPDATED_AT);
		});
	}
}
