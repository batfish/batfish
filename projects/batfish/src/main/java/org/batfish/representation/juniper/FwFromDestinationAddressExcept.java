package org.batfish.representation.juniper;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from destination-address except */
public final class FwFromDestinationAddressExcept implements FwFrom {

  @Nullable private final IpWildcard _ipWildcard;

  public FwFromDestinationAddressExcept(IpWildcard ipWildcard) {
    _ipWildcard = ipWildcard;
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @Override
  public Field getField() {
    return Field.DESTINATION_EXCEPT;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderspace(), getTraceElement());
  }

  @VisibleForTesting
  HeaderSpace toHeaderspace() {
    return HeaderSpace.builder().setNotDstIps(_ipWildcard.toIpSpace()).build();
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(
        String.format("Matched destination-address %s except", _ipWildcard.toString()));
  }
}
