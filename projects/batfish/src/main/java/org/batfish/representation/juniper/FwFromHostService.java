package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.IpAccessListLine;

public class FwFromHostService implements Serializable {

  private static final long serialVersionUID = 1L;

  private final HostSystemService _service;

  public FwFromHostService(HostSystemService service) {
    _service = service;
  }

  public void applyTo(List<IpAccessListLine> lines, Warnings w) {
    lines.addAll(_service.getLines());
  }
}
