package org.batfish.z3.state;

import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;

public class AclDeny extends StateExpr {

  public static class State {

    public static final State INSTANCE = new State();

    private State() {}
  }

  private final String _acl;

  private final String _hostname;

  public AclDeny(String hostname, String acl) {
    _hostname = hostname;
    _acl = acl;
  }

  @Override
  public <R> R accept(GenericStateExprVisitor<R> visitor) {
    return visitor.visitAclDeny(this);
  }

  public String getAcl() {
    return _acl;
  }

  public String getHostname() {
    return _hostname;
  }
}
