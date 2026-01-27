package se.sundsvall.intricdatacollector.datasource.confluence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConfluencePageMapperTests {

	@Mock
	private PageJsonParser pageJsonParserMock;
	@Mock
	private PageJsonParser.PageJson pageJsonMock;

	@InjectMocks
	private ConfluencePageMapper pageMapper;

	@Test
	void newPage() {
		final var pageId = "somePageId";
		final var municipalityId = "someMunicipalityId";

		final var page = pageMapper.newPage(municipalityId, pageId);

		assertThat(page).hasAllNullFieldsOrPropertiesExcept("municipalityId", "pageId");
		assertThat(page.municipalityId()).isEqualTo(municipalityId);
		assertThat(page.pageId()).isEqualTo(pageId);
	}

	@Test
	void toPage() {
		final var json = "someJson";
		final var pageId = "somePageId";
		final var municipalityId = "someMunicipalityId";
		final var title = "someTitle";
		final var body = "someBody";
		final var baseUrl = "someBaseUrl";
		final var path = "somePath";
		final var updatedAt = OffsetDateTime.now().minusDays(4);
		final var ancestorIds = List.of("someAncestorId", "someOtherAncestorId");

		when(pageJsonParserMock.parse(json)).thenReturn(pageJsonMock);
		when(pageJsonMock.getTitle()).thenReturn(title);
		when(pageJsonMock.getBody()).thenReturn(body);
		when(pageJsonMock.getBaseUrl()).thenReturn(baseUrl);
		when(pageJsonMock.getPath()).thenReturn(path);
		when(pageJsonMock.getUpdatedAt()).thenReturn(updatedAt.toString());
		when(pageJsonMock.getAncestorIds()).thenReturn(ancestorIds);

		assertThat(pageMapper.toPage(municipalityId, pageId, json)).satisfies(page -> {
			assertThat(page.pageId()).isEqualTo(pageId);
			assertThat(page.municipalityId()).isEqualTo(municipalityId);
			assertThat(page.title()).isEqualTo(title);
			assertThat(page.body()).isEqualTo(body);
			assertThat(page.baseUrl()).isEqualTo(baseUrl);
			assertThat(page.path()).isEqualTo(path);
			assertThat(page.updatedAt()).isEqualTo(updatedAt.toLocalDateTime());
			assertThat(page.ancestorIds()).isEqualTo(ancestorIds);
		});

		verify(pageJsonParserMock).parse(json);
		verify(pageJsonMock).getTitle();
		verify(pageJsonMock).getBody();
		verify(pageJsonMock).getBaseUrl();
		verify(pageJsonMock).getPath();
		verify(pageJsonMock).getUpdatedAt();
		verify(pageJsonMock).getAncestorIds();
	}

	@Test
	void toPageWhenUpdatedAtIsMissing() {
		final var json = "someJson";
		final var pageId = "somePageId";
		final var municipalityId = "someMunicipalityId";

		when(pageJsonParserMock.parse(json)).thenReturn(pageJsonMock);
		when(pageJsonMock.getUpdatedAt()).thenReturn(null);

		final var page = pageMapper.toPage(municipalityId, pageId, json);

		assertThat(page.updatedAt()).isNull();
	}
}
