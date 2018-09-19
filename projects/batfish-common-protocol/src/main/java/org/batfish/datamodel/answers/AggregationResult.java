package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;

public class AggregationResult {

  private static Object convertType(@Nullable Object o) {
    if (o instanceof Byte) {
      return ((Byte) o).longValue();
    }
    if (o instanceof Short) {
      return ((Short) o).longValue();
    }
    if (o instanceof Integer) {
      return ((Integer) o).longValue();
    }
    return o;
  }

  @JsonCreator
  public static AggregationResult of(@Nullable Object o) {
    return new AggregationResult(convertType(o));
  }

  private Object _value;

  private AggregationResult(@Nullable Object value) {
    _value = value;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof AggregationResult)) {
      return false;
    }
    return Objects.equals(_value, ((AggregationResult) obj)._value);
  }

  @JsonValue
  public @Nullable Object getValue() {
    return _value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_value);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("value", _value).toString();
  }
}
