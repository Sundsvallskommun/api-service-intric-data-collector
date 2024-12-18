package se.sundsvall.intricdatacollector.datasource.confluence.integration.db;

import org.springframework.stereotype.Component;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model.PageEntity;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model.PageEntityBuilder;
import se.sundsvall.intricdatacollector.datasource.confluence.model.Page;
import se.sundsvall.intricdatacollector.datasource.confluence.model.PageBuilder;

@Component
class PageMapper {

	Page toPage(final PageEntity pageEntity) {
		return PageBuilder.create()
			.withPageId(pageEntity.getPageId())
			.withMunicipalityId(pageEntity.getMunicipalityId())
			.withIntricGroupId(pageEntity.getIntricGroupId())
			.withIntricBlobId(pageEntity.getIntricBlobId())
			.withUpdatedAt(pageEntity.getUpdatedAt())
			.build();
	}

	PageEntity toPageEntity(final Page page) {
		return PageEntityBuilder.create()
			.withPageId(page.pageId())
			.withMunicipalityId(page.municipalityId())
			.withIntricGroupId(page.intricGroupId())
			.withIntricBlobId(page.intricBlobId())
			.withUpdatedAt(page.updatedAt())
			.build();
	}
}
