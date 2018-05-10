package org.batfish.datamodel.table;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.answers.Schema;

public class ColumnMetadata {
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_IS_KEY = "isKey";
  private static final String PROP_IS_VALUE = "isValue";
  private static final String PROP_SCHEMA = "schema";

  @Nonnull private String _description;
  private boolean _isKey;
  private boolean _isValue;
  @Nonnull private Schema _schema;

  public ColumnMetadata(Schema schema, String description) {
    this(schema, description, null, null);
  }

  @JsonCreator
  public ColumnMetadata(
      @Nonnull @JsonProperty(PROP_SCHEMA) Schema schema,
      @JsonProperty(PROP_DESCRIPTION) String description,
      @JsonProperty(PROP_IS_KEY) Boolean isKey,
      @JsonProperty(PROP_IS_VALUE) Boolean isValue) {
    _schema = schema;
    _description = description;
    _isKey = firstNonNull(isKey, true);
    _isValue = firstNonNull(isValue, true);
  }

  @JsonProperty(PROP_DESCRIPTION)
  public String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_IS_KEY)
  public boolean getIsKey() {
    return _isKey;
  }

  @JsonProperty(PROP_IS_VALUE)
  public boolean getIsValue() {
    return _isValue;
  }

  @JsonProperty(PROP_SCHEMA)
  public Schema getSchema() {
    return _schema;
  }
}
