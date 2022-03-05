package org.batfish.common.topology.bridge_domain.function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.PhysicalToL2;

/**
 * A {@link StateFunction} that assigns a VLAN based on the outer tag (or its absence) of a frame
 * and pops the outer tag if it was present.
 */
public final class AssignVlanFromOuterTag implements PhysicalToL2.Function {

  @Override
  public <T, U> T accept(StateFunctionVisitor<T, U> visitor, U arg) {
    return visitor.visitAssignVlanFromOuterTag(this, arg);
  }

  /**
   * If not {@code null}, the VLAN ID to set if the outer tag is absent. If {@code null} and the
   * outer tag is absent, the frame should be dropped.
   */
  public @Nullable Integer getNativeVlan() {
    return _nativeVlan;
  }

  static @Nonnull AssignVlanFromOuterTag of(@Nullable Integer nativeVlan) {
    return nativeVlan == null ? ONLY_TAGGED : new AssignVlanFromOuterTag(nativeVlan);
  }

  private static final AssignVlanFromOuterTag ONLY_TAGGED = new AssignVlanFromOuterTag(null);

  private AssignVlanFromOuterTag(@Nullable Integer nativeVlan) {
    _nativeVlan = nativeVlan;
  }

  private final @Nullable Integer _nativeVlan;
}
