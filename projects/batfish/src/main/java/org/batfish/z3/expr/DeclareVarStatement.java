package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.HeaderField;

public class DeclareVarStatement extends Statement {

  private final HeaderField _headerField;

  public DeclareVarStatement(HeaderField headerField) {
    _headerField = headerField;
  }

  @Override
  public <T> T accept(GenericStatementVisitor<T> visitor) {
    return visitor.visitDeclareVarStatement(this);
  }

  @Override
  public void accept(VoidStatementVisitor visitor) {
    visitor.visitDeclareVarStatement(this);
  }

  public HeaderField getHeaderField() {
    return _headerField;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_headerField);
  }

  @Override
  public boolean statementEquals(Statement e) {
    return Objects.equals(_headerField, ((DeclareVarStatement) e)._headerField);
  }
}
