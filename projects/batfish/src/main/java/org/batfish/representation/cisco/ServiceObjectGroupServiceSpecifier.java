package org.batfish.representation.cisco;

import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;

public class ServiceObjectGroupServiceSpecifier implements AccessListServiceSpecifier {

  /** */
  private static final long serialVersionUID = 1L;

  private final String _name;

  public ServiceObjectGroupServiceSpecifier(String name) {
    _name = name;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr() {
    return new PermittedByAcl(
        CiscoConfiguration.computeServiceObjectGroupAclName(_name),
        String.format("Match service object-group: '%s'", _name));
  }
}
