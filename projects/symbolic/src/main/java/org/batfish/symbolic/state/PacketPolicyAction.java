package org.batfish.symbolic.state;

import javax.annotation.Nonnull;
import org.batfish.datamodel.packet_policy.Action;

public final class PacketPolicyAction implements StateExpr {
  private final String _hostname;
  private final String _vrf;
  private final String _policyName;

  private final Action _action;

  public PacketPolicyAction(
      @Nonnull String hostname,
      @Nonnull String vrf,
      @Nonnull String policyName,
      @Nonnull Action action) {
    _hostname = hostname;
    _vrf = vrf;
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
    return _hostname.equals(that._hostname)
        && _vrf.equals(that._vrf)
        && _policyName.equals(that._policyName)
        && _action.equals(that._action);
  }

  @Override
  public int hashCode() {
    return 31 * 31 * 31 * 31 * PacketPolicyAction.class.hashCode()
        + 31 * 31 * 31 * _hostname.hashCode()
        + 31 * 31 * _vrf.hashCode()
        + 31 * _policyName.hashCode()
        + _action.hashCode();
  }

  public @Nonnull Action getAction() {
    return _action;
  }

  public @Nonnull String getHostname() {
    return _hostname;
  }

  public @Nonnull String getPolicyName() {
    return _policyName;
  }

  public @Nonnull String getVrf() {
    return _vrf;
  }

  @Override
  public String toString() {
    return String.format(
        "%s{%s,%s,%s}", PacketPolicyAction.class.getSimpleName(), _hostname, _policyName, _action);
  }
}
