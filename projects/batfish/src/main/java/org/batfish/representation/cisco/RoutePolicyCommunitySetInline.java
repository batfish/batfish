package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;

public class RoutePolicyCommunitySetInline extends RoutePolicyCommunitySet {

  private static final long serialVersionUID = 1L;

  private List<CommunitySetElem> _elements;

  public RoutePolicyCommunitySetInline(List<CommunitySetElem> elements) {
    _elements = ImmutableList.copyOf(elements);
  }

  public List<CommunitySetElem> getElements() {
    return _elements;
  }

  @Override
  public CommunitySetExpr toCommunitySetExpr(CiscoConfiguration cc, Configuration c, Warnings w) {
    return new CommunityList(
        "",
        _elements
            .stream()
            .map(CommunitySetElem::toCommunitySetExpr)
            .map(CommunityListLine::accepting)
            .collect(ImmutableList.toImmutableList()),
        false);
  }
}
