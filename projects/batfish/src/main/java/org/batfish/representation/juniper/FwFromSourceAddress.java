package org.batfish.representation.juniper;

import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.common.ip.IpWildcard;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from source-address */
public final class FwFromSourceAddress implements FwFrom {

  @Nullable private final IpWildcard _ipWildcard;

  public FwFromSourceAddress(IpWildcard ipWildcard) {
    _ipWildcard = ipWildcard;
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @Override
  public Field getField() {
    return Field.SOURCE;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched source-address %s", _ipWildcard.toString()));
  }

  private HeaderSpace toHeaderspace() {
    return HeaderSpace.builder().setSrcIps(_ipWildcard.toIpSpace()).build();
  }
}
