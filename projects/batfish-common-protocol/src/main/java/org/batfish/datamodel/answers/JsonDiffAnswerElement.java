package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.JsonDiff;

public class JsonDiffAnswerElement extends AnswerElement {
  private static final String PROP_JSON_DIFF = "jsonDiff";

  private final JsonDiff _jsonDiff;

  @JsonCreator
  public JsonDiffAnswerElement(@JsonProperty(PROP_JSON_DIFF) JsonDiff jsonDiff) {
    _jsonDiff = jsonDiff;
  }

  @JsonProperty(PROP_JSON_DIFF)
  public JsonDiff getJsonDiff() {
    return _jsonDiff;
  }
}
