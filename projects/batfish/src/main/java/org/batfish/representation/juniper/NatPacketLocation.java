package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nullable;

/** Represents how packets enter and exit a Nat */
public final class NatPacketLocation implements Serializable, Comparable<NatPacketLocation> {
  public static NatPacketLocation interfaceLocation(String name) {
    NatPacketLocation loc = new NatPacketLocation();
    loc.setInterface(name);
    return loc;
  }

  public static NatPacketLocation routingInstanceLocation(String name) {
    NatPacketLocation loc = new NatPacketLocation();
    loc.setRoutingInstance(name);
    return loc;
  }

  public static NatPacketLocation zoneLocation(String name) {
    NatPacketLocation loc = new NatPacketLocation();
    loc.setZone(name);
    return loc;
  }

  public void set(Type type, String name) {
    _type = type;
    _name = name;
  }

  // The types are defined in a prioritized order
  enum Type {
    InterfaceType,
    ZoneType,
    RoutingInstanceType
  }

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

  public Type getType() {
    return _type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NatPacketLocation)) {
      return false;
    }
    NatPacketLocation that = (NatPacketLocation) o;
    return Objects.equals(_name, that._name) && _type == that._type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _type);
  }

  @Override
  public int compareTo(NatPacketLocation o) {
    return COMPARATOR.compare(this, o);
  }
}
