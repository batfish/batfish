package org.batfish.z3.expr;

import org.batfish.datamodel.Prefix;
import org.batfish.z3.HeaderField;

public class PrefixMatchExpr extends BooleanExpr {

  private BooleanExpr _expr;

  public PrefixMatchExpr(HeaderField var, Prefix prefix) {
    VarIntExpr varExpr = new VarIntExpr(var);
    int length = prefix.getPrefixLength();
    if (length == 0) {
      _expr = TrueExpr.INSTANCE;
    } else if (length == Prefix.MAX_PREFIX_LENGTH) {
      _expr = new EqExpr(varExpr, new LitIntExpr(prefix.getStartIp()));
    } else {
      int low = Prefix.MAX_PREFIX_LENGTH - length;
      int high = Prefix.MAX_PREFIX_LENGTH - 1;
      IntExpr lhs = ExtractExpr.newExtractExpr(var, low, high);
      IntExpr rhs = new LitIntExpr(prefix.getStartIp().asLong(), low, high);
      _expr = new EqExpr(lhs, rhs);
    }
  }

  @Override
  public void accept(BooleanExprVisitor visitor) {
    visitor.visitPrefixMatchExpr(this);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitPrefixMatchExpr(this);
  }

  public BooleanExpr getExpr() {
    return _expr;
  }
}
