package se.sundsvall.intricdatacollector.integration.eneo;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.intricdatacollector.integration.eneo.EneoIntegrationConfiguration.INTEGRATION_NAME;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.intricdatacollector.integration.eneo.model.InfoBlobsRequest;
import se.sundsvall.intricdatacollector.integration.eneo.model.InfoBlobsResponse;

@FeignClient(
	name = INTEGRATION_NAME,
	configuration = EneoIntegrationConfiguration.class,
	url = "${integration.eneo.base-url}")
@CircuitBreaker(name = INTEGRATION_NAME)
public interface EneoClient {

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
