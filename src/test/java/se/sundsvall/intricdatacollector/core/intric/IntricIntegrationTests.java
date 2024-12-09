package se.sundsvall.intricdatacollector.core.intric;

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
import se.sundsvall.intricdatacollector.core.intric.model.InfoBlobBuilder;
import se.sundsvall.intricdatacollector.core.intric.model.InfoBlobsRequest;
import se.sundsvall.intricdatacollector.core.intric.model.InfoBlobsRequestBuilder;
import se.sundsvall.intricdatacollector.core.intric.model.InfoBlobsResponse;
import se.sundsvall.intricdatacollector.core.intric.model.MetadataBuilder;

@ExtendWith(MockitoExtension.class)
class IntricIntegrationTests {

	@Mock
	private IntricClient clientMock;

	@InjectMocks
	private IntricIntegration intricIntegration;

	@Test
	void addInfoBlob() {
		var groupId = "someGroupId";
		var title = "someTitle";
		var body = "someBody";
		var url = "someUrl";
		var itemId = "someItemId";

		var request = InfoBlobsRequestBuilder.create()
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

		var response = intricIntegration.addInfoBlob(groupId, title, body, url);

		assertThat(response).isEqualTo(itemId);

		verify(clientMock).addInfoBlobs(groupId, request);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void updateInfoBlob() {
		var groupId = "someGroupId";
		var title = "someTitle";
		var body = "someBody";
		var url = "someUrl";
		var itemId = "someItemId";
		var blobId = "someBlobId";

		when(clientMock.addInfoBlobs(eq(groupId), any(InfoBlobsRequest.class))).thenReturn(new InfoBlobsResponse(
			List.of(new InfoBlobsResponse.Item(itemId, null, null, null, null, null, null)), 1));

		var response = intricIntegration.updateInfoBlob(groupId, blobId, title, body, url);

		assertThat(response).isEqualTo(itemId);

		verify(clientMock).addInfoBlobs(eq(groupId), any(InfoBlobsRequest.class));
		verify(clientMock).deleteInfoBlob(blobId);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void deleteInfoBlob() {
		var blobId = "someBlobId";

		intricIntegration.deleteInfoBlob(blobId);

		verify(clientMock).deleteInfoBlob(blobId);
		verifyNoMoreInteractions(clientMock);
	}
}
