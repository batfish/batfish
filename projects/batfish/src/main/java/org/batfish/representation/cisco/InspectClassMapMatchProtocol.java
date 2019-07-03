package org.batfish.representation.cisco;

import com.google.common.collect.ImmutableList;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.TrueExpr;

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
      CiscoConfiguration cc, Configuration c, MatchSemantics matchSemantics, Warnings w) {
    /* TODO: Proper implementation: https://github.com/batfish/batfish/issues/1260 */
    HeaderSpace headerSpace;
    switch (_protocol) {
      case HTTP:
        headerSpace =
            HeaderSpace.builder().setIpProtocols(ImmutableList.of(IpProtocol.TCP)).build();
        break;
      case HTTPS:
        headerSpace =
            HeaderSpace.builder().setIpProtocols(ImmutableList.of(IpProtocol.TCP)).build();
        break;
      case ICMP:
        headerSpace =
            HeaderSpace.builder().setIpProtocols(ImmutableList.of(IpProtocol.ICMP)).build();
        break;
      case TCP:
        headerSpace =
            HeaderSpace.builder().setIpProtocols(ImmutableList.of(IpProtocol.TCP)).build();
        break;
      case TFTP:
        headerSpace =
            HeaderSpace.builder().setIpProtocols(ImmutableList.of(IpProtocol.UDP)).build();
        break;
      case UDP:
        headerSpace =
            HeaderSpace.builder().setIpProtocols(ImmutableList.of(IpProtocol.UDP)).build();
        break;
      default:
        w.unimplemented(
            String.format(
                "Unsupported %s: %s", InspectClassMapProtocol.class.getSimpleName(), _protocol));
        switch (matchSemantics) {
          case MATCH_ALL:
            return TrueExpr.INSTANCE;
          case MATCH_ANY:
            return FalseExpr.INSTANCE;
          default:
            throw new BatfishException(
                String.format(
                    "Unsupported %s: %s", MatchSemantics.class.getSimpleName(), matchSemantics));
        }
    }
    return new MatchHeaderSpace(headerSpace);
  }
}
