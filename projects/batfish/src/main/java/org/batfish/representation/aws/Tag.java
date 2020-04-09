package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
