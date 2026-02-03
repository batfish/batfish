package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;

public class FtdOspfArea implements Serializable {

  private final long _areaId;

  public FtdOspfArea(long areaId) {
    _areaId = areaId;
  }

  public long getAreaId() {
    return _areaId;
  }
}
