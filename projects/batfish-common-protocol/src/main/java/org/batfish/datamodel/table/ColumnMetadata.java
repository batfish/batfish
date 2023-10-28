package org.batfish.datamodel.table;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.Names.Type;
import org.batfish.datamodel.answers.Schema;

@ParametersAreNonnullByDefault
public final class ColumnMetadata {
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_IS_KEY = "isKey";
  private static final String PROP_IS_VALUE = "isValue";
  private static final String PROP_NAME = "name";
  private static final String PROP_SCHEMA = "schema";

  private @Nonnull String _description;
  private boolean _isKey;
  private boolean _isValue;
  private @Nonnull String _name;
  private @Nonnull Schema _schema;

  public ColumnMetadata(String name, Schema schema, String description) {
    this(name, schema, description, null, null);
  }

  @JsonCreator
  // visible for testing.
  static ColumnMetadata jsonCreator(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_SCHEMA) @Nullable Schema schema,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description,
      @JsonProperty(PROP_IS_KEY) @Nullable Boolean isKey,
      @JsonProperty(PROP_IS_VALUE) @Nullable Boolean isValue) {
    checkArgument(name != null, "'name' cannot be null for ColumnMetadata");
    checkArgument(description != null, "'description' cannot be null for ColumnMetadata");
    checkArgument(schema != null, "'schema' cannot be null for ColumnMetadata");
    return new ColumnMetadata(name, schema, description, isKey, isValue);
  }

  public ColumnMetadata(
      String name,
      Schema schema,
      String description,
      @Nullable Boolean isKey,
      @Nullable Boolean isValue) {
    Names.checkName(name, "table column", Type.TABLE_COLUMN);
    _name = name;
    _schema = schema;
    _description = description;
    _isKey = firstNonNull(isKey, true);
    _isValue = firstNonNull(isValue, true);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ColumnMetadata)) {
      return false;
    }
    return Objects.equals(_description, ((ColumnMetadata) o)._description)
        && Objects.equals(_isKey, ((ColumnMetadata) o)._isKey)
        && Objects.equals(_isValue, ((ColumnMetadata) o)._isValue)
        && Objects.equals(_name, ((ColumnMetadata) o)._name)
        && Objects.equals(_schema, ((ColumnMetadata) o)._schema);
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

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_SCHEMA)
  public Schema getSchema() {
    return _schema;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_description, _isKey, _isValue, _name, _schema);
  }

  @Override
  public String toString() {
    return String.format(
        "[%s, %s, %s, isKey:%s, isValue:%s]", _name, _schema, _description, _isKey, _isValue);
  }
}
