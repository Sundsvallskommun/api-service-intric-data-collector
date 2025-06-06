package se.sundsvall.intricdatacollector.datasource.confluence.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.jilt.Builder;

@Builder(setterPrefix = "with", factoryMethod = "create")
@JsonPropertyOrder({
	"timestamp", "event", "userKey", "page", "updateTrigger"
})
public record ConfluenceWebhookData(

	@NotNull @JsonProperty("event") @Schema(description = "Event type", allowableValues = {
		"page_created", "page_updated", "page_removed", "page_restored"
	}) String eventType,

	@Valid @NotNull Page page,

	String userKey,

	Long timestamp,

	String updateTrigger){

	@Builder(setterPrefix = "with", factoryMethod = "create")
	public record Page(@NotNull Long id) {}
}
