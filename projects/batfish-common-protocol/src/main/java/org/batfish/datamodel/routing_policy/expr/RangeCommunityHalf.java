package org.batfish.datamodel.routing_policy.expr;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.SubRange;

public class RangeCommunityHalf implements CommunityHalfExpr {

  private static final String PROP_RANGE = "range";

  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static @Nonnull RangeCommunityHalf create(@JsonProperty(PROP_RANGE) SubRange range) {
    return new RangeCommunityHalf(requireNonNull(range));
  }

  private final SubRange _range;

  public RangeCommunityHalf(@Nonnull SubRange range) {
    _range = range;
  }

  @Override
  public boolean dynamicMatchCommunity() {
    return false;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RangeCommunityHalf)) {
      return false;
    }
    return _range.equals(((RangeCommunityHalf) obj)._range);
  }

  @JsonProperty(PROP_RANGE)
  public @Nonnull SubRange getRange() {
    return _range;
  }

  @Override
  public int hashCode() {
    return _range.hashCode();
  }

  @Override
  public boolean matches(int communityHalf) {
    return _range.includes(communityHalf);
  }
}
