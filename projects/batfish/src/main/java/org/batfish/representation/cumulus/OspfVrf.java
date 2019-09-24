package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** OSPF configuration for a particular VRF. */
public class OspfVrf implements Serializable {

  private final @Nonnull String _vrfName;

  public OspfVrf(String name) {
    _vrfName = name;
  }

  @Nonnull
  public String getVrfName() {
    return _vrfName;
  }
}
