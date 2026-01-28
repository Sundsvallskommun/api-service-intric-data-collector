package se.sundsvall.aidatacollector.datasource.confluence.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.jsoup.Jsoup;

public class Page {

	private String municipalityId;
	private String pageId;
	private String title;
	private String body;
	private String baseUrl;
	private String path;
	private LocalDateTime updatedAt;
	private List<String> ancestorIds;
	private String eneoGroupId;
	private String eneoBlobId;

	public static Page create() {
		return new Page();
	}

	public String bodyAsText() {
		return Jsoup.parse(body).text();
	}

	public String url() {
		return baseUrl + path;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public Page withMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public String getPageId() {
		return pageId;
	}

	public void setPageId(final String pageId) {
		this.pageId = pageId;
	}

	public Page withPageId(final String pageId) {
		this.pageId = pageId;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public Page withTitle(final String title) {
		this.title = title;
		return this;
	}

	public String getBody() {
		return body;
	}

	public void setBody(final String body) {
		this.body = body;
	}

	public Page withBody(final String body) {
		this.body = body;
		return this;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(final String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public Page withBaseUrl(final String baseUrl) {
		this.baseUrl = baseUrl;
		return this;
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public Page withPath(final String path) {
		this.path = path;
		return this;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(final LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Page withUpdatedAt(final LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
		return this;
	}

	public List<String> getAncestorIds() {
		return ancestorIds;
	}

	public void setAncestorIds(final List<String> ancestorIds) {
		this.ancestorIds = ancestorIds;
	}

	public Page withAncestorIds(final List<String> ancestorIds) {
		this.ancestorIds = ancestorIds;
		return this;
	}

	public String getEneoGroupId() {
		return eneoGroupId;
	}

	public void setEneoGroupId(final String eneoGroupId) {
		this.eneoGroupId = eneoGroupId;
	}

	public Page withEneoGroupId(final String eneoGroupId) {
		this.eneoGroupId = eneoGroupId;
		return this;
	}

	public String getEneoBlobId() {
		return eneoBlobId;
	}

	public void setEneoBlobId(final String eneoBlobId) {
		this.eneoBlobId = eneoBlobId;
	}

	public Page withEneoBlobId(final String eneoBlobId) {
		this.eneoBlobId = eneoBlobId;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final Page that)) {
			return false;
		}
		return Objects.equals(municipalityId, that.municipalityId) &&
			Objects.equals(pageId, that.pageId) &&
			Objects.equals(title, that.title) &&
			Objects.equals(body, that.body) &&
			Objects.equals(baseUrl, that.baseUrl) &&
			Objects.equals(path, that.path) &&
			Objects.equals(updatedAt, that.updatedAt) &&
			Objects.equals(ancestorIds, that.ancestorIds) &&
			Objects.equals(eneoGroupId, that.eneoGroupId) &&
			Objects.equals(eneoBlobId, that.eneoBlobId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(municipalityId, pageId, title, body, baseUrl, path, updatedAt, ancestorIds, eneoGroupId, eneoBlobId);
	}

	@Override
	public String toString() {
		return "Page{" +
			"municipalityId='" + municipalityId + '\'' +
			", pageId='" + pageId + '\'' +
			", title='" + title + '\'' +
			", body='" + body + '\'' +
			", baseUrl='" + baseUrl + '\'' +
			", path='" + path + '\'' +
			", updatedAt=" + updatedAt +
			", ancestorIds=" + ancestorIds +
			", eneoGroupId='" + eneoGroupId + '\'' +
			", eneoBlobId='" + eneoBlobId + '\'' +
			'}';
	}
}
