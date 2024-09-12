package se.sundsvall.intricdatacollector.datasource.confluence.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model.PageEntity;

public interface PageRepository extends JpaRepository<PageEntity, String> {

    @Query("from PageEntity p where p.id = :id and p.municipalityId = :municipalityId")
    Optional<PageEntity> findByIdAndMunicipalityId(@Param("id") final String id, @Param("municipalityId") final String municipalityId);

    @Query("select count(p.id) = 1 from PageEntity p where p.id = :id and p.municipalityId = :municipalityId")
    boolean existsByIdAndMunicipalityId(@Param("id") final String id, @Param("municipalityId") final String municipalityId);

    @Modifying
    @Query("delete from PageEntity p where p.id = :id and p.municipalityId = :municipalityId")
    void deleteByIdAndMunicipalityId(@Param("id") final String id, @Param("municipalityId") final String municipalityId);
}
