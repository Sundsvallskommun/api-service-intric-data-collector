package se.sundsvall.intricdatacollector.datasource.confluence;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static se.sundsvall.intricdatacollector.util.OptionalUtil.peek;

import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.sundsvall.intricdatacollector.core.intric.IntricIntegration;
import se.sundsvall.intricdatacollector.datasource.AbstractDataSource;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceClient;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceIntegrationProperties;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.PageRepository;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model.PageEntityBuilder;
import se.sundsvall.intricdatacollector.datasource.confluence.model.EventType;

@Service
public class ConfluenceDataSource extends AbstractDataSource {

    private static final Logger LOG = LoggerFactory.getLogger(ConfluenceDataSource.class);

    private static final String TITLE           = "$.title";
    private static final String BASE_URL        = "$._links.base";
    private static final String PATH            = "$._links.webui";
    private static final String BODY            = "$.body.storage.value";
    private static final String CHILD_IDS       = "$.results..id";
    private static final String ANCESTOR_IDS    = "$.ancestors..id";

    private final ConfluenceClient confluenceClient;
    private final PageRepository pageRepository;

    private final List<String> blacklistedRootIds;
    private final Map<String, String> mappings;

    ConfluenceDataSource(final ConfluenceIntegrationProperties properties,
            final ConfluenceClient confluenceClient,
            final PageRepository pageRepository,
            final IntricIntegration intricIntegration) {
        super(intricIntegration);

        this.confluenceClient = confluenceClient;
        this.pageRepository = pageRepository;

        blacklistedRootIds = ofNullable(properties.blacklistedRootIds()).orElse(emptyList());
        // Re-map the mappings from Confluence root id to Intric group id
        mappings = properties.mappings().stream()
            .collect(toMap(ConfluenceIntegrationProperties.Mapping::rootId, ConfluenceIntegrationProperties.Mapping::intricGroupId));

        if (properties.doInitialImport()) {
            // Process any trees that don't exist locally
            mappings.keySet().stream()
                .filter(not(pageRepository::existsById))
                .forEach(rootId -> {
                    LOG.info("Processing tree with root {}", rootId);

                    processTree(rootId);
                });
        }
    }

    public void deletePage(final String pageId) {
        LOG.info("Deleting page {}", pageId);

        pageRepository.findById(pageId).ifPresentOrElse(page -> {
            // Delete the info blob from Intric
            deleteFromIntric(page.getBlobId());

            // Delete the page
            pageRepository.deleteById(pageId);

            LOG.info("The page {} was deleted", pageId);
        }, () -> LOG.info("Unable to delete page {} since it couldn't be found", pageId));
    }

    public void processPage(final EventType eventType, final String pageId) {
        LOG.info("Processing page {} triggered by a \"{}\" event", pageId, eventType);

        // Get page data from Confluence
        var json = confluenceClient.getContent(pageId);

        // Extract the page ancestor ids
        var ancestorIds = JsonPath.compile(ANCESTOR_IDS).<List<String>>read(json);

        // Check if the page has any blacklisted ancestor
        var hasBlacklistedAncestor = ancestorIds.stream()
            .filter(blacklistedRootIds::contains)
            .findFirst()
            .map(peek(prohibitedRootId -> LOG.info("Unable to process page {} as it has a blacklisted ancestor ({})", pageId, prohibitedRootId)))
            .isPresent();

        // If so - bail out
        if (hasBlacklistedAncestor) {
            return;
        }

        // Get the Intric group id by traversing up the page tree
        var intricGroupId = getIntricGroupId(ancestorIds);

        // If we don't have an Intric group id, we can't really do anything more - bail out
        if (isBlank(intricGroupId)) {
            LOG.info("Unable to map page {} to any Intric group", pageId);

            return;
        }

        // Extract page data
        var title = JsonPath.compile(TITLE).<String>read(json);
        var baseUrl = JsonPath.compile(BASE_URL).<String>read(json);
        var path = JsonPath.compile(PATH).<String>read(json);
        var body = JsonPath.compile(BODY).<String>read(json);

        // Strip off any HTML
        var bodyAsText = Jsoup.parse(body).text();
        // Construct the URL
        var url = baseUrl + path;

        switch (eventType) {
            case PAGE_CREATED, PAGE_RESTORED -> {
                // Add an info blob to Intric
                var blobId = addToIntric(intricGroupId, title, bodyAsText, url);

                // Save the page
                var page = PageEntityBuilder.create()
                    .withId(pageId)
                    .withGroupId(intricGroupId)
                    .withBlobId(blobId)
                    .build();
                pageRepository.save(page);

                LOG.info("The page {} was created", pageId);
            }
            case PAGE_UPDATED -> {
                pageRepository.findById(pageId).ifPresentOrElse(page -> {
                    // Update the info blob in Intric - since there is no option of actually updating an
                    // info blob since Intric can't update the text embedding, so the old info blob is
                    // deleted and a new one is created
                    var newBlobId = updateInIntric(intricGroupId, page.getBlobId(), title, bodyAsText, url);

                    // Update the page
                    page.setBlobId(newBlobId);
                    pageRepository.save(page);

                    LOG.info("The page {} was updated", pageId);
                }, () -> LOG.info("Unable to update page {} since it couldn't be found", pageId));
            }
            default -> LOG.info("Refusing to process page {} for event type {}", pageId, eventType);
        }
    }

    void processTree(final String rootId) {
        // Try to process the page (treating the event type as "created" since this traversal will
        // only be used for manual first-time imports)
        try {
            processPage(EventType.PAGE_CREATED, rootId);

            // Get data from Confluence
            var json = confluenceClient.getChildren(rootId);

            // Extract the id:s of child pages
            var childPageIds = JsonPath.compile(CHILD_IDS).<List<String>>read(json);

            // Iterate over the child page id:s
            for (var childPageId : childPageIds) {
                // Process any children of the page
                processTree(childPageId);
            }
        } catch (Exception e) {
            LOG.warn("Unable to process tree with root {}", rootId, e);
        }
    }

    String getIntricGroupId(final List<String> ancestorIds) {
        return mappings.entrySet().stream()
            .filter(mapping -> ancestorIds.stream()
                .anyMatch(ancestorId -> mapping.getKey().equals(ancestorId)))
            .findFirst()
            .map(Map.Entry::getValue)
            .orElse(null);
    }
}
