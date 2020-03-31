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

/** Class for firewall filter from source-port */
public final class FwFromSourcePort implements FwFrom {

  private final SubRange _portRange;

  public FwFromSourcePort(int port) {
    _portRange = SubRange.singleton(port);
  }

  public FwFromSourcePort(SubRange subrange) {
    _portRange = subrange;
  }

  public SubRange getPortRange() {
    return _portRange;
  }

  @Override
  public Field getField() {
    return Field.SOURCE_PORT;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  private TraceElement getTraceElement() {
    return _portRange.isSingleValue()
        ? TraceElement.of(String.format("Matched source-port %d", _portRange.getStart()))
        : TraceElement.of(
            String.format("Matched source-port %d-%d", _portRange.getStart(), _portRange.getEnd()));
  }

  private HeaderSpace toHeaderspace() {
    return HeaderSpace.builder()
        .setIpProtocols(IpProtocol.TCP, IpProtocol.UDP)
        .setSrcPorts(_portRange)
        .build();
  }
}
