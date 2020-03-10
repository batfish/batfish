package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a tag for an AWS resource */
@ParametersAreNonnullByDefault
public class Tag {
  private static final String PROP_KEY = "Key";
  private static final String PROP_VALUE = "Value";

  @Nonnull private final String _key;
  @Nonnull private final String _value;

  @JsonCreator
  private static Tag create(
      @Nullable @JsonProperty(PROP_KEY) String key,
      @Nullable @JsonProperty(PROP_VALUE) String value) {
    checkArgument(key != null, "Missing %s", PROP_KEY);
    checkArgument(value != null, "Missing %s", PROP_VALUE);
    return new Tag(key, value);
  }

  public Tag(String key, String value) {
    _key = key;
    _value = value;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Tag)) {
      return false;
    }
    Tag tag = (Tag) o;
    return _key.equals(tag._key) && _value.equals(tag._value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_key, _value);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("_key", _key).add("_value", _value).toString();
  }

  @JsonProperty(PROP_KEY)
  @Nonnull
  public String getKey() {
    return _key;
  }

  @JsonProperty(PROP_VALUE)
  @Nonnull
  public String getValue() {
    return _value;
  }
}
