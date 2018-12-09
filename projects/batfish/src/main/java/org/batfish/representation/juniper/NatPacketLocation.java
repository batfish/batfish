package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Comparator;
import javax.annotation.Nullable;

/** Represents how packets enter and exit a Nat */
public final class NatPacketLocation implements Serializable, Comparable<NatPacketLocation> {

  // The types are defined in a prioritized order
  enum Type {
    InterfaceType,
    ZoneType,
    RoutingInstanceType
  }

  private static final long serialVersionUID = 1L;

  private static final Comparator<NatPacketLocation> COMPARATOR =
      Comparator.comparing(NatPacketLocation::getType).thenComparing(NatPacketLocation::getName);

  private String _name;

  private Type _type;

  @Nullable
  public String getName() {
    return _name;
  }

  @Nullable
  public String getInterface() {
    return _type == Type.InterfaceType ? _name : null;
  }

  @Nullable
  public String getRoutingInstance() {
    return _type == Type.RoutingInstanceType ? _name : null;
  }

  @Nullable
  public String getZone() {
    return _type == Type.ZoneType ? _name : null;
  }

  public void setInterface(@Nullable String interfaceName) {
    _name = interfaceName;
    _type = Type.InterfaceType;
  }

  public void setRoutingInstance(@Nullable String routingInstance) {
    _name = routingInstance;
    _type = Type.RoutingInstanceType;
  }

  public void setZone(@Nullable String zone) {
    _name = zone;
    _type = Type.ZoneType;
  }

  private Type getType() {
    return _type;
  }

  @Override
  public int compareTo(NatPacketLocation o) {
    return COMPARATOR.compare(this, o);
  }
}
