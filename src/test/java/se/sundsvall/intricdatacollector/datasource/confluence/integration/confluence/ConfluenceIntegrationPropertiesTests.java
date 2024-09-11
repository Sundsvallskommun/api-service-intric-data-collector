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
        assertThat(properties.baseUrl()).isEqualTo("someBaseUrl");
        assertThat(properties.basicAuth()).satisfies(basicAuth -> {
            assertThat(basicAuth.username()).isEqualTo("someUsername");
            assertThat(basicAuth.password()).isEqualTo("somePassword");
        });
        assertThat(properties.webhookSecurity()).satisfies(webhookSecurity -> {
            assertThat(webhookSecurity.enabled()).isTrue();
            assertThat(webhookSecurity.secret()).isEqualTo("SUPER_DUPER_S3CR3T!");
        });
        assertThat(properties.doInitialImport()).isFalse();
        assertThat(properties.blacklistedRootIds()).containsExactly("4567890");
        assertThat(properties.mappings()).hasSize(1).first().satisfies(mapping -> {
            assertThat(mapping.rootId()).isEqualTo("4567110901");
            assertThat(mapping.intricGroupId()).isEqualTo("97332ac9-b05e-46ac-abb5-2e2563e86d87");
        });
        assertThat(properties.connectTimeoutInSeconds()).isEqualTo(123);
        assertThat(properties.readTimeoutInSeconds()).isEqualTo(456);
    }
}
