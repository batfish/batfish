package org.batfish.specifier.parboiled;

import java.util.Objects;

public class IpSpaceAstNode extends AstNode {

  public enum Type {
    ADDRESS_GROUP,
    COMMA,
    DASH,
    NOT
  }

  private Type _type;

  public IpSpaceAstNode(Type type, AstNode left, AstNode right) {
    super(left, right);
    this._type = type;
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
  public String getValue() {
    switch (_type) {
      case COMMA:
        return String.format("%s, %s", left().getValue(), right().getValue());
      case DASH:
        return String.format("%s-%s", left().getValue(), right().getValue());
      case NOT:
        return String.format("!%s", left().getValue());
      default:
        throw new IllegalStateException(String.format("Unknown type of IpSpaceAstNode: %s", _type));
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type, left(), right());
  }

  @Override
  public String toString() {
    return getValue();
  }
}
