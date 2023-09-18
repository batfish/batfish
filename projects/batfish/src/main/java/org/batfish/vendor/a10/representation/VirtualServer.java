package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Datamodel class representing configuration for a load balancer virtual-server. */
public final class VirtualServer implements Serializable {

  public @Nullable Boolean getEnable() {
    return _enable;
  }

  public @Nullable Integer getHaGroup() {
    return _haGroup;
  }

  public void setHaGroup(@Nullable Integer haGroup) {
    _haGroup = haGroup;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Map<VirtualServerPort.PortAndType, VirtualServerPort> getPorts() {
    return Collections.unmodifiableMap(_ports);
  }

  public @Nullable VirtualServerPort getPort(int port, VirtualServerPort.Type type) {
    return _ports.get(new VirtualServerPort.PortAndType(port, type));
  }

  public @Nonnull VirtualServerPort getOrCreatePort(
      int port, VirtualServerPort.Type type, @Nullable Integer range) {
    return _ports.computeIfAbsent(
        new VirtualServerPort.PortAndType(port, type),
        pat -> new VirtualServerPort(port, type, range));
  }

  public @Nullable Boolean getRedistributionFlagged() {
    return _redistributionFlagged;
  }

  public @Nonnull VirtualServerTarget getTarget() {
    return _target;
  }

  public @Nullable Integer getVrid() {
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

  private @Nullable Boolean _enable;
  private @Nullable Integer _haGroup;
  private final @Nonnull String _name;
  private final @Nonnull Map<VirtualServerPort.PortAndType, VirtualServerPort> _ports;
  private @Nullable Boolean _redistributionFlagged;
  private @Nonnull VirtualServerTarget _target;
  private @Nullable Integer _vrid;
}
