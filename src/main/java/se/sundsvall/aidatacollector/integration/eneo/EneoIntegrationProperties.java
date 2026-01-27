package se.sundsvall.aidatacollector.integration.eneo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "integration.eneo")
record EneoIntegrationProperties(

	@Valid @NotEmpty Map<String, Municipality> municipalities,

	@Valid @NotNull Oauth2 oauth2,

	@DefaultValue("120") int connectTimeoutInSeconds,

	@DefaultValue("120") int readTimeoutInSeconds) {

	record Municipality(

		@NotBlank String url,

		@NotBlank String apiKey) {
	}

	record Oauth2(

		@NotBlank String tokenUrl,
		@NotBlank String clientId,
		@NotBlank String clientSecret,
		@NotBlank String authorizationGrantType) {
	}
}
