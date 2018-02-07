package org.batfish.z3.expr;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.z3.expr.visitors.ExprVisitor;

public class ExpandedListExpr extends ListExpr {

  public ExpandedListExpr(List<Expr> subExpressions) {
    super(ImmutableList.<Expr>builder().add(new IdExpr("")).addAll(subExpressions).build());
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitExpandedListExpr(this);
  }
}
