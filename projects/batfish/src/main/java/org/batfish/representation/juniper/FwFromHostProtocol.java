package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ExprAclLine;

public class FwFromHostProtocol implements Serializable {

  private final HostProtocol _protocol;

  public FwFromHostProtocol(HostProtocol protocol) {
    _protocol = protocol;
  }

  public void applyTo(List<? super ExprAclLine> lines, Warnings w) {
    lines.addAll(_protocol.getLines());
  }
}
