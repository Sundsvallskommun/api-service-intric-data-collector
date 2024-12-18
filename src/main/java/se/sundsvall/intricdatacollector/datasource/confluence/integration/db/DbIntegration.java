package se.sundsvall.intricdatacollector.datasource.confluence.integration.db;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.intricdatacollector.datasource.confluence.model.Page;

@Component
@Transactional
public class DbIntegration {

	private final PageRepository pageRepository;
	private final PageMapper pageMapper;

	DbIntegration(final PageRepository pageRepository, final PageMapper pageMapper) {
		this.pageRepository = pageRepository;
		this.pageMapper = pageMapper;
	}

	@Transactional(readOnly = true)
	public Optional<String> getBlobId(final String pageId, final String municipalityId) {
		return pageRepository.findBlobIdByPageIdAndMunicipalityId(pageId, municipalityId);
	}

	@Transactional(readOnly = true)
	public List<Page> getAllPages(final String municipalityId) {
		return pageRepository.findPageEntitiesByMunicipalityId(municipalityId).stream()
			.map(pageMapper::toPage)
			.toList();
	}

	@Transactional(readOnly = true)
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
