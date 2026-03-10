package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class Evpn implements Serializable {
  public Evpn() {
    _vnis = new HashMap<>();
  }

  public @Nonnull Map<Integer, EvpnVni> getVnis() {
    return _vnis;
  }

  public @Nonnull EvpnVni getVni(int vni) {
    return _vnis.computeIfAbsent(vni, EvpnVni::new);
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final Map<Integer, EvpnVni> _vnis;
}
