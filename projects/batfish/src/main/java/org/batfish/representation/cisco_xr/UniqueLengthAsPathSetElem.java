package org.batfish.representation.cisco_xr;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.expr.IntComparator;

/**
 * An as-path-set element that matches an as-path with a given length by a given comparator,
 * considering only non-repeated elements of the as-path.
 */
@ParametersAreNonnullByDefault
public final class UniqueLengthAsPathSetElem implements AsPathSetElem {

  public UniqueLengthAsPathSetElem(IntComparator comparator, int length, boolean all) {
    _comparator = comparator;
    _length = length;
    _all = all;
  }

  @Override
  public <T> T accept(AsPathSetElemVisitor<T> visitor) {
    return visitor.visitUniqueLengthAsPathSetElem(this);
  }

  public @Nonnull IntComparator getComparator() {
    return _comparator;
  }

  public int getLength() {
    return _length;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UniqueLengthAsPathSetElem)) {
      return false;
    }
    UniqueLengthAsPathSetElem that = (UniqueLengthAsPathSetElem) o;
    return _length == that._length && _all == that._all && _comparator == that._comparator;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_comparator.ordinal(), _length, _all);
  }

  private final @Nonnull IntComparator _comparator;
  private final int _length;
  private final boolean _all;
}
