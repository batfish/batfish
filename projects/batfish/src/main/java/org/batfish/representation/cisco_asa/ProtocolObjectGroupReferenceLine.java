package org.batfish.representation.cisco_asa;

import static org.batfish.representation.cisco_asa.AsaConfiguration.computeProtocolObjectGroupAclName;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.PermittedByAcl;

public class ProtocolObjectGroupReferenceLine implements ProtocolObjectGroupLine {

  private final String _name;

  public ProtocolObjectGroupReferenceLine(@Nonnull String name) {
    _name = name;
  }

  public String getReferenceName() {
    return _name;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(
      Map<String, ProtocolObjectGroup> protocolObjectGroups) {
    if (!protocolObjectGroups.containsKey(_name)) {
      return AclLineMatchExprs.FALSE;
    }
    return new PermittedByAcl(computeProtocolObjectGroupAclName(_name));
  }
}
