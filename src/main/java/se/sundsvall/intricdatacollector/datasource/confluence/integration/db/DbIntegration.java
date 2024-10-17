package se.sundsvall.intricdatacollector.datasource.confluence.integration.db;

import java.util.Optional;

import org.springframework.stereotype.Component;

import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model.PageEntityBuilder;
import se.sundsvall.intricdatacollector.datasource.confluence.model.Page;
import se.sundsvall.intricdatacollector.datasource.confluence.model.PageBuilder;

@Component
public class DbIntegration {

    private final PageRepository pageRepository;

    DbIntegration(final PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    public Optional<String> getBlobId(final String pageId, final String municipalityId) {
        return pageRepository.findBlobIdByPageIdAndMunicipalityId(pageId, municipalityId);
    }

    public Optional<Page> getPage(final String pageId, final String municipalityId) {
        return pageRepository.findPageEntityByPageIdAndMunicipalityId(pageId, municipalityId)
            .map(pageEntity -> PageBuilder.create()
                .withPageId(pageEntity.getPageId())
                .withMunicipalityId(pageEntity.getMunicipalityId())
                .withIntricGroupId(pageEntity.getIntricGroupId())
                .withIntricBlobId(pageEntity.getIntricBlobId())
                .withUpdatedAt(pageEntity.getUpdatedAt())
                .build());
    }

    public void savePage(final Page page) {
        var pageEntity = PageEntityBuilder.create()
            .withPageId(page.pageId())
            .withMunicipalityId(page.municipalityId())
            .withIntricGroupId(page.intricGroupId())
            .withIntricBlobId(page.intricBlobId())
            .withUpdatedAt(page.updatedAt())
            .build();

        pageRepository.save(pageEntity);
    }
    
    public void deletePage(final String pageId, final String municipalityId) {
        pageRepository.deletePageEntityByPageIdAndMunicipalityId(pageId, municipalityId);
    }
}
