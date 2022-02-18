package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Optional;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public class FwFromHostService implements Serializable {

  private final HostSystemService _service;

  public FwFromHostService(HostSystemService service) {
    _service = service;
  }

  public Optional<AclLineMatchExpr> getMatchExpr() {
    return _service.getMatchExpr();
  }
}
