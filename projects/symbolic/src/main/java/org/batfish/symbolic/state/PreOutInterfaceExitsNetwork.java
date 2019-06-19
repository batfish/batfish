package org.batfish.symbolic.state;

/**
 * A {@link StateExpr} for flows being forwarded out an interface with the {@link
 * org.batfish.datamodel.FlowDisposition#EXITS_NETWORK} disposition, before the outgoing ACL(s) or
 * transformation are applied.
 */
public final class PreOutInterfaceExitsNetwork extends InterfaceStateExpr {
  public PreOutInterfaceExitsNetwork(String hostname, String iface) {
    super(hostname, iface);
  }

  @Override
  public <R> R accept(StateExprVisitor<R> visitor) {
    return visitor.visitPreOutInterfaceExitsNetwork(this);
  }
}
