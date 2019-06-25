package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Named VXLAN per-VNI settings */
public class Vxlan implements Serializable {

  private @Nullable Integer _bridgeAccessVlan;
  private @Nullable Integer _id;
  private @Nullable Ip _localTunnelip;
  private final @Nonnull String _name;

  public Vxlan(String name) {
    _name = name;
  }

  public @Nullable Integer getBridgeAccessVlan() {
    return _bridgeAccessVlan;
  }

  public @Nullable Integer getId() {
    return _id;
  }

  public @Nullable Ip getLocalTunnelip() {
    return _localTunnelip;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void setBridgeAccessVlan(@Nullable Integer bridgeAccessVlan) {
    _bridgeAccessVlan = bridgeAccessVlan;
  }

  public void setId(@Nullable Integer id) {
    _id = id;
  }

  public void setLocalTunnelip(@Nullable Ip localTunnelip) {
    _localTunnelip = localTunnelip;
  }
}
