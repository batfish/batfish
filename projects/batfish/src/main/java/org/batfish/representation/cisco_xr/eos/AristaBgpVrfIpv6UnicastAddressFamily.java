package org.batfish.representation.cisco_xr.eos;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix6;

/** Settings specific to IPv6 unicast address family, which can be set at the VRF level. */
public final class AristaBgpVrfIpv6UnicastAddressFamily extends AristaBgpVrfAddressFamily {
  @Nonnull private final Map<Prefix6, AristaBgpNetworkConfiguration> _networks;

  public AristaBgpVrfIpv6UnicastAddressFamily() {
    _networks = new HashMap<>(0);
  }

  @Nonnull
  public Map<Prefix6, AristaBgpNetworkConfiguration> getNetworks() {
    return _networks;
  }

  public AristaBgpNetworkConfiguration addNetwork(Prefix6 network) {
    AristaBgpNetworkConfiguration value = new AristaBgpNetworkConfiguration();
    _networks.put(network, value);
    return value;
  }
}
