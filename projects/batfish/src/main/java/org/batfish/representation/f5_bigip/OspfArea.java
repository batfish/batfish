package org.batfish.representation.f5_bigip;

import java.io.Serializable;

/** Configuration for an OSPF area within a process. */
public final class OspfArea implements Serializable {

  // TODO: determine. Current value is just a guess.
  private static final int DEFAULT_DEFAULT_COST = 1;

  public static int defaultDefaultCost() {
    return DEFAULT_DEFAULT_COST;
  }

  public OspfArea(long id) {
    _id = id;
  }

  public int getDefaultCost() {
    return _defaultCost;
  }

  public void setDefaultCost(int defaultCost) {
    _defaultCost = defaultCost;
  }

  public long getId() {
    return _id;
  }

  private int _defaultCost;
  private final long _id;
}
