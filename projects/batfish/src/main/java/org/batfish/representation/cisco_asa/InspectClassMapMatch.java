package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public interface InspectClassMapMatch extends Serializable {
  AclLineMatchExpr toAclLineMatchExpr(
      AsaConfiguration cc, Configuration c, MatchSemantics matchSemantics, Warnings w);
}
