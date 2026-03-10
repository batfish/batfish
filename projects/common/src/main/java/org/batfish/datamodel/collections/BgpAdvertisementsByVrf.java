package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serializable;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpAdvertisement;

public final class BgpAdvertisementsByVrf extends TreeMap<String, SortedSet<BgpAdvertisement>>
    implements Serializable {

  private boolean _unrecognized;

  public boolean getUnrecognized() {
    return _unrecognized;
  }

  public void setUnrecognized(boolean unrecognized) {
    _unrecognized = unrecognized;
  }

  @JsonCreator
  private static @Nonnull BgpAdvertisementsByVrf create(
      Map<String, SortedSet<BgpAdvertisement>> map) {
    BgpAdvertisementsByVrf value = new BgpAdvertisementsByVrf();
    value.putAll(map);
    return value;
  }
}
