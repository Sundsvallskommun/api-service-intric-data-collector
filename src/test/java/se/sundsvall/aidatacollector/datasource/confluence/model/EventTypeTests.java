package se.sundsvall.aidatacollector.datasource.confluence.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static se.sundsvall.aidatacollector.datasource.confluence.model.EventType.PAGE_CREATED;
import static se.sundsvall.aidatacollector.datasource.confluence.model.EventType.PAGE_REMOVED;
import static se.sundsvall.aidatacollector.datasource.confluence.model.EventType.PAGE_RESTORED;
import static se.sundsvall.aidatacollector.datasource.confluence.model.EventType.PAGE_UPDATED;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EventTypeTests {

	@Test
	void enumValues() {
		assertThat(EventType.values()).containsExactlyInAnyOrder(PAGE_CREATED, PAGE_UPDATED, PAGE_RESTORED, PAGE_REMOVED);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"PAGE_CREATED", "page_updated", "PAGE_RESTORED", "page_removed", "unknown"
	})
	void fromString(final String s) {
		if ("unknown".equals(s)) {
			assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> EventType.fromString(s))
				.withMessage("Unhandled event type: " + s);
		} else {
			assertThat(EventType.fromString(s)).isNotNull();
		}
	}
}
