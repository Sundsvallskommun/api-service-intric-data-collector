package se.sundsvall.intricdatacollector.datasource.confluence;

import static java.util.Optional.ofNullable;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import se.sundsvall.intricdatacollector.datasource.confluence.model.Page;
import se.sundsvall.intricdatacollector.datasource.confluence.model.PageBuilder;

@Component
class ConfluencePageMapper {

    private final JsonUtil jsonUtil;

    ConfluencePageMapper(final JsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    Page newPage(final String municipalityId, final String pageId) {
        return PageBuilder.create()
            .withPageId(pageId)
            .withMunicipalityId(municipalityId)
            .build();
    }

    Page toPage(final String municipalityId, final String pageId, final String json) {
        var document = jsonUtil.parse(json);

        return PageBuilder.create()
            .withMunicipalityId(municipalityId)
            .withPageId(pageId)
            .withTitle(document.getTitle())
            .withBody(document.getBody())
            .withBaseUrl(document.getBaseUrl())
            .withPath(document.getPath())
            .withUpdatedAt(ofNullable(document.getUpdatedAt())
                .map(OffsetDateTime::parse)
                .map(OffsetDateTime::toLocalDateTime)
                .orElse(null))
            .withAncestorIds(document.getAncestorIds())
            .build();
    }
}
