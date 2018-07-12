package org.batfish.z3.expr;

import java.util.Arrays;

public class Comment extends Statement {

  private String[] _lines;

  public Comment(String... lines) {
    _lines = lines;
  }

  @Override
  public <T> T accept(GenericStatementVisitor<T> visitor) {
    return visitor.visitComment(this);
  }

  @Override
  public void accept(VoidStatementVisitor visitor) {
    visitor.visitComment(this);
  }

  public String[] getLines() {
    return _lines;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_lines);
  }

  @Override
  public boolean statementEquals(Statement e) {
    return Arrays.equals(_lines, ((Comment) e)._lines);
  }
}
