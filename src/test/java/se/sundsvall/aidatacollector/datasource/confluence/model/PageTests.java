package se.sundsvall.aidatacollector.datasource.confluence.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PageTests {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), LocalDateTime.class);
	}

	@Test
	void classProperties() {
		MatcherAssert.assertThat(Page.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals()));
	}

	@Test
	void withMethods() {
		final var municipalityId = "someMunicipalityId";
		final var pageId = "somePageId";
		final var title = "someTitle";
		final var body = "someBody";
		final var baseUrl = "someBaseUrl";
		final var path = "somePath";
		final var updatedAt = LocalDateTime.now();
		final var ancestorIds = List.of("someAncestorId", "someOtherAncestorId");
		final var eneoGroupId = "someEneoGroupId";
		final var eneoBlobId = "someEneoBlobId";

		final var page = Page.create()
			.withMunicipalityId(municipalityId)
			.withPageId(pageId)
			.withTitle(title)
			.withBody(body)
			.withBaseUrl(baseUrl)
			.withPath(path)
			.withUpdatedAt(updatedAt)
			.withAncestorIds(ancestorIds)
			.withEneoGroupId(eneoGroupId)
			.withEneoBlobId(eneoBlobId);

		assertThat(page.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(page.getPageId()).isEqualTo(pageId);
		assertThat(page.getTitle()).isEqualTo(title);
		assertThat(page.getBody()).isEqualTo(body);
		assertThat(page.getBaseUrl()).isEqualTo(baseUrl);
		assertThat(page.getPath()).isEqualTo(path);
		assertThat(page.getUpdatedAt()).isEqualTo(updatedAt);
		assertThat(page.getAncestorIds()).isEqualTo(ancestorIds);
		assertThat(page.getEneoGroupId()).isEqualTo(eneoGroupId);
		assertThat(page.getEneoBlobId()).isEqualTo(eneoBlobId);
	}

	@Test
	void noDirtOnCreatedBean() {
		assertThat(new Page()).hasAllNullFieldsOrProperties();
		assertThat(Page.create()).hasAllNullFieldsOrProperties();
	}

	@Test
	void bodyAsText() {
		final var body = "<p>some text</p><ul><li>item 1</li><li>item 2</li></ul>";

		final var page = Page.create().withBody(body);

		assertThat(page.bodyAsText()).isEqualTo("some text item 1 item 2");
	}

	@Test
	void url() {
		final var baseUrl = "someBaseUrl";
		final var path = "somePath";

		final var page = Page.create()
			.withBaseUrl(baseUrl)
			.withPath(path);

		assertThat(page.url()).isEqualTo(baseUrl.concat(path));
	}
}
