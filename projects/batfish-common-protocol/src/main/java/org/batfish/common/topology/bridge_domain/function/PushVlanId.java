package org.batfish.common.topology.bridge_domain.function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L2ToPhysical;

/**
 * A {@link StateFunction} that pushes the state's VLAN ID onto the tag stack, unless it is an
 * optional exception VLAN ID.
 */
public final class PushVlanId implements L2ToPhysical.Function {

  @Override
  public <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg) {
    return visitor.visitPushVlanId(this, arg);
  }

  /**
   * An optional exception VLAN ID that is not pushed onto the tag stack.
   *
   * <p>If {@code null}, the state's VLAN ID is always pushed onto the tag stack.
   *
   * <p>The result is undefined if the state has no set VLAN ID.
   */
  public @Nullable Integer getExceptVlan() {
    return _exceptVlan;
  }

  static @Nonnull PushVlanId of(@Nullable Integer exceptVlan) {
    return exceptVlan == null ? ALWAYS : new PushVlanId(exceptVlan);
  }

  private PushVlanId(@Nullable Integer exceptVlan) {
    _exceptVlan = exceptVlan;
  }

  private static final PushVlanId ALWAYS = new PushVlanId(null);

  private final @Nullable Integer _exceptVlan;
}
