package se.sundsvall.intricdatacollector.core.intric;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.io.ByteArrayOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.security.Truststore;

@Import(FeignConfiguration.class)
@EnableConfigurationProperties(IntricIntegrationProperties.class)
class IntricIntegrationConfiguration {

	static final String INTEGRATION_NAME = "intric";

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final IntricIntegrationProperties properties,
		final IntricTokenService tokenService) {
		return FeignMultiCustomizer.create()
			// Use a custom OAuth2 request interceptor, since Spring has deprecated the required password grant
			// type, as recommended by IETF (https://datatracker.ietf.org/doc/html/draft-ietf-oauth-security-topics-19#section-2.4)
			.withRequestInterceptor(template -> template.header(AUTHORIZATION, "Bearer " + tokenService.getToken()))
			.withRequestTimeoutsInSeconds(properties.connectTimeoutInSeconds(), properties.readTimeoutInSeconds())
			.composeCustomizersToOne();
	}

	@Bean
	IntricTokenService intricTokenService(final RestClient restClient, final IntricIntegrationProperties properties) {
		return new IntricTokenService(restClient, properties.oauth2().username(), properties.oauth2().password());
	}

	@Bean
	RestClient intricTokenRestClient(final IntricIntegrationProperties properties, final Truststore truststore) {
		var httpClient = HttpClients.custom()
			.setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
				.setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
					.setSslContext(truststore.getSSLContext())
					.build())
				.build())
			.build();

		return RestClient.builder()
			.baseUrl(properties.oauth2().tokenUrl())
			.requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
			.defaultHeader(ACCEPT, APPLICATION_JSON_VALUE)
			.defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
			.defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
				var status = Status.valueOf(response.getStatusCode().value());
				var out = new ByteArrayOutputStream();
				IOUtils.copy(response.getBody(), out);
				var responseBody = out.toString(UTF_8);

				throw Problem.builder()
					.withStatus(INTERNAL_SERVER_ERROR)
					.withDetail("Unable to retrieve access token")
					.withCause(Problem.builder()
						.withStatus(status)
						.withDetail(responseBody)
						.build())
					.build();

				// throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Unable to retrieve access token (" + status + ")");
			})
			.build();
	}
}
