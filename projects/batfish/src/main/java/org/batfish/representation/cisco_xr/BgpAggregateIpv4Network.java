package org.batfish.representation.cisco_xr;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

@ParametersAreNonnullByDefault
public class BgpAggregateIpv4Network extends BgpAggregateNetwork {

  private final @Nonnull Prefix _prefix;

  public BgpAggregateIpv4Network(Prefix prefix) {
    this(false, prefix, null, false);
  }

  public BgpAggregateIpv4Network(
      boolean asSet, Prefix prefix, @Nullable String routePolicy, boolean summaryOnly) {
    setAsSet(asSet);
    _prefix = prefix;
    setRoutePolicy(routePolicy);
    setSummaryOnly(summaryOnly);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof BgpAggregateIpv4Network)) {
      return false;
    }
    BgpAggregateIpv4Network rhs = (BgpAggregateIpv4Network) o;
    return baseEquals(rhs) && _prefix.equals(rhs._prefix);
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _prefix);
  }
}
