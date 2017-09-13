package org.batfish.question.jsonpath;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Objects;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.util.CommonUtil;

public class JsonPathResult {

  public static class JsonPathResultEntry {

    private static final String PROP_PREFIX = "prefix";

    private static final String PROP_SUFFIX = "suffix";

    private final JsonNode _prefix;

    private final JsonNode _suffix;

    @JsonCreator
    public JsonPathResultEntry(
        @JsonProperty(PROP_PREFIX) JsonNode prefix,
        @JsonProperty(PROP_SUFFIX) JsonNode suffix) {
      _prefix = prefix;
      if (suffix != null && suffix.isNull()) {
        _suffix = null;
      } else {
        _suffix = suffix;
      }
    }

    @Override public boolean equals(Object o) {
      if (o == null) {
        return o == null;
      } else if (!(o instanceof JsonPathResultEntry)) {
        return false;
      }
      return (Objects.equal(_prefix, ((JsonPathResultEntry) o)._prefix)
              && Objects.equal(_suffix, ((JsonPathResultEntry) o)._suffix));
    }

    @JsonIgnore
    public String getMapKey() {
      String text = _prefix.textValue();
      String endsCut = text.substring(2, text.length() - 1);
      List<String> parts = Arrays.asList(endsCut.split("\\]\\["));
      return String.join("->", parts);
    }

    @JsonProperty(PROP_PREFIX)
    public JsonNode getPrefix() {
      return _prefix;
    }

    @JsonProperty(PROP_SUFFIX)
    public JsonNode getSuffix() {
      return _suffix;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(_prefix, _suffix);
    }
  }

  private Boolean _assertionResult;

  private Integer _numResults;

  private JsonPathQuery _path;

  private SortedMap<String, JsonPathResultEntry> _result;

  public JsonPathResult() {
    _result = new TreeMap<>();
  }

  public Boolean getAssertionResult() {
    return _assertionResult;
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

  public void setAssertionResult(Boolean assertionResult) {
    _assertionResult = assertionResult;
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
