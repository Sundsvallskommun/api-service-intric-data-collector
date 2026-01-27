package se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.WebhookSignatureVerificationConfiguration.SIGNATURE_HEADER;
import static se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.WebhookSignatureVerificationConfiguration.SIGNATURE_PREFIX;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.WebhookSignatureVerificationConfiguration.WebhookSignatureVerificationFilter;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.WebhookSignatureVerificationConfiguration.WebhookSignatureVerificationFilter.BodyCachingHttpServletRequestWrapper;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.WebhookSignatureVerificationConfiguration.WebhookSignatureVerificationFilter.BodyCachingServletInputStream;

@ExtendWith(MockitoExtension.class)
class WebhookSignatureVerificationConfigurationTests {

	private static final String MUNICIPALITY_ID = "1984";
	private static final String SECRET = "superSecret123";

	@Mock
	private ConfluenceIntegrationProperties propertiesMock;

	@Mock
	private HttpServletRequest requestMock;

	@Mock
	private HttpServletResponse responseMock;

	@Mock
	private FilterChain filterChainMock;

	@Test
	void webhookSignatureVerificationFilterRegistration() {
		final var configuration = new WebhookSignatureVerificationConfiguration();

		final var environmentMock = mock(ConfluenceIntegrationProperties.Environment.class);
		final var webhookMock = mock(ConfluenceIntegrationProperties.Environment.Webhook.class);
		final var securityMock = mock(ConfluenceIntegrationProperties.Environment.Webhook.WebhookSecurity.class);

		when(propertiesMock.environments()).thenReturn(Map.of(MUNICIPALITY_ID, environmentMock));
		when(environmentMock.webhook()).thenReturn(webhookMock);
		when(webhookMock.security()).thenReturn(securityMock);
		when(securityMock.enabled()).thenReturn(true);
		when(securityMock.secret()).thenReturn(SECRET);

		final var filterRegistration = configuration.webhookSignatureVerificationFilter(propertiesMock);

		assertThat(filterRegistration).isNotNull();
		assertThat(filterRegistration.getFilter()).isInstanceOf(WebhookSignatureVerificationFilter.class);
		assertThat(filterRegistration.getUrlPatterns()).containsExactly("/" + MUNICIPALITY_ID + "/confluence/webhook-event");
	}

	@Test
	void webhookSignatureVerificationFilterRegistrationWhenSecurityDisabled() {
		final var configuration = new WebhookSignatureVerificationConfiguration();

		final var environmentMock = mock(ConfluenceIntegrationProperties.Environment.class);
		final var webhookMock = mock(ConfluenceIntegrationProperties.Environment.Webhook.class);
		final var securityMock = mock(ConfluenceIntegrationProperties.Environment.Webhook.WebhookSecurity.class);

		when(propertiesMock.environments()).thenReturn(Map.of(MUNICIPALITY_ID, environmentMock));
		when(environmentMock.webhook()).thenReturn(webhookMock);
		when(webhookMock.security()).thenReturn(securityMock);
		when(securityMock.enabled()).thenReturn(false);

		final var filterRegistration = configuration.webhookSignatureVerificationFilter(propertiesMock);

		assertThat(filterRegistration).isNotNull();
		assertThat(filterRegistration.getUrlPatterns()).isEmpty();
	}

	@Test
	void doFilterInternalWithRootPath() throws Exception {
		final var filter = createFilter();

		when(requestMock.getRequestURI()).thenReturn("/");

		filter.doFilterInternal(requestMock, responseMock, filterChainMock);

		verify(filterChainMock).doFilter(requestMock, responseMock);
	}

	@Test
	void doFilterInternalWithUnknownMunicipalityId() throws Exception {
		final var filter = createFilter();

		when(requestMock.getRequestURI()).thenReturn("/unknownMunicipalityId/confluence/webhook-event");

		filter.doFilterInternal(requestMock, responseMock, filterChainMock);

		verify(filterChainMock).doFilter(requestMock, responseMock);
	}

	@Test
	void doFilterInternalWithValidSignature() throws Exception {
		final var filter = createFilter();
		final var body = "{\"key\": \"value\"}";
		final var minifiedBody = "{\"key\":\"value\"}";
		final var signature = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, SECRET).hmacHex(minifiedBody);

		when(requestMock.getRequestURI()).thenReturn("/" + MUNICIPALITY_ID + "/confluence/webhook-event");
		when(requestMock.getInputStream()).thenReturn(createServletInputStream(body));
		when(requestMock.getHeader(SIGNATURE_HEADER)).thenReturn(SIGNATURE_PREFIX + signature);

		filter.doFilterInternal(requestMock, responseMock, filterChainMock);

		verify(filterChainMock).doFilter(any(BodyCachingHttpServletRequestWrapper.class), any(HttpServletResponse.class));
	}

	@Test
	void doFilterInternalWithInvalidSignature() throws Exception {
		final var filter = createFilter();
		final var body = "{\"key\": \"value\"}";
		final var stringWriter = new StringWriter();
		final var printWriter = new PrintWriter(stringWriter);

		when(requestMock.getRequestURI()).thenReturn("/" + MUNICIPALITY_ID + "/confluence/webhook-event");
		when(requestMock.getInputStream()).thenReturn(createServletInputStream(body));
		when(requestMock.getHeader(SIGNATURE_HEADER)).thenReturn(SIGNATURE_PREFIX + "invalidSignature");
		when(responseMock.getWriter()).thenReturn(printWriter);

		filter.doFilterInternal(requestMock, responseMock, filterChainMock);

		verify(responseMock).setStatus(FORBIDDEN.value());
		verify(responseMock).setHeader("Content-Type", "application/problem+json");
		verify(filterChainMock, never()).doFilter(any(), any());

		assertThat(stringWriter.toString()).contains("Webhook signature verification failed");
	}

	@Test
	void doFilterInternalWithMissingSignatureHeader() throws Exception {
		final var filter = createFilter();
		final var body = "{\"key\": \"value\"}";
		final var stringWriter = new StringWriter();
		final var printWriter = new PrintWriter(stringWriter);

		when(requestMock.getRequestURI()).thenReturn("/" + MUNICIPALITY_ID + "/confluence/webhook-event");
		when(requestMock.getInputStream()).thenReturn(createServletInputStream(body));
		when(requestMock.getHeader(SIGNATURE_HEADER)).thenReturn(null);
		when(responseMock.getWriter()).thenReturn(printWriter);

		filter.doFilterInternal(requestMock, responseMock, filterChainMock);

		verify(responseMock).setStatus(FORBIDDEN.value());
		verify(filterChainMock, never()).doFilter(any(), any());
	}

	@Test
	void bodyCachingHttpServletRequestWrapperGetInputStream() throws Exception {
		final var body = "test body content";
		when(requestMock.getInputStream()).thenReturn(createServletInputStream(body));

		final var wrapper = new BodyCachingHttpServletRequestWrapper(requestMock);

		final var inputStream = wrapper.getInputStream();
		final var result = new String(inputStream.readAllBytes());

		assertThat(result).isEqualTo(body);
	}

	@Test
	void bodyCachingHttpServletRequestWrapperGetReader() throws Exception {
		final var body = "test body content";
		when(requestMock.getInputStream()).thenReturn(createServletInputStream(body));

		final var wrapper = new BodyCachingHttpServletRequestWrapper(requestMock);

		final var reader = wrapper.getReader();
		final var result = reader.readLine();

		assertThat(result).isEqualTo(body);
	}

	@Test
	void bodyCachingServletInputStreamIsFinished() throws Exception {
		final var body = "a";
		try (final var inputStream = new BodyCachingServletInputStream(body.getBytes())) {
			assertThat(inputStream.isFinished()).isFalse();
			assertThat(inputStream.read()).isEqualTo('a');
			assertThat(inputStream.isFinished()).isTrue();
		}
	}

	@Test
	void bodyCachingServletInputStreamIsFinishedAfterClose() throws Exception {
		final var body = "test";
		try (final var inputStream = new BodyCachingServletInputStream(body.getBytes())) {
			// Read all bytes and verify finished
			inputStream.readAllBytes();
			assertThat(inputStream.isFinished()).isTrue();
		}
	}

	@Test
	void bodyCachingServletInputStreamIsReady() throws Exception {
		try (final var inputStream = new BodyCachingServletInputStream("test".getBytes())) {
			assertThat(inputStream.isReady()).isTrue();
		}
	}

	@Test
	void bodyCachingServletInputStreamSetReadListenerThrowsUnsupportedOperationException() throws Exception {
		try (final var inputStream = new BodyCachingServletInputStream("test".getBytes())) {
			assertThatThrownBy(() -> inputStream.setReadListener(null))
				.isInstanceOf(UnsupportedOperationException.class);
		}
	}

	@Test
	void bodyCachingServletInputStreamRead() throws Exception {
		final var body = "test";
		try (final var inputStream = new BodyCachingServletInputStream(body.getBytes())) {
			assertThat(inputStream.read()).isEqualTo('t');
			assertThat(inputStream.read()).isEqualTo('e');
			assertThat(inputStream.read()).isEqualTo('s');
			assertThat(inputStream.read()).isEqualTo('t');
			assertThat(inputStream.read()).isEqualTo(-1);
		}
	}

	@Test
	void signatureHeaderConstant() {
		assertThat(SIGNATURE_HEADER).isEqualTo("x-hub-signature");
	}

	@Test
	void signaturePrefixConstant() {
		assertThat(SIGNATURE_PREFIX).isEqualTo("sha256=");
	}

	private WebhookSignatureVerificationFilter createFilter() {
		final var environmentMock = mock(ConfluenceIntegrationProperties.Environment.class);
		final var webhookMock = mock(ConfluenceIntegrationProperties.Environment.Webhook.class);
		final var securityMock = mock(ConfluenceIntegrationProperties.Environment.Webhook.WebhookSecurity.class);

		when(environmentMock.webhook()).thenReturn(webhookMock);
		when(webhookMock.security()).thenReturn(securityMock);
		when(securityMock.secret()).thenReturn(SECRET);

		return new WebhookSignatureVerificationFilter(Map.of(MUNICIPALITY_ID, environmentMock));
	}

	private ServletInputStream createServletInputStream(final String content) {
		return new ServletInputStream() {
			private final ByteArrayInputStream delegate = new ByteArrayInputStream(content.getBytes());

			@Override
			public boolean isFinished() {
				return delegate.available() == 0;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(final jakarta.servlet.ReadListener readListener) {
				// No implementation needed for this test
			}

			@Override
			public int read() {
				return delegate.read();
			}
		};
	}
}
