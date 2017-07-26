package org.batfish.datamodel.collections;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeMap;
import org.batfish.datamodel.BgpAdvertisement;

public final class BgpAdvertisementsByVrf extends TreeMap<String, SortedSet<BgpAdvertisement>>
    implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private boolean _unrecognized;

  public boolean getUnrecognized() {
    return _unrecognized;
  }

  public void setUnrecognized(boolean unrecognized) {
    _unrecognized = unrecognized;
  }
}
