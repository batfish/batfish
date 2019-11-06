package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public interface ServiceObjectGroupLine extends Serializable {
  AclLineMatchExpr toAclLineMatchExpr();
}
