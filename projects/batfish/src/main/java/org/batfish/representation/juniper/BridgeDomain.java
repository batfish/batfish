package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;

/** A Juniper {@code bridge-domain}. */
public final class BridgeDomain implements Serializable {

  public @Nullable String getRoutingInterface() {
    return _routingInterface;
  }

  public void setRoutingInterface(@Nullable String routingInterface) {
    _routingInterface = routingInterface;
  }

  public @Nullable BridgeDomainVlanId getVlanId() {
    return _vlanId;
  }

  public void setVlanId(@Nullable BridgeDomainVlanId vlanId) {
    _vlanId = vlanId;
  }

  private @Nullable String _routingInterface;
  private @Nullable BridgeDomainVlanId _vlanId;
}
