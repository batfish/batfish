package org.batfish.representation.cisco_asa;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.PermittedByAcl;

public class ServiceObjectServiceSpecifier implements AccessListServiceSpecifier {

  private String _name;

  public ServiceObjectServiceSpecifier(String name) {
    _name = name;
  }

  @Override
  public @Nonnull AclLineMatchExpr toAclLineMatchExpr(
      Map<String, ObjectGroup> objectGroups, Map<String, ServiceObject> serviceObjects) {
    if (!serviceObjects.containsKey(_name)) {
      return AclLineMatchExprs.FALSE;
    }
    String aclName = AsaConfiguration.computeServiceObjectAclName(_name);
    return new PermittedByAcl(aclName, String.format("Match service object: '%s'", _name));
  }
}
