package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public interface ProtocolObjectGroupLine extends Serializable {
  AclLineMatchExpr toAclLineMatchExpr();
}
