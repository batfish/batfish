package org.batfish.representation.juniper;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from destination-address */
public final class FwFromDestinationAddress implements FwFrom {

  @Nullable private final IpWildcard _ipWildcard;

  public FwFromDestinationAddress(IpWildcard ipWildcard) {
    _ipWildcard = ipWildcard;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    headerSpaceBuilder.setDstIps(
        AclIpSpace.union(headerSpaceBuilder.getDstIps(), _ipWildcard.toIpSpace()));
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @Override
  public Field getField() {
    return Field.DESTINATION;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched destination-address %s", _ipWildcard.toString()));
  }

  @VisibleForTesting
  HeaderSpace toHeaderspace() {
    return HeaderSpace.builder().setDstIps(_ipWildcard.toIpSpace()).build();
  }
}
