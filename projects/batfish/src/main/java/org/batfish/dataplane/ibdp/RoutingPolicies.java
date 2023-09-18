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
  public @Nonnull Optional<RoutingPolicy> get(String name) {
    return Optional.ofNullable(_policies.get(name));
  }

  /**
   * Return a routing policy of a given name.
   *
   * @throws IllegalStateException if the routing policy does not exist
   */
  public @Nonnull RoutingPolicy getOrThrow(String name) {
    return get(name)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    String.format("Routing policy %s does not exist on node %s", name, _hostname)));
  }

  static @Nonnull RoutingPolicies from(Configuration c) {
    return new RoutingPolicies(c.getRoutingPolicies(), c.getHostname());
  }

  private final @Nonnull Map<String, RoutingPolicy> _policies;
  // For internal informational purposes only
  private final @Nonnull String _hostname;
}
