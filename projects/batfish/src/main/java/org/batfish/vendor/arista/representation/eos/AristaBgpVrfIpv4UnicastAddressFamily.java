package org.batfish.vendor.arista.representation.eos;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/** Settings specific to IPv4 unicast address family, which can be set at the VRF level. */
public class AristaBgpVrfIpv4UnicastAddressFamily extends AristaBgpVrfAddressFamily {
  private @Nullable String _installMap;
  private final @Nonnull Map<Prefix, AristaBgpNetworkConfiguration> _networks;
  // TODO: "bgp next-hop address-family ipv6"
  private @Nullable Boolean _redistributeInternal;

  public AristaBgpVrfIpv4UnicastAddressFamily() {
    _networks = new HashMap<>(0);
  }

  public @Nullable String getInstallMap() {
    return _installMap;
  }

  public void setInstallMap(@Nullable String installMap) {
    _installMap = installMap;
  }

  public @Nonnull Map<Prefix, AristaBgpNetworkConfiguration> getNetworks() {
    return _networks;
  }

  public @Nonnull AristaBgpNetworkConfiguration getOrCreateNetwork(Prefix prefix) {
    return _networks.computeIfAbsent(prefix, p -> new AristaBgpNetworkConfiguration());
  }

  public @Nullable Boolean getRedistributeInternal() {
    return _redistributeInternal;
  }

  public void setRedistributeInternal(@Nullable Boolean redistributeInternal) {
    _redistributeInternal = redistributeInternal;
  }
}
