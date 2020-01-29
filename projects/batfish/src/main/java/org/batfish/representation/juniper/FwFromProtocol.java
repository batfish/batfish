package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from protocol */
public final class FwFromProtocol implements FwFrom {

  private final IpProtocol _protocol;

  public FwFromProtocol(IpProtocol protocol) {
    _protocol = protocol;
  }

  public IpProtocol getProtocol() {
    return _protocol;
  }

  @Override
  public Field getField() {
    return Field.PROTOCOL;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched protocol %s", _protocol.name().toLowerCase()));
  }

  private HeaderSpace toHeaderspace() {
    return HeaderSpace.builder().setIpProtocols(_protocol).build();
  }
}
