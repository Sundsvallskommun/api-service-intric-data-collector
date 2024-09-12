package se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.LocalDateTime;
import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PageEntityTests {

    @BeforeAll
    static void setup() {
        registerValueGenerator(() -> now().plusDays(new Random().nextInt()), LocalDateTime.class);
    }

    @Test
    void classProperties() {
        MatcherAssert.assertThat(PageEntity.class, allOf(
            hasValidBeanConstructor(),
            hasValidGettersAndSetters(),
            hasValidBeanHashCode(),
            hasValidBeanEquals()));
    }

    @Test
    void gettersAndSetters() {
        var id = "someId";
        var municipalityId = "someMunicipalityId";
        var groupId = "someGroupId";
        var blobId = "someBlobId";

        var pageEntity = new PageEntity();
        pageEntity.setId(id);
        pageEntity.setMunicipalityId(municipalityId);
        pageEntity.setGroupId(groupId);
        pageEntity.setBlobId(blobId);

        assertThat(pageEntity.getId()).isEqualTo(id);
        assertThat(pageEntity.getMunicipalityId()).isEqualTo(municipalityId);
        assertThat(pageEntity.getGroupId()).isEqualTo(groupId);
        assertThat(pageEntity.getBlobId()).isEqualTo(blobId);
    }

    @Test
    void builder() {
        var id = "someId";
        var municipalityId = "someMunicipalityId";
        var groupId = "someGroupId";
        var blobId = "someBlobId";

        var pageEntity = PageEntityBuilder.create()
            .withId(id)
            .withMunicipalityId(municipalityId)
            .withGroupId(groupId)
            .withBlobId(blobId)
            .build();

        assertThat(pageEntity.getId()).isEqualTo(id);
        assertThat(pageEntity.getMunicipalityId()).isEqualTo(municipalityId);
        assertThat(pageEntity.getGroupId()).isEqualTo(groupId);
        assertThat(pageEntity.getBlobId()).isEqualTo(blobId);
    }

    @Test
    void noDirtOnCreatedBean() {
        assertThat(new PageEntity()).hasAllNullFieldsOrProperties();
        assertThat(PageEntityBuilder.create().build()).hasAllNullFieldsOrProperties();
    }
}
