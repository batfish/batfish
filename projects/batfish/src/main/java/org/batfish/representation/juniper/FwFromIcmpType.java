package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.SubRange;

public class FwFromIcmpType extends FwFrom {

  private SubRange _icmpTypeRange;

  public FwFromIcmpType(SubRange icmpTypeRange) {
    _icmpTypeRange = icmpTypeRange;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    headerSpaceBuilder.setIcmpTypes(
        Iterables.concat(headerSpaceBuilder.getIcmpTypes(), ImmutableSet.of(_icmpTypeRange)));
  }
}
