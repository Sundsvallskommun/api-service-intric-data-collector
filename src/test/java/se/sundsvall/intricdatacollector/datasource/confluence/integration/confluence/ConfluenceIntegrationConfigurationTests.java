package se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceIntegrationConfiguration.INTEGRATION_NAME;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;

import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import feign.RequestInterceptor;

@ExtendWith(MockitoExtension.class)
class ConfluenceIntegrationConfigurationTests {

    @Mock
    private ConfluenceIntegrationProperties.BasicAuthentication basicAuthenticationMock;
    @Mock
    private ConfluenceIntegrationProperties propertiesMock;
    @Spy
    private FeignMultiCustomizer feignMultiCustomizerSpy;
    @Mock
    private FeignBuilderCustomizer feignBuilderCustomizerMock;

    @Test
    void feignBuilderCustomizer() {
        var configuration = new ConfluenceIntegrationConfiguration();

        when(basicAuthenticationMock.username()).thenReturn("someUsername");
        when(basicAuthenticationMock.password()).thenReturn("somePassword");
        when(propertiesMock.basicAuth()).thenReturn(basicAuthenticationMock);
        when(propertiesMock.connectTimeoutInSeconds()).thenReturn(1);
        when(propertiesMock.readTimeoutInSeconds()).thenReturn(2);
        when(feignMultiCustomizerSpy.composeCustomizersToOne()).thenReturn(feignBuilderCustomizerMock);

        try (var feignMultiCustomizerMock = mockStatic(FeignMultiCustomizer.class)) {
            feignMultiCustomizerMock.when(FeignMultiCustomizer::create).thenReturn(feignMultiCustomizerSpy);

            var customizer = configuration.feignBuilderCustomizer(propertiesMock);

            var errorDecoderCaptor = ArgumentCaptor.forClass(ProblemErrorDecoder.class);

            verify(feignMultiCustomizerSpy).withRequestInterceptor(any(RequestInterceptor.class));
            verify(feignMultiCustomizerSpy).withErrorDecoder(errorDecoderCaptor.capture());
            verify(feignMultiCustomizerSpy).withRequestTimeoutsInSeconds(1, 2);
            verify(feignMultiCustomizerSpy).composeCustomizersToOne();
            verify(basicAuthenticationMock).username();
            verify(basicAuthenticationMock).password();
            verify(propertiesMock, times(2)).basicAuth();
            verify(propertiesMock).connectTimeoutInSeconds();
            verify(propertiesMock).readTimeoutInSeconds();

            assertThat(errorDecoderCaptor.getValue()).hasFieldOrPropertyWithValue("integrationName", INTEGRATION_NAME);
            assertThat(customizer).isSameAs(feignBuilderCustomizerMock);
        }
    }
}
