package org.batfish.datamodel;

import javax.annotation.ParametersAreNonnullByDefault;

/** A base class for OSPF inter-area and intra-area routes */
@ParametersAreNonnullByDefault
public abstract class OspfInternalRoute extends OspfRoute {

  private static final long serialVersionUID = 1L;

  protected OspfInternalRoute(
      Prefix network,
      Ip nextHopIp,
      int admin,
      long metric,
      long area,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, nextHopIp, admin, metric, area, nonRouting, nonForwarding);
  }
}
