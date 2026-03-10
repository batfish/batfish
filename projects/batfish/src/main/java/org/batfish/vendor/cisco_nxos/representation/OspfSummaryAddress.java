package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

public final class OspfSummaryAddress implements Serializable {

  public OspfSummaryAddress(Prefix prefix) {
    _prefix = prefix;
  }

  public boolean getNotAdvertise() {
    return _notAdvertise;
  }

  public void setNotAdvertise(boolean notAdvertise) {
    _notAdvertise = notAdvertise;
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  public long getTag() {
    return _tag;
  }

  public void setTag(long tag) {
    _tag = tag;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private boolean _notAdvertise;
  private final @Nonnull Prefix _prefix;
  private long _tag;
}
