package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public interface ProtocolObjectGroupLine extends Serializable {
  AclLineMatchExpr toAclLineMatchExpr();
}
