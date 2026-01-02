package org.batfish.datamodel.answers;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.BatfishObjectMapper;

/**
 * Represents an object that carries its own Schema. Useful for insertion into answers when the
 * columns contain mixed Schemas
 */
@ParametersAreNonnullByDefault
public class SelfDescribingObject {

  static final String PROP_NAME = "name";
  static final String PROP_SCHEMA = "schema";
  static final String PROP_VALUE = "value";

  private final @Nullable String _name;
  private final @Nonnull Schema _schema;
  private final @Nullable Object _value;

  @JsonCreator
  private static @Nonnull SelfDescribingObject create(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_SCHEMA) Schema schema,
      @JsonProperty(PROP_VALUE) Object value) {
    return new SelfDescribingObject(name, schema, value);
  }

  public SelfDescribingObject(Schema schema, @Nullable Object value) {
    this(null, schema, value);
  }

  public SelfDescribingObject(@Nullable String name, Schema schema, @Nullable Object value) {
    checkArgument(schema != null, "'schema' cannot be null");
    checkArgument(
        SchemaUtils.isValidObject(value, schema), "'object' is not consistent with 'schema'");
    _name = name;
    _schema = schema;
    _value = value;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SelfDescribingObject)) {
      return false;
    }
    SelfDescribingObject rhs = (SelfDescribingObject) obj;
    return Objects.equals(_name, rhs._name)
        && _schema.equals(rhs._schema)
        && Objects.equals(_value, rhs._value);
  }

  @JsonProperty(PROP_NAME)
  public @Nullable String getName() {
    return _name;
  }

  @JsonProperty(PROP_SCHEMA)
  public @Nonnull Schema getSchema() {
    return _schema;
  }

  @JsonProperty(PROP_VALUE)
  public @Nullable Object getValue() {
    return _value;
  }

  @JsonIgnore
  public Object getTypedValue() {
    JsonNode jsonNode = BatfishObjectMapper.mapper().valueToTree(_value);
    return SchemaUtils.convertType(jsonNode, _schema);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _schema, _value);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_NAME, _name)
        .add(PROP_SCHEMA, _schema)
        .add(PROP_VALUE, _value)
        .toString();
  }
}
