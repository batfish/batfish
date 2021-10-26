package org.batfish.symbolic.state;

import javax.annotation.Nonnull;
import org.batfish.datamodel.packet_policy.Action;

public final class PacketPolicyAction implements StateExpr {
  private final String _hostname;
  private final String _policyName;

  private final Action _action;

  public PacketPolicyAction(
      @Nonnull String hostname, @Nonnull String policyName, @Nonnull Action action) {
    _hostname = hostname;
    _policyName = policyName;
    _action = action;
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitPacketPolicyAction(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PacketPolicyAction)) {
      return false;
    }

    PacketPolicyAction that = (PacketPolicyAction) o;
    return _hostname.equals(that._hostname) && _action.equals(that._action);
  }

  @Override
  public int hashCode() {
    return 31 * 31 * 31 * PacketPolicyAction.class.hashCode()
        + 31 * 31 * _hostname.hashCode()
        + 31 * _policyName.hashCode()
        + _action.hashCode();
  }

  public Action getAction() {
    return _action;
  }
}
