package org.batfish.specifier.parboiled;

import java.util.Objects;

public class IpSpaceAstNode extends AstNode {

  public enum Type {
    ADDRESS_GROUP,
    COMMA,
    RANGE
  }

  private final Type _type;

  public IpSpaceAstNode(Type type, AstNode left, AstNode right) {
    super(left, right);
    _type = type;
  }

  public Type getType() {
    return _type;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof IpSpaceAstNode)) {
      return false;
    }
    return Objects.equals(_type, ((IpSpaceAstNode) o)._type)
        && Objects.equals(left(), ((IpSpaceAstNode) o).left())
        && Objects.equals(right(), ((IpSpaceAstNode) o).right());
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type, left(), right());
  }

  @Override
  public String toString() {
    switch (_type) {
      case ADDRESS_GROUP:
        return String.format("addressgroup(%s, %s)", left(), right());
      case COMMA:
        return String.format("%s, %s", left(), right());
      case RANGE:
        return String.format("%s-%s", left(), right());
      default:
        throw new IllegalStateException(String.format("Unknown type of IpSpaceAstNode: %s", _type));
    }
  }
}
