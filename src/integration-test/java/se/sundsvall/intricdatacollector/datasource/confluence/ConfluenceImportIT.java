package se.sundsvall.intricdatacollector.datasource.confluence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.intricdatacollector.Application;
import se.sundsvall.intricdatacollector.core.intric.IntricIntegration;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.DbIntegration;

@WireMockAppTestSuite(files = "classpath:/ConfluenceImportIT/", classes = Application.class)
class ConfluenceImportIT extends AbstractAppTest {

    @Autowired
    private ConfluenceDataSource confluenceDataSource;

    @MockitoSpyBeanBean
    private IntricIntegration intricIntegrationMock;

    @Autowired
    private DbIntegration dbIntegration;

    @Test
    void test1_fetchFromConfluenceAndInsertInIntric() {
        // Setup Wiremock
        setupCall();

        // Get a Confluence worker for the current municipality id
        var worker = confluenceDataSource.getWorker("1984");

        // Run
        worker.run();

        // There should be five pages stored locally
        assertThat(dbIntegration.getAllPages("1984")).hasSize(5);
        // Five pages should also have been added to Intric
        verify(intricIntegrationMock, times(5)).addInfoBlob(any(), any(), any(), any());
    }
}
