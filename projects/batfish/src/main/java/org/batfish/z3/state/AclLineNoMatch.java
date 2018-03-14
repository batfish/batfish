package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class AclLineNoMatch extends StateExpr {

  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitAclLineNoMatch(this);
    }
  }

  private final String _acl;

  private final String _hostname;

  private final int _line;

  public AclLineNoMatch(String hostname, String acl, int line) {
    _hostname = hostname;
    _acl = acl;
    _line = line;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitAclLineNoMatch(this);
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

  @Override
  public State getState() {
    return State.INSTANCE;
  }
}
