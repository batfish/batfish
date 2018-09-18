package org.batfish.representation.cisco;

import static org.batfish.representation.cisco.CiscoConfiguration.computeProtocolObjectGroupAclName;

import javax.annotation.Nonnull;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;

public class ProtocolObjectGroupReferenceLine implements ProtocolObjectGroupLine {

  /** */
  private static final long serialVersionUID = 1L;

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
