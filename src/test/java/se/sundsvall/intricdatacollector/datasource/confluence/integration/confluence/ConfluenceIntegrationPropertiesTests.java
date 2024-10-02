package se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import se.sundsvall.intricdatacollector.test.annotation.UnitTest;

@UnitTest
@SpringBootTest
class ConfluenceIntegrationPropertiesTests {

    @Autowired
    private ConfluenceIntegrationProperties properties;

    @Test
    void testProperties() {
        assertThat(properties.environments()).hasSize(1);
        assertThat(properties.environments()).containsKey("1984");
        assertThat(properties.environments().get("1984")).satisfies(environment -> {
            assertThat(environment.baseUrl()).isEqualTo("someBaseUrl");
            assertThat(environment.basicAuth()).satisfies(basicAuth -> {
                assertThat(basicAuth.username()).isEqualTo("someUsername");
                assertThat(basicAuth.password()).isEqualTo("somePassword");
            });
            assertThat(environment.webhook()).satisfies(webhook -> {
                assertThat(webhook.enabled()).isTrue();
                assertThat(webhook.security()).satisfies(webhookSecurity -> {
                    assertThat(webhookSecurity.enabled()).isTrue();
                    assertThat(webhookSecurity.secret()).isEqualTo("SUPER_DUPER_S3CR3T!");
                });
            });
            assertThat(environment.blacklistedRootIds()).containsExactly("4567890");
            assertThat(environment.mappings()).hasSize(1).first().satisfies(mapping -> {
                assertThat(mapping.rootId()).isEqualTo("4567110901");
                assertThat(mapping.intricGroupId()).isEqualTo("97332ac9-b05e-46ac-abb5-2e2563e86d87");
            });
            assertThat(environment.connectTimeoutInSeconds()).isEqualTo(123);
            assertThat(environment.readTimeoutInSeconds()).isEqualTo(456);
        });
    }
}
