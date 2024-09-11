package se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class PageEntityTests {

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
        var groupId = "someGroupId";
        var blobId = "someBlobId";

        var pageEntity = new PageEntity();
        pageEntity.setId(id);
        pageEntity.setGroupId(groupId);
        pageEntity.setBlobId(blobId);

        assertThat(pageEntity.getId()).isEqualTo(id);
        assertThat(pageEntity.getGroupId()).isEqualTo(groupId);
        assertThat(pageEntity.getBlobId()).isEqualTo(blobId);
    }

    @Test
    void builder() {
        var id = "someId";
        var groupId = "someGroupId";
        var blobId = "someBlobId";

        var pageEntity = PageEntityBuilder.create()
            .withId(id)
            .withGroupId(groupId)
            .withBlobId(blobId)
            .build();

        assertThat(pageEntity.getId()).isEqualTo(id);
        assertThat(pageEntity.getGroupId()).isEqualTo(groupId);
        assertThat(pageEntity.getBlobId()).isEqualTo(blobId);
    }

    @Test
    void noDirtOnCreatedBean() {
        assertThat(new PageEntity()).hasAllNullFieldsOrProperties();
        assertThat(PageEntityBuilder.create().build()).hasAllNullFieldsOrProperties();
    }
}
