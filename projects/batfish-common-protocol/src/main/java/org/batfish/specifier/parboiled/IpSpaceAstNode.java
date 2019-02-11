package org.batfish.specifier.parboiled;

import java.util.Objects;

class IpSpaceAstNode implements AstNode {

  public enum Type {
    ADDRESS_GROUP,
    COMMA,
    RANGE
  }

  private final AstNode _left;
  private final AstNode _right;
  private final Type _type;

  IpSpaceAstNode(Type type, AstNode left, AstNode right) {
    _left = left;
    _right = right;
    _type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof IpSpaceAstNode)) {
      return false;
    }
    return Objects.equals(_type, ((IpSpaceAstNode) o)._type)
        && Objects.equals(_left, ((IpSpaceAstNode) o)._left)
        && Objects.equals(_right, ((IpSpaceAstNode) o)._right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type, _left, _right);
  }

  @Override
  public String toString() {
    switch (_type) {
      case ADDRESS_GROUP:
        return String.format("addressgroup(%s, %s)", _left, _right);
      case COMMA:
        return String.format("%s, %s", _left, _right);
      case RANGE:
        return String.format("%s-%s", _left, _right);
      default:
        throw new IllegalStateException(String.format("Unknown type of IpSpaceAstNode: %s", _type));
    }
  }

  public AstNode left() {
    return _left;
  }

  public AstNode right() {
    return _right;
  }

  public Type type() {
    return _type;
  }
}
