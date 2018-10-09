package org.batfish.identifiers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SnapshotId extends Id {
  private static final String PROP_ID = "id";

  @JsonCreator
  public SnapshotId(@JsonProperty(PROP_ID) String id) {
    super(id);
  }
}
