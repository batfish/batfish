package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

public class AclLineIndependentMatch extends StateExpr {

  public static class State {

    public static final State INSTANCE = new State();

    private State() {}
  }

  private final String _acl;

  private final String _hostname;

  private final int _line;

  public AclLineIndependentMatch(String hostname, String acl, int line) {
    _hostname = hostname;
    _acl = acl;
    _line = line;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitAclLineIndependentMatch(this);
  }

  public String getAcl() {
    return _acl;
  }

  public String getHostname() {
    return _hostname;
  }

  public int getLine() {
    return _line;
  }
}
