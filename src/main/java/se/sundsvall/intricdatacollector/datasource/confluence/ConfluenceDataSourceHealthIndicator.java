package se.sundsvall.intricdatacollector.datasource.confluence;

import static java.util.Optional.ofNullable;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

@Component
class ConfluenceDataSourceHealthIndicator implements HealthIndicator {

	static final Status RESTRICTED = new Status("RESTRICTED");
	static final String REASON = "Reason";
	static final String UNKNOWN_REASON = "Unknown";

	private Health health = Health.up().build();

	@Override
	public Health health() {
		return health;
	}

	boolean isHealthy() {
		return health().getStatus().equals(Status.UP);
	}

	void reset() {
		setHealthy();
	}

	void setHealthy() {
		health = Health.up().build();
	}

	void setUnhealthy() {
		setUnhealthy(null);
	}

	void setUnhealthy(final String detail) {
		health = Health.status(RESTRICTED)
			.withDetail(REASON, ofNullable(detail).orElse(UNKNOWN_REASON))
			.build();
	}
}
