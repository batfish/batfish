package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An as-path-set element that matches an as-path whose last ASes are given by provided AS ranges.
 * Duplicates are ignored unless this match is 'exact'.
 */
@ParametersAreNonnullByDefault
public final class OriginatesFromAsPathSetElem implements AsPathSetElem {

  @SafeVarargs
  @SuppressWarnings("varargs")
  public OriginatesFromAsPathSetElem(boolean exact, Range<Long>... asRanges) {
    this(exact, ImmutableList.copyOf(asRanges));
  }

  public OriginatesFromAsPathSetElem(boolean exact, Iterable<Range<Long>> asRanges) {
    _exact = exact;
    _asRanges = ImmutableList.copyOf(asRanges);
  }

  @Override
  public <T, U> T accept(AsPathSetElemVisitor<T, U> visitor, U arg) {
    return visitor.visitOriginatesFromAsPathSetElem(this, arg);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OriginatesFromAsPathSetElem)) {
      return false;
    }
    OriginatesFromAsPathSetElem that = (OriginatesFromAsPathSetElem) o;
    return _asRanges.equals(that._asRanges) && _exact == that._exact;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asRanges, _exact);
  }

  private final @Nonnull List<Range<Long>> _asRanges;
  private final boolean _exact;
}
