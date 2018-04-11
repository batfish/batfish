package org.batfish.representation.juniper;

import com.google.common.collect.Iterables;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.SubRange;

public class FwFromPacketLength extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private boolean _except;

  private final List<SubRange> _range;

  public FwFromPacketLength(List<SubRange> range, boolean except) {
    _range = range;
    _except = except;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    if (_except) {
      headerSpaceBuilder.setNotPacketLengths(
          Iterables.concat(headerSpaceBuilder.getNotPacketLengths(), _range));
    } else {
      headerSpaceBuilder.setPacketLengths(
          Iterables.concat(headerSpaceBuilder.getPacketLengths(), _range));
    }
  }
}
