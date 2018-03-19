package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class DataPlaneAnswerElement extends AnswerElement {

  public static final String PROP_VERSION = "version";

  @JsonProperty(PROP_VERSION)
  public abstract String getVersion();
}
