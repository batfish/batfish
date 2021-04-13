package org.batfish.representation.cisco_xr;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An {@link RdSetElem} composed of an expression for matching the high 32 bits, and en expression
 * for matching the low 16 bits of route distinguisher in ASPLAIN format with a 32-bit AS.
 */
@ParametersAreNonnullByDefault
public final class RdSetAsPlain32 implements RdSetElem {

  public RdSetAsPlain32(Uint32RangeExpr high, Uint16RangeExpr low) {
    _high = high;
    _low = low;
  }

  @Override
  public <T, U> T accept(RdSetElemVisitor<T, U> visitor, U arg) {
    return visitor.visitRdSetAsPlain32(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RdSetAsPlain32)) {
      return false;
    }
    RdSetAsPlain32 that = (RdSetAsPlain32) o;
    return _high.equals(that._high) && _low.equals(that._low);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_high, _low);
  }

  @Nonnull private final Uint32RangeExpr _high;
  @Nonnull private final Uint16RangeExpr _low;
}
