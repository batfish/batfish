package org.batfish.question.jsonpathtotable;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.answers.Schema;

@ParametersAreNonnullByDefault
public abstract class JsonPathToTableColumn {

  public static final String DEFAULT_DESCRIPTION = "No description provided";

  protected static final String PROP_DESCRIPTION = "description";
  protected static final String PROP_INCLUDE = "include";
  protected static final String PROP_IS_KEY = "isKey";
  protected static final String PROP_IS_VALUE = "isValue";
  protected static final String PROP_SCHEMA = "schema";

  @Nonnull private String _description;

  private boolean _include;

  private boolean _isKey;

  private boolean _isValue;

  @Nonnull private Schema _schema;

  public JsonPathToTableColumn(
      Schema schema,
      @Nullable String description,
      @Nullable Boolean include,
      @Nullable Boolean isKey,
      @Nullable Boolean isValue) {
    if (schema == null) {
      throw new IllegalArgumentException("Schema not specified in the composition");
    }
    _description = firstNonNull(description, DEFAULT_DESCRIPTION);
    _include = firstNonNull(include, true);
    _isKey = firstNonNull(isKey, true);
    _isValue = firstNonNull(isValue, true);
    _schema = schema;
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

  @JsonProperty(PROP_INCLUDE)
  public boolean getInclude() {
    return _include;
  }

  @JsonProperty(PROP_SCHEMA)
  public Schema getSchema() {
    return _schema;
  }
}
