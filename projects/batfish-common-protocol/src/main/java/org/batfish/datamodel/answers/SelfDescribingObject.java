package org.batfish.datamodel.answers;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SelfDescribingObject {

  static final String PROP_OBJECT = "object";
  static final String PROP_SCHEMA = "schema";

  @Nullable private Object _object;
  @Nonnull private Schema _schema;

  public SelfDescribingObject(
      @JsonProperty(PROP_SCHEMA) Schema schema, @JsonProperty(PROP_OBJECT) Object object) {
    checkArgument(schema != null, "'schema' cannot be null");
    checkArgument(
        SchemaUtils.isValidObject(object, schema), "'object' is not consistent with 'schema'");
    _schema = schema;
    _object = object;
  }

  @JsonProperty(PROP_OBJECT)
  public Object getObject() {
    return _object;
  }

  @JsonProperty(PROP_SCHEMA)
  public Schema getSchema() {
    return _schema;
  }
}
