package org.batfish.representation.cisco_asa;

import static org.batfish.representation.cisco_asa.AsaConfiguration.computeServiceObjectAclName;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.PermittedByAcl;

@ParametersAreNonnullByDefault
public final class ServiceObjectReferenceServiceObjectGroupLine implements ServiceObjectGroupLine {

  private final String _name;

  public ServiceObjectReferenceServiceObjectGroupLine(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  @Override
  public @Nonnull AclLineMatchExpr toAclLineMatchExpr(
      Map<String, ServiceObject> serviceObjects,
      Map<String, ServiceObjectGroup> serviceObjectGroups) {
    if (!serviceObjects.containsKey(_name)) {
      return AclLineMatchExprs.FALSE;
    }
    return new PermittedByAcl(computeServiceObjectAclName(_name));
  }
}
