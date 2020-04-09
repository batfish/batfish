package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
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
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  private HeaderSpace toHeaderspace() {
    return HeaderSpace.builder()
        .setIpProtocols(IpProtocol.TCP, IpProtocol.UDP)
        .setSrcOrDstPorts(ImmutableList.of(_portRange))
        .build();
  }

  private TraceElement getTraceElement() {
    return _portRange.isSingleValue()
        ? TraceElement.of(String.format("Matched port %d", _portRange.getStart()))
        : TraceElement.of(
            String.format("Matched port %d-%d", _portRange.getStart(), _portRange.getEnd()));
  }
}
