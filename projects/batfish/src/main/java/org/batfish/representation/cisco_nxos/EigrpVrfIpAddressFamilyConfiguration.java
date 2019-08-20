package org.batfish.representation.cisco_nxos;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the EIGRP configuration for IPv4 or IPv6 address families at the VRF level.
 *
 * <p>Child classes such as {@link EigrpVrfIpv4AddressFamilyConfiguration} contain the
 * v4/v6-specific code.
 */
public abstract class EigrpVrfIpAddressFamilyConfiguration implements Serializable {

  public EigrpVrfIpAddressFamilyConfiguration() {
    _redistributionPolicies = new HashMap<>();
  }

  /** Return all redistribution policies. */
  public final @Nonnull List<RedistributionPolicy> getRedistributionPolicies() {
    return ImmutableList.copyOf(_redistributionPolicies.values());
  }

  /** Return all redistribution policies for the given protocol. */
  public final @Nonnull List<RedistributionPolicy> getRedistributionPolicies(
      NxosRoutingProtocol protocol) {
    return _redistributionPolicies.values().stream()
        .filter(rp -> rp.getInstance().getProtocol() == protocol)
        .collect(ImmutableList.toImmutableList());
  }

  /** Return the redistribution policy for the given instance, if one has been configured. */
  public final @Nullable RedistributionPolicy getRedistributionPolicy(
      RoutingProtocolInstance protocol) {
    return _redistributionPolicies.get(protocol);
  }

  /** Set the redistribution policy for the given instance. */
  public final void setRedistributionPolicy(RoutingProtocolInstance instance, String routeMap) {
    _redistributionPolicies.put(instance, new RedistributionPolicy(instance, routeMap));
  }

  private final Map<RoutingProtocolInstance, RedistributionPolicy> _redistributionPolicies;
}
