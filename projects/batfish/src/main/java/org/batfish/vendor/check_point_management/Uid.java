package org.batfish.vendor.check_point_management;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import javax.annotation.Nonnull;

public final class Uid implements Serializable {

  @JsonCreator
  public static @Nonnull Uid of(String value) {
    return new Uid(value);
  }

  private Uid(String value) {
    _value = value;
  }

  @JsonValue
  public @Nonnull String getValue() {
    return _value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Uid)) {
      return false;
    }
    Uid uid = (Uid) o;
    return _value.equals(uid._value);
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_value", _value).toString();
  }

  @Override
  public int hashCode() {
    return _value.hashCode();
  }

  private @Nonnull String _value;
}
