package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An IS-IS interface within a process ({@code ... isis <instance> interface "<name>"}), keyed by
 * the router-interface name it enables IS-IS on. Holds the modeled subset: whether it is {@code
 * passive} (advertised but forms no adjacency) and the {@code interface-type}
 * (broadcast/point-to-point).
 */
public final class IsisProcessInterface implements Serializable {

  /** IS-IS interface type (the modeled subset of the SR-OS {@code interface-type} enum). */
  public enum InterfaceType {
    BROADCAST,
    POINT_TO_POINT;
  }

  public IsisProcessInterface(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** Whether the interface is {@code passive} (its subnet is advertised, no adjacency formed). */
  public boolean getPassive() {
    return _passive;
  }

  public void setPassive(boolean passive) {
    _passive = passive;
  }

  /** The {@code interface-type}, or {@code null} if unset. */
  public @Nullable InterfaceType getInterfaceType() {
    return _interfaceType;
  }

  public void setInterfaceType(@Nullable InterfaceType interfaceType) {
    _interfaceType = interfaceType;
  }

  private final @Nonnull String _name;
  private boolean _passive;
  private @Nullable InterfaceType _interfaceType;
}
