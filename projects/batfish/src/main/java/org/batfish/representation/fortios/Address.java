package org.batfish.representation.fortios;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** FortiOS datamodel component containing address configuration */
public class Address extends AddrgrpMember implements Serializable {
  public enum Type {
    IPMASK,
    IPRANGE,
    WILDCARD,
    // Not supported
    DYNAMIC,
    FQDN,
    INTERFACE_SUBNET,
    GEOGRAPHY,
    MAC,
  }

  // Fields that are only allowed to be set for a particular address type
  public static class TypeSpecificFields implements Serializable {
    private @Nullable String _interface; // for type INTERFACE_SUBNET
    private @Nullable Ip _ip1;
    private @Nullable Ip _ip2;

    // Type.SUBNET and Type.INTERFACE_SUBNET: Default subnet is 0.0.0.0/0
    // Type.WILDCARD: Default wildcard is 0.0.0.0 0.0.0.0 (meaning all IPs)
    // Type.IPRANGE: Default start IP is 0.0.0.0 (end IP must be specified)
    private static final Ip DEFAULT_IP = Ip.ZERO;

    public @Nullable String getInterface() {
      return _interface;
    }

    /**
     * Interpreted as:
     *
     * <ul>
     *   <li>Subnet IP for types {@link Type#IPMASK} and {@link Type#INTERFACE_SUBNET}
     *   <li>Start IP for type {@link Type#IPRANGE}
     *   <li>Wildcard IP for type {@link Type#WILDCARD}
     * </ul>
     */
    public @Nullable Ip getIp1() {
      return _ip1;
    }

    /**
     * Interpreted as:
     *
     * <ul>
     *   <li>Subnet mask for types {@link Type#IPMASK} and {@link Type#INTERFACE_SUBNET}
     *   <li>End IP for type {@link Type#IPRANGE}
     *   <li>Wildcard mask for type {@link Type#WILDCARD}
     * </ul>
     */
    public @Nullable Ip getIp2() {
      return _ip2;
    }

    /**
     * @see #getIp1
     */
    public @Nonnull Ip getIp1Effective() {
      return firstNonNull(_ip1, DEFAULT_IP);
    }

    /**
     * @see #getIp2
     */
    public @Nonnull Ip getIp2Effective() {
      return firstNonNull(_ip2, DEFAULT_IP);
    }

    public void setInterface(String iface) {
      _interface = iface;
    }

    public void setIp1(Ip ip1) {
      _ip1 = ip1;
    }

    public void setIp2(Ip ip2) {
      _ip2 = ip2;
    }
  }

  private @Nullable Boolean _allowRouting;
  private @Nullable String _associatedInterface;
  private @Nullable String _associatedInterfaceZone;
  private @Nullable BatfishUUID _associatedInterfaceZoneUuid;
  private @Nonnull String _name;
  private final @Nonnull BatfishUUID _uuid;
  private @Nullable Type _type;
  private final @Nonnull TypeSpecificFields _typeSpecificFields;

  public static final boolean DEFAULT_ALLOW_ROUTING = false;
  public static final Type DEFAULT_TYPE = Type.IPMASK;

  public Address(String name, BatfishUUID uuid) {
    _name = name;
    _typeSpecificFields = new TypeSpecificFields();
    _uuid = uuid;
  }

  public @Nullable Boolean getAllowRouting() {
    return _allowRouting;
  }

  public boolean getAllowRoutingEffective() {
    return firstNonNull(_allowRouting, DEFAULT_ALLOW_ROUTING);
  }

  /** Interface or zone associated with this address */
  public @Nullable String getAssociatedInterface() {
    return _associatedInterface;
  }

  /**
   * Name of zone referenced in associated-interface field in this address. Should be derived from
   * {@link this#getAssociatedInterfaceZoneUUID} when finishing building the VS model.
   */
  public @Nullable String getAssociatedInterfaceZone() {
    return _associatedInterfaceZone;
  }

  /** Batfish-internal UUID ccorresponding to the zone in associated-interface */
  public @Nullable BatfishUUID getAssociatedInterfaceZoneUUID() {
    return _associatedInterfaceZoneUuid;
  }

  @Override
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public BatfishUUID getBatfishUUID() {
    return _uuid;
  }

  @Override
  public void setName(String name) {
    _name = name;
  }

  public @Nullable Type getType() {
    return _type;
  }

  public @Nonnull TypeSpecificFields getTypeSpecificFields() {
    return _typeSpecificFields;
  }

  /**
   * Get the effective type of the address, inferring the value even if not explicitly configured.
   */
  public @Nonnull Type getTypeEffective() {
    return firstNonNull(_type, DEFAULT_TYPE);
  }

  public void setAllowRouting(boolean allowRouting) {
    _allowRouting = allowRouting;
  }

  public void setAssociatedInterface(String associatedInterface) {
    _associatedInterface = associatedInterface;
  }

  public void setAssociatedInterfaceZone(String associatedZone) {
    _associatedInterfaceZone = associatedZone;
  }

  public void setAssociatedInterfaceZoneUUID(BatfishUUID uuid) {
    _associatedInterfaceZoneUuid = uuid;
  }

  public void setType(Type type) {
    _type = type;
  }
}
