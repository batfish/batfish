package org.batfish.representation.cisco_nxos;

public final class OspfMetricManual implements OspfMetric {

  public OspfMetricManual(int metric) {
    _metric = metric;
  }

  public int getMetric() {
    return _metric;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final int _metric;
}
