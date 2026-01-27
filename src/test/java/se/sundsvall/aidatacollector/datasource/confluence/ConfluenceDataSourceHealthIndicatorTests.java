package se.sundsvall.aidatacollector.datasource.confluence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.OUT_OF_SERVICE;
import static org.springframework.boot.actuate.health.Status.UNKNOWN;
import static org.springframework.boot.actuate.health.Status.UP;
import static se.sundsvall.aidatacollector.datasource.confluence.ConfluenceDataSourceHealthIndicator.REASON;
import static se.sundsvall.aidatacollector.datasource.confluence.ConfluenceDataSourceHealthIndicator.RESTRICTED;
import static se.sundsvall.aidatacollector.datasource.confluence.ConfluenceDataSourceHealthIndicator.UNKNOWN_REASON;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

class ConfluenceDataSourceHealthIndicatorTests {

	private final ConfluenceDataSourceHealthIndicator healthIndicator = new ConfluenceDataSourceHealthIndicator();

	static Stream<Arguments> argumentsForIsHealthy() {
		return Stream.of(
			Arguments.of(UP, true),
			Arguments.of(DOWN, false),
			Arguments.of(OUT_OF_SERVICE, false),
			Arguments.of(UNKNOWN, false),
			Arguments.of(RESTRICTED, false));
	}

	@Test
	void healthIsCreatedWithStatusUpWhenHealthIndicatorIsCreated() {
		assertThat(healthIndicator.health()).isNotNull();
		assertThat(healthIndicator.health().getStatus()).isEqualTo(UP);
	}

	@ParameterizedTest
	@MethodSource("argumentsForIsHealthy")
	void isHealthy(final Status status, final boolean expectedResult) {
		final var healthMock = mock(Health.class);
		when(healthMock.getStatus()).thenReturn(status);
		final var healthIndicatorSpy = spy(healthIndicator);
		when(healthIndicatorSpy.health()).thenReturn(healthMock);

		assertThat(healthIndicatorSpy.isHealthy()).isEqualTo(expectedResult);
	}

	@Test
	void resetSetsStatusToUp() {
		healthIndicator.reset();

		assertThat(healthIndicator.health()).isNotNull();
		assertThat(healthIndicator.health().getStatus()).isEqualTo(UP);
	}

	@Test
	void setHealthySetsStatusToUp() {
		healthIndicator.setHealthy();

		assertThat(healthIndicator.health()).isNotNull();
		assertThat(healthIndicator.health().getStatus()).isEqualTo(UP);
	}

	@Test
	void setUnhealthy() {
		healthIndicator.setUnhealthy();

		assertThat(healthIndicator.health()).isNotNull();
		assertThat(healthIndicator.health().getStatus()).isEqualTo(RESTRICTED);
		assertThat(healthIndicator.health().getDetails()).containsEntry(REASON, UNKNOWN_REASON);
	}

	@Test
	void setUnhealthyWithDetail() {
		final var detail = "someDetail";

		healthIndicator.setUnhealthy(detail);

		assertThat(healthIndicator.health()).isNotNull();
		assertThat(healthIndicator.health().getStatus()).isEqualTo(RESTRICTED);
		assertThat(healthIndicator.health().getDetails()).containsEntry(REASON, detail);
	}
}
