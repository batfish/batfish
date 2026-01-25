package org.batfish.representation.cisco_ftd;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Represents a member of a Cisco FTD network object-group. */
public class FtdNetworkObjectGroupMember implements Serializable {

  public enum MemberType {
    HOST,
    NETWORK_MASK,
    OBJECT,
    GROUP_OBJECT
  }

  private final MemberType _type;
  private @Nullable Ip _ip;
  private @Nullable Ip _mask;
  private @Nullable String _objectName;

  private FtdNetworkObjectGroupMember(MemberType type) {
    _type = type;
  }

  public static FtdNetworkObjectGroupMember host(Ip ip) {
    FtdNetworkObjectGroupMember member = new FtdNetworkObjectGroupMember(MemberType.HOST);
    member._ip = ip;
    return member;
  }

  public static FtdNetworkObjectGroupMember networkMask(Ip network, Ip mask) {
    FtdNetworkObjectGroupMember member = new FtdNetworkObjectGroupMember(MemberType.NETWORK_MASK);
    member._ip = network;
    member._mask = mask;
    return member;
  }

  public static FtdNetworkObjectGroupMember object(String objectName) {
    FtdNetworkObjectGroupMember member = new FtdNetworkObjectGroupMember(MemberType.OBJECT);
    member._objectName = objectName;
    return member;
  }

  public static FtdNetworkObjectGroupMember groupObject(String groupName) {
    FtdNetworkObjectGroupMember member = new FtdNetworkObjectGroupMember(MemberType.GROUP_OBJECT);
    member._objectName = groupName;
    return member;
  }

  public MemberType getType() {
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
      case HOST:
        return "host " + _ip;
      case NETWORK_MASK:
        return _ip + " " + _mask;
      case OBJECT:
        return "object " + _objectName;
      case GROUP_OBJECT:
        return "group-object " + _objectName;
      default:
        return "unknown";
    }
  }
}
