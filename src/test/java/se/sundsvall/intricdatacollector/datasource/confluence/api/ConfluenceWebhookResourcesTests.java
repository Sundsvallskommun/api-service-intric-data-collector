package se.sundsvall.intricdatacollector.datasource.confluence.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static se.sundsvall.intricdatacollector.datasource.confluence.model.EventType.PAGE_CREATED;
import static se.sundsvall.intricdatacollector.datasource.confluence.model.EventType.PAGE_REMOVED;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import se.sundsvall.intricdatacollector.Application;
import se.sundsvall.intricdatacollector.datasource.confluence.ConfluenceDataSource;
import se.sundsvall.intricdatacollector.datasource.confluence.api.model.ConfluenceWebhookData;
import se.sundsvall.intricdatacollector.datasource.confluence.api.model.ConfluenceWebhookDataBuilder;
import se.sundsvall.intricdatacollector.datasource.confluence.api.model.PageBuilder;
import se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence.ConfluenceIntegrationProperties;
import se.sundsvall.intricdatacollector.datasource.confluence.model.EventType;
import se.sundsvall.intricdatacollector.test.annotation.UnitTest;

@UnitTest
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class ConfluenceWebhookResourcesTests {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .configure(SerializationFeature.INDENT_OUTPUT, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private HmacUtils hmacUtils;

    @MockBean
    private ConfluenceDataSource dataSourceMock;

    @Autowired
    private ConfluenceIntegrationProperties properties;

    @Autowired
    private WebTestClient testClient;

    @BeforeEach
    void setUp() {
        hmacUtils = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, properties.webhookSecurity().secret());
    }

    @ParameterizedTest
    @EnumSource(EventType.class)
    void handleWebhookEvent(final EventType eventType) {
        var pageId = 1203442627L;
        var request = createRequest(eventType, pageId);

        testClient.post()
            .uri("/confluence/webhook-event")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .header("x-hub-signature", createHmacSignature(request))
            .exchange()
            .expectStatus().isOk()
            .expectBody().isEmpty();

        if (eventType == PAGE_REMOVED) {
            verify(dataSourceMock).deletePage(Long.toString(pageId));
        } else {
            verify(dataSourceMock).processPage(eventType, Long.toString(pageId));
        }
    }

    @Test
    void handleWebhookEventWhenSignatureVerificationFails() {
        var pageId = 1203442627L;
        var request = createRequest(PAGE_CREATED, pageId);

        testClient.post()
            .uri("/confluence/webhook-event")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .header("x-hub-signature", "some-invalid-signature-data")
            .exchange()
            .expectStatus().isForbidden()
            .expectHeader().contentType(APPLICATION_PROBLEM_JSON_VALUE)
            .expectBody().jsonPath("$.detail").isEqualTo("Webhook signature verification failed");

        verifyNoInteractions(dataSourceMock);
    }

    private ConfluenceWebhookData createRequest(final EventType eventType, final Long pageId) {
        return ConfluenceWebhookDataBuilder.create()
            .withEventType(eventType.name().toLowerCase())
            .withPage(PageBuilder.create()
                .withId(pageId)
                .build())
            .withUserKey("4028a083917e668c01917e66f5a80000")
            .withTimestamp(1724928764451L)
            .withUpdateTrigger(eventType == EventType.PAGE_UPDATED ? "page_updated" : null)
            .build();
    }

    private String createHmacSignature(final ConfluenceWebhookData request) {
        try {
            var body = objectMapper.writeValueAsString(request);
            var minifiedBody = objectMapper.readTree(body).toString();

            return "sha256=" + hmacUtils.hmacHex(minifiedBody);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to create HMAC signature", e);
        }
    }
}
