package org.batfish.representation.cisco_xr;

import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.NamedPrefix6Set;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.Prefix6SetExpr;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;

public class RoutePolicyPrefixSetName extends RoutePolicyPrefixSet {

  private final String _name;

  public RoutePolicyPrefixSetName(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  @Nullable
  @Override
  public Prefix6SetExpr toPrefix6SetExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    if (cc.getPrefixLists().containsKey(_name)) {
      return null;
    }
    return new NamedPrefix6Set(_name);
  }

  @Nullable
  @Override
  public PrefixSetExpr toPrefixSetExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    if (cc.getPrefix6Lists().containsKey(_name)) {
      return null;
    }
    return new NamedPrefixSet(_name);
  }
}
