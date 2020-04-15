package org.batfish.representation.arista;

import java.io.Serializable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.acl.AclLineMatchExpr;

public interface InspectClassMapMatch extends Serializable {
  AclLineMatchExpr toAclLineMatchExpr(
      AristaConfiguration cc, Configuration c, MatchSemantics matchSemantics, Warnings w);
}
