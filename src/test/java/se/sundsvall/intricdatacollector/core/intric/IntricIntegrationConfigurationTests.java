package se.sundsvall.intricdatacollector.core.intric;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.web.client.RestClient;

import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.security.Truststore;

import feign.RequestInterceptor;

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

	@Test
	void intricTokenService() {
		var oauth2PropertiesMock = mock(IntricIntegrationProperties.Oauth2.class);
		var restClientMock = mock(RestClient.class);

		when(propertiesMock.oauth2()).thenReturn(oauth2PropertiesMock);

		var configuration = new IntricIntegrationConfiguration();
		var intricTokenService = configuration.intricTokenService(restClientMock, propertiesMock);

		assertThat(intricTokenService).isNotNull();

		verify(propertiesMock, times(2)).oauth2();
		verify(oauth2PropertiesMock).username();
		verify(oauth2PropertiesMock).password();
		verifyNoMoreInteractions(propertiesMock, oauth2PropertiesMock);
	}

	@Test
	void intricTokenRestClient() {
		var oauth2PropertiesMock = mock(IntricIntegrationProperties.Oauth2.class);
		var truststoreMock = mock(Truststore.class);

		when(propertiesMock.oauth2()).thenReturn(oauth2PropertiesMock);

		var configuration = new IntricIntegrationConfiguration();
		var intricTokenRestClient = configuration.intricTokenRestClient(propertiesMock, truststoreMock);

		assertThat(intricTokenRestClient).isNotNull();

		verify(oauth2PropertiesMock).tokenUrl();
		verify(truststoreMock).getSSLContext();
		verifyNoMoreInteractions(oauth2PropertiesMock, truststoreMock);
	}
}
