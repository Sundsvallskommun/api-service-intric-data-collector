package se.sundsvall.aidatacollector.datasource.confluence.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.aidatacollector.datasource.confluence.model.EventType.PAGE_CREATED;

import org.junit.jupiter.api.Test;

class ConfluenceWebhookDataTests {

	@Test
	void creationAndAccessors() {
		final var eventType = PAGE_CREATED.name().toLowerCase();
		final var pageId = 12345L;
		final var userKey = "someUserKey";
		final var timestamp = 1234567L;
		final var updateTrigger = "someUpdateTrigger";

		final var webhookData = new ConfluenceWebhookData(eventType, new ConfluenceWebhookData.Page(pageId), userKey, timestamp, updateTrigger);

		assertThat(webhookData.eventType()).isEqualTo(eventType);
		assertThat(webhookData.page().id()).isEqualTo(pageId);
		assertThat(webhookData.userKey()).isEqualTo(userKey);
		assertThat(webhookData.timestamp()).isEqualTo(timestamp);
		assertThat(webhookData.updateTrigger()).isEqualTo(updateTrigger);
	}
}
