package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Optional;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public class FwFromHostProtocol implements Serializable {

  private final HostProtocol _protocol;

  public FwFromHostProtocol(HostProtocol protocol) {
    _protocol = protocol;
  }

  public Optional<AclLineMatchExpr> getMatchExpr() {
    return _protocol.getMatchExpr();
  }
}
