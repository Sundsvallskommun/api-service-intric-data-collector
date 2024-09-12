package se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceIntegrationConfiguration.CLIENT_ID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.zalando.problem.ThrowableProblem;

@ExtendWith(MockitoExtension.class)
class ConfluenceClientRegistryTests {

    @Mock
    private ApplicationContext applicationContextMock;

    @InjectMocks
    private ConfluenceClientRegistry registry;

    @Test
    void getClient() {
        var municipalityId = "1984";
        var clientBeanName = "%s.%s".formatted(CLIENT_ID, municipalityId);

        when(applicationContextMock.containsBean(clientBeanName)).thenReturn(true);
        when(applicationContextMock.getBean(clientBeanName, ConfluenceClient.class)).thenReturn(new DummyClient());

        assertThat(registry.getClient(municipalityId)).isNotNull();

        verify(applicationContextMock).containsBean(clientBeanName);
        verify(applicationContextMock).getBean(clientBeanName, ConfluenceClient.class);
        verifyNoMoreInteractions(applicationContextMock);
    }

    @Test
    void getClientWhenClientDoesNotExist() {
        var municipalityId = "1984";
        var clientBeanName = "%s.%s".formatted(CLIENT_ID, municipalityId);

        when(applicationContextMock.containsBean(clientBeanName)).thenReturn(false);

        assertThatExceptionOfType(ThrowableProblem.class)
            .isThrownBy(() -> registry.getClient(municipalityId))
            .satisfies(thrownProblem -> {
                assertThat(thrownProblem.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
                assertThat(thrownProblem.getDetail()).startsWith("No Confluence client exists for");
            });

        verify(applicationContextMock).containsBean(clientBeanName);
        verifyNoMoreInteractions(applicationContextMock);
    }

    private class DummyClient implements ConfluenceClient {

        @Override
        public String getContent(final String pageId) {
            return "";
        }

        @Override
        public String getChildren(final String pageId) {
            return "";
        }
    }
}
