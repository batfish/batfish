package org.batfish.representation.fortios;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkState;

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
    @Nullable private String _interface; // for type INTERFACE_SUBNET
    @Nullable private Ip _ip1;
    @Nullable private Ip _ip2;

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

    /** @see #getIp1 */
    public @Nonnull Ip getIp1Effective() {
      return firstNonNull(_ip1, DEFAULT_IP);
    }

    /** @see #getIp2 */
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

  @Nullable private Boolean _allowRouting;
  @Nullable private String _associatedInterface;
  @Nonnull private String _name;
  @Nonnull private final BatfishUUID _uuid;
  @Nullable private Type _type;
  @Nonnull private final TypeSpecificFields _typeSpecificFields;

  public static final boolean DEFAULT_ALLOW_ROUTING = false;
  public static final Type DEFAULT_TYPE = Type.IPMASK;

  public Address(String name, BatfishUUID uuid) {
    _name = name;
    _typeSpecificFields = new TypeSpecificFields();
    _uuid = uuid;
  }

  public IpSpace toIpSpace(Warnings w) {
    // TODO Investigate & support _allowRouting, _associatedInterface, _fabricObject
    switch (getTypeEffective()) {
      case IPMASK:
        Ip subnetIp = getTypeSpecificFields().getIp1Effective();
        Ip subnetMask = getTypeSpecificFields().getIp2Effective();
        // Throw if mask is invalid; such an address should not have made it through extraction
        checkState(
            subnetMask.isValidNetmask1sLeading(),
            String.format("Cannot convert address %s: %s is an invalid mask", _name, subnetMask));
        return Prefix.create(subnetIp, subnetMask).toIpSpace();
      case IPRANGE:
        Ip startIp = getTypeSpecificFields().getIp1Effective();
        Ip endIp = getTypeSpecificFields().getIp2Effective();
        // Throw if end IP is zero; such an address should not have made it through extraction.
        // ("end IP cannot be 0" is the warning the CLI gives when end-ip was not set.)
        checkState(
            !endIp.equals(Ip.ZERO),
            String.format("Cannot convert address %s: end IP cannot be 0", _name));
        // Shouldn't have made it through extraction if end IP > start IP; let range throw if so
        return IpRange.range(startIp, endIp);
      case WILDCARD:
        Ip ip = getTypeSpecificFields().getIp1Effective();
        // Invert mask because IpWildcard interprets set bits as "don't care", whereas FortiOS
        // interprets unset bits as "don't care"
        Ip mask = getTypeSpecificFields().getIp2Effective().inverted();
        return IpWildcard.ipWithWildcardMask(ip, mask).toIpSpace();
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

  public void setType(Type type) {
    _type = type;
  }
}
