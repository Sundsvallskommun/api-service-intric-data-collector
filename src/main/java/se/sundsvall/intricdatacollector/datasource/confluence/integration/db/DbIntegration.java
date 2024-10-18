package se.sundsvall.intricdatacollector.datasource.confluence.integration.db;

import java.util.Optional;

import org.springframework.stereotype.Component;

import se.sundsvall.intricdatacollector.datasource.confluence.model.Page;

@Component
public class DbIntegration {

    private final PageRepository pageRepository;
    private final PageMapper pageMapper;

    DbIntegration(final PageRepository pageRepository, final PageMapper pageMapper) {
        this.pageRepository = pageRepository;
        this.pageMapper = pageMapper;
    }

    public Optional<String> getBlobId(final String pageId, final String municipalityId) {
        return pageRepository.findBlobIdByPageIdAndMunicipalityId(pageId, municipalityId);
    }

    public Optional<Page> getPage(final String pageId, final String municipalityId) {
        return pageRepository.findPageEntityByPageIdAndMunicipalityId(pageId, municipalityId)
            .map(pageMapper::toPage);
    }

    public void savePage(final Page page) {
        var pageEntity = pageMapper.toPageEntity(page);

        pageRepository.save(pageEntity);
    }
    
    public void deletePage(final String pageId, final String municipalityId) {
        pageRepository.deletePageEntityByPageIdAndMunicipalityId(pageId, municipalityId);
    }
}
