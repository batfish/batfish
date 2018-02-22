package org.batfish.z3.expr;

import com.microsoft.z3.Context;
import java.util.Objects;
import org.batfish.z3.expr.visitors.BoolExprTransformer;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericBooleanExprVisitor;

public class TestBooleanAtom extends BooleanExpr {

  private final Context _ctx;

  private final String _name;

  public TestBooleanAtom(int i, Context ctx) {
    _name = String.format("BoolConst%d", i);
    _ctx = ctx;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public <R> R accept(GenericBooleanExprVisitor<R> visitor) {
    if (visitor instanceof BoolExprTransformer) {
      return visitor.castToGenericBooleanExprVisitorReturnType(_ctx.mkBoolConst(_name));
    } else {
      throw new UnsupportedOperationException(
          String.format(
              "No implementation for %s: %s",
              GenericBooleanExprVisitor.class.getSimpleName(), visitor.getClass().getSimpleName()));
    }
  }

  @Override
  public boolean exprEquals(Expr e) {
    return Objects.equals(_name, ((TestBooleanAtom) e)._name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name);
  }
}
