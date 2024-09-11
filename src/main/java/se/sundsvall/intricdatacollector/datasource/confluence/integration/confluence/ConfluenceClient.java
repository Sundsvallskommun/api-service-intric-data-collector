package se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceIntegrationConfiguration.INTEGRATION_NAME;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = INTEGRATION_NAME,
    url = "${integration.confluence.base-url}",
    configuration = ConfluenceIntegrationConfiguration.class
)
public interface ConfluenceClient {

    @GetMapping(
        value = "/content/{pageId}?expand=body.storage,ancestors",
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE)
    String getContent(@PathVariable("pageId") String pageId);

    @GetMapping(
        value = "/content/{pageId}/child/page",
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE)
    String getChildren(@PathVariable("pageId") String pageId);
}
