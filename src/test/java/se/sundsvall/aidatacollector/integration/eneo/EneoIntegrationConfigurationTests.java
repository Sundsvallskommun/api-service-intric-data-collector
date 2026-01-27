package se.sundsvall.aidatacollector.integration.eneo;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.aidatacollector.integration.eneo.EneoIntegrationConfiguration.INTEGRATION_NAME;

import org.junit.jupiter.api.Test;

class EneoIntegrationConfigurationTests {

	@Test
	void integrationName() {
		assertThat(INTEGRATION_NAME).isEqualTo("eneo");
	}
}
