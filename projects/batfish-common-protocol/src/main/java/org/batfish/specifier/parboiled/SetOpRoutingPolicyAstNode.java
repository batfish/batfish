package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

abstract class SetOpRoutingPolicyAstNode implements RoutingPolicyAstNode {
  private final RoutingPolicyAstNode _left;
  private final RoutingPolicyAstNode _right;

  static SetOpRoutingPolicyAstNode create(Character c, AstNode left, AstNode right) {
    RoutingPolicyAstNode leftSpec = (RoutingPolicyAstNode) left;
    RoutingPolicyAstNode rightSpec = (RoutingPolicyAstNode) right;
    switch (c) {
      case ',':
        return new UnionRoutingPolicyAstNode(leftSpec, rightSpec);
      case '\\':
        return new DifferenceRoutingPolicyAstNode(leftSpec, rightSpec);
      default:
        throw new IllegalStateException("Unknown set operation for node spec " + c);
    }
  }

  SetOpRoutingPolicyAstNode(RoutingPolicyAstNode left, RoutingPolicyAstNode right) {
    _left = left;
    _right = right;
  }

  public final RoutingPolicyAstNode getLeft() {
    return _left;
  }

  public final RoutingPolicyAstNode getRight() {
    return _right;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SetOpRoutingPolicyAstNode that = (SetOpRoutingPolicyAstNode) o;
    return Objects.equals(_left, that._left) && Objects.equals(_right, that._right);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(getClass(), _left, _right);
  }

  @Override
  public final String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("left", _left)
        .add("right", _right)
        .toString();
  }
}
