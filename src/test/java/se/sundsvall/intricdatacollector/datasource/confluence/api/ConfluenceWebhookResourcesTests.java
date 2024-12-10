package se.sundsvall.intricdatacollector.datasource.confluence.api;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_256;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;
import static se.sundsvall.intricdatacollector.datasource.confluence.model.EventType.PAGE_CREATED;
import static se.sundsvall.intricdatacollector.datasource.confluence.model.EventType.PAGE_UPDATED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.apache.commons.codec.digest.HmacUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
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
		.configure(INDENT_OUTPUT, false)
		.setSerializationInclusion(NON_NULL);

	private HmacUtils hmacUtils;

	@MockitoBean
	private ConfluenceDataSource dataSourceMock;

	@Autowired
	private ConfluenceIntegrationProperties properties;

	@Autowired
	private WebTestClient testClient;

	@BeforeEach
	void setUp() {
		var environment = properties.environments().get("1984");

		hmacUtils = new HmacUtils(HMAC_SHA_256, environment.webhook().security().secret());
	}

	@ParameterizedTest
	@EnumSource(EventType.class)
	void handleWebhookEvent(final EventType eventType) {
		var pageId = "1203442627";
		var municipalityId = "1984";
		var request = createRequest(eventType, Long.valueOf(pageId));

		testClient.post()
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/confluence/webhook-event")
				.build(Map.of("municipalityId", municipalityId)))
			.contentType(APPLICATION_JSON)
			.body(fromValue(request))
			.header("x-hub-signature", createHmacSignature(request))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(ALL_VALUE)
			.expectBody().isEmpty();

		switch (eventType) {
			case PAGE_CREATED, PAGE_RESTORED -> verify(dataSourceMock).insertPage(municipalityId, pageId);
			case PAGE_UPDATED -> verify(dataSourceMock).updatePage(municipalityId, pageId);
			case PAGE_REMOVED -> verify(dataSourceMock).deletePage(municipalityId, pageId);
		}

		verifyNoMoreInteractions(dataSourceMock);
	}

	@Test
	void handleWebhookEventWhenSignatureVerificationFails() {
		var municipalityId = "1984";
		var pageId = "1203442627";
		var request = createRequest(PAGE_CREATED, Long.valueOf(pageId));

		testClient.post()
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/confluence/webhook-event")
				.build(Map.of("municipalityId", municipalityId)))
			.contentType(APPLICATION_JSON)
			.body(fromValue(request))
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
			.withUpdateTrigger(eventType == PAGE_UPDATED ? "page_updated" : null)
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
