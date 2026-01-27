package se.sundsvall.aidatacollector.integration.eneo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.sundsvall.aidatacollector.test.annotation.UnitTest;

@UnitTest
@SpringBootTest
class EneoIntegrationPropertiesTests {

	@Autowired
	private EneoIntegrationProperties properties;

	@Test
	void propertiesAreSet() {
		assertThat(properties.municipalities()).hasSize(1);
		assertThat(properties.municipalities().get("2281")).satisfies(municipality -> {
			assertThat(municipality.url()).isEqualTo("someUrl");
			assertThat(municipality.apiKey()).isEqualTo("someApiKey");
		});
		assertThat(properties.oauth2()).satisfies(oauth2 -> {
			assertThat(oauth2.tokenUrl()).isEqualTo("someTokenUrl");
			assertThat(oauth2.clientId()).isEqualTo("someClientId");
			assertThat(oauth2.clientSecret()).isEqualTo("someClientSecret");
			assertThat(oauth2.authorizationGrantType()).isEqualTo("client_credentials");
		});
		assertThat(properties.connectTimeoutInSeconds()).isEqualTo(123);
		assertThat(properties.readTimeoutInSeconds()).isEqualTo(456);
	}
}
