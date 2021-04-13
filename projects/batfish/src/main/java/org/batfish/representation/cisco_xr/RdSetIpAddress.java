package org.batfish.representation.cisco_xr;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/**
 * An {@link RdSetElem} that matches the high 32 bits of a route distinguisher via a literal IP
 * address, and the low 16 bits via an expression.
 */
@ParametersAreNonnullByDefault
public final class RdSetIpAddress implements RdSetElem {

  public RdSetIpAddress(Ip ip, Uint16RangeExpr low) {
    _ip = ip;
    _low = low;
  }

  @Override
  public <T, U> T accept(RdSetElemVisitor<T, U> visitor, U arg) {
    return visitor.visitRdSetIpAddress(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RdSetIpAddress)) {
      return false;
    }
    RdSetIpAddress that = (RdSetIpAddress) o;
    return _ip.equals(that._ip) && _low.equals(that._low);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ip, _low);
  }

  @Nonnull private final Ip _ip;
  @Nonnull private final Uint16RangeExpr _low;
}
