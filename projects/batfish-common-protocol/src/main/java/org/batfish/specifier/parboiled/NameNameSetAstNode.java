package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class NameNameSetAstNode implements NameSetAstNode {
  private final String _name;

  NameNameSetAstNode(AstNode nameAst) {
    this(((StringAstNode) nameAst).getStr());
  }

  NameNameSetAstNode(String name) {
    _name = name;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitNameNameSetAstNode(this);
  }

  @Override
  public <T> T accept(NameSetAstNodeVisitor<T> visitor) {
    return visitor.visitNameNameSetAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameNameSetAstNode)) {
      return false;
    }
    return Objects.equals(_name, ((NameNameSetAstNode) o)._name);
  }

  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("name", _name).toString();
  }
}
