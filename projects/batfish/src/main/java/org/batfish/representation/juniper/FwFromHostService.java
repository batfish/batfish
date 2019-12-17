package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ExprAclLine;

public class FwFromHostService implements Serializable {

  private final HostSystemService _service;

  public FwFromHostService(HostSystemService service) {
    _service = service;
  }

  public void applyTo(List<? super ExprAclLine> lines, Warnings w) {
    lines.addAll(_service.getLines());
  }
}
