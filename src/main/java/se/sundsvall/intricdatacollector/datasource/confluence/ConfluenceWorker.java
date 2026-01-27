package se.sundsvall.intricdatacollector.datasource.confluence;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceClient;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceClientRegistry;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceIntegrationProperties;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.DbIntegration;
import se.sundsvall.intricdatacollector.datasource.confluence.model.Page;
import se.sundsvall.intricdatacollector.datasource.confluence.model.PageBuilder;
import se.sundsvall.intricdatacollector.integration.eneo.EneoIntegration;

@Transactional
class ConfluenceWorker implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(ConfluenceWorker.class);

	private final String municipalityId;
	private final ConfluenceDataSourceHealthIndicator dataSourceHealthIndicator;
	private final EneoIntegration eneoIntegration;
	private final DbIntegration dbIntegration;
	private final PageJsonParser pageJsonParser;

	private final Map<String, String> mappings;
	private final List<String> blacklistedRootIds;
	private final ConfluenceClient client;
	private final ConfluencePageMapper pageMapper;

	ConfluenceWorker(final String municipalityId,
		final ConfluenceIntegrationProperties properties,
		final ConfluenceDataSourceHealthIndicator dataSourceHealthIndicator,
		final ConfluenceClientRegistry confluenceClientRegistry,
		final ConfluencePageMapper pageMapper,
		final EneoIntegration eneoIntegration,
		final DbIntegration dbIntegration,
		final PageJsonParser pageJsonParser) {
		this.municipalityId = municipalityId;
		this.dataSourceHealthIndicator = dataSourceHealthIndicator;
		this.pageMapper = pageMapper;
		this.eneoIntegration = eneoIntegration;
		this.dbIntegration = dbIntegration;
		this.pageJsonParser = pageJsonParser;

		// Get a Confluence client for the given municipality id
		client = confluenceClientRegistry.getClient(municipalityId);

		// Extract the mappings for the current municipality id, grouping them from Confluence root
		// id to Eneo group id
		mappings = properties.environments().get(municipalityId).mappings().stream()
			.collect(toMap(ConfluenceIntegrationProperties.Environment.Mapping::rootId, ConfluenceIntegrationProperties.Environment.Mapping::eneoGroupId));

		// Extract the black-listed root ids for the current municipality id
		blacklistedRootIds = properties.environments().get(municipalityId).blacklistedRootIds();
	}

	@Override
	public void run() {
		mappings.keySet().forEach(rootId -> {
			try {
				RequestId.init();
				LOG.info("Processing tree with root {} (municipalityId: {})", rootId, municipalityId);
				dataSourceHealthIndicator.reset();

				processTree(rootId);
			} finally {
				LOG.info("Finished processing tree with root {} (municipalityId: {})", rootId, municipalityId);

				RequestId.reset();
			}
		});
	}

	void processTree(final String pageId) {
		try {
			if (isBlacklisted(pageId)) {
				LOG.info("Skipping page {} (and any children) as it is blacklisted (municipalityId: {})", pageId, municipalityId);
			} else {
				// Process the page
				processPage(pageId);
				// Process any children of the page
				processChildren(pageId);
			}
		} catch (final Exception e) {
			dataSourceHealthIndicator.setUnhealthy("Error processing tree with root %s (municipalityId: %s): %s".formatted(pageId, municipalityId, e.getMessage()));

			LOG.warn("Unable to process tree with root {} (municipalityId: {})", pageId, municipalityId, e);
		}
	}

	void processChildren(final String pageId) {
		client.getChildren(pageId).ifPresent(json -> {
			// Extract the id:s of child pages
			final var childPageIds = pageJsonParser.parse(json).getChildIds();

			if (childPageIds.isEmpty()) {
				LOG.info("Page {} has no children (municipalityId: {})", pageId, municipalityId);
			} else {
				LOG.info("Processing children of {}: {} (municipalityId: {})", pageId, childPageIds, municipalityId);

				// Iterate over the child page id:s and process them
				for (final var childPageId : childPageIds) {
					processTree(childPageId);
				}
			}
		});
	}

	void processPage(final String pageId) {
		LOG.info("Processing page {} (municipalityId: {})", pageId, municipalityId);

		// Get the page version data from Confluence
		client.getContentVersion(pageId).ifPresentOrElse(json -> {
			// Extract the updated at timestamp
			final var updatedAtInConfluenceAsString = pageJsonParser.parse(json).getUpdatedAt();
			final var updatedAtInConfluence = OffsetDateTime.parse(updatedAtInConfluenceAsString)
				.toLocalDateTime()
				.truncatedTo(SECONDS);
			// Get the current page from the db or create a new one
			final var page = dbIntegration.getPage(pageId, municipalityId)
				.orElseGet(() -> pageMapper.newPage(municipalityId, pageId));

			// If no updated-at timestamp is set - insert the page
			// If the updated-at timestamp of the Confluence page is after the locally stored one - update
			// Otherwise - ignore
			if (page.updatedAt() == null) {
				insertPage(pageId);
			} else if (updatedAtInConfluence.isAfter(page.updatedAt())) {
				updatePage(pageId);
			} else {
				LOG.info("Not updating current page {} (municipalityId: {})", pageId, municipalityId);
			}
		}, () -> {
			LOG.info("Page {} was not found in Confluence (municipalityId: {})", pageId, municipalityId);

			deletePage(pageId);
		});
	}

	Optional<Page> getPageFromConfluence(final String pageId) {
		// Get page data from Confluence
		return client.getContent(pageId).map(json -> {
			// Extract the page data
			final var page = pageMapper.toPage(municipalityId, pageId, json);

			// Skip any blacklisted pages
			if (isBlacklisted(page.pageId(), page.ancestorIds())) {
				LOG.info("Unable to process the page {} as it is blacklisted or has a blacklisted ancestor (municipalityId: {})", pageId, municipalityId);

				return null;
			}

			// Get the Eneo group id by traversing up the page tree
			final var eneoGroupId = getEneoGroupId(pageId, page.ancestorIds());
			// If we don't have an Eneo group id, we can't really do anything more - bail out
			if (isNotBlank(eneoGroupId)) {
				LOG.info("The page {} was matched with the Eneo group id {} (municipalityId: {})", pageId, eneoGroupId, municipalityId);
			} else {
				LOG.info("The page {} couldn't be matched with any Eneo group (municipalityId: {})", pageId, municipalityId);

				return null;
			}

			return PageBuilder.from(page)
				.withEneoGroupId(eneoGroupId)
				.build();
		});
	}

	void insertPage(final String pageId) {
		// Get the page
		getPageFromConfluence(pageId).ifPresentOrElse(page -> {
			// Add an info blob to Eneo
			final var blobId = eneoIntegration.addInfoBlob(page.eneoGroupId(), page.title(), page.bodyAsText(), page.url());

			// Save the page
			dbIntegration.savePage(PageBuilder.from(page).withEneoBlobId(blobId).build());

			LOG.info("The page {} has been inserted (municipalityId: {})", pageId, municipalityId);
		}, () -> LOG.info("Unable to insert the page {} since it couldn't be found in Confluence (municipalityId: {})", pageId, municipalityId));
	}

	void updatePage(final String pageId) {
		// Get the page
		getPageFromConfluence(pageId).ifPresentOrElse(page -> dbIntegration.getBlobId(pageId, municipalityId).ifPresentOrElse(blobId -> {
			// Update the info blob in Eneo
			final var newBlobId = eneoIntegration.updateInfoBlob(page.eneoGroupId(), blobId, page.title(), page.bodyAsText(), page.url());
			// Save (update) the page
			dbIntegration.savePage(PageBuilder.from(page).withEneoBlobId(newBlobId).build());

			LOG.info("The page {} has been updated (municipalityId: {})", pageId, municipalityId);
		}, () -> LOG.info("Unable to update the page {} since no Eneo blob id could be found (municipalityId: {})", pageId, municipalityId)),
			() -> LOG.info("Unable to update the page {} since it couldn't be found in Confluence (municipalityId: {})", pageId, municipalityId));
	}

	void deletePage(final String pageId) {
		LOG.info("Deleting page {} (municipalityId: {})", pageId, municipalityId);

		dbIntegration.getBlobId(pageId, municipalityId).ifPresentOrElse(blobId -> {
			// Delete the page
			dbIntegration.deletePage(pageId, municipalityId);

			// Delete the info blob from Eneo
			eneoIntegration.deleteInfoBlob(blobId);

			LOG.info("The page {} was deleted (municipalityId: {})", pageId, municipalityId);
		}, () -> LOG.info("Unable to delete page {} since it couldn't be found (municipalityId: {})", pageId, municipalityId));
	}

	boolean isBlacklisted(final String pageId) {
		return isBlacklisted(pageId, List.of());
	}

	boolean isBlacklisted(final String pageId, final List<String> ancestorIds) {
		// Check if the page itself is blacklisted
		final var isBlacklisted = blacklistedRootIds.contains(pageId);

		// Check if the page has any blacklisted ancestor
		final var hasBlacklistedAncestor = ancestorIds.stream()
			.anyMatch(blacklistedRootIds::contains);

		return isBlacklisted || hasBlacklistedAncestor;
	}

	String getEneoGroupId(final String pageId, final List<String> ancestorIds) {
		// Check if the page itself *is* a mapping
		if (mappings.containsKey(pageId)) {
			return mappings.get(pageId);
		}

		// Otherwise - check the ancestors
		return mappings.entrySet().stream()
			.filter(mapping -> ancestorIds.stream()
				.anyMatch(ancestorId -> mapping.getKey().equals(ancestorId)))
			.findFirst()
			.map(Map.Entry::getValue)
			.orElse(null);
	}
}
