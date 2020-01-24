package org.batfish.representation.juniper;

import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.TraceElement;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for match destination-address except */
public final class FwFromDestinationAddressExcept extends FwFrom {

  @Nullable private final IpWildcard _ipWildcard;

  public FwFromDestinationAddressExcept(IpWildcard ipWildcard) {
    _ipWildcard = ipWildcard;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    headerSpaceBuilder.setNotDstIps(
        AclIpSpace.union(headerSpaceBuilder.getNotDstIps(), _ipWildcard.toIpSpace()));
  }

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @Override
  Field getField() {
    return Field.DESTINATION;
  }

  @Override
  TraceElement getTraceElement() {
    return TraceElement.of(
        String.format("Matched destination-address %s except", _ipWildcard.toString()));
  }

  @Override
  HeaderSpace toHeaderspace(JuniperConfiguration jc, Configuration c, Warnings w) {
    return HeaderSpace.builder().setNotDstIps(_ipWildcard.toIpSpace()).build();
  }
}
