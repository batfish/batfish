package org.batfish.vendor.arista.representation.eos;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix6;

/** Settings specific to IPv6 unicast address family, which can be set at the VRF level. */
public final class AristaBgpVrfIpv6UnicastAddressFamily extends AristaBgpVrfAddressFamily {
  private final @Nonnull Map<Prefix6, AristaBgpNetworkConfiguration> _networks;

  public AristaBgpVrfIpv6UnicastAddressFamily() {
    _networks = new HashMap<>(0);
  }

  public @Nonnull Map<Prefix6, AristaBgpNetworkConfiguration> getNetworks() {
    return _networks;
  }

  public AristaBgpNetworkConfiguration addNetwork(Prefix6 network) {
    AristaBgpNetworkConfiguration value = new AristaBgpNetworkConfiguration();
    _networks.put(network, value);
    return value;
  }
}
