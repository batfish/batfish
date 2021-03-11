package org.batfish.representation.cisco_asa;

import static org.batfish.representation.cisco_asa.AsaConfiguration.computeServiceObjectGroupAclName;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.PermittedByAcl;

@ParametersAreNonnullByDefault
public final class ServiceObjectGroupReferenceServiceObjectGroupLine
    implements ServiceObjectGroupLine {

  private final String _name;

  public ServiceObjectGroupReferenceServiceObjectGroupLine(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  @Override
  public @Nonnull AclLineMatchExpr toAclLineMatchExpr(
      Map<String, ServiceObject> serviceObjects,
      Map<String, ServiceObjectGroup> serviceObjectGroups) {
    if (!serviceObjectGroups.containsKey(_name)) {
      return AclLineMatchExprs.FALSE;
    }
    return new PermittedByAcl(computeServiceObjectGroupAclName(_name));
  }
}
