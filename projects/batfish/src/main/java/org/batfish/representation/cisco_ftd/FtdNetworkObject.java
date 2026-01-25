package org.batfish.representation.cisco_ftd;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;

/** Represents a Cisco FTD network object. */
public class FtdNetworkObject implements Serializable {

  public enum NetworkObjectType {
    HOST,
    SUBNET,
    RANGE,
    FQDN
  }

  private final @Nonnull String _name;
  private @Nullable String _description;
  private @Nullable NetworkObjectType _type;
  private @Nullable Ip _hostIp;
  private @Nullable Ip _subnetNetwork;
  private @Nullable Ip _subnetMask;
  private @Nullable Ip _rangeStart;
  private @Nullable Ip _rangeEnd;
  private @Nullable String _fqdn;

  public FtdNetworkObject(@Nonnull String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public @Nullable NetworkObjectType getType() {
    return _type;
  }

  public void setHost(@Nonnull Ip ip) {
    _type = NetworkObjectType.HOST;
    _hostIp = ip;
  }

  public @Nullable Ip getHostIp() {
    return _hostIp;
  }

  public void setSubnet(@Nonnull Ip network, @Nonnull Ip mask) {
    _type = NetworkObjectType.SUBNET;
    _subnetNetwork = network;
    _subnetMask = mask;
  }

  public @Nullable Ip getSubnetNetwork() {
    return _subnetNetwork;
  }

  public @Nullable Ip getSubnetMask() {
    return _subnetMask;
  }

  public void setRange(@Nonnull Ip start, @Nonnull Ip end) {
    _type = NetworkObjectType.RANGE;
    _rangeStart = start;
    _rangeEnd = end;
  }

  public @Nullable Ip getRangeStart() {
    return _rangeStart;
  }

  public @Nullable Ip getRangeEnd() {
    return _rangeEnd;
  }

  public void setFqdn(@Nonnull String fqdn) {
    _type = NetworkObjectType.FQDN;
    _fqdn = fqdn;
  }

  public @Nullable String getFqdn() {
    return _fqdn;
  }

  /**
   * Convert this network object to an IpSpace for use in ACL matching. Returns null if the object
   * type is not supported (e.g., FQDN).
   */
  public @Nullable IpSpace toIpSpace() {
    if (_type == null) {
      return null;
    }
    switch (_type) {
      case HOST:
        return _hostIp != null ? _hostIp.toIpSpace() : null;
      case SUBNET:
        if (_subnetNetwork != null && _subnetMask != null) {
          return org.batfish.datamodel.Prefix.create(_subnetNetwork, _subnetMask).toIpSpace();
        }
        return null;
      case RANGE:
        if (_rangeStart != null && _rangeEnd != null) {
          return org.batfish.datamodel.IpRange.range(_rangeStart, _rangeEnd);
        }
        return null;
      case FQDN:
        // FQDN objects are not supported for IpSpace conversion
        return null;
      default:
        return null;
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("NetworkObject: ").append(_name);
    if (_type != null) {
      sb.append(" (").append(_type).append(")");
    }
    return sb.toString();
  }
}
