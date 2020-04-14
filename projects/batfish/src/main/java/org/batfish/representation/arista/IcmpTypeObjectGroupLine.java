package org.batfish.representation.arista;

import java.io.Serializable;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public interface IcmpTypeObjectGroupLine extends Serializable {
  AclLineMatchExpr toAclLineMatchExpr();
}
