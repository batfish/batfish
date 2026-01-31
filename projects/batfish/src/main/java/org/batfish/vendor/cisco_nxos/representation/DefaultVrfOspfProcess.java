package org.batfish.vendor.cisco_nxos.representation;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public final class DefaultVrfOspfProcess extends OspfProcess {

  public DefaultVrfOspfProcess(String name) {
    _name = name;
    _vrfs = new HashMap<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Map<String, OspfVrf> getVrfs() {
    return _vrfs;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final @Nonnull String _name;
  private final @Nonnull Map<String, OspfVrf> _vrfs;
}
