package org.batfish.specifier;

import java.util.Set;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/** An abstract specification of a set of routing policies in the network. */
public interface RoutingPolicySpecifier {
  /**
   * Returns the routing policies on {@code node} that match this specifier.
   *
   * @param ctxt Information about the network that may be used to determine match.
   */
  Set<RoutingPolicy> resolve(String node, SpecifierContext ctxt);
}
