package org.batfish.vendor.huawei.representation;

/** OSPF area configuration. */
public class HuaweiOspfArea {
  private final long _areaId;

  public HuaweiOspfArea(long areaId) {
    _areaId = areaId;
  }

  public long getAreaId() {
    return _areaId;
  }
}
