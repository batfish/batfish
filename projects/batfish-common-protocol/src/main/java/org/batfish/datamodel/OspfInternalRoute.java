package org.batfish.datamodel;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.route.nh.NextHop;

/** A base class for OSPF inter-area and intra-area routes */
@ParametersAreNonnullByDefault
public abstract class OspfInternalRoute extends OspfRoute {

  protected OspfInternalRoute(
      Prefix network,
      NextHop nextHop,
      int admin,
      long metric,
      long area,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, nextHop, admin, metric, area, tag, nonRouting, nonForwarding);
  }
}
