package org.batfish.datamodel.answers;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface DataPlaneAnswerElement extends AnswerElement, Serializable {

   String VERSION_VAR = "version";

   @JsonProperty(VERSION_VAR)
   String getVersion();

}
