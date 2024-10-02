package se.sundsvall.intricdatacollector.datasource.confluence.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model.PageEntity;

public interface PageRepository extends JpaRepository<PageEntity, String> {

    @Query(value = "SELECT p.blob_id FROM confluence_pages AS p WHERE p.page_id = :id AND p.municipality_id = :municipalityId", nativeQuery = true)
    Optional<String> findBlobIdByIdAndMunicipalityId(@Param("id") final String id, @Param("municipalityId") final String municipalityId);

    @Query("FROM PageEntity p WHERE p.pageId = :id AND p.municipalityId = :municipalityId")
    Optional<PageEntity> findByIdAndMunicipalityId(@Param("id") final String id, @Param("municipalityId") final String municipalityId);

    @Query("SELECT COUNT(p.pageId) = 1 FROM PageEntity p WHERE p.pageId = :id AND p.municipalityId = :municipalityId")
    boolean existsByIdAndMunicipalityId(@Param("id") final String id, @Param("municipalityId") final String municipalityId);

    @Modifying
    @Query("DELETE FROM PageEntity p WHERE p.pageId = :id AND p.municipalityId = :municipalityId")
    void deleteByIdAndMunicipalityId(@Param("id") final String id, @Param("municipalityId") final String municipalityId);
}
