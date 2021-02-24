package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/** OSPF configuration for {@code area A range A.B.C.D/M [cost C]}. */
public class OspfAreaRange implements Serializable {

  private final @Nonnull Prefix _range;
  private @Nullable Integer _cost;

  public OspfAreaRange(@Nonnull Prefix range) {
    _range = range;
  }

  public @Nonnull Prefix getRange() {
    return _range;
  }

  public @Nullable Integer getCost() {
    return _cost;
  }

  public void setCost(@Nullable Integer cost) {
    _cost = cost;
  }
}
