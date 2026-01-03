package org.batfish.datamodel.ospf;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.RoutingProtocol;

/** Type of metric for OSPF external routes */
public enum OspfMetricType {
  /** External type 1 */
  E1,
  /** External type 2 */
  E2;

  public static OspfMetricType fromInteger(int i) {
    if (i == 1) {
      return E1;
    } else if (i == 2) {
      return E2;
    }
    throw new BatfishException("invalid ospf metric type");
  }

  public RoutingProtocol toRoutingProtocol() {
    return switch (this) {
      case E1 -> RoutingProtocol.OSPF_E1;
      case E2 -> RoutingProtocol.OSPF_E2;
    };
  }
}
