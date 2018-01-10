package org.batfish.representation.juniper;

import com.google.common.collect.Iterables;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
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
  public void applyTo(IpAccessListLine line, JuniperConfiguration jc, Warnings w, Configuration c) {
    if (_except) {
      line.setNotPacketLengths(Iterables.concat(line.getNotPacketLengths(), _range));
    } else {
      line.setPacketLengths(Iterables.concat(line.getPacketLengths(), _range));
    }
  }
}
