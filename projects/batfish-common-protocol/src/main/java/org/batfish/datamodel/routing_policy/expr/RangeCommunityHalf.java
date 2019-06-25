package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.SubRange;

/**
 * A match expression for a 16-bit half of a community that matches when the half is in the
 * contained interval
 */
public class RangeCommunityHalf implements CommunityHalfExpr {

  public static final RangeCommunityHalf ALL = new RangeCommunityHalf(new SubRange(0, 65535));
  private static final String PROP_RANGE = "range";

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

  @Override
  public String toString() {
    return toStringHelper(getClass()).add(PROP_RANGE, _range).toString();
  }
}
