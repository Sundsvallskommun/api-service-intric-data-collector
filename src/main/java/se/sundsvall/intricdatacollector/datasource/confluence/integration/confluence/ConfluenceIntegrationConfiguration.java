package se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import feign.auth.BasicAuthRequestInterceptor;

@Import(FeignConfiguration.class)
@EnableConfigurationProperties(ConfluenceIntegrationProperties.class)
class ConfluenceIntegrationConfiguration {

    static final String INTEGRATION_NAME = "confluence";

    @Bean
    FeignBuilderCustomizer feignBuilderCustomizer(final ConfluenceIntegrationProperties properties) {
        return FeignMultiCustomizer.create()
            .withErrorDecoder(new ProblemErrorDecoder(INTEGRATION_NAME))
            .withRequestInterceptor(new BasicAuthRequestInterceptor(properties.basicAuth().username(), properties.basicAuth().password()))
            .withRequestTimeoutsInSeconds(properties.connectTimeoutInSeconds(), properties.readTimeoutInSeconds())
            .composeCustomizersToOne();
    }
}
