package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Class representing an ipv4 nexthop, used in ipv4 access-list forwarding. */
@ParametersAreNonnullByDefault
public class Ipv4Nexthop implements Serializable {
  public Ip getIp() {
    return _ip;
  }

  public @Nullable String getVrf() {
    return _vrf;
  }

  public Ipv4Nexthop(Ip ip, @Nullable String vrf) {
    _ip = ip;
    _vrf = vrf;
  }

  private final @Nonnull Ip _ip;
  private final @Nullable String _vrf;
}
