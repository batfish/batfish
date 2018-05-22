package org.batfish.question.jsonpathtotable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.batfish.datamodel.table.ColumnMetadata;
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

  public static TableMetadata create(JsonPathToTableQuestion question) {
    List<ColumnMetadata> columnMetadata = new LinkedList<>();
    for (Entry<String, JsonPathToTableExtraction> entry :
        question.getPathQuery().getExtractions().entrySet()) {
      JsonPathToTableExtraction extraction = entry.getValue();
      if (extraction.getInclude()) {
        columnMetadata.add(
            new ColumnMetadata(
                entry.getKey(),
                extraction.getSchema(),
                extraction.getDescription(),
                extraction.getIsKey(),
                extraction.getIsValue()));
      }
    }
    for (Entry<String, JsonPathToTableComposition> entry :
        question.getPathQuery().getCompositions().entrySet()) {
      JsonPathToTableComposition composition = entry.getValue();
      if (composition.getInclude()) {
        columnMetadata.add(
            new ColumnMetadata(
                entry.getKey(),
                composition.getSchema(),
                composition.getDescription(),
                composition.getIsKey(),
                composition.getIsValue()));
      }
    }
    return new TableMetadata(columnMetadata, question.getDisplayHints());
  }

  public void addDebugInfo(String key, Object value) {
    _debug.put(key, value);
  }

  @JsonProperty(PROP_DEBUG)
  public Map<String, Object> getDebug() {
    return _debug;
  }
}
