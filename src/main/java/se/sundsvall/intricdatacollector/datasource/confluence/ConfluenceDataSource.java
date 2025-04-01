package se.sundsvall.intricdatacollector.datasource.confluence;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.javacrumbs.shedlock.core.DefaultLockManager;
import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.spring.LockableTaskScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.intricdatacollector.core.intric.IntricIntegration;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceClientRegistry;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceIntegrationProperties;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.DbIntegration;

@Service
public class ConfluenceDataSource {

	private static final Logger LOG = LoggerFactory.getLogger(ConfluenceDataSource.class);

	private final Map<String, ConfluenceWorker> workers = new HashMap<>();

	ConfluenceDataSource(final ConfluenceIntegrationProperties properties,
		final ConfluenceDataSourceHealthIndicator healthIndicator,
		final ConfluenceClientRegistry confluenceClientRegistry,
		final ConfluencePageMapper confluencePageMapper,
		final DbIntegration dbIntegration,
		final IntricIntegration intricIntegration,
		final PageJsonParser pageJsonParser,
		final TaskScheduler taskScheduler,
		final LockProvider lockProvider) {
		var executor = new DefaultLockingTaskExecutor(lockProvider);

		properties.environments().forEach((municipalityId, environment) -> {
			// Create a worker for the current environment
			var worker = new ConfluenceWorker(municipalityId, properties, healthIndicator, confluenceClientRegistry, confluencePageMapper, intricIntegration, dbIntegration, pageJsonParser);
			// "Cache" the worker
			workers.put(municipalityId, worker);

			var scheduling = environment.scheduling();

			// Schedule the worker if scheduling is enabled for the current environment
			if (scheduling.enabled()) {
				var lockConfiguration = new LockConfiguration(Instant.now(), "confluence-datasource-lock-" + municipalityId, scheduling.lockAtMostFor(), Duration.ZERO);
				var lockManager = new DefaultLockManager(executor, lockConfigurationExtractor -> Optional.of(lockConfiguration));
				var cronTrigger = new CronTrigger(scheduling.cronExpression());
				var lockableTaskScheduler = new LockableTaskScheduler(taskScheduler, lockManager);

				lockableTaskScheduler.schedule(worker, cronTrigger);

				LOG.info("Scheduling has been enabled for municipalityId {}", municipalityId);
			} else {
				LOG.info("Scheduling is disabled for municipalityId {}", municipalityId);
			}
		});
	}

	public void insertPage(final String municipalityId, final String pageId) {
		getWorker(municipalityId).insertPage(pageId);
	}

	public void updatePage(final String municipalityId, final String pageId) {
		getWorker(municipalityId).updatePage(pageId);
	}

	public void deletePage(final String municipalityId, final String pageId) {
		getWorker(municipalityId).deletePage(pageId);
	}

	ConfluenceWorker getWorker(final String municipalityId) {
		if (!workers.containsKey(municipalityId)) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "No worker for municipalityId " + municipalityId);
		}

		return workers.get(municipalityId);
	}
}
