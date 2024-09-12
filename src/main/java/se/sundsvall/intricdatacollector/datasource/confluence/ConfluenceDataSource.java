package se.sundsvall.intricdatacollector.datasource.confluence;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static se.sundsvall.intricdatacollector.datasource.confluence.model.EventType.PAGE_CREATED;
import static se.sundsvall.intricdatacollector.datasource.confluence.model.EventType.PAGE_UPDATED;
import static se.sundsvall.intricdatacollector.util.OptionalUtil.peek;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.sundsvall.intricdatacollector.core.intric.IntricIntegration;
import se.sundsvall.intricdatacollector.datasource.AbstractDataSource;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceClientRegistry;
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

    private final ParseContext jsonPathParser;
    private final ConfluenceClientRegistry confluenceClientRegistry;
    private final PageRepository pageRepository;

    private final Map<String, List<String>> blacklistedRootIds;
    private final Map<String, Map<String, String>> mappings;

    ConfluenceDataSource(final ObjectMapper objectMapper,
            final ConfluenceIntegrationProperties properties,
            final ConfluenceClientRegistry confluenceClientRegistry,
            final PageRepository pageRepository,
            final IntricIntegration intricIntegration) {
        super(intricIntegration);

        jsonPathParser = JsonPath.using(Configuration.defaultConfiguration()
            .jsonProvider(new JacksonJsonProvider(objectMapper))
            .mappingProvider(new JacksonMappingProvider(objectMapper))
            .addOptions(Option.SUPPRESS_EXCEPTIONS));

        this.confluenceClientRegistry = confluenceClientRegistry;
        this.pageRepository = pageRepository;

        // Group the black-listed root ids by municipality id
        blacklistedRootIds = properties.environments().entrySet().stream()
            .collect(toMap(Map.Entry::getKey, entry -> ofNullable(entry.getValue().blacklistedRootIds()).orElse(emptyList())));

        // Group the mappings by municipality id, from Confluence root id to Intric group id
        mappings = properties.environments().entrySet().stream()
            .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().mappings().stream()
                .collect(toMap(ConfluenceIntegrationProperties.Environment.Mapping::rootId, ConfluenceIntegrationProperties.Environment.Mapping::intricGroupId))));

        // Do initial import if enabled
        properties.environments().forEach((municipalityId, environment) -> {
            if (environment.doInitialImport()) {
                // Process any trees that don't exist locally
                mappings.get(municipalityId).keySet().stream()
                    //.filter(not(rootId -> pageRepository.existsByIdAndMunicipalityId(rootId, municipalityId)))
                    .forEach(rootId -> {
                        LOG.info("Processing tree with root {} (municipalityId: {})", rootId, municipalityId);

                        processTree(municipalityId, rootId);
                    });
            }
        });
    }

    public void deletePage(final String municipalityId, final String pageId) {
        LOG.info("Deleting page {} (municipalityId: {})", pageId, municipalityId);

        pageRepository.findByIdAndMunicipalityId(pageId, municipalityId).ifPresentOrElse(page -> {
            // Delete the info blob from Intric
            deleteFromIntric(page.getBlobId());

            // Delete the page
            pageRepository.deleteByIdAndMunicipalityId(pageId, municipalityId);

            LOG.info("The page {} was deleted (municipalityId: {})", pageId, municipalityId);
        }, () -> LOG.info("Unable to delete page {} since it couldn't be found (municipalityId: {})", pageId, municipalityId));
    }

    public void processPage(final String municipalityId, final EventType eventType, final String pageId) {
        LOG.info("Processing page {} triggered by a \"{}\" event (municipalityId: {})", pageId, eventType, municipalityId);

        // Get the Confluence client
        var confluenceClient = confluenceClientRegistry.getClient(municipalityId);
        // Get page data from Confluence
        var json = confluenceClient.getContent(pageId);
        // Parse the JSON page data
        var jsonPath = jsonPathParser.parse(json);

        // Extract the page ancestor ids
        var ancestorIds = jsonPath.read(ANCESTOR_IDS, new TypeRef<List<String>>() { });

        // Check if the page has any blacklisted ancestor
        var hasBlacklistedAncestor = ancestorIds.stream()
            .filter(ancestorId -> blacklistedRootIds.get(municipalityId).contains(ancestorId))
            .findFirst()
            .map(peek(prohibitedRootId -> LOG.info("Unable to process page {} as it has a blacklisted ancestor {} (municipalityId: {})", pageId, prohibitedRootId, municipalityId)))
            .isPresent();

        // If the page either is, or has, a blacklisted ancestor - bail out
        if (blacklistedRootIds.get(municipalityId).contains(pageId) || hasBlacklistedAncestor) {
            return;
        }

        // Get the Intric group id by traversing up the page tree
        var intricGroupId = getIntricGroupId(municipalityId, pageId, ancestorIds);

        // If we don't have an Intric group id, we can't really do anything more - bail out
        if (isBlank(intricGroupId)) {
            LOG.info("Unable to map page {} to any Intric group (municipalityId: {})", pageId, municipalityId);

            return;
        }

        // Extract page data
        var title = jsonPath.read(TITLE, String.class);
        var baseUrl = jsonPath.read(BASE_URL, String.class);
        var path = jsonPath.read(PATH, String.class);
        var body = jsonPath.read(BODY, String.class);

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
                    .withMunicipalityId(municipalityId)
                    .withGroupId(intricGroupId)
                    .withBlobId(blobId)
                    .build();
                pageRepository.save(page);

                LOG.info("The page {} was created (municipalityId: {})", pageId, municipalityId);
            }
            case PAGE_UPDATED -> {
                pageRepository.findByIdAndMunicipalityId(pageId, municipalityId).ifPresentOrElse(page -> {
                    // Update the info blob in Intric - since there is no option of actually updating an
                    // info blob since Intric can't update the text embedding, so the old info blob is
                    // deleted and a new one is created
                    var newBlobId = updateInIntric(intricGroupId, page.getBlobId(), title, bodyAsText, url);

                    // Update the page
                    page.setBlobId(newBlobId);
                    pageRepository.save(page);

                    LOG.info("The page {} was updated (municipalityId: {})", pageId, municipalityId);
                }, () -> LOG.info("Unable to update page {} since it couldn't be found (municipalityId: {})", pageId, municipalityId));
            }
            default -> LOG.info("Refusing to process page {} for event type {} (municipalityId: {})", pageId, eventType, municipalityId);
        }
    }

    void processTree(final String municipalityId, final String rootId) {
        // Try to process the page (treating the event type as "created" since this traversal will
        // only be used for manual first-time imports)
        try {
            var eventType = pageRepository.existsByIdAndMunicipalityId(rootId, municipalityId) ? PAGE_UPDATED : PAGE_CREATED;

            processPage(municipalityId, eventType, rootId);

            // Get the Confluence client
            var confluenceClient = confluenceClientRegistry.getClient(municipalityId);
            // Get data from Confluence
            var json = confluenceClient.getChildren(rootId);
            // Parse the JSON page data
            var jsonPath = jsonPathParser.parse(json);

            // Extract the id:s of child pages
            var childPageIds = jsonPath.read(CHILD_IDS, new TypeRef<List<String>>() {});

            // Iterate over the child page id:s
            for (var childPageId : childPageIds) {
                // Process any children of the page
                processTree(municipalityId, childPageId);
            }
        } catch (Exception e) {
            LOG.warn("Unable to process tree with root {} (municipalityId: {})", rootId, municipalityId, e);
        }
    }

    String getIntricGroupId(final String municipalityId, final String pageId, final List<String> ancestorIds) {
        // Check if the page itself is a mapping
        if (mappings.get(municipalityId).containsKey(pageId)) {
            return mappings.get(municipalityId).get(pageId);
        }

        // Otherwise - check the ancestors
        return mappings.get(municipalityId).entrySet().stream()
            .filter(mapping -> ancestorIds.stream()
                .anyMatch(ancestorId -> mapping.getKey().equals(ancestorId)))
            .findFirst()
            .map(Map.Entry::getValue)
            .orElse(null);
    }
}
