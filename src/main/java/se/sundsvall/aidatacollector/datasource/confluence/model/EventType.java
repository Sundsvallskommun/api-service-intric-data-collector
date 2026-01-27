package se.sundsvall.aidatacollector.datasource.confluence.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;

public enum EventType {

	PAGE_CREATED,
	PAGE_UPDATED,
	PAGE_RESTORED,
	PAGE_REMOVED;

	@JsonCreator
	public static EventType fromString(final String eventType) {
		return Arrays.stream(EventType.values())
			.filter(enumInstance -> enumInstance.name().equalsIgnoreCase(eventType))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unhandled event type: " + eventType));
	}
}
