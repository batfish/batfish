package org.batfish.representation.cisco;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;

public class ProtocolOrServiceObjectGroupServiceSpecifier implements AccessListServiceSpecifier {

  /** */
  private static final long serialVersionUID = 1L;

  private String _name;

  public ProtocolOrServiceObjectGroupServiceSpecifier(String name) {
    _name = name;
  }

  @Override
  @Nonnull
  public AclLineMatchExpr toAclLineMatchExpr(Map<String, ObjectGroup> objectGroups) {
    String aclName =
        (objectGroups.get(_name) instanceof ProtocolObjectGroup)
            ? CiscoConfiguration.computeProtocolObjectGroupAclName(_name)
            : CiscoConfiguration.computeServiceObjectGroupAclName(_name);
    return new PermittedByAcl(aclName, String.format("Match object-group: '%s'", _name));
  }
}
