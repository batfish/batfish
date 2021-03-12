package org.batfish.representation.cisco_asa;

import static org.batfish.representation.cisco_asa.AsaConfiguration.computeProtocolObjectGroupAclName;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
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
  public AclLineMatchExpr toAclLineMatchExpr() {
    return new PermittedByAcl(computeProtocolObjectGroupAclName(_name));
  }
}
