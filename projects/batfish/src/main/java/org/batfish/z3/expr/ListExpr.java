package org.batfish.z3.expr;

import java.util.List;

public abstract class ListExpr extends Expr {

  private final List<Expr> _subExpressions;

  public ListExpr(List<Expr> subExpressions) {
    _subExpressions = subExpressions;
  }

  public List<Expr> getSubExpressions() {
    return _subExpressions;
  }
}
