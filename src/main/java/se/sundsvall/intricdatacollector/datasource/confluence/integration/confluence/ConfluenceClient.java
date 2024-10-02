package se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Optional;

import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ConfluenceClient {

    @GetMapping(
        value = "/content/{pageId}?expand=version",
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE)
    Optional<String> getContentVersion(@PathVariable("pageId") String pageId);

    @CollectionFormat(feign.CollectionFormat.CSV)
    @GetMapping(
        value = "/content/{pageId}?expand=body.storage,ancestors,version",
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE)
    Optional<String> getContent(@PathVariable("pageId") String pageId);

    @GetMapping(
        value = "/content/{pageId}/child/page",
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE)
    Optional<String> getChildren(@PathVariable("pageId") String pageId);
}
