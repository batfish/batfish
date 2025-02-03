package org.batfish.representation.cisco_asa;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;

public class InspectClassMapMatchProtocol implements InspectClassMapMatch {

  private final InspectClassMapProtocol _protocol;

  public InspectClassMapMatchProtocol(InspectClassMapProtocol protocol) {
    _protocol = protocol;
  }

  public InspectClassMapProtocol getProtocol() {
    return _protocol;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(
      AsaConfiguration cc, Configuration c, MatchSemantics matchSemantics, Warnings w) {
    /* TODO: Proper implementation: https://github.com/batfish/batfish/issues/1260 */
    IpProtocol protocol =
        switch (_protocol) {
          case HTTP -> IpProtocol.TCP;
          case HTTPS -> IpProtocol.TCP;
          case ICMP -> IpProtocol.ICMP;
          case TCP -> IpProtocol.TCP;
          case TFTP -> IpProtocol.UDP;
          case UDP -> IpProtocol.UDP;
        };
    return AclLineMatchExprs.matchIpProtocol(protocol);
  }
}
