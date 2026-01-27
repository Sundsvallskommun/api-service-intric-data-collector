package se.sundsvall.intricdatacollector.integration.eneo;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.intricdatacollector.integration.eneo.EneoIntegrationConfiguration.INTEGRATION_NAME;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import javax.net.ssl.SSLContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.web.client.RestClient;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.security.Truststore;
import se.sundsvall.intricdatacollector.integration.eneo.EneoIntegrationProperties.Oauth2;

@ExtendWith(MockitoExtension.class)
class EneoIntegrationConfigurationTests {

	@Mock
	private EneoIntegrationProperties propertiesMock;
	@Mock
	private EneoTokenService tokenServiceMock;

	@Spy
	private FeignMultiCustomizer feignMultiCustomizerSpy;
	@Mock
	private FeignBuilderCustomizer feignBuilderCustomizerMock;

	@Test
	void feignBuilderCustomizer() {
		final var configuration = new EneoIntegrationConfiguration();

		when(propertiesMock.connectTimeoutInSeconds()).thenReturn(123);
		when(propertiesMock.readTimeoutInSeconds()).thenReturn(456);
		when(feignMultiCustomizerSpy.composeCustomizersToOne()).thenReturn(feignBuilderCustomizerMock);
		when(feignMultiCustomizerSpy.withRequestInterceptor(any(RequestInterceptor.class))).thenReturn(feignMultiCustomizerSpy);

		try (final var feignMultiCustomizerMock = mockStatic(FeignMultiCustomizer.class)) {
			feignMultiCustomizerMock.when(FeignMultiCustomizer::create).thenReturn(feignMultiCustomizerSpy);

			final var customizer = configuration.feignBuilderCustomizer(propertiesMock, tokenServiceMock);

			verify(feignMultiCustomizerSpy).withRequestInterceptor(any(RequestInterceptor.class));
			verify(propertiesMock).connectTimeoutInSeconds();
			verify(propertiesMock).readTimeoutInSeconds();
			verify(feignMultiCustomizerSpy).withRequestTimeoutsInSeconds(123, 456);
			verify(feignMultiCustomizerSpy).composeCustomizersToOne();

			assertThat(customizer).isSameAs(feignBuilderCustomizerMock);
		}
	}

	@Test
	void feignBuilderCustomizerRequestInterceptorAddsAuthorizationHeader() {
		final var configuration = new EneoIntegrationConfiguration();
		final var token = "someToken";

		when(propertiesMock.connectTimeoutInSeconds()).thenReturn(123);
		when(propertiesMock.readTimeoutInSeconds()).thenReturn(456);
		when(feignMultiCustomizerSpy.composeCustomizersToOne()).thenReturn(feignBuilderCustomizerMock);
		when(tokenServiceMock.getToken()).thenReturn(token);

		final var requestInterceptorCaptor = ArgumentCaptor.forClass(RequestInterceptor.class);
		when(feignMultiCustomizerSpy.withRequestInterceptor(requestInterceptorCaptor.capture())).thenReturn(feignMultiCustomizerSpy);

		try (final var feignMultiCustomizerMock = mockStatic(FeignMultiCustomizer.class)) {
			feignMultiCustomizerMock.when(FeignMultiCustomizer::create).thenReturn(feignMultiCustomizerSpy);

			configuration.feignBuilderCustomizer(propertiesMock, tokenServiceMock);

			final var requestInterceptor = requestInterceptorCaptor.getValue();
			final var requestTemplate = new RequestTemplate();

			requestInterceptor.apply(requestTemplate);

			assertThat(requestTemplate.headers().get("Authorization")).asInstanceOf(LIST).containsExactly("Bearer " + token);
			verify(tokenServiceMock).getToken();
		}
	}

	@Test
	void eneoTokenService() {
		final var oauth2PropertiesMock = mock(Oauth2.class);
		final var restClientMock = mock(RestClient.class);

		when(propertiesMock.oauth2()).thenReturn(oauth2PropertiesMock);

		final var configuration = new EneoIntegrationConfiguration();
		final var eneoTokenService = configuration.eneoTokenService(restClientMock, propertiesMock);

		assertThat(eneoTokenService).isNotNull();

		verify(propertiesMock, times(2)).oauth2();
		verify(oauth2PropertiesMock).username();
		verify(oauth2PropertiesMock).password();
		verifyNoMoreInteractions(propertiesMock, oauth2PropertiesMock);
	}

	@Test
	void eneoTokenRestClient() throws Exception {
		final var oauth2PropertiesMock = mock(Oauth2.class);
		final var truststoreMock = mock(Truststore.class);

		when(propertiesMock.oauth2()).thenReturn(oauth2PropertiesMock);
		when(truststoreMock.getSSLContext()).thenReturn(SSLContext.getDefault());

		final var configuration = new EneoIntegrationConfiguration();
		final var eneoTokenRestClient = configuration.eneoTokenRestClient(propertiesMock, truststoreMock);

		assertThat(eneoTokenRestClient).isNotNull();

		verify(oauth2PropertiesMock).tokenUrl();
		verify(truststoreMock).getSSLContext();
		verifyNoMoreInteractions(oauth2PropertiesMock, truststoreMock);
	}

	@Test
	void integrationName() {
		assertThat(INTEGRATION_NAME).isEqualTo("eneo");
	}
}
