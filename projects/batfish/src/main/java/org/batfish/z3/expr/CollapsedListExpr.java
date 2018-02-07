package org.batfish.z3.expr;

import java.util.List;

public class CollapsedListExpr extends ListExpr {

  public CollapsedListExpr(List<Expr> subExpressions) {
    super(subExpressions);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitCollapsedListExpr(this);
  }
}
