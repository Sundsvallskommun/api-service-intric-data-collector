package se.sundsvall.intricdatacollector.core.intric;

import java.util.List;

import org.springframework.stereotype.Service;

import se.sundsvall.intricdatacollector.core.intric.model.InfoBlobBuilder;
import se.sundsvall.intricdatacollector.core.intric.model.InfoBlobsRequestBuilder;
import se.sundsvall.intricdatacollector.core.intric.model.MetadataBuilder;

@Service
public class IntricIntegration {

    private final IntricClient intricClient;

    IntricIntegration(final IntricClient intricClient) {
        this.intricClient = intricClient;
    }

    public String addInfoBlob(final String groupId, final String title, final String body, final String url) {
        var request = InfoBlobsRequestBuilder.create()
            .withInfoBlobs(List.of(
                InfoBlobBuilder.create()
                    .withMetadata(MetadataBuilder.create()
                        .withTitle(title)
                        .withUrl(url)
                        .build())
                    .withText(body)
                    .build()))
            .build();

        var response = intricClient.addInfoBlobs(groupId, request);

        return response.items().getFirst().id();
    }

    public String updateInfoBlob(final String groupId, final String blobId, final String title, final String body, final String url) {
        // Delete the current info blob
        deleteInfoBlob(blobId);
        // Add a new info blob
        return addInfoBlob(groupId, title, body, url);
    }

    public void deleteInfoBlob(final String blobId) {
        intricClient.deleteInfoBlob(blobId);
    }
}
