package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** XR datamodel component containing bridge group configuration */
@ParametersAreNonnullByDefault
public class BridgeGroup implements Serializable {
  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Map<String, BridgeDomain> getBridgeDomains() {
    return _bridgeDomains;
  }

  public BridgeGroup(String name) {
    _name = name;
    _bridgeDomains = new HashMap<>();
  }

  private final @Nonnull String _name;
  private final @Nonnull Map<String, BridgeDomain> _bridgeDomains;
}
