package org.batfish.question.jsonpath;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.SortedMap;
import java.util.TreeMap;

public class JsonPathResult {

  public static class JsonPathResultEntry {

    private static final String PROP_CONCRETE_PATH = "concretePath";

    private static final String PROP_SUFFIX = "suffix";

    private final ConcreteJsonPath _concretePath;

    private final JsonNode _suffix;

    @JsonCreator
    public JsonPathResultEntry(
        @JsonProperty(PROP_CONCRETE_PATH) ConcreteJsonPath concretePath,
        @JsonProperty(PROP_SUFFIX) JsonNode suffix) {
      _concretePath = concretePath;
      if (suffix != null && suffix.isNull()) {
        _suffix = null;
      } else {
        _suffix = suffix;
      }
    }

    @JsonProperty(PROP_CONCRETE_PATH)
    public ConcreteJsonPath getConcretePath() {
      return _concretePath;
    }

    @JsonProperty(PROP_SUFFIX)
    public JsonNode getSuffix() {
      return _suffix;
    }
  }

  private Integer _numResults;

  private JsonPathQuery _path;

  private SortedMap<String, JsonPathResultEntry> _result;

  public JsonPathResult() {
    _result = new TreeMap<>();
  }

  public Integer getNumResults() {
    return _numResults;
  }

  public JsonPathQuery getPath() {
    return _path;
  }

  public SortedMap<String, JsonPathResultEntry> getResult() {
    return _result;
  }

  public void setNumResults(Integer numResults) {
    _numResults = numResults;
  }

  public void setPath(JsonPathQuery path) {
    _path = path;
  }

  public void setResult(SortedMap<String, JsonPathResultEntry> result) {
    _result = result;
  }
}
