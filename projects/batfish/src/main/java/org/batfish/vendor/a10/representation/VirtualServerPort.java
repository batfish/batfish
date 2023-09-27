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
    DIAMETER,
    HTTP,
    HTTPS,
    RADIUS,
    SIP,
    SMTP,
    SSL_PROXY,
    TCP,
    TCP_PROXY,
    UDP
  }

  public @Nullable String getAccessList() {
    return _accessList;
  }

  public void setAccessList(String accessList) {
    _accessList = accessList;
  }

  public @Nullable String getAflex() {
    return _aflex;
  }

  public void setAflex(@Nullable String aflex) {
    _aflex = aflex;
  }

  public @Nullable Integer getBucketCount() {
    return _bucketCount;
  }

  /** Description from the cli: Use default server selection method if prefer method failed. */
  public @Nullable Boolean getDefSelectionIfPrefFailed() {
    return _defSelectionIfPrefFailed;
  }

  public @Nullable Boolean getEnable() {
    return _enable;
  }

  public @Nullable String getName() {
    return _name;
  }

  public int getNumber() {
    return _number;
  }

  public @Nullable Integer getRange() {
    return _range;
  }

  public @Nullable String getServiceGroup() {
    return _serviceGroup;
  }

  public @Nullable String getSourceNat() {
    return _sourceNat;
  }

  public @Nonnull Type getType() {
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

  /**
   * From docs: Force the ACOS device to send replies to clients back through the last hop on which
   * the request for the virtual port's service was received.
   */
  public @Nullable Boolean getUseRcvHopForResp() {
    return _useRcvHopForResp;
  }

  public void setUseRcvHopForResp(Boolean useRcvHopForResp) {
    _useRcvHopForResp = useRcvHopForResp;
  }

  public VirtualServerPort(int number, Type type, @Nullable Integer range) {
    _number = number;
    _type = type;
    _range = range;
  }

  private @Nullable String _accessList;
  private @Nullable String _aflex;
  private @Nullable Integer _bucketCount;
  private @Nullable Boolean _defSelectionIfPrefFailed;
  private @Nullable Boolean _enable;
  private @Nullable String _name;
  private final int _number;
  private @Nullable Integer _range;
  private @Nullable String _serviceGroup;
  private @Nullable String _sourceNat;
  private @Nonnull Type _type;
  private @Nullable Boolean _useRcvHopForResp;
}
