package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public abstract class DataPlaneAnswerElement extends AnswerElement implements Serializable {

  public static final String PROP_VERSION = "version";

  /** */
  private static final long serialVersionUID = 1L;

  @JsonProperty(PROP_VERSION)
  public abstract String getVersion();
}
