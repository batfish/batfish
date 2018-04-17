package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class AnswerElement {

  public static final String PROP_SUMMARY = "summary";

  protected AnswerSummary _summary;

  @Nullable
  @JsonProperty(PROP_SUMMARY)
  public final AnswerSummary getSummary() {
    return _summary;
  }

  public String prettyPrint() {
    try {
      return BatfishObjectMapper.writePrettyString(this);
    } catch (JsonProcessingException e) {
      throw new BatfishException("Failed to pretty print answer element", e);
    }
  }

  @JsonProperty(PROP_SUMMARY)
  public final void setSummary(AnswerSummary summary) {
    _summary = summary;
  }
}
