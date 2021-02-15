package org.batfish.representation.cumulus_nclu;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** OSPF configuration for a particular VRF. */
public class OspfVrf implements Serializable {

  private final @Nonnull String _vrfName;
  private @Nullable Ip _routerId;

  public OspfVrf(String name) {
    _vrfName = name;
  }

  @Nonnull
  public String getVrfName() {
    return _vrfName;
  }

  @Nullable
  public Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }
}
