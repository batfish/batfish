package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;

/** Represents a "from interface" line in a {@link PsTerm} */
public final class PsFromInterface extends PsFrom {

  private final String _name;

  public PsFromInterface(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    Interface iface = c.getAllInterfaces().get(_name);
    if (iface == null) {
      // No such interface, won't match anything
      return BooleanExprs.FALSE;
    }
    // Convert to conjunction of connected protocol and matching at least one of the interface
    // prefixes
    return new Conjunction(
        ImmutableList.of(
            new MatchProtocol(RoutingProtocol.CONNECTED),
            new MatchPrefixSet(
                DestinationNetwork.instance(),
                new ExplicitPrefixSet(
                    new PrefixSpace(
                        c.getAllInterfaces().get(_name).getAllConcreteAddresses().stream()
                            .map(ConcreteInterfaceAddress::getPrefix)
                            .map(PrefixRange::fromPrefix)
                            .collect(ImmutableSet.toImmutableSet()))))));
  }
}
