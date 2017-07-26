package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public interface DataPlaneAnswerElement extends AnswerElement, Serializable {

  String VERSION_VAR = "version";

  @JsonProperty(VERSION_VAR)
  String getVersion();
}
