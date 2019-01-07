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
    switch (i) {
      case 1:
        return E1;
      case 2:
        return E2;
      default:
        throw new BatfishException("invalid ospf metric type");
    }
  }

  public RoutingProtocol toRoutingProtocol() {
    switch (this) {
      case E1:
        return RoutingProtocol.OSPF_E1;
      case E2:
        return RoutingProtocol.OSPF_E2;
      default:
        throw new BatfishException("invalid ospf metric type");
    }
  }
}
