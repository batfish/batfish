package org.batfish.representation.cisco;

import javax.annotation.Nonnull;
import org.batfish.datamodel.routing_policy.expr.CommunityHalfExpr;
import org.batfish.datamodel.routing_policy.expr.VarCommunityHalf;

public class VarCommunitySetElemHalf implements CommunitySetElemHalfExpr {

  private static final long serialVersionUID = 1L;

  private final String _var;

  public VarCommunitySetElemHalf(@Nonnull String var) {
    _var = var;
  }

  public @Nonnull String getVar() {
    return _var;
  }

  @Override
  public CommunityHalfExpr toCommunityHalfExpr() {
    return new VarCommunityHalf(_var);
  }
}
