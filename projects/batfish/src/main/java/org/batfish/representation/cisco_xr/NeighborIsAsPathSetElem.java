package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An as-path-set element that matches an as-path whose immediate neighbor ASes are given by
 * provided AS ranges. Duplicates are ignored unless this match is 'exact'.
 */
@ParametersAreNonnullByDefault
public final class NeighborIsAsPathSetElem implements AsPathSetElem {

  @SafeVarargs
  @SuppressWarnings("varargs")
  public NeighborIsAsPathSetElem(boolean exact, Range<Long>... asRanges) {
    this(exact, ImmutableList.copyOf(asRanges));
  }

  public NeighborIsAsPathSetElem(boolean exact, Iterable<Range<Long>> asRanges) {
    _exact = exact;
    _asRanges = ImmutableList.copyOf(asRanges);
  }

  @Override
  public <T, U> T accept(AsPathSetElemVisitor<T, U> visitor, U arg) {
    return visitor.visitNeighborIsAsPathSetElem(this, arg);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NeighborIsAsPathSetElem)) {
      return false;
    }
    NeighborIsAsPathSetElem that = (NeighborIsAsPathSetElem) o;
    return _asRanges.equals(that._asRanges) && _exact == that._exact;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asRanges, _exact);
  }

  private final @Nonnull List<Range<Long>> _asRanges;
  private final boolean _exact;
}
