package org.batfish.z3.expr;

import java.util.Objects;

public class QueryStatement extends Statement {

  private final StateExpr _stateExpr;

  public QueryStatement(StateExpr expr) {
    _stateExpr = expr;
  }

  @Override
  public <T> T accept(GenericStatementVisitor<T> visitor) {
    return visitor.visitQueryStatement(this);
  }

  @Override
  public void accept(VoidStatementVisitor visitor) {
    visitor.visitQueryStatement(this);
  }

  public StateExpr getStateExpr() {
    return _stateExpr;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_stateExpr);
  }

  @Override
  public boolean statementEquals(Statement e) {
    return Objects.equals(_stateExpr, ((QueryStatement) e)._stateExpr);
  }
}
