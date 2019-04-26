package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

public class Accept extends StateExpr {

  public static final Accept INSTANCE = new Accept();

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitAccept();
  }
}
