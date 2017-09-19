package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Set;

public class DisplayHints {

  public static class ExtractionHint {
    private static final String PROP_HINTS = "hints";

    private static final String PROP_TYPE = "type";

    private Map<String, JsonNode> _hints;

    private String _type;

    @JsonCreator public ExtractionHint(
        @JsonProperty(PROP_TYPE) String type,
        @JsonProperty(PROP_HINTS) Map<String, JsonNode> hints) {
      _type = type;
      _hints = hints;
    }

    @JsonProperty(PROP_HINTS) public Map<String, JsonNode> getHints() {
      return _hints;
    }

    @JsonProperty(PROP_TYPE) public String getType() {
      return _type;
    }

    @JsonProperty(PROP_HINTS) public void setHints(Map<String, JsonNode> hints) {
      _hints = hints;
    }

    @JsonProperty(PROP_TYPE) public void setType(String type) {
      _type = type;
    }
  }

  private static final String PROP_EXTRACTION_HINTS = "extractionHints";

  private static final String PROP_TEXT_DESC = "textDesc";

  private static final String PROP_VISUALIZE = "visualize";

  private Map<String, ExtractionHint> _extractionHints;

  private String _textDesc;

  private Set<String> _visualize;

  @JsonProperty(PROP_EXTRACTION_HINTS)
  public Map<String, ExtractionHint> getExtractionHints() {
    return _extractionHints;
  }

  @JsonProperty(PROP_TEXT_DESC)
  public String getTextDesc() {
    return _textDesc;
  }

  @JsonProperty(PROP_VISUALIZE)
  public Set<String> getVisualize() {
    return _visualize;
  }

  @JsonProperty(PROP_EXTRACTION_HINTS)
  public void setExtractionHints(Map<String, ExtractionHint> extractionHints) {
    _extractionHints = extractionHints;
  }

  @JsonProperty(PROP_TEXT_DESC)
  public void setTextDesc(String textDesc) {
    _textDesc = textDesc;
  }

  @JsonProperty(PROP_VISUALIZE)
  public void setVisualize(Set<String> visualize) {
    _visualize = visualize;
  }
}
