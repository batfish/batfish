package org.batfish.z3.expr;

import java.util.Objects;

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

  @Override
  public boolean statementEquals(Statement e) {
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
