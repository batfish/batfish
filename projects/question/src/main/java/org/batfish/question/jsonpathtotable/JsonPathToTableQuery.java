package org.batfish.question.jsonpathtotable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;

public class JsonPathToTableQuery {

  private static final String PROP_COMPOSITIONS = "compositions";

  private static final String PROP_EXTRACTIONS = "extractions";

  private static final String PROP_PATH = "path";

  private Map<String, JsonPathToTableComposition> _compositions;

  private Map<String, JsonPathToTableExtraction> _extractions;

  private String _path;

  @JsonCreator
  public JsonPathToTableQuery(
      @JsonProperty(PROP_PATH) String path,
      @JsonProperty(PROP_EXTRACTIONS) Map<String, JsonPathToTableExtraction> extractions,
      @JsonProperty(PROP_COMPOSITIONS) Map<String, JsonPathToTableComposition> compositions) {

    _path = path;
    _extractions = extractions == null ? new HashMap<>() : extractions;
    _compositions = compositions == null ? new HashMap<>() : compositions;

    // now sanity check what we have
    if (_path == null) {
      throw new IllegalArgumentException("Path cannot be null in JsonPathToTableQuery");
    }
    if (_extractions.isEmpty() && _compositions.isEmpty()) {
      throw new IllegalArgumentException("No compositions or extractions in JsonPathToTableQuery");
    }

    // all vars mentioned in compositions should have extractions
    Set<String> varsInCompositions = new HashSet<>();
    for (Entry<String, JsonPathToTableComposition> entry : _compositions.entrySet()) {
      varsInCompositions.addAll(entry.getValue().getVars());
    }
    Set<String> extractionVars = _extractions.keySet();
    SetView<String> missingExtractionVars = Sets.difference(varsInCompositions, extractionVars);
    if (!missingExtractionVars.isEmpty()) {
      throw new IllegalArgumentException(
          "compositions refer to variables missing from extractions hints: "
              + missingExtractionVars);
    }

    // the names of compositions and extraction vars should have no overlap
    Set<String> commonNames = Sets.intersection(extractionVars, _compositions.keySet());
    if (!commonNames.isEmpty()) {
      throw new BatfishException(
          "compositions and extraction vars should not have common names: " + commonNames);
    }
  }

  @JsonProperty(PROP_COMPOSITIONS)
  public Map<String, JsonPathToTableComposition> getCompositions() {
    return _compositions;
  }

  @JsonProperty(PROP_EXTRACTIONS)
  public Map<String, JsonPathToTableExtraction> getExtractions() {
    return _extractions;
  }

  @JsonProperty(PROP_PATH)
  public String getPath() {
    return _path;
  }

  @Override
  public String toString() {
    try {
      return BatfishObjectMapper.writePrettyString(this);
    } catch (JsonProcessingException e) {
      throw new BatfishException("Cannot serialize to Json", e);
    }
  }
}
