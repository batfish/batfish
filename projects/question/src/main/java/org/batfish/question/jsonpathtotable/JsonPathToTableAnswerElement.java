package org.batfish.question.jsonpathtotable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;

public class JsonPathToTableAnswerElement extends TableAnswerElement {

  private static final String PROP_DEBUG = "debug";

  private Map<String, Object> _debug;

  @JsonCreator
  public JsonPathToTableAnswerElement(
      @JsonProperty(PROP_METADATA) TableMetadata metadata,
      @JsonProperty(PROP_DEBUG) Map<String, Object> debug) {
    super(metadata);
    _debug = new HashMap<>();
  }

  public JsonPathToTableAnswerElement(@Nonnull TableMetadata metadata) {
    this(metadata, new HashMap<>());
  }

  public void addDebugInfo(String key, Object value) {
    _debug.put(key, value);
  }

  @JsonProperty(PROP_DEBUG)
  public Map<String, Object> getDebug() {
    return _debug;
  }
}
