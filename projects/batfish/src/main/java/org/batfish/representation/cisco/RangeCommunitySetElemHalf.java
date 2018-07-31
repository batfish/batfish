package org.batfish.representation.cisco;

import javax.annotation.Nonnull;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.expr.CommunityHalfExpr;
import org.batfish.datamodel.routing_policy.expr.RangeCommunityHalf;

public class RangeCommunitySetElemHalf implements CommunitySetElemHalfExpr {

  private static final long serialVersionUID = 1L;

  private final SubRange _range;

  public RangeCommunitySetElemHalf(@Nonnull SubRange range) {
    _range = range;
  }

  public @Nonnull SubRange getRange() {
    return _range;
  }

  @Override
  public CommunityHalfExpr toCommunityHalfExpr() {
    return new RangeCommunityHalf(_range);
  }
}
