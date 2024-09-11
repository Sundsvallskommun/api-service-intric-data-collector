package se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import se.sundsvall.intricdatacollector.test.annotation.UnitTest;

@ExtendWith(MockitoExtension.class)
class WebhookSignatureVerificationConfigurationTests {

    @Test
    void webhookSignatureVerificationFilter() {
        var webhookSecurityMock = mock(ConfluenceIntegrationProperties.WebhookSecurity.class);
        when(webhookSecurityMock.secret()).thenReturn("s0m3s3cr3t");
        var propertiesMock = mock(ConfluenceIntegrationProperties.class);
        when(propertiesMock.webhookSecurity()).thenReturn(webhookSecurityMock);

        var filterRegistrationBean = new WebhookSignatureVerificationConfiguration().webhookSignatureVerificationFilter(propertiesMock);

        assertThat(filterRegistrationBean.getFilter()).isInstanceOf(WebhookSignatureVerificationConfiguration.WebhookSignatureVerificationFilter.class);
        assertThat(filterRegistrationBean.getUrlPatterns()).containsExactly("/confluence/webhook-event");

        verify(propertiesMock).webhookSecurity();
        verify(webhookSecurityMock).secret();
        verifyNoMoreInteractions(propertiesMock, webhookSecurityMock);
    }

    @TestConfiguration
    @EnableConfigurationProperties(ConfluenceIntegrationProperties.class)
    static class DummyConfluenceConfiguration { }

    @Nested
    @UnitTest
    @SpringBootTest(
        classes = { DummyConfluenceConfiguration.class, WebhookSignatureVerificationConfiguration.class },
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
    @EnableConfigurationProperties(ConfluenceIntegrationProperties.class)
    class ConfigurationPropertyExplicitlyEnabled {

        @Autowired
        private FilterRegistrationBean<WebhookSignatureVerificationConfiguration.WebhookSignatureVerificationFilter> filterRegistrationBean;

        @Test
        void filterRegistrationBeanShouldBeCreated() {
            assertThat(filterRegistrationBean).isNotNull();
        }
    }

    @Nested
    @SpringBootTest(
        classes = { DummyConfluenceConfiguration.class, WebhookSignatureVerificationConfiguration.class },
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
    class ConfigurationPropertyMissing {

        @Autowired
        private FilterRegistrationBean<WebhookSignatureVerificationConfiguration.WebhookSignatureVerificationFilter> filterRegistrationBean;

        @Test
        void filterRegistrationBeanShouldBeCreated() {
            assertThat(filterRegistrationBean).isNotNull();
        }
    }

    @Nested
    @SpringBootTest(
        classes = { DummyConfluenceConfiguration.class, WebhookSignatureVerificationConfiguration.class },
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = { "integration.confluence.webhook-security.enabled=false" })
    class ConfigurationPropertyExplicitlyDisabled {

        @Autowired(required = false)
        private FilterRegistrationBean<WebhookSignatureVerificationConfiguration.WebhookSignatureVerificationFilter> filterRegistrationBean;

        @Test
        void noFilterRegistrationBeanShouldBeCreated() {
            assertThat(filterRegistrationBean).isNull();
        }
    }
}
