package org.batfish.vendor.huawei.representation;

/** VLAN configuration for Huawei device. */
public class HuaweiVlan {
  private final int _id;

  public HuaweiVlan(int id) {
    _id = id;
  }

  public int getId() {
    return _id;
  }
}
