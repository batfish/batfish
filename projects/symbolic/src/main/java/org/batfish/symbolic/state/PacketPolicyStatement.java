package org.batfish.symbolic.state;

import javax.annotation.Nonnull;

public final class PacketPolicyStatement implements StateExpr {
  private final String _hostname;
  private final String _vrf;
  private final String _packetPolicyName;
  private final int _id;

  public PacketPolicyStatement(
      @Nonnull String hostname, @Nonnull String vrf, @Nonnull String packetPolicyName, int id) {
    _hostname = hostname;
    _vrf = vrf;
    _packetPolicyName = packetPolicyName;
    _id = id;
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitPacketPolicyStatement(this);
  }

  public int getId() {
    return _id;
  }

  public String getVrf() {
    return _vrf;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PacketPolicyStatement)) {
      return false;
    }
    PacketPolicyStatement that = (PacketPolicyStatement) o;
    return _id == that._id
        && _hostname.equals(that._hostname)
        && _vrf.equals(that._vrf)
        && _packetPolicyName.equals(that._packetPolicyName);
  }

  @Override
  public int hashCode() {
    return 31 * 31 * 31 * 31 * PacketPolicyStatement.class.hashCode()
        + 31 * 31 * 31 * _hostname.hashCode()
        + 31 * 31 * _vrf.hashCode()
        + 31 * _packetPolicyName.hashCode()
        + Integer.hashCode(_id);
  }

  @Override
  public final String toString() {
    return String.format(
        "%s{%s,%s,%s,%s}", getClass().getSimpleName(), _hostname, _vrf, _packetPolicyName, _id);
  }
}
