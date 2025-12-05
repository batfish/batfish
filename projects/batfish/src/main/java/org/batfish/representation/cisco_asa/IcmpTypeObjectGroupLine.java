package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import java.util.Map;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public interface IcmpTypeObjectGroupLine extends Serializable {
  AclLineMatchExpr toAclLineMatchExpr(Map<String, IcmpTypeObjectGroup> icmpTypeObjectGroups);
}
