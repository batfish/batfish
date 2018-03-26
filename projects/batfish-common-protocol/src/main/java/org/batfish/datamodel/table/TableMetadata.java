package org.batfish.datamodel.table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.answers.Schema;

public class TableMetadata {

  private static final String PROP_COLUMN_SCHEMAS = "columnSchemas";

  private static final String PROP_PRIMARY_KEY = "primaryKey";

  private static final String PROP_PRIMARY_VALUE = "primaryValue";

  private static final String PROP_TEXT_DESC = "textDesc";

  @Nonnull private Map<String, Schema> _columnSchemas;

  @Nullable private List<String> _primaryKey;

  @Nullable private List<String> _primaryValue;

  @Nullable private String _textDesc;

  @JsonCreator
  public TableMetadata(
      @Nullable @JsonProperty(PROP_COLUMN_SCHEMAS) Map<String, Schema> columnSchemas,
      @Nullable @JsonProperty(PROP_PRIMARY_KEY) List<String> primaryKey,
      @Nullable @JsonProperty(PROP_PRIMARY_VALUE) List<String> primaryValue,
      @Nullable @JsonProperty(PROP_TEXT_DESC) String textDesc) {
    _columnSchemas = columnSchemas == null ? new HashMap<>() : columnSchemas;
    _primaryKey = primaryKey;
    _primaryValue = primaryValue;
    _textDesc = textDesc;
  }

  @JsonProperty(PROP_COLUMN_SCHEMAS)
  public Map<String, Schema> getColumnSchemas() {
    return _columnSchemas;
  }

  @JsonProperty(PROP_PRIMARY_KEY)
  public List<String> getPrimaryKey() {
    return _primaryKey;
  }

  @JsonProperty(PROP_PRIMARY_VALUE)
  public List<String> getPrimaryValue() {
    return _primaryValue;
  }

  @JsonProperty(PROP_TEXT_DESC)
  public String getTextDesc() {
    return _textDesc;
  }
}
