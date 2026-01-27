package se.sundsvall.aidatacollector.datasource.confluence.integration.confluence;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.aidatacollector.datasource.confluence.integration.confluence.ConfluenceIntegrationConfiguration.CLIENT_ID;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;

@Component
public class ConfluenceClientRegistry {

	private final ApplicationContext applicationContext;

	ConfluenceClientRegistry(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public ConfluenceClient getClient(final String municipalityId) {
		final var clientBeanName = "%s.%s".formatted(CLIENT_ID, municipalityId);

		if (applicationContext.containsBean(clientBeanName)) {
			return applicationContext.getBean(clientBeanName, ConfluenceClient.class);
		}

		throw Problem.valueOf(INTERNAL_SERVER_ERROR, String.format("No Confluence client exists for municipalityId %s", municipalityId));
	}
}
