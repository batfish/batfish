package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Datamodel class representing configuration for a load balancer virtual-server port. */
public class VirtualServerPort implements Serializable {
  /**
   * A combination of port number and type, which uniquely identifies a {@link VirtualServerPort}
   */
  public static class PortAndType implements Serializable {
    @Override
    public int hashCode() {
      return Objects.hash(_port, _type);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
      if (obj == this) {
        return true;
      } else if (!(obj instanceof PortAndType)) {
        return false;
      }
      PortAndType o = (PortAndType) obj;
      return _port == o._port && _type == o._type;
    }

    public PortAndType(int port, Type type) {
      _port = port;
      _type = type;
    }

    private final int _port;
    private final Type _type;
  }

  // TODO support more types
  public enum Type {
    HTTP,
    HTTPS,
    TCP,
    TCP_PROXY,
    UDP
  }

  @Nullable
  public String getAccessList() {
    return _accessList;
  }

  public void setAccessList(String accessList) {
    _accessList = accessList;
  }

  @Nullable
  public String getAflex() {
    return _aflex;
  }

  public void setAflex(@Nullable String aflex) {
    _aflex = aflex;
  }

  @Nullable
  public Integer getBucketCount() {
    return _bucketCount;
  }

  /** Description from the cli: Use default server selection method if prefer method failed. */
  @Nullable
  public Boolean getDefSelectionIfPrefFailed() {
    return _defSelectionIfPrefFailed;
  }

  @Nullable
  public Boolean getEnable() {
    return _enable;
  }

  @Nullable
  public String getName() {
    return _name;
  }

  public int getNumber() {
    return _number;
  }

  @Nullable
  public Integer getRange() {
    return _range;
  }

  @Nullable
  public String getServiceGroup() {
    return _serviceGroup;
  }

  @Nullable
  public String getSourceNat() {
    return _sourceNat;
  }

  @Nonnull
  public Type getType() {
    return _type;
  }

  public void setBucketCount(int bucketCount) {
    _bucketCount = bucketCount;
  }

  public void setDefSelectionIfPrefFailed(boolean defSelectionIfPrefFailed) {
    _defSelectionIfPrefFailed = defSelectionIfPrefFailed;
  }

  public void setEnable(boolean enable) {
    _enable = enable;
  }

  public void setName(String name) {
    _name = name;
  }

  public void setRange(@Nullable Integer range) {
    _range = range;
  }

  public void setServiceGroup(String serviceGroup) {
    _serviceGroup = serviceGroup;
  }

  public void setSourceNat(String sourceNat) {
    _sourceNat = sourceNat;
  }

  public VirtualServerPort(int number, Type type, @Nullable Integer range) {
    _number = number;
    _type = type;
    _range = range;
  }

  @Nullable private String _accessList;
  @Nullable private String _aflex;
  @Nullable private Integer _bucketCount;
  @Nullable private Boolean _defSelectionIfPrefFailed;
  @Nullable private Boolean _enable;
  @Nullable private String _name;
  private final int _number;
  @Nullable private Integer _range;
  @Nullable private String _serviceGroup;
  @Nullable private String _sourceNat;
  @Nonnull private Type _type;
}
