package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.expr.visitors.ExprPrinter;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericIntExprVisitor;
import org.batfish.z3.expr.visitors.IntExprVisitor;
import org.batfish.z3.expr.visitors.IsComplexVisitor;

public class MockIntAtom extends IntExpr {

  private final String _name;

  private final int _numBits;

  public MockIntAtom(int i) {
    _name = String.format("BVConst%d", i);
    _numBits = 1;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    if (visitor instanceof ExprPrinter) {
      ((ExprPrinter) visitor).visitIdExpr(new IdExpr(_name));
      return;
    } else if (visitor instanceof IsComplexVisitor) {
      return;
    }
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public <R> R accept(GenericIntExprVisitor<R> visitor) {
    throw new UnsupportedOperationException(
        String.format(
            "No implementation for %s: %s",
            GenericIntExprVisitor.class.getSimpleName(), visitor.getClass().getSimpleName()));
  }

  @Override
  public void accept(IntExprVisitor visitor) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public boolean exprEquals(Expr e) {
    return Objects.equals(_name, ((MockIntAtom) e)._name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name);
  }

  @Override
  public int numBits() {
    return _numBits;
  }

  @Override
  public String toString() {
    return _name;
  }
}
