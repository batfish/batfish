package org.batfish.representation.cisco_xr;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.PermittedByAcl;

public class ProtocolOrServiceObjectGroupServiceSpecifier implements AccessListServiceSpecifier {

  private String _name;

  public ProtocolOrServiceObjectGroupServiceSpecifier(String name) {
    _name = name;
  }

  @Override
  @Nonnull
  public AclLineMatchExpr toAclLineMatchExpr(Map<String, ObjectGroup> objectGroups) {
    ObjectGroup objectGroup = objectGroups.get(_name);
    String aclName;
    if (objectGroup instanceof ProtocolObjectGroup) {
      aclName = CiscoXrConfiguration.computeProtocolObjectGroupAclName(_name);
    } else if (objectGroup instanceof ServiceObjectGroup) {
      aclName = CiscoXrConfiguration.computeServiceObjectGroupAclName(_name);
    } else {
      return FalseExpr.INSTANCE;
    }
    return new PermittedByAcl(aclName, String.format("Match object-group: '%s'", _name));
  }
}
