package org.batfish.representation.juniper;

import java.util.Collections;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
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
  public void applyTo(IpAccessListLine line, JuniperConfiguration jc, Warnings w, Configuration c) {
    Set<SubRange> offsets = Collections.singleton(_offsetRange);
    if (_except) {
      line.setNotFragmentOffsets(offsets);
    } else {
      line.setFragmentOffsets(offsets);
    }
  }
}
