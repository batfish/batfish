package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestrigMetadata {
  private static final String PROP_CREATIONTIMESTAMP = "creationTimestamp";
  private java.time.Instant _creationTimestamp;

  @JsonCreator
  public TestrigMetadata(
      @JsonProperty(PROP_CREATIONTIMESTAMP) java.time.Instant creationTimestamp) {
    this._creationTimestamp = creationTimestamp;
  }

  @JsonProperty(PROP_CREATIONTIMESTAMP)
  public java.time.Instant getCreationTimestamp() {
    return _creationTimestamp;
  }
}
