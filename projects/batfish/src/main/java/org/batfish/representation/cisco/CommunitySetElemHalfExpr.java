package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.datamodel.routing_policy.expr.CommunityHalfExpr;

public interface CommunitySetElemHalfExpr extends Serializable {

  CommunityHalfExpr toCommunityHalfExpr();
}
