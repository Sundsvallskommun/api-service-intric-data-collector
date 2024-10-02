package se.sundsvall.intricdatacollector.datasource.confluence;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.TypeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.intricdatacollector.core.intric.IntricIntegration;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceClient;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceClientRegistry;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceIntegrationProperties;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.DbIntegration;
import se.sundsvall.intricdatacollector.datasource.confluence.model.Page;
import se.sundsvall.intricdatacollector.datasource.confluence.model.PageBuilder;

class ConfluenceWorker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ConfluenceWorker.class);

    static final String LAST_UPDATED_AT = "$.version.when";
    static final String TITLE           = "$.title";
    static final String BASE_URL        = "$._links.base";
    static final String PATH            = "$._links.webui";
    static final String BODY            = "$.body.storage.value";
    static final String CHILD_IDS       = "$.results..id";
    static final String ANCESTOR_IDS    = "$.ancestors..id";

    private final String municipalityId;
    private final ParseContext jsonPathParser;
    private final IntricIntegration intricIntegration;
    private final DbIntegration dbIntegration;

    private final Map<String, String> mappings;
    private final List<String> blacklistedRootIds;
    private final ConfluenceClient client;

    ConfluenceWorker(final String municipalityId,
            final ConfluenceIntegrationProperties properties,
            final ParseContext jsonPathParser,
            final ConfluenceClientRegistry confluenceClientRegistry,
            final IntricIntegration intricIntegration,
            final DbIntegration dbIntegration) {
        this.municipalityId = municipalityId;
        this.intricIntegration = intricIntegration;
        this.dbIntegration = dbIntegration;
        this.jsonPathParser = jsonPathParser;

        // Get a Confluence client for the given municipality id
        client = confluenceClientRegistry.getClient(municipalityId);

        // Extract the mappings for the current municipality id, grouping them from Confluence root
        // id to Intric group id
        mappings = properties.environments().get(municipalityId).mappings().stream()
            .collect(toMap(ConfluenceIntegrationProperties.Environment.Mapping::rootId, ConfluenceIntegrationProperties.Environment.Mapping::intricGroupId));

        // Extract the black-listed root ids for the current municipality id
        blacklistedRootIds = properties.environments().get(municipalityId).blacklistedRootIds();
    }

    @Override
    public void run() {
        mappings.keySet().forEach(rootId -> {
            LOG.info("Processing tree with root {} (municipalityId: {})", rootId, municipalityId);

            processTree(rootId);
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
        } catch (Exception e) {
            LOG.warn("Unable to process tree with root {} (municipalityId: {})", pageId, municipalityId, e);
        }
    }

    void processChildren(final String pageId) {
        client.getChildren(pageId).ifPresent(json -> {
            // Parse the JSON page data
            var jsonDocument = jsonPathParser.parse(json);
            // Extract the id:s of child pages
            var childPageIds = jsonDocument.read(CHILD_IDS, new TypeRef<List<String>>() {});

            if (childPageIds.isEmpty()) {
                LOG.info("Page {} has no children (municipalityId: {})", pageId, municipalityId);
            } else {
                LOG.info("Processing children of {}: {} (municipalityId: {})", pageId, childPageIds, municipalityId);

                // Iterate over the child page id:s and process them
                for (var childPageId : childPageIds) {
                    processTree(childPageId);
                }
            }
        });
    }

    void processPage(final String pageId) {
        LOG.info("Processing page {} (municipalityId: {})", pageId, municipalityId);

        // Get the page version data from Confluence
        client.getContentVersion(pageId).ifPresentOrElse(json -> {
            // Parse the JSON page data
            var jsonDocument = jsonPathParser.parse(json);
            // Extract the last updated at timestamp
            var lastUpdatedAtAsString = jsonDocument.read(LAST_UPDATED_AT, String.class);
            var lastUpdatedAtInConfluence = OffsetDateTime.parse(lastUpdatedAtAsString)
                .toLocalDateTime()
                .truncatedTo(SECONDS);
            // Get the current page from the db or create a new one
            var page = dbIntegration.getPage(pageId, municipalityId)
                .orElseGet(() -> PageBuilder.create()
                    .withPageId(pageId)
                    .withMunicipalityId(municipalityId)
                    .build());

            // If no updated-at timestamp is set - insert the page
            // If the updated-at timestamp of the Confluence page is after the locally stored one - update
            // Otherwise - ignore
            if (page.updatedAt() == null) {
                insertPage(pageId);
            } else if (lastUpdatedAtInConfluence.isAfter(page.updatedAt())) {
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
            // Parse the JSON page data
            var jsonDocument = jsonPathParser.parse(json);
            // Extract the page data
            var page = toPage(municipalityId, pageId, jsonDocument);

            // Skip any blacklisted pages
            if (isBlacklisted(page.pageId(), page.ancestorIds())) {
                LOG.info("Unable to process the page {} as it is blacklisted or has a blacklisted ancestor (municipalityId: {})", pageId, municipalityId);

                return null;
            }

            // Get the Intric group id by traversing up the page tree
            var intricGroupId = getIntricGroupId(pageId, page.ancestorIds());
            // If we don't have an Intric group id, we can't really do anything more - bail out
            if (isNotBlank(intricGroupId)) {
                LOG.info("The page {} was matched with the Intric group id {} (municipalityId: {})", pageId, intricGroupId, municipalityId);
            } else {
                LOG.info("The page {} couldn't be matched with any Intric group (municipalityId: {})", pageId, municipalityId);

                return null;
            }

            return PageBuilder.from(page)
                .withIntricGroupId(intricGroupId)
                .build();
        });
    }

    void insertPage(final String pageId) {
        // Get the page
        getPageFromConfluence(pageId).ifPresentOrElse(page -> {
            // Add an info blob to Intric
            var blobId = intricIntegration.addInfoBlob(page.intricGroupId(), page.title(), page.bodyAsText(), page.url());

            // Save the page
            dbIntegration.savePage(PageBuilder.from(page).withIntricBlobId(blobId).build());

            LOG.info("The page {} has been inserted (municipalityId: {})", pageId, municipalityId);
        }, () -> LOG.info("Unable to insert the page {} since it couldn't be found in Confluence (municipalityId: {})", pageId, municipalityId));
    }

    void updatePage(final String pageId) {
        // Get the page
        getPageFromConfluence(pageId).ifPresentOrElse(page -> {
            dbIntegration.getBlobId(pageId, municipalityId).ifPresentOrElse(blobId -> {
                // Update the info blob in Intric
                var newBlobId = intricIntegration.updateInfoBlob(page.intricGroupId(), blobId, page.title(), page.bodyAsText(), page.url());
                // Save (update) the page
                dbIntegration.savePage(PageBuilder.from(page).withIntricBlobId(newBlobId).build());

                LOG.info("The page {} has been updated (municipalityId: {})", pageId, municipalityId);
            }, () -> LOG.info("Unable to update the page {} since no Intric blob id could be found (municipalityId: {})", pageId, municipalityId));
        }, () -> LOG.info("Unable to update the page {} since it couldn't be found in Confluence (municipalityId: {})", pageId, municipalityId));
    }

    void deletePage(final String pageId) {
        LOG.info("Deleting page {} (municipalityId: {})", pageId, municipalityId);

        dbIntegration.getBlobId(pageId, municipalityId).ifPresentOrElse(blobId -> {
            // Delete the info blob from Intric
            intricIntegration.deleteInfoBlob(blobId);

            // Delete the page
            dbIntegration.deletePage(pageId, municipalityId);

            LOG.info("The page {} was deleted (municipalityId: {})", pageId, municipalityId);
        }, () -> LOG.info("Unable to delete page {} since it couldn't be found (municipalityId: {})", pageId, municipalityId));
    }

    boolean isBlacklisted(final String pageId) {
        return isBlacklisted(pageId, List.of());
    }

    boolean isBlacklisted(final String pageId, final List<String> ancestorIds) {
        // Check if the page itself is blacklisted
        var isBlacklisted = blacklistedRootIds.contains(pageId);

        // Check if the page has any blacklisted ancestor
        var hasBlacklistedAncestor = ancestorIds.stream()
            .anyMatch(blacklistedRootIds::contains);

        return isBlacklisted || hasBlacklistedAncestor;
    }

    String getIntricGroupId(final String pageId, final List<String> ancestorIds) {
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

    Page toPage(final String municipalityId, final String pageId, final DocumentContext jsonDocument) {
        return PageBuilder.create()
            .withMunicipalityId(municipalityId)
            .withPageId(pageId)
            .withTitle(jsonDocument.read(TITLE, String.class))
            .withBody(jsonDocument.read(BODY, String.class))
            .withBaseUrl(jsonDocument.read(BASE_URL, String.class))
            .withPath(jsonDocument.read(PATH, String.class))
            .withUpdatedAt(ofNullable(jsonDocument.read(LAST_UPDATED_AT, String.class))
                .map(OffsetDateTime::parse)
                .map(OffsetDateTime::toLocalDateTime)
                .orElse(null))
            .withAncestorIds(jsonDocument.read(ANCESTOR_IDS))
            .build();
    }
}
