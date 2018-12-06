package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Comparator;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;

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

  @Nullable private String _interface;

  @Nullable private String _routingInstance;

  @Nullable private String _zone;

  private Type _type;

  @Nullable
  public String getName() {
    return _interface != null ? _interface : _zone != null ? _zone : _routingInstance;
  }

  @Nullable
  public String getInterface() {
    return _interface;
  }

  @Nullable
  public String getRoutingInstance() {
    return _routingInstance;
  }

  @Nullable
  public String getZone() {
    return _zone;
  }

  public void setInterface(@Nullable String interfaceName) {
    _interface = interfaceName;
  }

  public void setRoutingInstance(@Nullable String routingInstance) {
    _routingInstance = routingInstance;
  }

  public void setZone(@Nullable String zone) {
    _zone = zone;
  }

  private Type getType() {
    if (_type == null) {
      if (_interface == null && _zone == null && _routingInstance == null) {
        throw new BatfishException("No type found for NatPacketLocation");
      }
      _type =
          _interface != null
              ? Type.InterfaceType
              : _zone != null ? Type.ZoneType : Type.RoutingInstanceType;
    }
    return _type;
  }

  @Override
  public int compareTo(NatPacketLocation o) {
    return COMPARATOR.compare(this, o);
  }
}
