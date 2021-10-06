package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Datamodel class representing configuration for a NAT pool. */
public final class NatPool implements Serializable {

  @Nonnull
  public Ip getEnd() {
    return _end;
  }

  @Nullable
  public Ip getGateway() {
    return _gateway;
  }

  @Nullable
  public Integer getHaGroupId() {
    return _haGroupId;
  }

  public boolean getIpRr() {
    return _ipRr;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  public int getNetmask() {
    return _netmask;
  }

  public boolean getPortOverload() {
    return _portOverload;
  }

  @Nullable
  public Integer getScaleoutDeviceId() {
    return _scaleoutDeviceId;
  }

  @Nonnull
  public Ip getStart() {
    return _start;
  }

  @Nullable
  public Integer getVrid() {
    return _vrid;
  }

  public void setGateway(Ip gateway) {
    _gateway = gateway;
  }

  public void setHaGroupId(int haGroupId) {
    _haGroupId = haGroupId;
  }

  public void setIpRr(boolean ipRr) {
    _ipRr = ipRr;
  }

  public void setPortOverload(boolean portOverload) {
    _portOverload = portOverload;
  }

  public void setScaleoutDeviceId(int scaleoutDeviceId) {
    _scaleoutDeviceId = scaleoutDeviceId;
  }

  public void setVrid(int vrid) {
    _vrid = vrid;
  }

  public NatPool(String name, Ip start, Ip end, int netmask) {
    _end = end;
    _name = name;
    _netmask = netmask;
    _start = start;
  }

  @Nonnull private final Ip _end;
  @Nullable private Ip _gateway;
  @Nullable private Integer _haGroupId;
  private boolean _ipRr;
  @Nonnull private final String _name;
  private final int _netmask;
  private boolean _portOverload;
  @Nullable private Integer _scaleoutDeviceId;
  @Nonnull private final Ip _start;
  @Nullable private Integer _vrid;
}
