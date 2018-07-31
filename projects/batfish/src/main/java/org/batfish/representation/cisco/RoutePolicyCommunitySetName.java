package org.batfish.representation.cisco;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;

public class RoutePolicyCommunitySetName extends RoutePolicyCommunitySet {

  private static final long serialVersionUID = 1L;

  private final String _name;

  public RoutePolicyCommunitySetName(@Nonnull String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public CommunitySetExpr toCommunitySetExpr(CiscoConfiguration cc, Configuration c, Warnings w) {
    return new NamedCommunitySet(_name);
  }
}
