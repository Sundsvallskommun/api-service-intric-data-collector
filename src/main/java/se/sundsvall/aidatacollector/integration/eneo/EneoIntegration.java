package se.sundsvall.aidatacollector.integration.eneo;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import generated.se.sundsvall.eneo.InfoBlobAddPublic;
import generated.se.sundsvall.eneo.InfoBlobMetadataUpsertPublic;
import generated.se.sundsvall.eneo.InfoBlobUpsertRequest;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

@Service
public class EneoIntegration {

	private final Map<String, EneoClient> eneoClients;

	EneoIntegration(final Map<String, EneoClient> eneoClients) {
		this.eneoClients = eneoClients;
	}

	public String addInfoBlob(final String municipalityId, final String groupId, final String title, final String body, final String url) {
		final var request = new InfoBlobUpsertRequest()
			.infoBlobs(List.of(
				new InfoBlobAddPublic()
					.metadata(new InfoBlobMetadataUpsertPublic()
						.title(title)
						.url(url))
					.text(body)));

		final var response = getClient(municipalityId).addInfoBlobs(groupId, request);

		return response.getItems().getFirst().getId().toString();
	}

	public String updateInfoBlob(final String municipalityId, final String groupId, final String blobId, final String title, final String body, final String url) {
		// Delete the current info blob
		deleteInfoBlob(municipalityId, blobId);
		// Add a new info blob
		return addInfoBlob(municipalityId, groupId, title, body, url);
	}

	public void deleteInfoBlob(final String municipalityId, final String blobId) {
		getClient(municipalityId).deleteInfoBlob(blobId);
	}

	private EneoClient getClient(final String municipalityId) {
		if (!eneoClients.containsKey(municipalityId)) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "No Eneo client configured for municipalityId " + municipalityId);
		}
		return eneoClients.get(municipalityId);
	}
}
