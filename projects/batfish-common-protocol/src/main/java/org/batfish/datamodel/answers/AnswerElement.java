package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public interface AnswerElement {

  String PROP_SUMMARY = "summary";

  @Nullable
  @JsonProperty(PROP_SUMMARY)
  default AnswerSummary getSummary() {
    return null;
  }

  default String prettyPrint() {
    ObjectMapper mapper = new BatfishObjectMapper();
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new BatfishException("Failed to pretty print answer element", e);
    }
  }

  @JsonProperty(PROP_SUMMARY)
  default void setSummary(AnswerSummary summary) {}
}
