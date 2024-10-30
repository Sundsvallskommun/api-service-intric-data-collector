package se.sundsvall.intricdatacollector.datasource.confluence.integration.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model.PageEntity;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@CircuitBreaker(name = "pageRepository")
interface PageRepository extends JpaRepository<PageEntity, String> {

	@Query(value = "SELECT p.blob_id FROM confluence_pages AS p WHERE p.page_id = :pageId AND p.municipality_id = :municipalityId", nativeQuery = true)
	Optional<String> findBlobIdByPageIdAndMunicipalityId(@Param("pageId") String pageId, @Param("municipalityId") String municipalityId);

	List<PageEntity> findPageEntitiesByMunicipalityId(String municipalityId);

	Optional<PageEntity> findPageEntityByPageIdAndMunicipalityId(String pageId, String municipalityId);

	void deletePageEntityByPageIdAndMunicipalityId(String pageId, String municipalityId);
}
