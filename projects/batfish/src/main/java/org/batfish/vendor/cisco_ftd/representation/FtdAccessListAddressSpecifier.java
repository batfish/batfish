package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * Represents an address specifier in an FTD access list line. Supports: host, object, object-group,
 * any, any4, any6, and network/mask notation.
 */
public class FtdAccessListAddressSpecifier implements Serializable {

  public enum AddressType {
    ANY,
    ANY4,
    ANY6,
    HOST,
    NETWORK_MASK,
    OBJECT,
    OBJECT_GROUP
  }

  private final AddressType _type;
  private @Nullable Ip _ip;
  private @Nullable Ip _mask;
  private @Nullable String _objectName;

  private FtdAccessListAddressSpecifier(AddressType type) {
    _type = type;
  }

  public static FtdAccessListAddressSpecifier any() {
    return new FtdAccessListAddressSpecifier(AddressType.ANY);
  }

  public static FtdAccessListAddressSpecifier any4() {
    return new FtdAccessListAddressSpecifier(AddressType.ANY4);
  }

  public static FtdAccessListAddressSpecifier any6() {
    return new FtdAccessListAddressSpecifier(AddressType.ANY6);
  }

  public static FtdAccessListAddressSpecifier host(Ip ip) {
    FtdAccessListAddressSpecifier spec = new FtdAccessListAddressSpecifier(AddressType.HOST);
    spec._ip = ip;
    return spec;
  }

  public static FtdAccessListAddressSpecifier networkMask(Ip network, Ip mask) {
    FtdAccessListAddressSpecifier spec =
        new FtdAccessListAddressSpecifier(AddressType.NETWORK_MASK);
    spec._ip = network;
    spec._mask = mask;
    return spec;
  }

  public static FtdAccessListAddressSpecifier object(String objectName) {
    FtdAccessListAddressSpecifier spec = new FtdAccessListAddressSpecifier(AddressType.OBJECT);
    spec._objectName = objectName;
    return spec;
  }

  public static FtdAccessListAddressSpecifier objectGroup(String objectGroupName) {
    FtdAccessListAddressSpecifier spec =
        new FtdAccessListAddressSpecifier(AddressType.OBJECT_GROUP);
    spec._objectName = objectGroupName;
    return spec;
  }

  public AddressType getType() {
    return _type;
  }

  public @Nullable Ip getIp() {
    return _ip;
  }

  public @Nullable Ip getMask() {
    return _mask;
  }

  public @Nullable String getObjectName() {
    return _objectName;
  }

  @Override
  public String toString() {
    switch (_type) {
      case ANY:
        return "any";
      case ANY4:
        return "any4";
      case ANY6:
        return "any6";
      case HOST:
        return "host " + _ip;
      case NETWORK_MASK:
        return _ip + " " + _mask;
      case OBJECT:
        return "object " + _objectName;
      case OBJECT_GROUP:
        return "object-group " + _objectName;
    }
    throw new IllegalStateException("Unhandled AddressType: " + _type);
  }
}
