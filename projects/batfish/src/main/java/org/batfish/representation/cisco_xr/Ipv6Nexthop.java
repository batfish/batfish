package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip6;

/** Class representing an ipv6 nexthop, used in ipv6 access-list forwarding. */
@ParametersAreNonnullByDefault
public class Ipv6Nexthop implements Serializable {
  public Ip6 getIp() {
    return _ip;
  }

  @Nullable
  public String getVrf() {
    return _vrf;
  }

  public Ipv6Nexthop(Ip6 ip, @Nullable String vrf) {
    _ip = ip;
    _vrf = vrf;
  }

  @Nonnull private final Ip6 _ip;
  @Nullable private final String _vrf;
}
