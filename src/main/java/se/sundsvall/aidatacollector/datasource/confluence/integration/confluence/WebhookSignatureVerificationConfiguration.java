package se.sundsvall.aidatacollector.datasource.confluence.integration.confluence;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_256;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
class WebhookSignatureVerificationConfiguration {

	static final String SIGNATURE_HEADER = "x-hub-signature";
	static final String SIGNATURE_PREFIX = "sha256=";
	private static final Logger LOG = LoggerFactory.getLogger(WebhookSignatureVerificationConfiguration.class);

	@Bean
	FilterRegistrationBean<WebhookSignatureVerificationFilter> webhookSignatureVerificationFilter(final ConfluenceIntegrationProperties properties) {
		// Extract the environments where webhook security is enabled
		final var securityEnabledEnvironments = properties.environments().entrySet().stream()
			.filter(entry -> entry.getValue().webhook().security().enabled())
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		// Construct the URL patterns for the webhook security-enabled environments
		final var securityEnabledUrlPatterns = securityEnabledEnvironments.keySet().stream()
			.map("/%s/confluence/webhook-event"::formatted)
			.toList();

		LOG.info("Enabling webhook signature verification for municipality ids {}", securityEnabledEnvironments.keySet());

		// Create the filter registration
		final var filterRegistration = new FilterRegistrationBean<>(
			new WebhookSignatureVerificationFilter(securityEnabledEnvironments));
		filterRegistration.setUrlPatterns(securityEnabledUrlPatterns);
		return filterRegistration;
	}

	static class WebhookSignatureVerificationFilter extends OncePerRequestFilter {

		private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
			.configure(SerializationFeature.INDENT_OUTPUT, false);

		private final Map<String, HmacUtils> hmacUtils = new HashMap<>();

		WebhookSignatureVerificationFilter(final Map<String, ConfluenceIntegrationProperties.Environment> environments) {
			environments.forEach((municipalityId, environment) -> hmacUtils.put(municipalityId, new HmacUtils(HMAC_SHA_256, environment.webhook().security().secret())));
		}

		@Override
		protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws ServletException, IOException {
			// "Extract" the municipality id
			final var pathComponents = request.getRequestURI().split("/");
			if (pathComponents.length == 0) {
				LOG.debug("Skipping webhook signature verification");

				chain.doFilter(request, response);
				return;
			}
			final var municipalityId = pathComponents[1];
			final var sanitizedMunicipalityId = sanitizeForLogging(municipalityId);

			// Get the HMAC utils instance for the provided municipality id, if any
			final var hmacUtilsInstance = hmacUtils.get(municipalityId);
			if (hmacUtilsInstance == null) {
				LOG.debug("Skipping webhook signature verification (municipalityId: {})", sanitizedMunicipalityId);

				chain.doFilter(request, response);
				return;
			}

			// Use an HTTP request-wrapper that allows for the request body to be read more than once
			final var requestWrapper = new BodyCachingHttpServletRequestWrapper(request);
			// Extract the request body
			final var body = new String(requestWrapper.cachedBody, UTF_8);
			// Minify the body
			final var minifiedBody = OBJECT_MAPPER.readTree(body).toString();
			// Get the body HMAC signature
			final var bodySignature = hmacUtilsInstance.hmacHex(minifiedBody);
			// Get the provided webhook HMAC signature header
			final var headerSignature = ofNullable(requestWrapper.getHeader(SIGNATURE_HEADER))
				.map(s -> s.replace(SIGNATURE_PREFIX, ""))
				.orElse("");
			// Verify
			if (!bodySignature.equals(headerSignature)) {
				LOG.info("Webhook signature verification failed (municipalityId: {})", sanitizedMunicipalityId);

				final var problem = ProblemDetail.forStatus(FORBIDDEN);
				problem.setDetail("Webhook signature verification failed");

				response.setStatus(FORBIDDEN.value());
				response.setHeader(CONTENT_TYPE, APPLICATION_PROBLEM_JSON_VALUE);
				response.getWriter().println(OBJECT_MAPPER.writeValueAsString(problem));
				response.flushBuffer();
			} else {
				LOG.debug("Webhook signature verified");

				chain.doFilter(requestWrapper, response);
			}
		}

		static class BodyCachingHttpServletRequestWrapper extends HttpServletRequestWrapper {

			private final byte[] cachedBody;

			BodyCachingHttpServletRequestWrapper(final HttpServletRequest request) throws IOException {
				super(request);

				cachedBody = toByteArray(request.getInputStream());
			}

			@Override
			public ServletInputStream getInputStream() {
				return new BodyCachingServletInputStream(cachedBody);
			}

			@Override
			public BufferedReader getReader() {
				return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cachedBody)));
			}
		}

		static class BodyCachingServletInputStream extends ServletInputStream {

			private final InputStream cachedInputStream;

			BodyCachingServletInputStream(final byte[] bodyToCache) {
				cachedInputStream = new ByteArrayInputStream(bodyToCache);
			}

			@Override
			public boolean isFinished() {
				try {
					return cachedInputStream.available() == 0;
				} catch (IOException _) {
					return false;
				}
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(final ReadListener readListener) {
				throw new UnsupportedOperationException();
			}

			@Override
			public int read() throws IOException {
				return cachedInputStream.read();
			}
		}
	}
}
