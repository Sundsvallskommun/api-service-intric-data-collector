package se.sundsvall.intricdatacollector.datasource;

import se.sundsvall.intricdatacollector.core.intric.IntricIntegration;

public abstract class AbstractDataSource {

    private final IntricIntegration intricIntegration;

    protected AbstractDataSource(final IntricIntegration intricIntegration) {
        this.intricIntegration = intricIntegration;
    }

    protected String addToIntric(final String intricGroupId, final String title, final String body, final String url) {
        return intricIntegration.addInfoBlob(intricGroupId, title, body, url);
    }

    protected String updateInIntric(final String intricGroupId, final String blobId, final String title, final String body, final String url) {
        return intricIntegration.updateInfoBlob(intricGroupId, blobId, title, body, url);
    }

    protected void deleteFromIntric(final String blobId) {
        intricIntegration.deleteInfoBlob(blobId);
    }
}
