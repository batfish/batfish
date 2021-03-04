package org.batfish.representation.fortios;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

/** FortiOS datamodel component containing address configuration */
public class Address implements Serializable {
  public enum Type {
    INTERFACE_SUBNET,
    IPMASK,
    IPRANGE,
    UNKNOWN,
    UNSUPPORTED,
    WILDCARD
  }

  // Fields that are only allowed to be set for a particular address type
  public static class TypeSpecificFields implements Serializable {
    @Nullable private String _interface; // for type INTERFACE_SUBNET
    @Nullable private Ip _startIp; // for type IPRANGE
    @Nullable private Ip _endIp; // for type IPRANGE
    @Nullable private Prefix _subnet; // for type IPMASK
    @Nullable private IpWildcard _wildcard; // for type WILDCARD

    public @Nullable String getInterface() {
      return _interface;
    }

    public @Nullable Ip getStartIp() {
      return _startIp;
    }

    public @Nullable Ip getEndIp() {
      return _endIp;
    }

    public @Nullable Prefix getSubnet() {
      return _subnet;
    }

    @Nullable
    public IpWildcard getWildcard() {
      return _wildcard;
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

  private boolean _allowRouting; // false by default
  @Nullable private String _associatedInterface;
  @Nullable private String _comment;
  private boolean _fabricObject; // false by default
  @Nonnull private final String _name;
  @Nonnull private Type _type;
  @Nonnull private final TypeSpecificFields _typeSpecificFields;

  public Address(String name) {
    _name = name;
    _type = Type.UNKNOWN;
    _typeSpecificFields = new TypeSpecificFields();
  }

  public static final Type DEFAULT_TYPE = Type.IPMASK;

  public boolean getAllowRouting() {
    return _allowRouting;
  }

  /** Interface or zone associated with this address */
  public @Nullable String getAssociatedInterface() {
    return _associatedInterface;
  }

  public @Nullable String getComment() {
    return _comment;
  }

  public boolean getFabricObject() {
    return _fabricObject;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Type getType() {
    return _type;
  }

  public @Nonnull TypeSpecificFields getTypeSpecificFields() {
    return _typeSpecificFields;
  }

  /**
   * Get the effective type of the address, inferring the value even if not explicitly configured.
   */
  public Type getTypeEffective() {
    return _type == Type.UNKNOWN ? DEFAULT_TYPE : _type;
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
