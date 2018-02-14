package org.batfish.z3.expr;

import java.util.Objects;

public class QueryStatement extends Statement {

  private final BooleanExpr _subExpression;

  public QueryStatement(BooleanExpr expr) {
    _subExpression = expr;
  }

  @Override
  public <T> T accept(GenericStatementVisitor<T> visitor) {
    return visitor.visitQueryStatement(this);
  }

  @Override
  public void accept(VoidStatementVisitor visitor) {
    visitor.visitQueryStatement(this);
  }

  @Override
  public boolean statementEquals(Statement e) {
    return Objects.equals(_subExpression, ((QueryStatement) e)._subExpression);
  }

  public BooleanExpr getSubExpression() {
    return _subExpression;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_subExpression);
  }
}
