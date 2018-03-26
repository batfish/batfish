package org.batfish.question.jsonpathtotable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.questions.DisplayHints.Composition;
import org.batfish.datamodel.questions.DisplayHints.Extraction;
import org.batfish.datamodel.table.TableMetadata;

public class JsonPathToTableQuery {

  private static final String PROP_DISPLAY_HINTS = "displayHints";

  private static final String PROP_PATH = "path";

  private static final String PROP_SUFFIX = "suffix";

  private DisplayHints _displayHints;

  private String _path;

  private boolean _suffix;

  @JsonCreator
  public JsonPathToTableQuery(
      @Nonnull @JsonProperty(PROP_PATH) String path,
      @Nonnull @JsonProperty(PROP_SUFFIX) boolean suffix,
      @Nonnull @JsonProperty(PROP_DISPLAY_HINTS) DisplayHints displayHints) {
    _path = path;
    _suffix = suffix;
    _displayHints = displayHints;
    if (displayHints == null
        || (displayHints.getCompositions().size() == 0
            && displayHints.getExtractions().size() == 0)) {
      throw new IllegalArgumentException("No compositions or extractions specified");
    }
  }

  public TableMetadata computeTableMetadata() {
    Map<String, Schema> schemas = new HashMap<>();
    if (_displayHints.getExtractions() != null) {
      for (Entry<String, Extraction> entry : _displayHints.getExtractions().entrySet()) {
        schemas.put(entry.getKey(), entry.getValue().getSchemaAsObject());
      }
    }
    if (_displayHints.getCompositions() != null) {
      for (Entry<String, Composition> entry : _displayHints.getCompositions().entrySet()) {
        schemas.put(entry.getKey(), entry.getValue().getSchemaAsObject());
      }
    }
    return new TableMetadata(schemas, null, null, _displayHints.getTextDesc());
  }

  @JsonProperty(PROP_DISPLAY_HINTS)
  public DisplayHints getDisplayHints() {
    return _displayHints;
  }

  @JsonProperty(PROP_PATH)
  public String getPath() {
    return _path;
  }

  @JsonProperty(PROP_SUFFIX)
  public boolean getSuffix() {
    return _suffix;
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
