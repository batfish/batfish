package org.batfish.representation.cisco;

import static org.batfish.representation.cisco.CiscoConfiguration.computeServiceObjectGroupAclName;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;

public final class ServiceObjectGroupReferenceServiceObjectGroupLine
    implements ServiceObjectGroupLine {

  private static final long serialVersionUID = 1L;

  private final String _name;

  public ServiceObjectGroupReferenceServiceObjectGroupLine(@Nonnull String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr() {
    return new PermittedByAcl(computeServiceObjectGroupAclName(_name));
  }
}
