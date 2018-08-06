package org.batfish.datamodel.answers;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SelfDescribingObject {

  static final String PROP_SCHEMA = "schema";
  static final String PROP_VALUE = "value";

  @Nonnull private Schema _schema;
  @Nullable private Object _value;

  public SelfDescribingObject(
      @JsonProperty(PROP_SCHEMA) Schema schema, @JsonProperty(PROP_VALUE) Object value) {
    checkArgument(schema != null, "'schema' cannot be null");
    checkArgument(
        SchemaUtils.isValidObject(value, schema), "'object' is not consistent with 'schema'");
    _schema = schema;
    _value = value;
  }

  @JsonProperty(PROP_SCHEMA)
  public Schema getSchema() {
    return _schema;
  }

  @JsonProperty(PROP_VALUE)
  public Object getValue() {
    return _value;
  }
}
