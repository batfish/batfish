package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.expr.visitors.ExprVisitor;

public class Comment extends Statement {

  private String[] _lines;

  public Comment(String... lines) {
    _lines = lines;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitComment(this);
  }

  @Override
  public boolean exprEquals(Expr e) {
    return Objects.equals(_lines, ((Comment) e)._lines);
  }

  public String[] getLines() {
    return _lines;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_lines);
  }
}
