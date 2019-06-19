package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;

public interface CommunitySetElem extends Serializable {

  @Nonnull
  CommunitySetExpr toCommunitySetExpr();
}
