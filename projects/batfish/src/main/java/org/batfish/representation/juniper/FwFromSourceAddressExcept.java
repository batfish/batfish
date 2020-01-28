package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from source-address except */
public final class FwFromSourceAddressExcept implements FwFrom {

  private final IpWildcard _ipWildcard;

  public FwFromSourceAddressExcept(IpWildcard ipWildcard) {
    _ipWildcard = ipWildcard;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    headerSpaceBuilder.setNotSrcIps(
        AclIpSpace.union(headerSpaceBuilder.getNotSrcIps(), _ipWildcard.toIpSpace()));
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @Override
  public Field getField() {
    return Field.SOURCE_EXCEPT;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(
        String.format("Matched source-address %s except", _ipWildcard.toString()));
  }

  private HeaderSpace toHeaderspace() {
    return HeaderSpace.builder().setNotSrcIps(_ipWildcard.toIpSpace()).build();
  }
}
