package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.NamedCommunitySet;

public class RoutePolicyCommunitySetName extends RoutePolicyCommunitySet {

  private static final long serialVersionUID = 1L;

  private final List<CommunitySetElem> _elements;

  private final String _name;

  public RoutePolicyCommunitySetName(
      @Nonnull String name, @Nonnull List<CommunitySetElem> elements) {
    _name = name;
    _elements = ImmutableList.copyOf(elements);
  }

  public @Nonnull List<CommunitySetElem> getElements() {
    return _elements;
  }

  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public CommunitySetExpr toCommunitySetExpr(CiscoConfiguration cc, Configuration c, Warnings w) {
    return new NamedCommunitySet(_name);
  }
}
