package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a tag for a given instance */
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
    checkArgument(key != null, "Tag key is null");
    checkArgument(value != null, "Tag value is null");
    return new Tag(key, value);
  }

  private Tag(String key, String value) {
    _key = key;
    _value = value;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Nonnull
  @JsonProperty(PROP_KEY)
  public String getKey() {
    return _key;
  }

  @Nonnull
  @JsonProperty(PROP_VALUE)
  public String getValue() {
    return _value;
  }

  @Override
  public boolean equals(Object o) {
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

  public static final class Builder {
    @Nullable private String _key;
    @Nullable private String _value;

    @Nonnull
    public Builder setKey(String key) {
      _key = key;
      return this;
    }

    @Nonnull
    public Builder setValue(String value) {
      _value = value;
      return this;
    }

    public Builder() {}

    public Tag build() {
      checkArgument(_key != null, "Missing %s", PROP_KEY);
      checkArgument(_value != null, "Missing %s", PROP_VALUE);
      return new Tag(_key, _value);
    }
  }
}
