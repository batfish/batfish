package org.batfish.representation.cisco;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;

public class RoutePolicyCommunitySetInline extends RoutePolicyCommunitySet {

  private static final long serialVersionUID = 1L;

  private List<CommunitySetElem> _elems;

  public RoutePolicyCommunitySetInline(List<CommunitySetElem> elems) {
    _elems = elems;
  }

  public List<CommunitySetElem> getNumbers() {
    return _elems;
  }

  @Override
  public CommunitySetExpr toCommunitySetExpr(CiscoConfiguration cc, Configuration c, Warnings w) {
    return new InlineCommunitySet(_elems);
  }
}
