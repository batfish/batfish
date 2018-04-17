package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;

public final class FwFromDestinationAddress extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final Prefix _prefix;

  public FwFromDestinationAddress(Prefix prefix) {
    _prefix = prefix;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    headerSpaceBuilder.setDstIps(
        Iterables.concat(
            ((IpWildcardSetIpSpace) headerSpaceBuilder.getDstIps()).getWhitelist(),
            ImmutableSet.of(new IpWildcard(_prefix))));
  }

  public Prefix getPrefix() {
    return _prefix;
  }
}
