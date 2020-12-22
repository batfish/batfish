package org.batfish.dataplane.ibdp;

import com.google.common.collect.ImmutableSortedMap;
import java.util.Collection;
import java.util.Optional;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.dataplane.rib.RibId;
import org.batfish.dataplane.rib.Rib;

/** Dataplane-specific encapsulation of {@link Configuration} */
@ParametersAreNonnullByDefault
public final class Node {

  private final Configuration _c;
  private final SortedMap<String, VirtualRouter> _virtualRouters;
  @Nonnull private final RoutingPolicies _routingPolicies;

  /**
   * Create a new node based on the configuration. Initializes virtual routers based on {@link
   * Configuration} VRFs.
   *
   * @param configuration the {@link Configuration} backing this node
   */
  public Node(Configuration configuration) {
    _c = configuration;
    ImmutableSortedMap.Builder<String, VirtualRouter> b = ImmutableSortedMap.naturalOrder();
    for (String vrfName : _c.getVrfs().keySet()) {
      VirtualRouter vr = new VirtualRouter(vrfName, this);
      b.put(vrfName, vr);
    }
    _virtualRouters = b.build();
    _routingPolicies = RoutingPolicies.from(configuration);
  }

  /** @return The {@link Configuration} backing this Node */
  @Nonnull
  public Configuration getConfiguration() {
    return _c;
  }

  /** Returns all routing policies present in the configuration of this node. */
  @Nonnull
  public RoutingPolicies getRoutingPolicies() {
    return _routingPolicies;
  }

  /** Return the list of virtual routers at this node */
  @Nonnull
  Collection<VirtualRouter> getVirtualRouters() {
    return _virtualRouters.values();
  }

  /** Return a virtual router with a given name, if it exists */
  @Nonnull
  Optional<VirtualRouter> getVirtualRouter(String vrfName) {
    return Optional.ofNullable(_virtualRouters.get(vrfName));
  }

  /**
   * Return a virtual router with a given name, if it exists, or throw
   *
   * @throws IllegalStateException if the virtual router does not exist.
   */
  @Nonnull
  VirtualRouter getVirtualRouterOrThrow(String vrfName) {
    return getVirtualRouter(vrfName)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    String.format(
                        "Cannot find VirtualRouter %s on node %s", vrfName, _c.getHostname())));
  }

  /** Return a specific main RIB */
  @Nonnull
  Optional<Rib> getRib(RibId ribId) {
    if (!_c.getHostname().equals(ribId.getHostname())) {
      return Optional.empty();
    }
    return getVirtualRouter(ribId.getVrfName()).map(vr -> vr.getRib(ribId)).get();
  }
}
