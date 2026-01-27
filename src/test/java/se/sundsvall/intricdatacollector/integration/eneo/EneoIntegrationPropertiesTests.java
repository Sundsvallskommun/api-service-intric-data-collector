package se.sundsvall.intricdatacollector.integration.eneo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.sundsvall.intricdatacollector.test.annotation.UnitTest;

@UnitTest
@SpringBootTest
class EneoIntegrationPropertiesTests {

	@Autowired
	private EneoIntegrationProperties properties;

	@Test
	void propertiesAreSet() {
		assertThat(properties.baseUrl()).isEqualTo("someBaseUrl");
		assertThat(properties.oauth2()).satisfies(oauth2 -> {
			assertThat(oauth2.tokenUrl()).isEqualTo("someTokenUrl");
			assertThat(oauth2.username()).isEqualTo("someUsername");
			assertThat(oauth2.password()).isEqualTo("somePassword");
		});
		assertThat(properties.connectTimeoutInSeconds()).isEqualTo(123);
		assertThat(properties.readTimeoutInSeconds()).isEqualTo(456);
	}
}
