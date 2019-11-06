package org.batfish.representation.cisco_xr;

import static org.batfish.representation.cisco_xr.CiscoXrConfiguration.computeIcmpObjectGroupAclName;

import org.batfish.datamodel.acl.AclLineMatchExpr;
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
  public AclLineMatchExpr toAclLineMatchExpr() {
    return new PermittedByAcl(computeIcmpObjectGroupAclName(_name));
  }
}
