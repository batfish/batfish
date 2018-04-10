package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;

public final class FwFromProtocol extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final IpProtocol _protocol;

  public FwFromProtocol(IpProtocol protocol) {
    _protocol = protocol;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    headerSpaceBuilder.setIpProtocols(
        Iterables.concat(headerSpaceBuilder.getIpProtocols(), ImmutableSet.of(_protocol)));
  }

  public IpProtocol getProtocol() {
    return _protocol;
  }
}
