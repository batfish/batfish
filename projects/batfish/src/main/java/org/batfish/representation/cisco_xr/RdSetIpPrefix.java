package org.batfish.representation.cisco_xr;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

/**
 * An {@link RdSetElem} that matches the a route distinguisher whose high 16 bits fall within a
 * {@link Prefix}, and whose low 16 bits are matched by an expression.
 */
@ParametersAreNonnullByDefault
public final class RdSetIpPrefix implements RdSetElem {

  public RdSetIpPrefix(Prefix prefix, Uint16RangeExpr low) {
    _prefix = prefix;
    _low = low;
  }

  @Override
  public <T, U> T accept(RdSetElemVisitor<T, U> visitor, U arg) {
    return visitor.visitRdSetIpPrefix(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RdSetIpPrefix)) {
      return false;
    }
    RdSetIpPrefix that = (RdSetIpPrefix) o;
    return _prefix.equals(that._prefix) && _low.equals(that._low);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefix, _low);
  }

  @Nonnull private final Prefix _prefix;
  @Nonnull private final Uint16RangeExpr _low;
}
