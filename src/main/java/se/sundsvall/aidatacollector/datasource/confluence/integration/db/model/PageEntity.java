package se.sundsvall.aidatacollector.datasource.confluence.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import org.jilt.Builder;

@Entity
@Table(name = "confluence_pages")
@Builder(setterPrefix = "with", factoryMethod = "create", toBuilder = "from")
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

	public String getPageId() {
		return pageId;
	}

	public void setPageId(final String pageId) {
		this.pageId = pageId;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public String getEneoGroupId() {
		return eneoGroupId;
	}

	public void setEneoGroupId(final String eneoGroupId) {
		this.eneoGroupId = eneoGroupId;
	}

	public String getEneoBlobId() {
		return eneoBlobId;
	}

	public void setEneoBlobId(final String eneoBlobId) {
		this.eneoBlobId = eneoBlobId;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(final LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
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
