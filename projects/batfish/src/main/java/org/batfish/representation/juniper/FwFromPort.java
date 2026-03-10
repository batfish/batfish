package org.batfish.representation.juniper;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from port */
public final class FwFromPort implements FwFrom {

  private final SubRange _portRange;

  public FwFromPort(int port) {
    _portRange = SubRange.singleton(port);
  }

  public FwFromPort(SubRange subrange) {
    _portRange = subrange;
  }

  public SubRange getPortRange() {
    return _portRange;
  }

  @Override
  public Field getField() {
    return Field.PORT;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    IntegerSpace space = IntegerSpace.of(_portRange);
    return and(
        getTraceElement(),
        or(
            matchIpProtocol(IpProtocol.TCP),
            matchIpProtocol(IpProtocol.UDP),
            matchIpProtocol(IpProtocol.SCTP)),
        or(
            matchDstPort(space, FwFromDestinationPort.getTraceElement(_portRange)),
            matchSrcPort(space, FwFromSourcePort.getTraceElement(_portRange))));
  }

  private TraceElement getTraceElement() {
    return _portRange.isSingleValue()
        ? TraceElement.of(String.format("Matched port %d", _portRange.getStart()))
        : TraceElement.of(
            String.format("Matched port %d-%d", _portRange.getStart(), _portRange.getEnd()));
  }
}
