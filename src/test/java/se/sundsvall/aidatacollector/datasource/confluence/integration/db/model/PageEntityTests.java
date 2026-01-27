package se.sundsvall.aidatacollector.datasource.confluence.integration.db.model;

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
		final var pageId = "somePageId";
		final var municipalityId = "someMunicipalityId";
		final var eneoGroupId = "someEneoGroupId";
		final var eneoBlobId = "someEneoBlobId";

		final var pageEntity = new PageEntity();
		pageEntity.setPageId(pageId);
		pageEntity.setMunicipalityId(municipalityId);
		pageEntity.setEneoGroupId(eneoGroupId);
		pageEntity.setEneoBlobId(eneoBlobId);

		assertThat(pageEntity.getPageId()).isEqualTo(pageId);
		assertThat(pageEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(pageEntity.getEneoGroupId()).isEqualTo(eneoGroupId);
		assertThat(pageEntity.getEneoBlobId()).isEqualTo(eneoBlobId);
	}

	@Test
	void builder() {
		final var pageId = "somePageId";
		final var municipalityId = "someMunicipalityId";
		final var eneoGroupId = "someEneoGroupId";
		final var eneoBlobId = "someEneoBlobId";

		final var pageEntity = PageEntityBuilder.create()
			.withPageId(pageId)
			.withMunicipalityId(municipalityId)
			.withEneoGroupId(eneoGroupId)
			.withEneoBlobId(eneoBlobId)
			.build();

		assertThat(pageEntity.getPageId()).isEqualTo(pageId);
		assertThat(pageEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(pageEntity.getEneoGroupId()).isEqualTo(eneoGroupId);
		assertThat(pageEntity.getEneoBlobId()).isEqualTo(eneoBlobId);
	}

	@Test
	void noDirtOnCreatedBean() {
		assertThat(new PageEntity()).hasAllNullFieldsOrProperties();
		assertThat(PageEntityBuilder.create().build()).hasAllNullFieldsOrProperties();
	}
}
