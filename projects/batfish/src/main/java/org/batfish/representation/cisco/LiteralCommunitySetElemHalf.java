package org.batfish.representation.cisco;

import org.batfish.datamodel.routing_policy.expr.CommunityHalfExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunityHalf;

public class LiteralCommunitySetElemHalf implements CommunitySetElemHalfExpr {

  private final int _value;

  public LiteralCommunitySetElemHalf(int value) {
    _value = value;
  }

  public int getValue() {
    return _value;
  }

  @Override
  public CommunityHalfExpr toCommunityHalfExpr() {
    return new LiteralCommunityHalf(_value);
  }
}
