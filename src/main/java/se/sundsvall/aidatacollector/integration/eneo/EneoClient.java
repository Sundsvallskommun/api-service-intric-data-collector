package se.sundsvall.aidatacollector.integration.eneo;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import generated.se.sundsvall.eneo.InfoBlobUpsertRequest;
import generated.se.sundsvall.eneo.PaginatedResponseInfoBlobPublic;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface EneoClient {

	@PostMapping(
		value = "/groups/{id}/info-blobs/",
		produces = APPLICATION_JSON_VALUE,
		consumes = APPLICATION_JSON_VALUE)
	PaginatedResponseInfoBlobPublic addInfoBlobs(@PathVariable String id, @RequestBody InfoBlobUpsertRequest infoBlobs);

	@DeleteMapping(
		value = "/info-blobs/{id}/",
		produces = APPLICATION_JSON_VALUE)
	void deleteInfoBlob(@PathVariable String id);

	@GetMapping("/users/me/")
	String getMe();
}
