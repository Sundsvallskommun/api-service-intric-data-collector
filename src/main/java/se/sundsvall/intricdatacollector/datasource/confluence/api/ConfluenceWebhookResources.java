package se.sundsvall.intricdatacollector.datasource.confluence.api;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.zalando.problem.Status.FORBIDDEN;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.intricdatacollector.datasource.confluence.ConfluenceDataSource;
import se.sundsvall.intricdatacollector.datasource.confluence.api.model.ConfluenceWebhookData;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceIntegrationProperties;
import se.sundsvall.intricdatacollector.datasource.confluence.model.EventType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Confluence Datasource Resources")
class ConfluenceWebhookResources {

	private final ConfluenceIntegrationProperties properties;
	private final ConfluenceDataSource dataSource;

	ConfluenceWebhookResources(final ConfluenceIntegrationProperties properties, final ConfluenceDataSource dataSource) {
		this.properties = properties;
		this.dataSource = dataSource;
	}

	@Operation(
		summary = "Receives and handles incoming Confluence webhook data",
		responses = @ApiResponse(responseCode = "200", description = "Ok", useReturnTypeSchema = true))
	@PostMapping(
		value = "/{municipalityId}/confluence/webhook-event",
		consumes = APPLICATION_JSON_VALUE,
		produces = ALL_VALUE)
	ResponseEntity<Void> handleWebhookEvent(@PathVariable("municipalityId") @ValidMunicipalityId final String municipalityId,
		@RequestBody @Valid final ConfluenceWebhookData request) {
		// Manually check if webhooks are enabled for the given municipality
		boolean webhookEnabled = ofNullable(properties.environments().get(municipalityId))
			.map(ConfluenceIntegrationProperties.Environment::webhook)
			.map(ConfluenceIntegrationProperties.Environment.Webhook::enabled)
			.orElse(false);
		if (!webhookEnabled) {
			throw Problem.valueOf(FORBIDDEN, "Webhooks are disabled for the requested municipality id");
		}

		var eventType = EventType.fromString(request.eventType());
		var pageId = request.page().id().toString();

		switch (eventType) {
			case PAGE_CREATED, PAGE_RESTORED -> dataSource.insertPage(municipalityId, pageId);
			case PAGE_UPDATED -> dataSource.updatePage(municipalityId, pageId);
			case PAGE_REMOVED -> dataSource.deletePage(municipalityId, pageId);
		}

		return ok().header(CONTENT_TYPE, ALL_VALUE).build();
	}
}
