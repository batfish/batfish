package org.batfish.representation.cisco;

import static org.batfish.representation.cisco.CiscoConfiguration.computeIcmpObjectGroupAclName;

import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;

public class IcmpTypeGroupReferenceLine implements IcmpTypeObjectGroupLine {

  /** */
  private static final long serialVersionUID = 1L;

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
