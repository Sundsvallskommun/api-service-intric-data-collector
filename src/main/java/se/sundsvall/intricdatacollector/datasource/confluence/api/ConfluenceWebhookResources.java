package se.sundsvall.intricdatacollector.datasource.confluence.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import se.sundsvall.intricdatacollector.datasource.confluence.ConfluenceDataSource;
import se.sundsvall.intricdatacollector.datasource.confluence.api.model.ConfluenceWebhookData;
import se.sundsvall.intricdatacollector.datasource.confluence.model.EventType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Confluence Datasource Resources")
class ConfluenceWebhookResources {

    private final ConfluenceDataSource dataSource;

    ConfluenceWebhookResources(final ConfluenceDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Operation(
        summary = "Receives and handles incoming Confluence webhook data",
        responses = @ApiResponse(responseCode = "200", description = "Ok", useReturnTypeSchema = true)
    )
    @PostMapping(
        value = "/{municipalityId}/confluence/webhook-event",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    ResponseEntity<Void> handleWebhookEvent(@PathVariable("municipalityId") final String municipalityId, @RequestBody @Valid final ConfluenceWebhookData request) {
        var eventType = EventType.fromString(request.eventType());

        switch (eventType) {
            case PAGE_CREATED, PAGE_UPDATED, PAGE_RESTORED -> dataSource.processPage(municipalityId, eventType, request.page().id().toString());
            case PAGE_REMOVED -> dataSource.deletePage(municipalityId, request.page().id().toString());
        }

        return ok().build();
    }
}
