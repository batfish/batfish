package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Interface Ospf data */
public class OspfInterface implements Serializable {
  private @Nullable Long _ospfArea;

  @Nullable
  public Long getOspfArea() {
    return _ospfArea;
  }

  public void setOspfArea(@Nullable Long ospfArea) {
    _ospfArea = ospfArea;
  }
}
