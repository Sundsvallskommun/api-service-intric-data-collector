package se.sundsvall.intricdatacollector.datasource.confluence.integration.db.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import org.jilt.Builder;

@Entity
@Table(name = "confluence_pages")
@Builder(setterPrefix = "with", factoryMethod = "create")
public class PageEntity {

    @Id
    @Column(name = "page_id", length = 16)
    private String id;

    @Column(name = "municipality_id", length = 4)
    private String municipalityId;

    @Column(name = "intric_group_id", length = 36)
    private String groupId;

    @Column(name = "blob_id", length = 36)
    private String blobId;

    @Builder.Ignore
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder.Ignore
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PageEntity() {
    }

    /*
     * "All-args constructor", required by the generated Jilt builder
     */
    protected PageEntity(final String id, final String municipalityId, final String groupId, final String blobId) {
        this.id = id;
        this.municipalityId = municipalityId;
        this.groupId = groupId;
        this.blobId = blobId;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(final String municipalityId) {
        this.municipalityId = municipalityId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PageEntity that)) {
            return false;
        }
        return Objects.equals(id, that.id) &&
            Objects.equals(municipalityId, that.municipalityId) &&
            Objects.equals(groupId, that.groupId) &&
            Objects.equals(blobId, that.blobId) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, municipalityId, groupId, blobId, createdAt, updatedAt);
    }
}
