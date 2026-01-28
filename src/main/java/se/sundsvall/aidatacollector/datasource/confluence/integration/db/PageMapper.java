package se.sundsvall.aidatacollector.datasource.confluence.integration.db;

import org.springframework.stereotype.Component;
import se.sundsvall.aidatacollector.datasource.confluence.integration.db.model.PageEntity;
import se.sundsvall.aidatacollector.datasource.confluence.model.Page;

@Component
class PageMapper {

	Page toPage(final PageEntity pageEntity) {
		return Page.create()
			.withPageId(pageEntity.getPageId())
			.withMunicipalityId(pageEntity.getMunicipalityId())
			.withEneoGroupId(pageEntity.getEneoGroupId())
			.withEneoBlobId(pageEntity.getEneoBlobId())
			.withUpdatedAt(pageEntity.getUpdatedAt());
	}

	PageEntity toPageEntity(final Page page) {
		return PageEntity.create()
			.withPageId(page.getPageId())
			.withMunicipalityId(page.getMunicipalityId())
			.withEneoGroupId(page.getEneoGroupId())
			.withEneoBlobId(page.getEneoBlobId())
			.withUpdatedAt(page.getUpdatedAt());
	}
}
