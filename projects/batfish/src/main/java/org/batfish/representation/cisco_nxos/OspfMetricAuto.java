package org.batfish.representation.cisco_nxos;

import javax.annotation.Nonnull;

public final class OspfMetricAuto implements OspfMetric {

  public static @Nonnull OspfMetricAuto instance() {
    return INSTANCE;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private static final OspfMetricAuto INSTANCE = new OspfMetricAuto();

  private OspfMetricAuto() {}
}
