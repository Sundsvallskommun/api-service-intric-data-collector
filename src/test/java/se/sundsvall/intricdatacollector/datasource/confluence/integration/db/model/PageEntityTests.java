package se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.LocalDateTime;
import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PageEntityTests {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), LocalDateTime.class);
	}

	@Test
	void classProperties() {
		MatcherAssert.assertThat(PageEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals()));
	}

	@Test
	void gettersAndSetters() {
		var pageId = "somePageId";
		var municipalityId = "someMunicipalityId";
		var intricGroupId = "someIntricGroupId";
		var intricBlobId = "someIntricBlobId";

		var pageEntity = new PageEntity();
		pageEntity.setPageId(pageId);
		pageEntity.setMunicipalityId(municipalityId);
		pageEntity.setIntricGroupId(intricGroupId);
		pageEntity.setIntricBlobId(intricBlobId);

		assertThat(pageEntity.getPageId()).isEqualTo(pageId);
		assertThat(pageEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(pageEntity.getIntricGroupId()).isEqualTo(intricGroupId);
		assertThat(pageEntity.getIntricBlobId()).isEqualTo(intricBlobId);
	}

	@Test
	void builder() {
		var pageId = "somePageId";
		var municipalityId = "someMunicipalityId";
		var intricGroupId = "someIntricGroupId";
		var intricBlobId = "someIntricBlobId";

		var pageEntity = PageEntityBuilder.create()
			.withPageId(pageId)
			.withMunicipalityId(municipalityId)
			.withIntricGroupId(intricGroupId)
			.withIntricBlobId(intricBlobId)
			.build();

		assertThat(pageEntity.getPageId()).isEqualTo(pageId);
		assertThat(pageEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(pageEntity.getIntricGroupId()).isEqualTo(intricGroupId);
		assertThat(pageEntity.getIntricBlobId()).isEqualTo(intricBlobId);
	}

	@Test
	void noDirtOnCreatedBean() {
		assertThat(new PageEntity()).hasAllNullFieldsOrProperties();
		assertThat(PageEntityBuilder.create().build()).hasAllNullFieldsOrProperties();
	}
}
