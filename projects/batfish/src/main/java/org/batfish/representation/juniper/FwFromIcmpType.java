package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from icmp-type */
public class FwFromIcmpType implements FwFrom {

  private SubRange _icmpTypeRange;

  public FwFromIcmpType(SubRange icmpTypeRange) {
    _icmpTypeRange = icmpTypeRange;
  }

  @Override
  public Field getField() {
    return Field.ICMP_TYPE;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  private HeaderSpace toHeaderspace() {
    return HeaderSpace.builder()
        .setIpProtocols(IpProtocol.ICMP)
        .setIcmpTypes(_icmpTypeRange)
        .build();
  }

  private TraceElement getTraceElement() {
    return _icmpTypeRange.getStart() == _icmpTypeRange.getEnd()
        ? TraceElement.of(String.format("Matched icmp-type %d", _icmpTypeRange.getStart()))
        : TraceElement.of(
            String.format(
                "Matched icmp-type %d-%d", _icmpTypeRange.getStart(), _icmpTypeRange.getEnd()));
  }
}
