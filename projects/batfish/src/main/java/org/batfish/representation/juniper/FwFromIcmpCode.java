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

/** Class for firewall filter from icmp code */
public class FwFromIcmpCode implements FwFrom {

  private SubRange _icmpCodeRange;

  public FwFromIcmpCode(SubRange icmpCodeRange) {
    _icmpCodeRange = icmpCodeRange;
  }

  @Override
  public Field getField() {
    return Field.ICMP_CODE;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  private HeaderSpace toHeaderspace() {
    return HeaderSpace.builder()
        .setIpProtocols(IpProtocol.ICMP)
        .setIcmpCodes(_icmpCodeRange)
        .build();
  }

  private TraceElement getTraceElement() {
    return _icmpCodeRange.getStart() == _icmpCodeRange.getEnd()
        ? TraceElement.of(String.format("Matched icmp-code %d", _icmpCodeRange.getStart()))
        : TraceElement.of(
            String.format(
                "Matched icmp-code %d-%d", _icmpCodeRange.getStart(), _icmpCodeRange.getEnd()));
  }
}
