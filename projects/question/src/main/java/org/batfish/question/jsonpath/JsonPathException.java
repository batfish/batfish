package org.batfish.question.jsonpath;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.batfish.question.jsonpath.JsonPathResult.JsonPathResultEntry;

/** For now, exceptions are same as result entries */
public class JsonPathException extends JsonPathResultEntry {

  @JsonCreator
  public JsonPathException(
      @JsonProperty(PROP_CONCRETE_PATH) List<String> concretePath,
      @JsonProperty(PROP_SUFFIX) JsonNode suffix) {
    super(concretePath, suffix);
  }
}
