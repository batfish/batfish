package org.batfish.datamodel.acl;

import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.LineAction;

/**
 * {@link GenericAclLineVisitor} that returns the action that the line will take, or the opposite
 * action if {@code flip} is true
 */
public class ActionGetter implements GenericAclLineVisitor<LineAction> {
  private final boolean _flip;

  public ActionGetter(boolean flip) {
    _flip = flip;
  }

  private static LineAction flip(LineAction action) {
    return action == LineAction.PERMIT ? LineAction.DENY : LineAction.PERMIT;
  }

  @Override
  public LineAction visitExprAclLine(ExprAclLine exprAclLine) {
    return _flip ? flip(exprAclLine.getAction()) : exprAclLine.getAction();
  }
}
