package org.batfish.representation.cisco;

import javax.annotation.Nonnull;

public class VarCommunitySetElemHalf implements CommunitySetElemHalfExpr {

  private static final long serialVersionUID = 1L;

  private final String _var;

  public VarCommunitySetElemHalf(@Nonnull String var) {
    _var = var;
  }

  public @Nonnull String getVar() {
    return _var;
  }
}
