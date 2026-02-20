package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;

/** Represents a member of a Cisco FTD service object-group. */
public class FtdServiceObjectGroupMember implements Serializable {

  public enum MemberType {
    SERVICE_OBJECT,
    PORT_OBJECT,
    GROUP_OBJECT
  }

  private final MemberType _type;
  private @Nullable String _protocol;
  private @Nullable String _portSpec;
  private @Nullable String _objectName;

  private FtdServiceObjectGroupMember(MemberType type) {
    _type = type;
  }

  public static FtdServiceObjectGroupMember serviceObject(
      @Nullable String protocol, @Nullable String portSpec) {
    FtdServiceObjectGroupMember member = new FtdServiceObjectGroupMember(MemberType.SERVICE_OBJECT);
    member._protocol = protocol;
    member._portSpec = portSpec;
    return member;
  }

  public static FtdServiceObjectGroupMember portObject(
      @Nullable String protocol, @Nullable String portSpec) {
    FtdServiceObjectGroupMember member = new FtdServiceObjectGroupMember(MemberType.PORT_OBJECT);
    member._protocol = protocol;
    member._portSpec = portSpec;
    return member;
  }

  public static FtdServiceObjectGroupMember groupObject(String objectName) {
    FtdServiceObjectGroupMember member = new FtdServiceObjectGroupMember(MemberType.GROUP_OBJECT);
    member._objectName = objectName;
    return member;
  }

  public MemberType getType() {
    return _type;
  }

  public @Nullable String getProtocol() {
    return _protocol;
  }

  public @Nullable String getPortSpec() {
    return _portSpec;
  }

  public @Nullable String getObjectName() {
    return _objectName;
  }

  @Override
  public String toString() {
    switch (_type) {
      case SERVICE_OBJECT:
        return "service-object "
            + (_protocol != null ? _protocol : "ip")
            + (_portSpec != null ? " " + _portSpec : "");
      case PORT_OBJECT:
        return "port-object " + (_portSpec != null ? _portSpec : "");
      case GROUP_OBJECT:
        return "group-object " + _objectName;
    }
    throw new IllegalStateException("Unhandled MemberType: " + _type);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FtdServiceObjectGroupMember)) {
      return false;
    }
    FtdServiceObjectGroupMember that = (FtdServiceObjectGroupMember) o;
    return _type == that._type
        && Objects.equals(_protocol, that._protocol)
        && Objects.equals(_portSpec, that._portSpec)
        && Objects.equals(_objectName, that._objectName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type, _protocol, _portSpec, _objectName);
  }
}
