package org.batfish.representation.arista.eos;

import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;

public final class AristaBgpVlanAwareBundle extends AristaBgpVlanBase {
  private final String _name;
  @Nullable private IntegerSpace _vlans;

  public AristaBgpVlanAwareBundle(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  @Nullable
  public IntegerSpace getVlans() {
    return _vlans;
  }

  public void setVlans(@Nullable IntegerSpace vlans) {
    _vlans = vlans;
  }
}
