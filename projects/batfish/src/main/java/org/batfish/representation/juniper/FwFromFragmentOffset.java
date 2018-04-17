package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.SubRange;

public class FwFromFragmentOffset extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private boolean _except;

  private SubRange _offsetRange;

  public FwFromFragmentOffset(SubRange offsetRange, boolean except) {
    _offsetRange = offsetRange;
    _except = except;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    Set<SubRange> offsets = ImmutableSet.of(_offsetRange);
    if (_except) {
      headerSpaceBuilder.setNotFragmentOffsets(offsets);
    } else {
      headerSpaceBuilder.setFragmentOffsets(offsets);
    }
  }
}
