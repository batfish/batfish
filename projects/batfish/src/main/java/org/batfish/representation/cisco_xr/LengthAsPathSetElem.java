package org.batfish.representation.cisco_xr;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.expr.IntComparator;

/** An as-path-set element that matches an as-path with a given length by a given comparator. */
@ParametersAreNonnullByDefault
public final class LengthAsPathSetElem implements AsPathSetElem {

  public LengthAsPathSetElem(IntComparator comparator, int length, boolean all) {
    _comparator = comparator;
    _length = length;
    _all = all;
  }

  @Override
  public <T> T accept(AsPathSetElemVisitor<T> visitor) {
    return visitor.visitLengthAsPathSetElem(this);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LengthAsPathSetElem)) {
      return false;
    }
    LengthAsPathSetElem that = (LengthAsPathSetElem) o;
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
