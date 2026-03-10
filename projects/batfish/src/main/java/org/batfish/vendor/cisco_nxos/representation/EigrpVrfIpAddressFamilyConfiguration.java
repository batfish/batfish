package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/**
 * Represents the EIGRP configuration for IPv4 or IPv6 address families at the VRF level.
 *
 * <p>Child classes such as {@link EigrpVrfIpv4AddressFamilyConfiguration} contain the
 * v4/v6-specific code.
 */
public abstract class EigrpVrfIpAddressFamilyConfiguration implements Serializable {

  public EigrpVrfIpAddressFamilyConfiguration() {
    _networks = new HashSet<>();
    _redistributionPolicies = new HashMap<>();
  }

  /** Return default metric if configured. */
  public @Nullable EigrpMetric getDefaultMetric() {
    return _defaultMetric;
  }

  public final @Nonnull Set<Prefix> getNetworks() {
    return ImmutableSet.copyOf(_networks);
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

  public final void addNetwork(Prefix network) {
    _networks.add(network);
  }

  /** Set the default metric values for the given instance. */
  public final void setDefaultMetric(EigrpMetric defaultMetric) {
    _defaultMetric = defaultMetric;
  }

  /** Set the redistribution policy for the given instance. */
  public final void setRedistributionPolicy(RoutingProtocolInstance instance, String routeMap) {
    _redistributionPolicies.put(instance, new RedistributionPolicy(instance, routeMap));
  }

  private @Nullable EigrpMetric _defaultMetric;
  private final Set<Prefix> _networks;
  private final Map<RoutingProtocolInstance, RedistributionPolicy> _redistributionPolicies;
}
