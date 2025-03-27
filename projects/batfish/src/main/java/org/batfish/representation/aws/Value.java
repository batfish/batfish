package org.batfish.representation.aws;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Value implements Serializable {
  @Nonnull private final String _value;

  @JsonCreator
  public Value(@JsonProperty("Value") @Nullable String value) {
    _value = value != null ? value : "";
  }

  @Nonnull
  String getValue() {
    return _value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Value that)) {
      return false;
    }
    return this._value.equals(that._value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_value);
  }
}
