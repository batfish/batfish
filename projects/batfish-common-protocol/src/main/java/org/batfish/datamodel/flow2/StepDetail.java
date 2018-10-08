package org.batfish.datamodel.flow2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;

public abstract class StepDetail {

  private static final String PROP_NAME = "name";

  private final String _name;

  @JsonCreator
  public StepDetail(@JsonProperty(PROP_NAME) @Nonnull String name) {
    _name = name;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }
}
