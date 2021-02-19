package org.batfish.representation.cumulus_nclu;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;

/**
 * A route-map condition that matches a route whose network is assigned to one of a set of provided
 * interfaces.
 */
public class RouteMapMatchInterface implements RouteMapMatch {

  private final @Nonnull Set<String> _interfaces;

  public RouteMapMatchInterface(Set<String> interfaces) {
    _interfaces = ImmutableSet.copyOf(interfaces);
  }

  public @Nonnull Set<String> getInterfaces() {
    return _interfaces;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RouteMapMatchInterface)) {
      return false;
    }
    return _interfaces.equals(((RouteMapMatchInterface) obj)._interfaces);
  }

  @Override
  public int hashCode() {
    return _interfaces.hashCode();
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, CumulusNcluConfiguration vc, Warnings w) {
    return new MatchPrefixSet(
        DestinationNetwork.instance(),
        new ExplicitPrefixSet(
            new PrefixSpace(
                _interfaces.stream()
                    // remove non-existent interfaces mentioned in the configuration
                    .filter(ifaceName -> c.getAllInterfaces().containsKey(ifaceName))
                    .flatMap(
                        ifaceName ->
                            c.getAllInterfaces().get(ifaceName).getAllConcreteAddresses().stream())
                    .map(ConcreteInterfaceAddress::getPrefix)
                    .map(PrefixRange::fromPrefix)
                    .collect(ImmutableSet.toImmutableSet()))));
  }
}
