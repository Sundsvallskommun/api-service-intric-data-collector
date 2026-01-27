package se.sundsvall.aidatacollector.datasource.confluence.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.aidatacollector.Application;
import se.sundsvall.aidatacollector.datasource.confluence.integration.db.DbIntegration;

@WireMockAppTestSuite(files = "classpath:/ConfluenceWebhookResourcesIT/", classes = Application.class)
@Sql("/db/truncate.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
// @DirtiesContext is used since the custom Eneo OAuth2 token service should get a new token for
// each test, instead of reusing the one fetched in previous tests, if any
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ConfluenceWebhookResourcesIT extends AbstractAppTest {

	@Autowired
	private DbIntegration dbIntegration;

	@Test
	void test1_insert_successfulRequest() {
		setupCall()
			.withServicePath("/1984/confluence/webhook-event")
			.withRequest("request.json")
			.withHeader("x-hub-signature", "sha256=4e58d90a1951dcadb96684e8d3d64ad2cc23ddb8f11a4edbcce84187c8f03288")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(dbIntegration.getAllPages("1984")).hasSize(1);
	}

	@Test
	@Sql("/db/testdata.sql")
	void test2_update_successfulRequest() {
		setupCall()
			.withServicePath("/1984/confluence/webhook-event")
			.withRequest("request.json")
			.withHeader("x-hub-signature", "sha256=fcfa5a7f04e9e681ff2ac8449cf18f7c74b05973e3d7a4bb45922835d97b542b")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(dbIntegration.getAllPages("1984")).hasSize(1);
	}

	@Test
	@Sql("/db/testdata.sql")
	void test3_delete_successfulRequest() {
		setupCall()
			.withServicePath("/1984/confluence/webhook-event")
			.withRequest("request.json")
			.withHeader("x-hub-signature", "sha256=e87ac13bb4fa9d1c4f2c92341bdebf62eb371f932390a40819cefa30322d43b7")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(dbIntegration.getAllPages("1984")).isEmpty();
	}

	@Test
	void test4_invalidSignature() {
		setupCall()
			.withServicePath("/1984/confluence/webhook-event")
			.withRequest("request.json")
			.withHeader("x-hub-signature", "sha256=some-invalid-signature")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(FORBIDDEN)
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse();

		assertThat(dbIntegration.getAllPages("1984")).isEmpty();
	}
}
