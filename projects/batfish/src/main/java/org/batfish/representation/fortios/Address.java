package org.batfish.representation.fortios;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

/** FortiOS datamodel component containing address configuration */
public class Address implements FortiosRenameableObject, Serializable {
  public enum Type {
    INTERFACE_SUBNET,
    IPMASK,
    IPRANGE,
    WILDCARD,
    // Not supported
    DYNAMIC,
    FQDN,
    GEOGRAPHY,
    MAC,
  }

  // Fields that are only allowed to be set for a particular address type
  public static class TypeSpecificFields implements Serializable {
    @Nullable private String _interface; // for type INTERFACE_SUBNET
    @Nullable private Ip _startIp; // for type IPRANGE
    @Nullable private Ip _endIp; // for type IPRANGE
    @Nullable private Prefix _subnet; // for type IPMASK
    @Nullable private IpWildcard _wildcard; // for type WILDCARD

    private static final Ip DEFAULT_START_IP = Ip.ZERO;
    private static final Prefix DEFAULT_SUBNET = Prefix.ZERO;
    // TODO Test that wildcard-type address with no wildcard set matches all addresses
    private static final IpWildcard DEFAULT_WILDCARD = IpWildcard.ANY;

    public @Nullable String getInterface() {
      return _interface;
    }

    public @Nullable Ip getStartIp() {
      return _startIp;
    }

    public @Nonnull Ip getStartIpEffective() {
      return firstNonNull(_startIp, DEFAULT_START_IP);
    }

    public @Nullable Ip getEndIp() {
      return _endIp;
    }

    public @Nullable Prefix getSubnet() {
      return _subnet;
    }

    public @Nonnull Prefix getSubnetEffective() {
      return firstNonNull(_subnet, DEFAULT_SUBNET);
    }

    public @Nullable IpWildcard getWildcard() {
      return _wildcard;
    }

    public @Nonnull IpWildcard getWildcardEffective() {
      return firstNonNull(_wildcard, DEFAULT_WILDCARD);
    }

    public void setInterface(String iface) {
      _interface = iface;
    }

    public void setStartIp(Ip startIp) {
      _startIp = startIp;
    }

    public void setEndIp(Ip endIp) {
      _endIp = endIp;
    }

    public void setSubnet(Prefix subnet) {
      _subnet = subnet;
    }

    public void setWildcard(IpWildcard wildcard) {
      _wildcard = wildcard;
    }
  }

  @Nullable private Boolean _allowRouting;
  @Nullable private String _associatedInterface;
  @Nullable private String _comment;
  @Nullable private Boolean _fabricObject;
  @Nonnull private String _name;
  @Nonnull private final BatfishUUID _uuid;
  @Nullable private Type _type;
  @Nonnull private final TypeSpecificFields _typeSpecificFields;

  public static final boolean DEFAULT_ALLOW_ROUTING = false;
  public static final boolean DEFAULT_FABRIC_OBJECT = false;
  public static final Type DEFAULT_TYPE = Type.IPMASK;

  public Address(String name, BatfishUUID uuid) {
    _name = name;
    _typeSpecificFields = new TypeSpecificFields();
    _uuid = uuid;
  }

  public IpSpace toIpSpace(Warnings w) {
    // TODO Investigate & support _allowRouting, _associatedInterface, _fabricObject
    // TODO Support edge cases; e.g. if subnet is already set and then type is set to iprange,
    //  device will automatically reinterpret subnet IP and mask as start and end IPs.
    switch (getTypeEffective()) {
      case IPMASK:
        return getTypeSpecificFields().getSubnetEffective().toIpSpace();
      case IPRANGE:
        Ip startIp = getTypeSpecificFields().getStartIpEffective();
        Ip endIp = getTypeSpecificFields().getEndIp();
        assert endIp != null;
        return IpRange.range(startIp, endIp);
      case WILDCARD:
        return getTypeSpecificFields().getWildcardEffective().toIpSpace();
      case INTERFACE_SUBNET:
        // TODO test what IPs this actually includes. Docs say it will:
        //  "automatically create an address object that matches the interface subnet"
        //  but it's unclear because it supports both "set subnet" and "set interface".
      case DYNAMIC: // Based on SDN connectors, whose addresses aren't known statically
      case FQDN: // Based on domain names
      case GEOGRAPHY: // Based on countries
      case MAC: // Based on MAC addresses
        // Unsupported address types.
        w.redFlag(
            String.format(
                "Addresses of type %s are unsupported and will be considered unmatchable.",
                getType()));
        return EmptyIpSpace.INSTANCE;
      default:
        throw new IllegalStateException("Unrecognized address type " + getTypeEffective());
    }
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

  public @Nullable String getComment() {
    return _comment;
  }

  public @Nullable Boolean getFabricObject() {
    return _fabricObject;
  }

  public boolean getFabricObjectEffective() {
    return firstNonNull(_fabricObject, DEFAULT_FABRIC_OBJECT);
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

  public void setComment(String comment) {
    _comment = comment;
  }

  public void setFabricObject(boolean fabricObject) {
    _fabricObject = fabricObject;
  }

  public void setType(Type type) {
    _type = type;
  }
}
