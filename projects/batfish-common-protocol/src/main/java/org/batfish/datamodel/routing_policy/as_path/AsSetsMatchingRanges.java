package org.batfish.datamodel.routing_policy.as_path;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An {@link AsPathMatchExpr} matching an {@link org.batfish.datamodel.AsPath} that contains a
 * sequence of {@link org.batfish.datamodel.AsSet}s, each of which contains an AS within a
 * corresponding sequence of AS ranges. The match may depend on the sequence being anchored at
 * either, both, or neither of the start/end of the {@link org.batfish.datamodel.AsPath}.
 */
public final class AsSetsMatchingRanges extends AsPathMatchExpr {

  public static @Nonnull AsSetsMatchingRanges of(
      boolean anchorEnd, boolean anchorStart, List<Range<Long>> asRanges) {
    return new AsSetsMatchingRanges(anchorEnd, anchorStart, asRanges);
  }

  @Override
  public <T, U> T accept(AsPathMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitAsSetsMatchingRanges(this, arg);
  }

  /**
   * Returns {@code true} iff a match must terminate at the last element of the input {@link
   * org.batfish.datamodel.AsPath}.
   */
  @JsonProperty(PROP_ANCHOR_END)
  public boolean getAnchorEnd() {
    return _anchorEnd;
  }

  /**
   * Returns {@code true} iff a match must begin at the first element of the input {@link
   * org.batfish.datamodel.AsPath}.
   */
  @JsonProperty(PROP_ANCHOR_START)
  public boolean getAnchorStart() {
    return _anchorStart;
  }

  /**
   * Returns the list of ranges to be matched against a subsequence of the {@link
   * org.batfish.datamodel.AsSet}s of the input {@link org.batfish.datamodel.AsPath}. A range
   * matches an {@link org.batfish.datamodel.AsSet} if any element of the {@link
   * org.batfish.datamodel.AsSet} is within the range.
   */
  @JsonProperty(PROP_AS_RANGES)
  public @Nonnull List<Range<Long>> getAsRanges() {
    return _asRanges;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof AsSetsMatchingRanges)) {
      return false;
    }
    AsSetsMatchingRanges that = (AsSetsMatchingRanges) obj;
    return _anchorEnd == that._anchorEnd
        && _anchorStart == that._anchorStart
        && _asRanges.equals(that._asRanges);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_anchorEnd, _anchorStart, _asRanges);
  }

  private static final String PROP_ANCHOR_END = "anchorEnd";
  private static final String PROP_ANCHOR_START = "anchorStart";
  private static final String PROP_AS_RANGES = "asRanges";

  @JsonCreator
  private static @Nonnull AsSetsMatchingRanges create(
      @JsonProperty(PROP_ANCHOR_END) boolean anchorEnd,
      @JsonProperty(PROP_ANCHOR_START) boolean anchorStart,
      @JsonProperty(PROP_AS_RANGES) @Nullable List<Range<Long>> asRanges) {
    return of(
        anchorEnd, anchorStart, ImmutableList.copyOf(firstNonNull(asRanges, ImmutableList.of())));
  }

  private final boolean _anchorEnd;
  private final boolean _anchorStart;
  private final List<Range<Long>> _asRanges;

  private AsSetsMatchingRanges(boolean anchorEnd, boolean anchorStart, List<Range<Long>> asRanges) {
    _anchorEnd = anchorEnd;
    _anchorStart = anchorStart;
    _asRanges = asRanges;
  }
}
