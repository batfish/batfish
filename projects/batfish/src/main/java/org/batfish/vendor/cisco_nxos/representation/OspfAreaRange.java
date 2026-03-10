package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

public final class OspfAreaRange implements Serializable {

  public OspfAreaRange(Prefix prefix) {
    _prefix = prefix;
  }

  public @Nullable Integer getCost() {
    return _cost;
  }

  public void setCost(@Nullable Integer cost) {
    _cost = cost;
  }

  public boolean getNotAdvertise() {
    return _notAdvertise;
  }

  public void setNotAdvertise(boolean notAdvertise) {
    _notAdvertise = notAdvertise;
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private @Nullable Integer _cost;
  private boolean _notAdvertise;
  private final @Nonnull Prefix _prefix;
}
