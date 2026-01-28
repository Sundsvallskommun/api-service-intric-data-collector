package se.sundsvall.aidatacollector.integration.eneo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import generated.se.sundsvall.eneo.InfoBlobAddPublic;
import generated.se.sundsvall.eneo.InfoBlobMetadataUpsertPublic;
import generated.se.sundsvall.eneo.InfoBlobPublic;
import generated.se.sundsvall.eneo.InfoBlobUpsertRequest;
import generated.se.sundsvall.eneo.PaginatedResponseInfoBlobPublic;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;

@ExtendWith(MockitoExtension.class)
class EneoIntegrationTests {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private EneoClient clientMock;

	private EneoIntegration eneoIntegration;

	@BeforeEach
	void setUp() {
		eneoIntegration = new EneoIntegration(Map.of(MUNICIPALITY_ID, clientMock));
	}

	@Test
	void addInfoBlob() {
		final var groupId = "someGroupId";
		final var title = "someTitle";
		final var body = "someBody";
		final var url = "someUrl";
		final var itemId = UUID.randomUUID();

		final var request = new InfoBlobUpsertRequest()
			.infoBlobs(List.of(new InfoBlobAddPublic()
				.text(body)
				.metadata(new InfoBlobMetadataUpsertPublic()
					.title(title)
					.url(url))));

		when(clientMock.addInfoBlobs(groupId, request)).thenReturn(new PaginatedResponseInfoBlobPublic()
			.items(List.of(new InfoBlobPublic().id(itemId)))
			.count(1));

		final var response = eneoIntegration.addInfoBlob(MUNICIPALITY_ID, groupId, title, body, url);

		assertThat(response).isEqualTo(itemId.toString());

		verify(clientMock).addInfoBlobs(groupId, request);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void updateInfoBlob() {
		final var groupId = "someGroupId";
		final var title = "someTitle";
		final var body = "someBody";
		final var url = "someUrl";
		final var itemId = UUID.randomUUID();
		final var blobId = "someBlobId";

		when(clientMock.addInfoBlobs(eq(groupId), any(InfoBlobUpsertRequest.class))).thenReturn(new PaginatedResponseInfoBlobPublic()
			.items(List.of(new InfoBlobPublic().id(itemId)))
			.count(1));

		final var response = eneoIntegration.updateInfoBlob(MUNICIPALITY_ID, groupId, blobId, title, body, url);

		assertThat(response).isEqualTo(itemId.toString());

		verify(clientMock).addInfoBlobs(eq(groupId), any(InfoBlobUpsertRequest.class));
		verify(clientMock).deleteInfoBlob(blobId);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void deleteInfoBlob() {
		final var blobId = "someBlobId";

		eneoIntegration.deleteInfoBlob(MUNICIPALITY_ID, blobId);

		verify(clientMock).deleteInfoBlob(blobId);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getClientThrowsForUnknownMunicipality() {
		final var unknownMunicipalityId = "unknown";

		assertThatThrownBy(() -> eneoIntegration.deleteInfoBlob(unknownMunicipalityId, "blobId"))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessageContaining("No Eneo client configured for municipalityId " + unknownMunicipalityId)
			.extracting("status").isEqualTo(INTERNAL_SERVER_ERROR);
	}
}
