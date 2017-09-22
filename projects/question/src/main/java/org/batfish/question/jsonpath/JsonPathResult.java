package org.batfish.question.jsonpath;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Objects;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.ConfigurationBuilder;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.DisplayHints.ExtractionHint;
import org.batfish.question.jsonpath.JsonPathExtractionHint.UseType;
import org.batfish.question.jsonpath.JsonPathQuestionPlugin.JsonPathAnswerer;

public class JsonPathResult {

  public static class JsonPathResultEntry {

    private static final String PROP_PREFIX = "prefix";

    private static final String PROP_SUFFIX = "suffix";

    private final JsonNode _prefix;

    private final JsonNode _suffix;

    @JsonCreator
    public JsonPathResultEntry(
        @JsonProperty(PROP_PREFIX) JsonNode prefix, @JsonProperty(PROP_SUFFIX) JsonNode suffix) {
      _prefix = prefix;
      if (suffix != null && suffix.isNull()) {
        _suffix = null;
      } else {
        _suffix = suffix;
      }
    }

    @Override
    public boolean equals(Object o) {
      if (o == null) {
        return o == null;
      } else if (!(o instanceof JsonPathResultEntry)) {
        return false;
      }
      return Objects.equal(_prefix, ((JsonPathResultEntry) o)._prefix)
          && Objects.equal(_suffix, ((JsonPathResultEntry) o)._suffix);
    }

    @JsonIgnore
    public String getMapKey() {
      return String.join("->", getPrefixParts());
    }

    @JsonProperty(PROP_PREFIX)
    public JsonNode getPrefix() {
      return _prefix;
    }

    public String getPrefixPart(int index) {
      List<String> parts = getPrefixParts();
      if (parts.size() <= index) {
        throw new BatfishException("No valid part at index " + index + "for prefix " + _prefix);
      }
      // remove the single quotes around the string
      return parts.get(index).replaceAll("^\'|\'$", "");
    }

    private List<String> getPrefixParts() {
      String text = _prefix.textValue();
      if (text.equals("$")) {
        return Arrays.asList("$");
      }
      if (text.length() < 2) {
        throw new BatfishException("Unexpected prefix " + text);
      }
      String endsCut = text.substring(2, text.length() - 1);
      return Arrays.asList(endsCut.split("\\]\\["));
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

  private static final String PROP_ASSERTION_RESULT = "assertionResult";

  private static final String PROP_EXTRACTED_VALUES = "extractedValues";

  private static final String PROP_NUM_RESULTS = "numResults";

  private static final String PROP_PATH = "path";

  private static final String PROP_RESULT = "result";

  private Boolean _assertionResult;

  private Map<String, Map<String, JsonNode>> _extractedValues;

  private Integer _numResults;

  private JsonPathQuery _path;

  private SortedMap<String, JsonPathResultEntry> _result;

  public JsonPathResult() {
    _result = new TreeMap<>();
  }

  public Map<String, Map<String, JsonNode>> extractDisplayValues(DisplayHints displayHints) {
    // this will reset anything we've done in the past
    _extractedValues = new HashMap<>();
    for (Entry<String, ExtractionHint> entry : displayHints.getExtractionHints().entrySet()) {
      JsonPathExtractionHint jpeHint = null;
      try {
        jpeHint = JsonPathExtractionHint.fromExtractionHint(entry.getValue());
      } catch (IOException e) {
        throw new BatfishException("Could not extract JsonPathExtractionHint from ExtractionHint");
      }
      switch (jpeHint.getUse()) {
      case PREFIX:
        extractDisplayValuesPrefix(entry.getKey(), entry.getValue(), jpeHint);
        break;
      case PREFIXOFSUFFIX:
      case SUFFIXOFSUFFIX:
        extractDisplayValuesSuffix(entry.getKey(), entry.getValue(), jpeHint);
        break;
      default:
        throw new BatfishException("Unknown use type " + jpeHint.getUse());
      }
    }
    return _extractedValues;
  }

  private void extractDisplayValuesPrefix(String displayVar,
      ExtractionHint extractionHint, JsonPathExtractionHint jpeHint) {
    if (extractionHint.getIsList()) {
      throw new BatfishException("Prefix-based hints are incompatible with list types");
    }
    for (Entry<String, JsonPathResultEntry> entry : _result.entrySet()) {
      if (!_extractedValues.containsKey(entry.getKey())) {
        _extractedValues.put(entry.getKey(), new HashMap<>());
      }
      String prefixPart = entry.getValue().getPrefixPart(jpeHint.getIndex());
      _extractedValues.get(entry.getKey()).put(displayVar, new TextNode(prefixPart));
    }
  }

  private void extractDisplayValuesSuffix(String displayVar,
      ExtractionHint extractionHint, JsonPathExtractionHint jpeHint) {
    for (Entry<String, JsonPathResultEntry> entry : _result.entrySet()) {
      if (!_extractedValues.containsKey(entry.getKey())) {
        _extractedValues.put(entry.getKey(), new HashMap<>());
      }
      // can happen when the original query was not pulling suffixes at all
      if (entry.getValue().getSuffix() == null) {
        throw new BatfishException("Cannot compute suffix-based display values with null suffix. "
            + "(Was suffix set to True in the original JsonPath Query?)");
      }
      Configuration.setDefaults(BatfishJsonPathDefaults.INSTANCE);
      Configuration c = (new ConfigurationBuilder()).build();
      Object jsonObject = JsonPath.parse(entry.getValue().getSuffix(), c).json();

      JsonPathQuery query = new JsonPathQuery();
      query.setPath(jpeHint.getFilter());
      query.setSuffix(true);

      JsonPathResult filterResult = JsonPathAnswerer.computeResult(jsonObject, query);
      Map<String, JsonPathResultEntry> filterResultEntries = filterResult.getResult();
      if (filterResult.getNumResults() == 0) {
        throw new BatfishException("Got no results after filtering suffix values of the answer");
      }
      List<JsonNode> extractedList = new LinkedList<>();
      for (Entry<String, JsonPathResultEntry> resultEntry : filterResultEntries.entrySet()) {
        JsonNode value;
        // only two values possible: PREFIXOFSUFFIX, SUFFIXOFSUFFIX
        if (jpeHint.getUse() == UseType.PREFIXOFSUFFIX) {
          value = new TextNode(resultEntry.getValue().getPrefixPart(jpeHint.getIndex()));
        } else {
          value = resultEntry.getValue().getSuffix();
        }
        confirmValueType(value, extractionHint.getType());
        extractedList.add(value);
      }

      if (extractionHint.getIsList()) {
        BatfishObjectMapper mapper = new BatfishObjectMapper();
        ArrayNode arrayNode = mapper.valueToTree(extractedList);
        _extractedValues.get(entry.getKey()).put(displayVar, arrayNode);
      } else {
        if (filterResult.getNumResults() > 1) {
          throw new BatfishException("Got multiple results after filtering suffix values "
              + " of the answer, but the display type is non-list");
        }
        _extractedValues.get(entry.getKey()).put(displayVar, extractedList.get(0));
      }
    }
  }

  private static void confirmValueType(JsonNode value, DisplayHints.ValueType type) {
    // type check what we got
    switch (type) {
    case INT:
      if (!value.isInt()) {
          throw new BatfishException("Mismatch in extracted vs expected type.\n"
              + "Expected  " + type + "\n"
              + "Extracted " + value);
        }
      break;
    case STRING:
      if (!value.isTextual()) {
        throw new BatfishException("Mismatch in extracted vs expected type.\n"
            + "Expected  " + type + "\n"
            + "Extracted " + value);
      }
      break;
    default:
      throw new BatfishException("Unknown expected type " + type);
    }
  }

  @JsonProperty(PROP_ASSERTION_RESULT)
  public Boolean getAssertionResult() {
    return _assertionResult;
  }

  @JsonProperty(PROP_EXTRACTED_VALUES)
  public Map<String, Map<String, JsonNode>> getExtractedValues() {
    return _extractedValues;
  }

  @JsonProperty(PROP_NUM_RESULTS)
  public Integer getNumResults() {
    return _numResults;
  }

  @JsonProperty(PROP_PATH)
  public JsonPathQuery getPath() {
    return _path;
  }

  @JsonProperty(PROP_RESULT)
  public SortedMap<String, JsonPathResultEntry> getResult() {
    return _result;
  }

  @JsonProperty(PROP_ASSERTION_RESULT)
  public void setAssertionResult(Boolean assertionResult) {
    _assertionResult = assertionResult;
  }

  @JsonProperty(PROP_EXTRACTED_VALUES)
  public void setExtractedValues(Map<String, Map<String, JsonNode>> extractedValues) {
    _extractedValues = extractedValues;
  }

  @JsonProperty(PROP_NUM_RESULTS)
  public void setNumResults(Integer numResults) {
    _numResults = numResults;
  }

  @JsonProperty(PROP_PATH)
  public void setPath(JsonPathQuery path) {
    _path = path;
  }

  @JsonProperty(PROP_RESULT)
  public void setResult(SortedMap<String, JsonPathResultEntry> result) {
    _result = result;
  }
}
