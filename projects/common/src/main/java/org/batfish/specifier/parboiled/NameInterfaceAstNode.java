package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class NameInterfaceAstNode implements InterfaceAstNode {
  private final String _name;

  NameInterfaceAstNode(AstNode nameAst) {
    this(((StringAstNode) nameAst).getStr());
  }

  NameInterfaceAstNode(String name) {
    _name = name;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitNameInterfaceAstNode(this);
  }

  @Override
  public <T> T accept(InterfaceAstNodeVisitor<T> visitor) {
    return visitor.visitNameInterfaceNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameInterfaceAstNode)) {
      return false;
    }
    NameInterfaceAstNode that = (NameInterfaceAstNode) o;
    return Objects.equals(_name, that._name);
  }

  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_name);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("name", _name).toString();
  }
}
