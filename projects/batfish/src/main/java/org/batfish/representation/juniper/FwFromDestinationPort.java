package org.batfish.representation.juniper;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from destination port */
public final class FwFromDestinationPort implements FwFrom {

  private final SubRange _portRange;

  public FwFromDestinationPort(int port) {
    _portRange = SubRange.singleton(port);
  }

  public FwFromDestinationPort(SubRange subrange) {
    _portRange = subrange;
  }

  public SubRange getPortRange() {
    return _portRange;
  }

  @Override
  public Field getField() {
    return Field.DESTINATION_PORT;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return and(
        getTraceElement(_portRange),
        or(
            matchIpProtocol(IpProtocol.TCP),
            matchIpProtocol(IpProtocol.UDP),
            matchIpProtocol(IpProtocol.SCTP)),
        matchDstPort(IntegerSpace.of(_portRange)));
  }

  // Visible for use by siblings.
  static TraceElement getTraceElement(SubRange portRange) {
    return portRange.getStart() == portRange.getEnd()
        ? TraceElement.of(String.format("Matched destination-port %d", portRange.getStart()))
        : TraceElement.of(
            String.format(
                "Matched destination-port %d-%d", portRange.getStart(), portRange.getEnd()));
  }
}
