package org.batfish.representation.cisco_xr;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An {@link RdSetElem} composed of 3 expressions for matching the high, middle, and low 16-bits of
 * a route distinguisher in ASDOT format.
 */
@ParametersAreNonnullByDefault
public final class RdSetAsDot implements RdSetElem {

  public RdSetAsDot(Uint16RangeExpr asHigh16, Uint16RangeExpr asLow16, Uint16RangeExpr low16) {
    _asHigh16 = asHigh16;
    _asLow16 = asLow16;
    _low16 = low16;
  }

  @Override
  public <T, U> T accept(RdSetElemVisitor<T, U> visitor, U arg) {
    return visitor.visitRdSetAsDot(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RdSetAsDot)) {
      return false;
    }
    RdSetAsDot that = (RdSetAsDot) o;
    return _asHigh16.equals(that._asHigh16)
        && _asLow16.equals(that._asLow16)
        && _low16.equals(that._low16);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asHigh16, _asLow16, _low16);
  }

  @Nonnull private final Uint16RangeExpr _asHigh16;
  @Nonnull private final Uint16RangeExpr _asLow16;
  @Nonnull private final Uint16RangeExpr _low16;
}
