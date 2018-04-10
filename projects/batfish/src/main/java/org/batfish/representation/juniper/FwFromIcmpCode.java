package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.SubRange;

public class FwFromIcmpCode extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private SubRange _icmpCodeRange;

  public FwFromIcmpCode(SubRange icmpCodeRange) {
    _icmpCodeRange = icmpCodeRange;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    headerSpaceBuilder.setIcmpCodes(
        Iterables.concat(headerSpaceBuilder.getIcmpCodes(), ImmutableSet.of(_icmpCodeRange)));
  }
}
