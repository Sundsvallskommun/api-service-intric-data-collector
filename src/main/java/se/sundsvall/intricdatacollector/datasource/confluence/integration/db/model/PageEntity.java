package se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.jilt.Builder;

@Entity
@Table(name = "confluence_pages")
@Builder(setterPrefix = "with", factoryMethod = "create")
public class PageEntity {

    @Id
    @Column(name = "page_id", length = 16)
    private String id;

    @Column(name = "intric_group_id", length = 36)
    private String groupId;

    @Column(name = "blob_id", length = 36)
    private String blobId;

    public PageEntity() {
    }

    /*
     * "All-args constructor", required by the generated Jilt builder
     */
    protected PageEntity(final String id, final String groupId, final String blobId) {
        this.id = id;
        this.groupId = groupId;
        this.blobId = blobId;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }

    public String getBlobId() {
        return blobId;
    }

    public void setBlobId(final String blobId) {
        this.blobId = blobId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PageEntity that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(groupId, that.groupId) && Objects.equals(blobId, that.blobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, groupId, blobId);
    }
}
