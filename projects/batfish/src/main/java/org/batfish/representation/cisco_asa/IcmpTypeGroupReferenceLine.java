package org.batfish.representation.cisco_asa;

import static org.batfish.representation.cisco_asa.AsaConfiguration.computeIcmpObjectGroupAclName;

import java.util.Map;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.PermittedByAcl;

public class IcmpTypeGroupReferenceLine implements IcmpTypeObjectGroupLine {

  private final String _name;

  public IcmpTypeGroupReferenceLine(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(
      Map<String, IcmpTypeObjectGroup> icmpTypeObjectGroups) {
    if (!icmpTypeObjectGroups.containsKey(_name)) {
      return AclLineMatchExprs.FALSE;
    }
    return new PermittedByAcl(computeIcmpObjectGroupAclName(_name));
  }
}
