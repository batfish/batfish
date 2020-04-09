package org.batfish.representation.juniper;

import com.google.common.annotations.VisibleForTesting;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
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
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  @VisibleForTesting
  HeaderSpace toHeaderspace() {
    return HeaderSpace.builder()
        .setIpProtocols(IpProtocol.TCP, IpProtocol.UDP)
        .setDstPorts(_portRange)
        .build();
  }

  private TraceElement getTraceElement() {
    return _portRange.getStart() == _portRange.getEnd()
        ? TraceElement.of(String.format("Matched destination-port %d", _portRange.getStart()))
        : TraceElement.of(
            String.format(
                "Matched destination-port %d-%d", _portRange.getStart(), _portRange.getEnd()));
  }
}
