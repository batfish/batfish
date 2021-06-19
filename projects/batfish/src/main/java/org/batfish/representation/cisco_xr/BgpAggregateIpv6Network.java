package org.batfish.representation.cisco_xr;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix6;

@ParametersAreNonnullByDefault
public class BgpAggregateIpv6Network extends BgpAggregateNetwork {

  private final @Nonnull Prefix6 _prefix6;

  public BgpAggregateIpv6Network(Prefix6 prefix6) {
    _prefix6 = prefix6;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof BgpAggregateIpv6Network)) {
      return false;
    }
    BgpAggregateIpv6Network rhs = (BgpAggregateIpv6Network) o;
    return baseEquals(rhs) && _prefix6.equals(rhs._prefix6);
  }

  public @Nonnull Prefix6 getPrefix6() {
    return _prefix6;
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _prefix6);
  }
}
