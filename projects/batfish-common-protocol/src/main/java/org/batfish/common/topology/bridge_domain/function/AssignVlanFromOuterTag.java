package org.batfish.common.topology.bridge_domain.function;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L2ToVlanAwareBridgeDomain;

/**
 * A {@link StateFunction} that assigns a VLAN based on the outer tag (or its absence) of a frame
 * and pops the outer tag if it was present.
 */
public final class AssignVlanFromOuterTag implements L2ToVlanAwareBridgeDomain.Function {

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

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof AssignVlanFromOuterTag)) {
      return false;
    }
    AssignVlanFromOuterTag that = (AssignVlanFromOuterTag) o;
    return Objects.equals(_nativeVlan, that._nativeVlan);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_nativeVlan);
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
