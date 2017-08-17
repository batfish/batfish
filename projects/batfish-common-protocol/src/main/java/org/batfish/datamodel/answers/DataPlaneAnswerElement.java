package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public interface DataPlaneAnswerElement extends AnswerElement, Serializable {

  String PROP_VERSION = "version";

  @JsonProperty(PROP_VERSION)
  String getVersion();
}
