package se.sundsvall.intricdatacollector.core.intric;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.intricdatacollector.core.intric.IntricIntegrationConfiguration.INTEGRATION_NAME;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import se.sundsvall.intricdatacollector.core.intric.model.InfoBlobsRequest;
import se.sundsvall.intricdatacollector.core.intric.model.InfoBlobsResponse;

@FeignClient(
    name = INTEGRATION_NAME,
    configuration = IntricIntegrationConfiguration.class,
    url = "${integration.intric.base-url}"
)
public interface IntricClient {

    @PostMapping(
        value = "/groups/{groupId}/info-blobs/",
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE)
    InfoBlobsResponse addInfoBlobs(@PathVariable("groupId") String groupId, @RequestBody InfoBlobsRequest infoBlobs);

    @DeleteMapping(
        value = "/info-blobs/{blobId}/",
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE)
    InfoBlobsResponse deleteInfoBlob(@PathVariable("blobId") String blobId);

    @GetMapping("/users/me/")
    String getMe();
}
