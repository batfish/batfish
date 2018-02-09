package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.StateExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public class AclDeny extends StateExpr {

  public static class State extends StateExpr.State {

    public static final State INSTANCE = new State();

    private State() {}

    @Override
    public void accept(StateVisitor visitor) {
      visitor.visitAclDeny(this);
    }
  }

  private final String _acl;

  private final String _hostname;

  public AclDeny(String hostname, String acl) {
    _hostname = hostname;
    _acl = acl;
  }

  @Override
  public void accept(StateExprVisitor visitor) {
    visitor.visitAclDeny(this);
  }

  public String getAcl() {
    return _acl;
  }

  public String getHostname() {
    return _hostname;
  }
}
