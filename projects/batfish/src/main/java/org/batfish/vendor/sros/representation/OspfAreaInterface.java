package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An OSPF interface within an area ({@code ... area <id> interface "<name>"}), keyed by the
 * router-interface name it enables OSPF on. Holds the modeled subset: the explicit metric (cost)
 * and the interface-type (broadcast/point-to-point).
 */
public final class OspfAreaInterface implements Serializable {

  /** OSPF interface network type (the modeled subset of the SR-OS {@code interface-type} enum). */
  public enum InterfaceType {
    BROADCAST,
    POINT_TO_POINT;
  }

  public OspfAreaInterface(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** The explicit {@code metric} (OSPF cost), or {@code null} to use the derived/default cost. */
  public @Nullable Integer getMetric() {
    return _metric;
  }

  public void setMetric(@Nullable Integer metric) {
    _metric = metric;
  }

  /** The {@code interface-type}, or {@code null} if unset. */
  public @Nullable InterfaceType getInterfaceType() {
    return _interfaceType;
  }

  public void setInterfaceType(@Nullable InterfaceType interfaceType) {
    _interfaceType = interfaceType;
  }

  private final @Nonnull String _name;
  private @Nullable Integer _metric;
  private @Nullable InterfaceType _interfaceType;
}
