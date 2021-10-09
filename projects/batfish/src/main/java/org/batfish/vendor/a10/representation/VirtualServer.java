package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Datamodel class representing configuration for a load balancer virtual-server. */
public final class VirtualServer implements Serializable {

  @Nullable
  public Boolean getEnable() {
    return _enable;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nonnull
  public Map<VirtualServerPort.PortAndType, VirtualServerPort> getPorts() {
    return Collections.unmodifiableMap(_ports);
  }

  @Nullable
  public VirtualServerPort getPort(int port, VirtualServerPort.Type type) {
    return _ports.get(new VirtualServerPort.PortAndType(port, type));
  }

  @Nonnull
  public VirtualServerPort getOrCreatePort(
      int port, VirtualServerPort.Type type, @Nullable Integer range) {
    return _ports.computeIfAbsent(
        new VirtualServerPort.PortAndType(port, type),
        pat -> new VirtualServerPort(port, type, range));
  }

  @Nullable
  public Boolean getRedistributionFlagged() {
    return _redistributionFlagged;
  }

  @Nonnull
  public VirtualServerTarget getTarget() {
    return _target;
  }

  @Nullable
  public Integer getVrid() {
    return _vrid;
  }

  public void setRedistributionFlagged(boolean redistributionFlagged) {
    _redistributionFlagged = redistributionFlagged;
  }

  public void setEnable(boolean enable) {
    _enable = enable;
  }

  public void setTarget(VirtualServerTarget target) {
    _target = target;
  }

  public void setVrid(int vrid) {
    _vrid = vrid;
  }

  public VirtualServer(String name, VirtualServerTarget target) {
    _name = name;
    _ports = new HashMap<>();
    _target = target;
  }

  @Nullable private Boolean _enable;
  @Nonnull private final String _name;
  @Nonnull private final Map<VirtualServerPort.PortAndType, VirtualServerPort> _ports;
  @Nullable private Boolean _redistributionFlagged;
  @Nonnull private VirtualServerTarget _target;
  @Nullable private Integer _vrid;
}
