package org.batfish.z3.expr;

public class Comment extends Statement {

  private String[] _lines;

  public Comment(String... lines) {
    _lines = lines;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitComment(this);
  }

  public String[] getLines() {
    return _lines;
  }
}
