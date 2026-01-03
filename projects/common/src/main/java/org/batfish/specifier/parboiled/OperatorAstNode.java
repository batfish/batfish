package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/** An AST node that represents an operator. */
final class OperatorAstNode implements AstNode {
  private final char _operator;

  OperatorAstNode(char operator) {
    _operator = operator;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitOperatorAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OperatorAstNode)) {
      return false;
    }
    OperatorAstNode that = (OperatorAstNode) o;
    return Objects.equals(_operator, that._operator);
  }

  public char getOperator() {
    return _operator;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_operator);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("operator", _operator).toString();
  }
}
