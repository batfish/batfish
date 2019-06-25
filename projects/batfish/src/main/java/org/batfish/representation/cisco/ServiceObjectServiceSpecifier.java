package org.batfish.representation.cisco;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;

public class ServiceObjectServiceSpecifier implements AccessListServiceSpecifier {

  private String _name;

  public ServiceObjectServiceSpecifier(String name) {
    _name = name;
  }

  @Override
  @Nonnull
  public AclLineMatchExpr toAclLineMatchExpr(Map<String, ObjectGroup> objectGroups) {
    String aclName = CiscoConfiguration.computeServiceObjectAclName(_name);
    return new PermittedByAcl(aclName, String.format("Match service object: '%s'", _name));
  }
}
