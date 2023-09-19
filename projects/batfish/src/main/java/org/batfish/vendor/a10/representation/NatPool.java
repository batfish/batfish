package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Datamodel class representing configuration for a NAT pool. */
public final class NatPool implements Serializable {

  public @Nonnull Ip getEnd() {
    return _end;
  }

  public @Nullable Ip getGateway() {
    return _gateway;
  }

  public @Nullable Integer getHaGroupId() {
    return _haGroupId;
  }

  public boolean getIpRr() {
    return _ipRr;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public int getNetmask() {
    return _netmask;
  }

  public boolean getPortOverload() {
    return _portOverload;
  }

  public @Nullable Integer getScaleoutDeviceId() {
    return _scaleoutDeviceId;
  }

  public @Nonnull Ip getStart() {
    return _start;
  }

  public @Nullable Integer getVrid() {
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

  private final @Nonnull Ip _end;
  private @Nullable Ip _gateway;
  private @Nullable Integer _haGroupId;
  private boolean _ipRr;
  private final @Nonnull String _name;
  private final int _netmask;
  private boolean _portOverload;
  private @Nullable Integer _scaleoutDeviceId;
  private final @Nonnull Ip _start;
  private @Nullable Integer _vrid;
}
