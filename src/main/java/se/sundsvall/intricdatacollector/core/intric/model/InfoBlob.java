package se.sundsvall.intricdatacollector.core.intric.model;

import org.jilt.Builder;

@Builder(setterPrefix = "with", factoryMethod = "create")
public record InfoBlob(String text, Metadata metadata) { }
