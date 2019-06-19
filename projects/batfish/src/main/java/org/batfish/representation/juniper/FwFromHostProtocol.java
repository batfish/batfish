package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.IpAccessListLine;

public class FwFromHostProtocol implements Serializable {

  private static final long serialVersionUID = 1L;

  private final HostProtocol _protocol;

  public FwFromHostProtocol(HostProtocol protocol) {
    _protocol = protocol;
  }

  public void applyTo(List<IpAccessListLine> lines, Warnings w) {
    lines.addAll(_protocol.getLines());
  }
}
