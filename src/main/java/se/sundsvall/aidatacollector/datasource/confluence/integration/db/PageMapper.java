package se.sundsvall.aidatacollector.datasource.confluence.integration.db;

import org.springframework.stereotype.Component;
import se.sundsvall.aidatacollector.datasource.confluence.integration.db.model.PageEntity;
import se.sundsvall.aidatacollector.datasource.confluence.integration.db.model.PageEntityBuilder;
import se.sundsvall.aidatacollector.datasource.confluence.model.Page;
import se.sundsvall.aidatacollector.datasource.confluence.model.PageBuilder;

@Component
class PageMapper {

	Page toPage(final PageEntity pageEntity) {
		return PageBuilder.create()
			.withPageId(pageEntity.getPageId())
			.withMunicipalityId(pageEntity.getMunicipalityId())
			.withEneoGroupId(pageEntity.getEneoGroupId())
			.withEneoBlobId(pageEntity.getEneoBlobId())
			.withUpdatedAt(pageEntity.getUpdatedAt())
			.build();
	}

	PageEntity toPageEntity(final Page page) {
		return PageEntityBuilder.create()
			.withPageId(page.pageId())
			.withMunicipalityId(page.municipalityId())
			.withEneoGroupId(page.eneoGroupId())
			.withEneoBlobId(page.eneoBlobId())
			.withUpdatedAt(page.updatedAt())
			.build();
	}
}
