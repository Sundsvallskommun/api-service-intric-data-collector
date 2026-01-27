package se.sundsvall.aidatacollector.integration.eneo;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.aidatacollector.integration.eneo.model.InfoBlobBuilder;
import se.sundsvall.aidatacollector.integration.eneo.model.InfoBlobsRequestBuilder;
import se.sundsvall.aidatacollector.integration.eneo.model.MetadataBuilder;

@Service
public class EneoIntegration {

	private final Map<String, EneoClient> eneoClients;

	EneoIntegration(final Map<String, EneoClient> eneoClients) {
		this.eneoClients = eneoClients;
	}

	public String addInfoBlob(final String municipalityId, final String groupId, final String title, final String body, final String url) {
		final var request = InfoBlobsRequestBuilder.create()
			.withInfoBlobs(List.of(
				InfoBlobBuilder.create()
					.withMetadata(MetadataBuilder.create()
						.withTitle(title)
						.withUrl(url)
						.build())
					.withText(body)
					.build()))
			.build();

		final var response = getClient(municipalityId).addInfoBlobs(groupId, request);

		return response.items().getFirst().id();
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
