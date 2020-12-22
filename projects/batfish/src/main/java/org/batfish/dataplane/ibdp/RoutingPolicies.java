package org.batfish.dataplane.ibdp;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/** Internal iBDP implementation. A collection of all routing polices on a single device. */
@ParametersAreNonnullByDefault
final class RoutingPolicies {

  RoutingPolicies(Map<String, RoutingPolicy> policies, String hostname) {
    _policies = ImmutableMap.copyOf(policies);
    _hostname = hostname;
  }

  /** Return a routing policy of a given name */
  @Nonnull
  public Optional<RoutingPolicy> get(String name) {
    return Optional.ofNullable(_policies.get(name));
  }

  /**
   * Return a routing policy of a given name.
   *
   * @throws IllegalStateException if the routing policy does not exist
   */
  @Nonnull
  public RoutingPolicy getOrThrow(String name) {
    return get(name)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    String.format("Routing policy %s does not exist on node %s", name, _hostname)));
  }

  @Nonnull
  static RoutingPolicies from(Configuration c) {
    return new RoutingPolicies(c.getRoutingPolicies(), c.getHostname());
  }

  @Nonnull private final Map<String, RoutingPolicy> _policies;
  // For internal informational purposes only
  @Nonnull private final String _hostname;
}
