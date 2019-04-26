package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

public class NodeDropAclOut extends StateExpr {

  private final String _hostname;

  public NodeDropAclOut(String hostname) {
    _hostname = hostname;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitNodeDropAclOut(this);
  }

  public String getHostname() {
    return _hostname;
  }
}
