package se.sundsvall.intricdatacollector.datasource.confluence.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model.PageEntity;

public interface PageRepository extends JpaRepository<PageEntity, String> {

}
