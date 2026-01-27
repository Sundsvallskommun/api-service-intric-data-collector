package se.sundsvall.intricdatacollector.integration.eneo;

import java.util.List;
import org.springframework.stereotype.Service;
import se.sundsvall.intricdatacollector.integration.eneo.model.InfoBlobBuilder;
import se.sundsvall.intricdatacollector.integration.eneo.model.InfoBlobsRequestBuilder;
import se.sundsvall.intricdatacollector.integration.eneo.model.MetadataBuilder;

@Service
public class EneoIntegration {

	private final EneoClient eneoClient;

	EneoIntegration(final EneoClient eneoClient) {
		this.eneoClient = eneoClient;
	}

	public String addInfoBlob(final String groupId, final String title, final String body, final String url) {
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

		final var response = eneoClient.addInfoBlobs(groupId, request);

		return response.items().getFirst().id();
	}

	public String updateInfoBlob(final String groupId, final String blobId, final String title, final String body, final String url) {
		// Delete the current info blob
		deleteInfoBlob(blobId);
		// Add a new info blob
		return addInfoBlob(groupId, title, body, url);
	}

	public void deleteInfoBlob(final String blobId) {
		eneoClient.deleteInfoBlob(blobId);
	}
}
