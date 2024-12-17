package se.sundsvall.intricdatacollector.core.intric.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.jilt.Builder;

@Builder(setterPrefix = "with", factoryMethod = "create")
public record InfoBlobsRequest(@JsonProperty("info_blobs") List<InfoBlob> infoBlobs) {}
