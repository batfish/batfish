package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from destination port */
public final class FwFromDestinationPort extends FwFrom {

  private final SubRange _portRange;

  public FwFromDestinationPort(int port) {
    _portRange = SubRange.singleton(port);
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

  @Override
  Field getField() {
    return Field.DESTINATION_PORT;
  }

  @Override
  TraceElement getTraceElement() {
    return TraceElement.of(
        String.format(
            "Matched destination-port %d-%d", _portRange.getStart(), _portRange.getEnd()));
  }

  @Override
  HeaderSpace toHeaderspace(JuniperConfiguration jc, Configuration c, Warnings w) {
    return HeaderSpace.builder().setDstPorts(_portRange).build();
  }
}
