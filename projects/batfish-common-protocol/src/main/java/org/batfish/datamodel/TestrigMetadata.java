package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class TestrigMetadata {
  private static final String PROP_CREATIONTIMESTAMP = "creationTimestamp";
  private Instant _creationTimestamp;

  @JsonCreator
  public TestrigMetadata(
      @JsonProperty(PROP_CREATIONTIMESTAMP) Instant creationTimestamp) {
    this._creationTimestamp = creationTimestamp;
  }

  @JsonProperty(PROP_CREATIONTIMESTAMP)
  public Instant getCreationTimestamp() {
    return _creationTimestamp;
  }
}
