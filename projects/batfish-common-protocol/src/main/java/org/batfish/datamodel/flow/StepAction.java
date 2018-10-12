package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import javax.annotation.Nonnull;

/** Represents an action taken at the end of a {@link Step} */
@JsonSchemaDescription("Represents an action taken in a step")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class StepAction {

  protected static final String PROP_NAME = "name";

  private final String _name;

  @JsonCreator
  public StepAction(@JsonProperty(PROP_NAME) @Nonnull String name) {
    _name = name;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }
}
