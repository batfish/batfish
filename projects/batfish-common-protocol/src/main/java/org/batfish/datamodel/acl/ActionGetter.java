package org.batfish.datamodel.acl;

import javax.annotation.Nullable;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.LineAction;

/** {@link GenericAclLineVisitor} that returns the action that the line will take */
public class ActionGetter implements GenericAclLineVisitor<LineAction> {
  private static final ActionGetter INSTANCE = new ActionGetter();

  @Nullable
  public static LineAction getAction(AclLine aclLine) {
    return INSTANCE.visit(aclLine);
  }

  private ActionGetter() {}

  @Override
  public LineAction visitAclAclLine(AclAclLine aclAclLine) {
    return null;
  }

  @Override
  public LineAction visitExprAclLine(ExprAclLine exprAclLine) {
    return exprAclLine.getAction();
  }
}
