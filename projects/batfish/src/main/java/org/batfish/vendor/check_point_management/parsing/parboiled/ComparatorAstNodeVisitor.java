package org.batfish.vendor.check_point_management.parsing.parboiled;

/**
 * A visitor of {@link ComparatorAstNode} that takes a generic argument and returns a generic value.
 */
public interface ComparatorAstNodeVisitor<T, U> {
  default T visit(ComparatorAstNode comparatorAstNode, U arg) {
    return comparatorAstNode.accept(this, arg);
  }

  T visitEqualsAstNode(EqualsAstNode equalsAstNode, U arg);

  T visitGreaterThanAstNode(GreaterThanAstNode greaterThanAstNode, U arg);

  T visitGreaterThanOrEqualsAstNode(GreaterThanOrEqualsAstNode greaterThanOrEqualsAstNode, U arg);

  T visitLessThanAstNode(LessThanAstNode lessThanAstNode, U arg);

  T visitLessThanOrEqualsAstNode(LessThanOrEqualsAstNode lessThanOrEqualsAstNode, U arg);
}
