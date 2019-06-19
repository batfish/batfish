package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class AnalysisMetadata {
  private static final String PROP_CREATIONTIMESTAMP = "creationTimestamp";
  private static final String PROP_SUGGESTED = "suggested";

  private Instant _creationTimestamp;
  private boolean _suggested;

  @JsonCreator
  public AnalysisMetadata(
      @JsonProperty(PROP_CREATIONTIMESTAMP) Instant creationTimestamp,
      @JsonProperty(PROP_SUGGESTED) boolean suggested) {
    _creationTimestamp = creationTimestamp;
    _suggested = suggested;
  }

  @JsonProperty(PROP_CREATIONTIMESTAMP)
  public Instant getCreationTimestamp() {
    return _creationTimestamp;
  }

  @JsonProperty(PROP_SUGGESTED)
  public boolean getSuggested() {
    return _suggested;
  }

  public void setSuggested(boolean suggested) {
    _suggested = suggested;
  }
}
