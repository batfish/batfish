package org.batfish.representation.cisco;

import javax.annotation.Nonnull;
import org.batfish.datamodel.RegexCommunitySet;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;

public class CommunitySetElemIosRegex implements CommunitySetElem {

  private static final long serialVersionUID = 1L;

  private final String _regex;

  public CommunitySetElemIosRegex(@Nonnull String regex) {
    _regex = regex;
  }

  @Override
  public CommunitySetExpr toCommunitySetExpr() {
    return new RegexCommunitySet(_regex);
  }
}
