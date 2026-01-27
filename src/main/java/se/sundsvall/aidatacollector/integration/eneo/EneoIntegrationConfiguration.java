package se.sundsvall.aidatacollector.integration.eneo;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Configuration
@Import(FeignConfiguration.class)
@EnableConfigurationProperties(EneoIntegrationProperties.class)
class EneoIntegrationConfiguration {

	static final String INTEGRATION_NAME = "eneo";

	private final ApplicationContext applicationContext;
	private final EneoIntegrationProperties properties;

	EneoIntegrationConfiguration(final ApplicationContext applicationContext, final EneoIntegrationProperties properties) {
		this.applicationContext = applicationContext;
		this.properties = properties;
	}

	@Bean
	Map<String, EneoClient> eneoClients() {
		final Map<String, EneoClient> eneoClients = new HashMap<>();

		properties.municipalities().forEach((municipalityId, config) -> {
			final var client = createEneoClient(municipalityId, config.apiKey(), config.url());
			eneoClients.put(municipalityId, client);
		});

		return eneoClients;
	}

	private EneoClient createEneoClient(final String municipalityId, final String apiKey, final String url) {
		final var clientId = INTEGRATION_NAME + "-" + municipalityId;
		return new FeignClientBuilder(applicationContext)
			.forType(EneoClient.class, clientId)
			.customize(feignBuilderCustomizer(clientId, apiKey))
			.url(url)
			.build();
	}

	private FeignBuilderCustomizer feignBuilderCustomizer(final String clientId, final String apiKey) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProblemErrorDecoder(clientId))
			.withRequestInterceptor(request -> request.header("api-key", apiKey))
			.withRetryableOAuth2InterceptorForClientRegistration(ClientRegistration
				.withRegistrationId(clientId)
				.tokenUri(properties.oauth2().tokenUrl())
				.clientId(properties.oauth2().clientId())
				.clientSecret(properties.oauth2().clientSecret())
				.authorizationGrantType(new AuthorizationGrantType(properties.oauth2().authorizationGrantType()))
				.build())
			.withRequestTimeoutsInSeconds(properties.connectTimeoutInSeconds(), properties.readTimeoutInSeconds())
			.composeCustomizersToOne();
	}
}
