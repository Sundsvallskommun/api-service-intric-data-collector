package se.sundsvall.intricdatacollector.integration.eneo.model;

import org.jilt.Builder;

@Builder(setterPrefix = "with", factoryMethod = "create")
public record Metadata(String title, String url) {
}
