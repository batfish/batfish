package org.batfish.vendor.arista.representation.eos;

import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;

public final class AristaBgpVlanAwareBundle extends AristaBgpVlanBase {
  private final String _name;
  private @Nullable IntegerSpace _vlans;

  public AristaBgpVlanAwareBundle(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public @Nullable IntegerSpace getVlans() {
    return _vlans;
  }

  public void setVlans(@Nullable IntegerSpace vlans) {
    _vlans = vlans;
  }
}
