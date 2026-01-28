package se.sundsvall.aidatacollector.datasource.confluence.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "confluence_pages")
public class PageEntity {

	@Id
	@Column(name = "page_id", length = 16)
	private String pageId;

	@Column(name = "municipality_id", length = 4)
	private String municipalityId;

	@Column(name = "eneo_group_id", length = 36)
	private String eneoGroupId;

	@Column(name = "blob_id", length = 36)
	private String eneoBlobId;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public PageEntity() {}

	PageEntity(final String pageId, final String municipalityId, final String eneoGroupId, final String eneoBlobId, final LocalDateTime updatedAt) {
		this.pageId = pageId;
		this.municipalityId = municipalityId;
		this.eneoGroupId = eneoGroupId;
		this.eneoBlobId = eneoBlobId;
		this.updatedAt = updatedAt;
	}

	public static PageEntity create() {
		return new PageEntity();
	}

	public String getPageId() {
		return pageId;
	}

	public void setPageId(final String pageId) {
		this.pageId = pageId;
	}

	public PageEntity withPageId(final String pageId) {
		this.pageId = pageId;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public PageEntity withMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public String getEneoGroupId() {
		return eneoGroupId;
	}

	public void setEneoGroupId(final String eneoGroupId) {
		this.eneoGroupId = eneoGroupId;
	}

	public PageEntity withEneoGroupId(final String eneoGroupId) {
		this.eneoGroupId = eneoGroupId;
		return this;
	}

	public String getEneoBlobId() {
		return eneoBlobId;
	}

	public void setEneoBlobId(final String eneoBlobId) {
		this.eneoBlobId = eneoBlobId;
	}

	public PageEntity withEneoBlobId(final String eneoBlobId) {
		this.eneoBlobId = eneoBlobId;
		return this;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(final LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public PageEntity withUpdatedAt(final LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final PageEntity that)) {
			return false;
		}
		return Objects.equals(pageId, that.pageId) &&
			Objects.equals(municipalityId, that.municipalityId) &&
			Objects.equals(eneoGroupId, that.eneoGroupId) &&
			Objects.equals(eneoBlobId, that.eneoBlobId) &&
			Objects.equals(updatedAt, that.updatedAt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pageId, municipalityId, eneoGroupId, eneoBlobId, updatedAt);
	}
}
