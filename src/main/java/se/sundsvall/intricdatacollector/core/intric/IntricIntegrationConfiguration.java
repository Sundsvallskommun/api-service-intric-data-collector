package se.sundsvall.intricdatacollector.core.intric;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.zalando.problem.Problem;

import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;

@Import(FeignConfiguration.class)
@EnableConfigurationProperties(IntricIntegrationProperties.class)
class IntricIntegrationConfiguration {

    static final String INTEGRATION_NAME = "intric";

    @Bean
    FeignBuilderCustomizer feignBuilderCustomizer(final IntricIntegrationProperties properties,
            final IntricTokenService tokenService) {
        return FeignMultiCustomizer.create()
            .withRequestInterceptor(template -> template.header(AUTHORIZATION, "Bearer " + tokenService.getToken()))
            .withRequestTimeoutsInSeconds(properties.connectTimeoutInSeconds(), properties.readTimeoutInSeconds())
            .composeCustomizersToOne();
    }

    @Bean
    IntricTokenService intricTokenService(final RestClient restClient, final IntricIntegrationProperties properties) {
        return new IntricTokenService(restClient, properties.oauth2().username(), properties.oauth2().password());
    }

    @Bean
    RestClient intricTokenRestClient(final IntricIntegrationProperties properties) {
        return RestClient.builder()
            .baseUrl(properties.oauth2().tokenUrl())
            .defaultHeader(ACCEPT, APPLICATION_JSON_VALUE)
            .defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
            .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Unable to retrieve access token");
            })
            .build();
    }
}
