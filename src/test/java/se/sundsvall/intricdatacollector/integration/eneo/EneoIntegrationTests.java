package se.sundsvall.intricdatacollector.integration.eneo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.intricdatacollector.integration.eneo.model.InfoBlobBuilder;
import se.sundsvall.intricdatacollector.integration.eneo.model.InfoBlobsRequest;
import se.sundsvall.intricdatacollector.integration.eneo.model.InfoBlobsRequestBuilder;
import se.sundsvall.intricdatacollector.integration.eneo.model.InfoBlobsResponse;
import se.sundsvall.intricdatacollector.integration.eneo.model.MetadataBuilder;

@ExtendWith(MockitoExtension.class)
class EneoIntegrationTests {

	@Mock
	private EneoClient clientMock;

	@InjectMocks
	private EneoIntegration eneoIntegration;

	@Test
	void addInfoBlob() {
		final var groupId = "someGroupId";
		final var title = "someTitle";
		final var body = "someBody";
		final var url = "someUrl";
		final var itemId = "someItemId";

		final var request = InfoBlobsRequestBuilder.create()
			.withInfoBlobs(List.of(InfoBlobBuilder.create()
				.withText(body)
				.withMetadata(MetadataBuilder.create()
					.withTitle(title)
					.withUrl(url)
					.build())
				.build()))
			.build();

		when(clientMock.addInfoBlobs(groupId, request)).thenReturn(new InfoBlobsResponse(
			List.of(new InfoBlobsResponse.Item(itemId, null, null, null, null, null, null)), 1));

		final var response = eneoIntegration.addInfoBlob(groupId, title, body, url);

		assertThat(response).isEqualTo(itemId);

		verify(clientMock).addInfoBlobs(groupId, request);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void updateInfoBlob() {
		final var groupId = "someGroupId";
		final var title = "someTitle";
		final var body = "someBody";
		final var url = "someUrl";
		final var itemId = "someItemId";
		final var blobId = "someBlobId";

		when(clientMock.addInfoBlobs(eq(groupId), any(InfoBlobsRequest.class))).thenReturn(new InfoBlobsResponse(
			List.of(new InfoBlobsResponse.Item(itemId, null, null, null, null, null, null)), 1));

		final var response = eneoIntegration.updateInfoBlob(groupId, blobId, title, body, url);

		assertThat(response).isEqualTo(itemId);

		verify(clientMock).addInfoBlobs(eq(groupId), any(InfoBlobsRequest.class));
		verify(clientMock).deleteInfoBlob(blobId);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void deleteInfoBlob() {
		final var blobId = "someBlobId";

		eneoIntegration.deleteInfoBlob(blobId);

		verify(clientMock).deleteInfoBlob(blobId);
		verifyNoMoreInteractions(clientMock);
	}
}
