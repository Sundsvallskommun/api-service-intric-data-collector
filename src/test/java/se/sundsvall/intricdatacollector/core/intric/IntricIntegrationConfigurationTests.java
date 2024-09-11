package se.sundsvall.intricdatacollector.core.intric;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;

import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@ExtendWith(MockitoExtension.class)
class IntricIntegrationConfigurationTests {

    @Mock
    private IntricIntegrationProperties propertiesMock;
    @Mock
    private IntricTokenService tokenServiceMock;

    @Spy
    private FeignMultiCustomizer feignMultiCustomizerSpy;
    @Mock
    private FeignBuilderCustomizer feignBuilderCustomizerMock;

    @Test
    void feignBuilderCustomizer() {
        var configuration = new IntricIntegrationConfiguration();

        var requestInterceptor = new RequestInterceptor() {

            @Override
            public void apply(final RequestTemplate request) {

            }
        };

        when(propertiesMock.connectTimeoutInSeconds()).thenReturn(123);
        when(propertiesMock.readTimeoutInSeconds()).thenReturn(456);
        when(feignMultiCustomizerSpy.composeCustomizersToOne()).thenReturn(feignBuilderCustomizerMock);
        when(feignMultiCustomizerSpy.withRequestInterceptor(any(RequestInterceptor.class))).thenReturn(feignMultiCustomizerSpy);

        try (var feignMultiCustomizerMock = mockStatic(FeignMultiCustomizer.class)) {
            feignMultiCustomizerMock.when(FeignMultiCustomizer::create).thenReturn(feignMultiCustomizerSpy);

            var customizer = configuration.feignBuilderCustomizer(propertiesMock, tokenServiceMock);

            verify(feignMultiCustomizerSpy).withRequestInterceptor(any(RequestInterceptor.class));
            verify(propertiesMock).connectTimeoutInSeconds();
            verify(propertiesMock).readTimeoutInSeconds();
            verify(feignMultiCustomizerSpy).withRequestTimeoutsInSeconds(123, 456);
            verify(feignMultiCustomizerSpy).composeCustomizersToOne();

            assertThat(customizer).isSameAs(feignBuilderCustomizerMock);
        }
    }
}
