package se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import feign.Request;
import feign.auth.BasicAuthRequestInterceptor;

@Configuration
@Import(FeignConfiguration.class)
@EnableConfigurationProperties(ConfluenceIntegrationProperties.class)
class ConfluenceIntegrationConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(ConfluenceIntegrationConfiguration.class);

	static final String CLIENT_ID = "confluence";

	@Bean
	static BeanDefinitionRegistryPostProcessor confluenceClientBeanDefinitionRegistryPostProcessor(final ApplicationContext applicationContext) {
		var binder = Binder.get(applicationContext.getEnvironment());
		var properties = binder.bind("integration.confluence", ConfluenceIntegrationProperties.class).get();

		return registry -> properties.environments().forEach((municipalityId, environment) -> {
			var beanName = "%s.%s".formatted(CLIENT_ID, municipalityId);
			var clientName = "%s-%s".formatted(CLIENT_ID, municipalityId);

			var beanDefinition = new GenericBeanDefinition();
			beanDefinition.setBeanClass(ConfluenceClient.class);
			beanDefinition.setInstanceSupplier(() -> new FeignClientBuilder(applicationContext)
				.forType(ConfluenceClient.class, "%s-%s".formatted(CLIENT_ID, municipalityId))
				.url(environment.baseUrl())
				.customize(builder -> builder
					.dismiss404()
					.errorDecoder(new ProblemErrorDecoder(clientName))
					.requestInterceptor(new BasicAuthRequestInterceptor(environment.basicAuth().username(), environment.basicAuth().password()))
					.options(new Request.Options(environment.connectTimeoutInSeconds(), SECONDS, environment.readTimeoutInSeconds(), SECONDS, true))
					.build())
				.build());

			registry.registerBeanDefinition(beanName, beanDefinition);

			LOG.info("Registered Confluence client {} (municipalityId: {})", beanName, municipalityId);
		});
	}
}
