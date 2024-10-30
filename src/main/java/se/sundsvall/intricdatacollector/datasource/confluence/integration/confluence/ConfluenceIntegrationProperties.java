package se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "integration.confluence")
public record ConfluenceIntegrationProperties(Map<String, Environment> environments) {

	public record Environment(

		@NotBlank String baseUrl,

		@Valid @NotNull BasicAuthentication basicAuth,

		Scheduling scheduling,

		Webhook webhook,

		@DefaultValue List<@NotBlank String> blacklistedRootIds,

		@NotEmpty List<@Valid Mapping> mappings,

		@DefaultValue("5") int connectTimeoutInSeconds,

		@DefaultValue("20") int readTimeoutInSeconds) {

		public record Mapping(
			@NotBlank String intricGroupId,

			@NotBlank String rootId) {}

		public record Scheduling(

			@DefaultValue("true") boolean enabled,

			@NotBlank String cronExpression,

			@DefaultValue("PT2M") Duration lockAtMostFor) {}

		public record Webhook(boolean enabled, WebhookSecurity security) {

			public record WebhookSecurity(String secret, boolean enabled) {}
		}

		public record BasicAuthentication(

			@NotBlank String username,

			@NotBlank String password) {
		}
	}
}
