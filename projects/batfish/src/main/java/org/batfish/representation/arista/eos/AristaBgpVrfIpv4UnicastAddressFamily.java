package org.batfish.representation.arista.eos;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/** Settings specific to IPv4 unicast address family, which can be set at the VRF level. */
public class AristaBgpVrfIpv4UnicastAddressFamily extends AristaBgpVrfAddressFamily {
  @Nullable private String _installMap;
  @Nonnull private final Map<Prefix, AristaBgpNetworkConfiguration> _networks;
  // TODO: "bgp next-hop address-family ipv6"
  @Nullable private Boolean _redistributeInternal;

  public AristaBgpVrfIpv4UnicastAddressFamily() {
    _networks = new HashMap<>(0);
  }

  @Nullable
  public String getInstallMap() {
    return _installMap;
  }

  public void setInstallMap(@Nullable String installMap) {
    _installMap = installMap;
  }

  @Nonnull
  public Map<Prefix, AristaBgpNetworkConfiguration> getNetworks() {
    return _networks;
  }

  @Nonnull
  public AristaBgpNetworkConfiguration getOrCreateNetwork(Prefix prefix) {
    return _networks.computeIfAbsent(prefix, p -> new AristaBgpNetworkConfiguration());
  }

  @Nullable
  public Boolean getRedistributeInternal() {
    return _redistributeInternal;
  }

  public void setRedistributeInternal(@Nullable Boolean redistributeInternal) {
    _redistributeInternal = redistributeInternal;
  }
}
