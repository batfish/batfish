package org.batfish.symbolic.state;

/**
 * A {@link StateExpr} for flows being forwarded out an interface with the {@link
 * org.batfish.datamodel.FlowDisposition#NEIGHBOR_UNREACHABLE} disposition, before the outgoing
 * ACL(s) or transformation are applied.
 */
public final class PreOutInterfaceNeighborUnreachable extends InterfaceStateExpr {
  public PreOutInterfaceNeighborUnreachable(String hostname, String iface) {
    super(hostname, iface);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitPreOutInterfaceNeighborUnreachable(this);
  }
}
