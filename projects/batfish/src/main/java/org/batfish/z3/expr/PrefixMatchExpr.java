package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.datamodel.Prefix;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericBooleanExprVisitor;

public final class PrefixMatchExpr extends BooleanExpr {

  private BooleanExpr _expr;

  public PrefixMatchExpr(IntExpr var, Prefix prefix) {
    int length = prefix.getPrefixLength();
    if (length == 0) {
      _expr = TrueExpr.INSTANCE;
    } else if (length == Prefix.MAX_PREFIX_LENGTH) {
      _expr = new EqExpr(var, new LitIntExpr(prefix.getStartIp()));
    } else {
      int low = Prefix.MAX_PREFIX_LENGTH - length;
      int high = Prefix.MAX_PREFIX_LENGTH - 1;
      IntExpr lhs = ExtractExpr.newExtractExpr(var, low, high);
      IntExpr rhs = new LitIntExpr(prefix.getStartIp().asLong(), low, high);
      _expr = new EqExpr(lhs, rhs);
    }
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitPrefixMatchExpr(this);
  }

  @Override
  public <R> R accept(GenericBooleanExprVisitor<R> visitor) {
    return visitor.visitPrefixMatchExpr(this);
  }

  @Override
  protected boolean exprEquals(Expr e) {
    return Objects.equals(_expr, ((PrefixMatchExpr) e)._expr);
  }

  public BooleanExpr getExpr() {
    return _expr;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_expr);
  }
}
