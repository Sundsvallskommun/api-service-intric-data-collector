package se.sundsvall.intricdatacollector.datasource.confluence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;
import se.sundsvall.intricdatacollector.core.intric.IntricIntegration;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceClientRegistry;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceIntegrationProperties;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.DbIntegration;

import net.javacrumbs.shedlock.core.DefaultLockManager;
import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.spring.LockableTaskScheduler;

@ExtendWith({ MockitoExtension.class, ResourceLoaderExtension.class })
class ConfluenceDataSourceTests {

    private static final String MUNICIPALITY_ID_1 = "someMunicipalityId";
    private static final String MUNICIPALITY_ID_2 = "someOtherMunicipalityId";
    private static final String PAGE_ID = "somePageId";

    @Mock
    private ConfluenceIntegrationProperties propertiesMock;
    @Mock
    private Map<String, ConfluenceIntegrationProperties.Environment> environmentsMock;
    @Mock
    private ConfluenceIntegrationProperties.Environment environmentMock1;
    @Mock
    private ConfluenceIntegrationProperties.Environment environmentMock2;
    @Mock
    private ConfluenceIntegrationProperties.Environment.Scheduling schedulingMock1;
    @Mock
    private ConfluenceIntegrationProperties.Environment.Scheduling schedulingMock2;
    @Mock
    private JsonUtil jsonUtilMock;
    @Mock
    private ConfluenceClientRegistry confluenceClientRegistryMock;
    @Mock
    private ConfluencePageMapper confluencePageMapperMock;
    @Mock
    private DbIntegration dbIntegrationMock;
    @Mock
    private IntricIntegration intricIntegrationMock;
    @Mock
    private TaskScheduler taskSchedulerMock;
    @Mock
    private LockProvider lockProviderMock;

    private ConfluenceDataSource dataSource;

    @BeforeEach
    void setUp() {
        // Tedious setup for the Map#forEach call...
        doCallRealMethod().when(environmentsMock).forEach(any());
        when(environmentsMock.entrySet()).thenReturn(Map.of(MUNICIPALITY_ID_1, environmentMock1, MUNICIPALITY_ID_2, environmentMock2).entrySet());

        when(propertiesMock.environments()).thenReturn(environmentsMock);

        // First environment, with scheduling enabled
        when(environmentMock1.scheduling()).thenReturn(schedulingMock1);
        when(schedulingMock1.enabled()).thenReturn(true);
        when(schedulingMock1.cronExpression()).thenReturn("* * * * * *");
        when(schedulingMock1.lockAtMostFor()).thenReturn(Duration.ofMinutes(2));

        // Second environment, with scheduling disabled
        when(environmentMock2.scheduling()).thenReturn(schedulingMock2);
        when(schedulingMock2.enabled()).thenReturn(false);
    }

    private ConfluenceDataSource createDataSource() {
        return new ConfluenceDataSource(propertiesMock, confluenceClientRegistryMock, confluencePageMapperMock,
            dbIntegrationMock, intricIntegrationMock,jsonUtilMock, taskSchedulerMock, lockProviderMock);
    }

    @Test
    void creationAndScheduling() {
        try (var defaultLockingTaskExecutorMock = mockConstruction(DefaultLockingTaskExecutor.class);
             var lockConfigurationMock = mockConstruction(LockConfiguration.class);
             var defaultLockManagerMock = mockConstruction(DefaultLockManager.class);
             var cronTriggerMock = mockConstruction(CronTrigger.class);
             var lockableTaskScheduler = mockConstruction(LockableTaskScheduler.class);
             var workerMock = mockConstruction(ConfluenceWorker.class)) {
            // Create the data source
            dataSource = createDataSource();

            // Verify mock interactions
            verify(propertiesMock).environments();
            verify(environmentMock1).scheduling();
            verify(schedulingMock1).enabled();
            verify(schedulingMock1).cronExpression();
            verify(schedulingMock1).lockAtMostFor();
            verify(environmentMock2).scheduling();
            verify(schedulingMock2).enabled();
            verifyNoMoreInteractions(schedulingMock2);

            // Since just one of the environments has scheduling enabled - a single one of each should have been created
            assertThat(defaultLockingTaskExecutorMock.constructed()).hasSize(1);
            assertThat(lockConfigurationMock.constructed()).hasSize(1);
            assertThat(defaultLockManagerMock.constructed()).hasSize(1);
            assertThat(cronTriggerMock.constructed()).hasSize(1);
            assertThat(lockableTaskScheduler.constructed()).hasSize(1);
            // Two workers should have been created (although just one of them is scheduled)
            assertThat(workerMock.constructed()).hasSize(2);
        }
    }

    @Test
    void insertPage() {
        try (var workerMock = mockConstruction(ConfluenceWorker.class)) {
            // Create the data source
            dataSource = createDataSource();

            // Insert the page
            dataSource.insertPage(MUNICIPALITY_ID_1, PAGE_ID);

            // Extract the mocked, constructed worker and verify
            var actualWorker = dataSource.getWorker(MUNICIPALITY_ID_1);
            verify(actualWorker).insertPage(PAGE_ID);
            verifyNoMoreInteractions(actualWorker);
        }
    }

    @Test
    void updatePage() {
        try (var workerMock = mockConstruction(ConfluenceWorker.class)) {
            // Create the data source
            dataSource = createDataSource();

            // Update the page
            dataSource.updatePage(MUNICIPALITY_ID_1, PAGE_ID);

            // Extract the mocked, constructed worker and verify
            var actualWorker = dataSource.getWorker(MUNICIPALITY_ID_1);
            verify(actualWorker).updatePage(PAGE_ID);
            verifyNoMoreInteractions(actualWorker);
        }
    }

    @Test
    void deletePage() {
        try (var workerMock = mockConstruction(ConfluenceWorker.class)) {
            // Create the data source
            dataSource = createDataSource();

            // Delete the page
            dataSource.deletePage(MUNICIPALITY_ID_1, PAGE_ID);

            // Extract the mocked, constructed worker and verify
            var actualWorker = dataSource.getWorker(MUNICIPALITY_ID_1);
            verify(actualWorker).deletePage(PAGE_ID);
            verifyNoMoreInteractions(actualWorker);
        }
    }
}
