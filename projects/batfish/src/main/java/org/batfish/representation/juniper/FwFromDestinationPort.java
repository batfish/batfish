package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.SubRange;

public final class FwFromDestinationPort extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final SubRange _portRange;

  public FwFromDestinationPort(int port) {
    _portRange = new SubRange(port, port);
  }

  public FwFromDestinationPort(SubRange subrange) {
    _portRange = subrange;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    headerSpaceBuilder.setDstPorts(
        Iterables.concat(headerSpaceBuilder.getDstPorts(), ImmutableSet.of(_portRange)));
  }

  public SubRange getPortRange() {
    return _portRange;
  }
}
