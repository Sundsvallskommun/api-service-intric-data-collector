package se.sundsvall.aidatacollector.datasource.confluence;

import static java.util.Optional.ofNullable;

import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;
import se.sundsvall.aidatacollector.datasource.confluence.model.Page;
import se.sundsvall.aidatacollector.datasource.confluence.model.PageBuilder;

@Component
class ConfluencePageMapper {

	private final PageJsonParser pageJsonParser;

	ConfluencePageMapper(final PageJsonParser pageJsonParser) {
		this.pageJsonParser = pageJsonParser;
	}

	Page newPage(final String municipalityId, final String pageId) {
		return PageBuilder.create()
			.withPageId(pageId)
			.withMunicipalityId(municipalityId)
			.build();
	}

	Page toPage(final String municipalityId, final String pageId, final String json) {
		final var pageJson = pageJsonParser.parse(json);

		return PageBuilder.create()
			.withMunicipalityId(municipalityId)
			.withPageId(pageId)
			.withTitle(pageJson.getTitle())
			.withBody(pageJson.getBody())
			.withBaseUrl(pageJson.getBaseUrl())
			.withPath(pageJson.getPath())
			.withUpdatedAt(ofNullable(pageJson.getUpdatedAt())
				.map(OffsetDateTime::parse)
				.map(OffsetDateTime::toLocalDateTime)
				.orElse(null))
			.withAncestorIds(pageJson.getAncestorIds())
			.build();
	}
}
