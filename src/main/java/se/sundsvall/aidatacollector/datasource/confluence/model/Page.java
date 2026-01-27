package se.sundsvall.aidatacollector.datasource.confluence.model;

import java.time.LocalDateTime;
import java.util.List;
import org.jilt.Builder;
import org.jsoup.Jsoup;

@Builder(setterPrefix = "with", factoryMethod = "create", toBuilder = "from")
public record Page(
	String municipalityId,
	String pageId,
	String title,
	String body,
	String baseUrl,
	String path,
	LocalDateTime updatedAt,
	List<String> ancestorIds,
	String eneoGroupId,
	String eneoBlobId) {

	public String bodyAsText() {
		return Jsoup.parse(body).text();
	}

	public String url() {
		return baseUrl + path;
	}
}
