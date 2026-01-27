package se.sundsvall.aidatacollector.integration.eneo;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.aidatacollector.integration.eneo.model.InfoBlobsRequest;
import se.sundsvall.aidatacollector.integration.eneo.model.InfoBlobsResponse;

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
