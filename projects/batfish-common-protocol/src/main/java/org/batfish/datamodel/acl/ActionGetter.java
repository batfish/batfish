package org.batfish.datamodel.acl;

import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.LineAction;

/** {@link GenericAclLineVisitor} that returns the action that the line will take */
public class ActionGetter implements GenericAclLineVisitor<LineAction> {
  public ActionGetter() {}

  @Override
  public LineAction visitAclAclLine(AclAclLine aclAclLine) {
    return null;
  }

  @Override
  public LineAction visitExprAclLine(ExprAclLine exprAclLine) {
    return exprAclLine.getAction();
  }
}
