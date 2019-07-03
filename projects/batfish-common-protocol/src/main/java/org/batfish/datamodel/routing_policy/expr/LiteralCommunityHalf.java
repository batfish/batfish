package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A match expression for a 16-bit half of a community that matches when the half is equal to the
 * contained value
 */
public class LiteralCommunityHalf implements CommunityHalfExpr {
  private static final String PROP_VALUE = "value";

  @JsonCreator
  private static @Nonnull LiteralCommunityHalf create(@JsonProperty(PROP_VALUE) int value) {
    return new LiteralCommunityHalf(value);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LiteralCommunityHalf)) {
      return false;
    }
    return _value == ((LiteralCommunityHalf) obj)._value;
  }

  @Override
  public int hashCode() {
    return _value;
  }

  private final int _value;

  public LiteralCommunityHalf(int value) {
    _value = value;
  }

  @Override
  public boolean dynamicMatchCommunity() {
    return false;
  }

  @JsonProperty(PROP_VALUE)
  public int getValue() {
    return _value;
  }

  @Override
  public boolean matches(int communityHalf) {
    return communityHalf == _value;
  }
}
