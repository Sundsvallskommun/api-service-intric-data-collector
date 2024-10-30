package se.sundsvall.intricdatacollector.core.intric.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jilt.Builder;

@Builder(setterPrefix = "with", factoryMethod = "create")
public record InfoBlobsRequest(@JsonProperty("info_blobs") List<InfoBlob> infoBlobs) {}
