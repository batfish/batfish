package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import java.util.Map;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public interface ProtocolObjectGroupLine extends Serializable {
  AclLineMatchExpr toAclLineMatchExpr(Map<String, ProtocolObjectGroup> protocolObjectGroups);
}
