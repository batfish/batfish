package org.batfish.representation.juniper;

import com.google.common.collect.Iterables;
import java.util.Collections;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.SubRange;

public final class FwFromSourcePort extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final SubRange _portRange;

  public FwFromSourcePort(int port) {
    _portRange = new SubRange(port, port);
  }

  public FwFromSourcePort(SubRange subrange) {
    _portRange = subrange;
  }

  @Override
  public void applyTo(IpAccessListLine line, JuniperConfiguration jc, Warnings w, Configuration c) {
    line.setSrcPorts(Iterables.concat(line.getSrcPorts(), Collections.singleton(_portRange)));
  }

  public SubRange getPortRange() {
    return _portRange;
  }
}
