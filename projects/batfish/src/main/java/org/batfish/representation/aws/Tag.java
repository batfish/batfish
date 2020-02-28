package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a tag for a given instance */
@ParametersAreNonnullByDefault
public class Tag {

  @Nonnull private final String _key;

  @Nonnull private final String _value;

  @JsonCreator
  private static Tag create(
      @Nullable @JsonProperty("Key") String key, @Nullable @JsonProperty("Value") String value) {
    checkArgument(key != null, "Tag key is null");
    checkArgument(value != null, "Tag value is null");
    return new Tag(key, value);
  }

  private Tag(String key, String value) {
    _key = key;
    _value = value;
  }

  @Nonnull
  public String getKey() {
    return _key;
  }

  @Nonnull
  public String getValue() {
    return _value;
  }
}
